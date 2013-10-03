package com.polarnick.col1;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DrawerThread extends Thread {

    private final SurfaceHolder surfaceHolder;

    private final static int THREAD_COUNT = Runtime.getRuntime().availableProcessors() + 1;
    private final static int FIELD_UPDATER_COUNT = THREAD_COUNT;

    private final static int HEADER_HEIGHT = 128;
    //Default:     240 x 320
    //Xperia Sola: 480 x 854
    //Galaxy S3:   720 x 1280
    private final static int WIDTH = HelloAndroidActivity.getScreenWidth();
    private final static int HEIGHT = HelloAndroidActivity.getScreenHeight() - HEADER_HEIGHT;

    private static final int MS_IN_SECOND = 1000;

    private static final Paint FPS_COLOR_TEXT = new Paint();

    private static final float TEXT_SIZE = 30f;
    private static final int TEXT_ALPHA = 255;

    private static final int TEXT_OFFSET_X = 15;
    private static final int TEXT_OFFSET_Y = 35 + HEADER_HEIGHT;

    private final static Random random = new Random();

    public static final int POINT_COLOUR = Color.WHITE;
    public static final int BACKGROUND_COLOUR = Color.BLACK;
    public static final int FIXED_POINTS_COLOUR = Color.RED;

    private final Profiler<WhirlViewFunctions> profiler = new Profiler<WhirlViewFunctions>(this.getClass());

    private final ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
    private final List<FieldUpdater> updaters = new ArrayList<FieldUpdater>(FIELD_UPDATER_COUNT);

    private int[] field;
    private boolean[] wasBlocked;
    private boolean[] willBlocked;
    private int[] pointsCount;
    private Queue<Point> points;

    private boolean runFlag;

    public DrawerThread(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        initStartState();
    }

    private void addPoints(int count) {
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(WIDTH * HEIGHT);
            while (wasBlocked[index]) {
                index = random.nextInt(WIDTH * HEIGHT);
            }
            Point p = new Point(index, false);
            points.add(p);
            ++pointsCount[index];
        }
    }

    private void addFixPoint(int x, int y) {
        int index = y * WIDTH + x;
        if (!wasBlocked[index]) {
            ++pointsCount[index];
            field[index] = FIXED_POINTS_COLOUR;
            wasBlocked[index] = true;
            willBlocked[index] = true;
        }
    }

    private void initStartState() {
        wasBlocked = new boolean[WIDTH * HEIGHT];
        willBlocked = new boolean[WIDTH * HEIGHT];
        pointsCount = new int[WIDTH * HEIGHT];
        field = new int[WIDTH * HEIGHT];
        points = new ConcurrentLinkedQueue<Point>();

        initFieldUpdaters();

        final int index = HEIGHT / 2 * WIDTH + WIDTH / 2;
        wasBlocked[index] = true;
        ++pointsCount[index];

        Arrays.fill(field, BACKGROUND_COLOUR);
        field[index] = FIXED_POINTS_COLOUR;
        addPoints(5000);
    }

    private void initFieldUpdaters() {
        for (int i = 0; i < FIELD_UPDATER_COUNT; i++) {
            updaters.add(new FieldUpdater(i, i, FIELD_UPDATER_COUNT, WIDTH, HEIGHT, field, points, wasBlocked, willBlocked, pointsCount));
        }
    }

    @Override
    public void run() {
        while (runFlag) {
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                    if (canvas != null) {
                        onDraw(canvas);
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            if (profiler.isToLogNextStep()) {
                Log.d(this.getClass().getName(), profiler.getAverangeLog());
            }
            sleepNano(1);
        }
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

    public void onDraw(final Canvas canvas) {
        profiler.in(WhirlViewFunctions.onDraw);
        update(canvas);
        profiler.out(WhirlViewFunctions.onDraw);
    }

    private void update(final Canvas canvas) {
        if (touched) {
            if (y > HEADER_HEIGHT) {
                addFixPoint((int) x, (int) y - HEADER_HEIGHT);
            } else {
                if (x < WIDTH / 3) {
                    addPoints(1000);
                } else if (x < 2 * WIDTH / 3) {
                    removePoints(1000);
                } else {
                    initStartState();
                }
            }
            touched = false;
        }
        final CountDownLatch counter = new CountDownLatch(THREAD_COUNT);
        for (FieldUpdater updater : updaters) {
            updater.setCounter(counter);
            pool.execute(updater);
        }

        pool.execute(new Runnable() {
            @Override
            public void run() {
                counter.countDown();
            }
        });

        try {
            counter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        renderFieldBitmap(field, canvas);
        updateData();
    }

    private void removePoints(int count) {
        Iterator<Point> iterator = points.iterator();
        while (count > 0 && iterator.hasNext()) {
            Point p = iterator.next();
            if (!p.isWasFixed() && !p.isNowFixed()) {
                iterator.remove();
                int index = p.getIndex();
                this.pointsCount[index]--;
                if (this.pointsCount[index] == 0) {
                    field[index] = BACKGROUND_COLOUR;
                }
                count--;
            }
        }
    }

    private void updateData() {
        Iterator<Point> iterator = points.iterator();
        while (iterator.hasNext()) {
            Point p = iterator.next();
            if (p.isNowFixed()) {
                wasBlocked[p.getIndex()] = true;
                iterator.remove();
                if (p.updateBlockedState()) {
                    field[p.getIndex()] = FIXED_POINTS_COLOUR;
                }
            }
        }

        boolean[] tmp = wasBlocked;
        wasBlocked = willBlocked;
        willBlocked = tmp;
    }

    private void sleepNano(int time) {
        try {
            Thread.sleep(0L, time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long fpsOverall = 0;
    private final long appStart = System.currentTimeMillis();

    static {
        FPS_COLOR_TEXT.setARGB(TEXT_ALPHA, 200, 255, 200);
        FPS_COLOR_TEXT.setTextSize(TEXT_SIZE);
    }

    final int dxButton = 30;
    final int dyButton = 30;

    private void renderFieldBitmap(int[] colors, Canvas canvas) {
        profiler.in(WhirlViewFunctions.renderFieldBitmap);
        canvas.scale(1f * HelloAndroidActivity.getScreenWidth() / WIDTH,
                1f * (HelloAndroidActivity.getScreenHeight() - HEADER_HEIGHT) / HEIGHT);
        canvas.drawBitmap(colors, 0, WIDTH, 0, HEADER_HEIGHT, WIDTH, HEIGHT, false, null);
        ++fpsOverall;
        canvas.drawText((fpsOverall * MS_IN_SECOND / (System.currentTimeMillis() - appStart)) + " FPS", TEXT_OFFSET_X, TEXT_OFFSET_Y, FPS_COLOR_TEXT);
        canvas.drawText("+1000", dxButton, dyButton, FPS_COLOR_TEXT);
        canvas.drawText("-1000", WIDTH / 3 + dxButton, dyButton, FPS_COLOR_TEXT);
        canvas.drawText("reset", 2 * WIDTH / 3 + dxButton, dyButton, FPS_COLOR_TEXT);
        canvas.drawLine(WIDTH / 3, 0, WIDTH / 3, HEADER_HEIGHT, FPS_COLOR_TEXT);
        canvas.drawLine(2 * WIDTH / 3, 0, 2 * WIDTH / 3, HEADER_HEIGHT, FPS_COLOR_TEXT);
        canvas.drawLine(0, HEADER_HEIGHT - 1, WIDTH, HEADER_HEIGHT - 1, FPS_COLOR_TEXT);
        profiler.out(WhirlViewFunctions.renderFieldBitmap);
    }

    boolean touched;
    float x;
    float y;

    public void onTouched(float x, float y) {
        if (!touched) {
            touched = true;
            this.x = x;
            this.y = y;
        }
    }

    private enum WhirlViewFunctions {

        onDraw, updateField, getNewColor, updateField2, renderFieldBitmap, updateField3, updateField4, renderField

    }

}
