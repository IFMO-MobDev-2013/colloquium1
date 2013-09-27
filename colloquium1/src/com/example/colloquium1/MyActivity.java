package com.example.colloquium1;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Canvas;
import android.view.*;

import java.util.Random;

public class MyActivity extends Activity {
    MySurfaceView mSV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSV = new MySurfaceView(this);
        setContentView(mSV);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 101, Menu.NONE, "Add Thousand");
        menu.add(Menu.NONE, 102, Menu.NONE, "Remove Thousand");
        menu.add(Menu.NONE, 103, Menu.NONE, "Starting Configuration");
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == 101) {
            mSV.drawThread.addThousand();
        } else if (i == 102) {
            mSV.drawThread.removeThousand();
        } else if (i == 103) {
            mSV.drawThread.startingConfiguration();
        }
        return false;
    }

    public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private DrawThread drawThread;
        private static final int MAX_SIZE = 64000;
        private int size = 5000;
        private int W;
        private int H;
        private int X;
        private int Y;
        private boolean[][] isRed;
        private int[] x = new int[MAX_SIZE];
        private int[] y = new int[MAX_SIZE];
        private boolean[] isDead = new boolean[MAX_SIZE];
        private int[] pixels;
        private long time = 0;

        private Random r = new Random();
        private Paint p = new Paint();

        public MySurfaceView(Context context) {
            super(context);
            getHolder().addCallback(this);
        }


        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            p.setTextSize(20);
            p.setColor(Color.WHITE);
            W = getWidth();
            H = getHeight();
            X = W/2;
            Y = H/2;
            pixels = new int[W * H];
            isRed = new boolean[H + 2][W + 2];
            isRed[Y + 1][X + 1] = true;
            for (int i = 0; i < size; i++)
                x[i] = r.nextInt(W);
            for (int i = 0; i < size; i++)
                y[i] = r.nextInt(H);
            drawThread = new DrawThread(getHolder());
            drawThread.setRunning(true);
            drawThread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            drawThread.setRunning(false);
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }

        public boolean onTouchEvent(MotionEvent event)
        {
            if(event.getAction() == MotionEvent.ACTION_UP)
            {
                X = (int)event.getX();
                Y = (int)event.getY();
                isRed[Y+1][X+1] = true;
            }
            return true;
        }
        class DrawThread extends Thread {
            private boolean runFlag = false;
            private SurfaceHolder surfaceHolder;

            public DrawThread(SurfaceHolder surfaceHolder) {
                this.surfaceHolder = surfaceHolder;
            }

            public void setRunning(boolean run) {
                runFlag = run;
            }

            @Override
            public void run() {
                Canvas canvas;
                while (runFlag) {
                    canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas(null);
                        if (canvas != null) {
                            synchronized (surfaceHolder) {
                                long newTime = System.currentTimeMillis();
                                long d = newTime - time;
                                time = newTime;
                                long fps = 1000 / d;
                                nextField();
                                drawField(canvas);
                                canvas.drawText(Long.toString(fps) + " FPS", 20, 20, p);
                            }
                        }
                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }

            private void nextField() {
                boolean[][] newIsRed = new boolean[H + 2][W + 2];
                newBorder();
                for (int i = 0; i < size; i++) {
                    if (!isDead[i]) {
                        int direction = r.nextInt(3);
                        if (direction == 0) {
                            x[i]--;
                            if (x[i] == -1)
                                x[i] = W - 1;
                        } else if (direction == 2) {
                            x[i]++;
                            if (x[i] == W)
                                x[i] = 0;
                        }
                        direction = r.nextInt(3);
                        if (direction == 0) {
                            y[i]--;
                            if (y[i] == -1)
                                y[i] = H - 1;
                        } else if (direction == 2) {
                            y[i]++;
                            if (y[i] == H)
                                y[i] = 0;
                        }
                        if (isRed[y[i]][x[i] + 1]
                                || isRed[y[i]][x[i] + 2]
                                || isRed[y[i] + 1][x[i]]
                                || isRed[y[i] + 1][x[i] + 2]
                                || isRed[y[i] + 2][x[i]]
                                || isRed[y[i] + 2][x[i] + 1]
                                || isRed[y[i] + 2][x[i] + 2]) {
                            newIsRed[y[i] + 1][x[i] + 1] = true;
                            isDead[i] = true;
                        }
                    }
                }
                for (int i = 1; i <= H; i++)
                    for (int j = 1; j <= W; j++)
                        if (newIsRed[i][j])
                            isRed[i][j] = true;
            }

            private void newBorder() {
                for (int i = 1; i <= W; i++) {
                    isRed[0][i] = isRed[H][i];
                    isRed[H + 1][i] = isRed[1][i];
                }
                for (int i = 1; i <= H; i++) {
                    isRed[i][0] = isRed[i][W];
                    isRed[i][W + 1] = isRed[i][1];
                }
                isRed[0][0] = isRed[H][W];
                isRed[H + 1][0] = isRed[1][W];
                isRed[H + 1][W + 1] = isRed[1][1];
                isRed[0][W + 1] = isRed[H][1];
            }

            private void drawField(Canvas canvas) {
                for (int i = 0; i < pixels.length; i++)
                    pixels[i] = Color.BLACK;
                for (int i = 0; i < size; i++)
                    pixels[y[i] * W + x[i]] = Color.WHITE;
                for (int i = 0; i < H; i++)
                    for (int j = 0; j < W; j++)
                        if (isRed[i + 1][j + 1])
                            pixels[i * W + j] = Color.RED;
                canvas.drawBitmap(pixels, 0, W, 0, 0, W, H, false, p);
            }

            public void addThousand() {
                if (size < MAX_SIZE) {
                    size += 1000;
                    for (int i = size - 1000; i < size; i++)
                        x[i] = r.nextInt(W);
                    for (int i = size - 1000; i < size; i++)
                        y[i] = r.nextInt(H);
                }
            }

            public void removeThousand() {
                if (size > 1000) {
                    for (int i = size - 1000; i < size; i++)
                        isDead[i] = false;
                    size -= 1000;
                }
            }

            public void startingConfiguration() {
                size = 5000;
                pixels = new int[W * H];
                isRed = new boolean[H + 2][W + 2];
                isRed[Y+ 1][X + 1] = true;
                for (int i = 0; i < size; i++)
                    x[i] = r.nextInt(W);
                for (int i = 0; i < size; i++)
                    y[i] = r.nextInt(H);
            }
        }
    }

}
