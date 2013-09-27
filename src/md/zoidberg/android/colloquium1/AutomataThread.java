package md.zoidberg.android.colloquium1;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class AutomataThread extends Thread {
    private int[] colors;
    private int fieldWidth;
    private int fieldHeight;

    private int[][] field;
    private int[][] nextGeneration;

    private Random rand = new Random();

    private int boundingXmin, boundingYmin, boundingXmax, boundingYmax;

    private List<Point> fixed;
    private List<Point> floating;
    private List<Point> tagged;

    private boolean addFlag = false;
    private boolean removeFlag = false;
    private boolean resetFlag = false;

    private Paint textPaint = new Paint();
    {
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(20);
        textPaint.setFakeBoldText(true);
    }

    private long startTime;
    private int lastFrame = 0;
    private DecimalFormat fpsFormat = new DecimalFormat("fps ##.##");

    private int[] palette = {Color.argb(0, 255, 0, 0), Color.argb(0, 0, 0, 0), Color.argb(0, 255, 255, 255)};

    private static final int FLOATING = 1;
    private static final int FIXED = -1;

    private final SurfaceHolder holder;

    private boolean isRunning;

    public AutomataThread(SurfaceHolder holder, int w, int h) {
        this.holder = holder;
        initField(w, h);
        isRunning = false;
    }

    public void setRunning(boolean newRunning) {
        isRunning = newRunning;
    }

    private void addPoints(int number) {
        synchronized (floating) {
            int x, y;
            for (int i = 0; i < number; i++) {
                Random rand = new Random();

                x = rand.nextInt(fieldWidth);
                y = rand.nextInt(fieldHeight);

                field[y][x] = FLOATING;
                floating.add(new Point(x, y, false));
            }
        }
    }

    private void initField(int w, int h) {
                    this.fieldWidth = w;
                    this.fieldHeight = h;
                    field = new int[h][w];
                    nextGeneration = new int[h][w];

                    colors = new int[h * w];

                    fixed = new ArrayList<Point>();
                    floating = new LinkedList<Point>();
                    tagged = new LinkedList<Point>();

                    startTime = System.nanoTime();

                    addPoints(5000);

                    // the first fixed point
                    int centerX = fieldWidth / 2;
                    int centerY = fieldHeight / 2;
                    fixed.add(new Point(centerX, centerY, true));
                    field[centerY][centerX] = FIXED;

                    boundingXmin = centerX - 1;
                    boundingYmin = centerY - 1;
                    boundingXmax = centerX + 1;
                    boundingYmax = centerY + 1;
    }

    private boolean adjacent(Point a, Point b) {
        if ((Math.abs(a.x - b.x) <= 1 || Math.abs(a.x - b.x) == fieldWidth - 1) && (Math.abs(a.y - b.y) <= 1 || Math.abs(a.y - b.y) == fieldHeight - 1))
            return true;
        return false;
    }

    public void recalculateField() {
        for (Point point: floating) {
            int nx = rand.nextInt(3) - 1 + point.x;
            int ny = rand.nextInt(3) - 1 + point.y;

            if (nx == -1) nx += fieldWidth;
            if (nx == fieldWidth) nx = 0;
            if (ny == -1) ny += fieldHeight;
            if (ny == fieldHeight) ny = 0;

            point.x = nx;
            point.y = ny;

            field[point.y][point.x] = 0;
            nextGeneration[ny][nx] = FLOATING;

            if (!(point.x < boundingXmin || point.x > boundingXmax || point.y < boundingYmin || point.y > boundingYmax)) {
                tagged.add(point);
            }
        }

        List<Point> newFixed = new LinkedList<Point>();
        for (Point point: fixed) {
            nextGeneration[point.y][point.x] = FIXED;
            for (Point tag: tagged) {
                if (adjacent(point, tag)) {
                    newFixed.add(tag);
                    floating.remove(tag);
                }
            }
        }

        fixed.addAll(newFixed);
        tagged.clear();

        int[][] tmp = field;
        field = nextGeneration;
        nextGeneration = tmp;
    }

    private void redrawScreen(Canvas screen) {
        if (screen == null) return;
        for (int y = 0; y < fieldHeight; y++) {
            for (int x = 0; x < fieldWidth; x++) {
                colors[y* fieldWidth + x] = palette[field[y][x] + 1];
                nextGeneration[y][x] = 0;
            }
        }

        screen.drawBitmap(colors, 0, fieldWidth, 0.0F, 0.0F, fieldWidth, fieldHeight, false, null);

        long timeDelta = System.nanoTime() - startTime;
        float fps = lastFrame * 1000000000.0f/timeDelta; // dragons ahoy! 10^9 nanoseconds

        screen.drawText(fpsFormat.format(fps), 20.0f, 40.0f, textPaint);
        screen.drawText("frame " + Integer.toString(lastFrame), 20.0f, 60.0f, textPaint);
        screen.drawText("fixed " + Integer.toString(fixed.size()), 20.0f, 80.0f, textPaint);

        lastFrame++;
    }

    public void run() {
        Canvas canvas = null;
        while (isRunning) {
            if (addFlag) {
                addPoints(1000);
                addFlag = false;
            }

            if (removeFlag) {
                removePoints(1000);
                removeFlag = false;
            }

            if (resetFlag) {
                initField(fieldWidth, fieldHeight);
                resetFlag = false;
            }

            recalculateField();
            try {
                canvas = holder.lockCanvas();
                synchronized (holder) {
                    redrawScreen(canvas);
                }
            } finally {
                if (canvas != null) {
                    holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void addCells(int i) {
        addFlag = true;
    }

    public void removeCells(int i) {
        removeFlag = true;
    }

    private void removePoints(int toRemove) {
            if (floating.size() <= toRemove) {
                floating.clear();
                return;
            }

            for (int i = 0; i < toRemove; i++) {
                floating.remove(0);
            }
    }


    public void reset() {
        resetFlag = true;
    }
}
