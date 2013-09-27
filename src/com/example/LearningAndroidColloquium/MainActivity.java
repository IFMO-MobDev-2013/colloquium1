package com.example.LearningAndroidColloquium;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.*;

import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;

public class MainActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        cv = new CellView(this);
        setContentView(cv);
    }

    CellView cv;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu.size() == 0)
            getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mnuAdd) {
            cv.ud.doRandomSeed(1000);
        } else if (item.getItemId() == R.id.mnuRemove) {
            cv.ud.removeWalkingCells(1000);
        } else if (item.getItemId() == R.id.mnuReset) {
            cv.ud.resetField();
        }
        return super.onOptionsItemSelected(item);
    }

    class CellView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

        UpdateDispatcher ud;

        public CellView(Context context) {
            super(context);
            getHolder().addCallback(this);
            setOnTouchListener(this);
        }

        int  frames = 0;
        long fps    = 0;

        Paint p = new Paint();

        {
            p.setColor(Color.WHITE);
            p.setTextSize(25);
        }

        @Override
        public void surfaceCreated(SurfaceHolder surfaceHolder) {
            ud = new UpdateDispatcher();
            new Thread(ud).start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            ud.needsStop = true;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (!ud.map[(int)(motionEvent.getY())*ud.mapWidth+(int)motionEvent.getX()])
                ud.map[(int)(motionEvent.getY())*ud.mapWidth+(int)motionEvent.getX()] = true;
            return true;
        }


        private class UpdateDispatcher implements Runnable {

            boolean[] map;

            boolean needsStop = false;

            SurfaceHolder sh = CellView.this.getHolder();

            boolean[][] inc;

            Canvas c;

            int   walkingCellsCount;
            int[] walkingCellX;
            int[] walkingCellY;
            int   makeRedCount;
            int[] makeRedX;
            int[] makeRedY;

            void updateMap() {
                int d;
                int dx;
                int dy;
                int yIndex;
                boolean done;
                while (!needsStop) {
                    makeRedCount = 0;
                    for (int i = 0; i < walkingCellsCount; i++) {
                        dx = rng.nextInt(3)-1;
                        dy = rng.nextInt(3)-1;
                        if (dx != 0 || dy != 0) {
                            walkingCellX[i] += dx;
                            walkingCellY[i] += dy;
                            if (walkingCellX[i] == -1) walkingCellX[i] = mapWidth - 1;
                            else if (walkingCellX[i] == mapWidth) walkingCellX[i] = 0;
                            if (walkingCellY[i] == -1) walkingCellY[i] = mapHeight - 1;
                            else if (walkingCellY[i] == mapHeight) walkingCellY[i] = 0;
                            done = false;
                            for (int cy = walkingCellY[i] - 1; !done && cy <= walkingCellY[i] + 1; cy++) {
                                yIndex = (cy >= 0 ? cy <= mapHeight - 1 ? cy : 0 : mapHeight - 1) * mapWidth;
                                for (int cx = walkingCellX[i] - 1; cx <= walkingCellX[i] + 1; cx++) {
                                    if (map[yIndex + (cx >= 0 ? cx <= mapWidth - 1 ? cx : 0 : mapWidth - 1)]) {
                                        makeRedX[makeRedCount] = walkingCellX[i];
                                        makeRedY[makeRedCount] = walkingCellY[i];
                                        makeRedCount++;
                                        walkingCellsCount--;
                                        if (walkingCellsCount > 0) {
                                            int t;
                                            t = walkingCellX[walkingCellsCount];
                                            walkingCellX[walkingCellsCount] = walkingCellX[i];
                                            walkingCellX[i] = t;
                                            t = walkingCellY[walkingCellsCount];
                                            walkingCellY[walkingCellsCount] = walkingCellX[i];
                                            walkingCellY[i] = t;
                                        }
                                        done = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    for (int i = 0; i < makeRedCount; i++)
                        map[makeRedY[i] * mapWidth + makeRedX[i]] = true;
                    for (int y = 0; y < mapHeight; y++) {
                        int yy = y * mapWidth;
                        for (int x = 0; x < mapWidth; x++) {
                            bmp[yy + x] = map[yy + x] ? Color.RED : Color.BLACK;
                        }
                    }
                    for (int i = 0; i < walkingCellsCount; i++) {
                        bmp[walkingCellY[i] * mapWidth + walkingCellX[i]] = Color.WHITE;
                    }
                    try {
                        c = sh.lockCanvas();
                        if (c != null)
                            synchronized (sh) {
                                c.drawBitmap(bmp, 0, mapWidth, 0, 0, mapWidth, mapHeight, false, p);
                                c.drawText(Long.toString(1000*frames/((System.currentTimeMillis()+1-starttime))) + " fps", 35, 35, p);
                            }
                    } finally {
                        if (c != null) {
                            sh.unlockCanvasAndPost(c);
                        }
                    }
                    frames++;
                }
            }

            long starttime;

            int frames;

            void start() {
                updateMap();
            }

            float scaleX, scaleY;

            int mapHeight, mapWidth;
            private static final int START_WALKING_CELLS_COUNT = 5000;
            Random rng;

            public void doRandomSeed(int cells) {
                for (int i = walkingCellsCount; i < (walkingCellsCount + cells - 1); i++) {
                    do {
                        walkingCellX[i] = rng.nextInt(mapWidth);
                        walkingCellY[i] = rng.nextInt(mapHeight);
                    } while (map[walkingCellY[i] * mapWidth + walkingCellX[i]]);
                }
                walkingCellsCount += cells;
            }

            public void removeWalkingCells(int cells) {
                walkingCellsCount -= cells;
                if (walkingCellsCount < 0) walkingCellsCount = 0;
            }

            int[] bmp = new int[mapWidth * mapHeight];

            private void resetField() {
                frames = 0;
                starttime = System.currentTimeMillis();
                walkingCellsCount = 0;
                map = new boolean[(mapWidth = getWidth()) * (mapHeight = getHeight())];
                walkingCellX = new int[mapHeight * mapWidth];
                walkingCellY = new int[mapHeight * mapWidth];
                makeRedX = new int[mapHeight * mapWidth];
                makeRedY = new int[mapHeight * mapWidth];
                bmp = new int[mapHeight * mapWidth];
                rng = new Random();
                map[mapHeight / 2 * mapWidth + mapWidth / 2] = true;
                doRandomSeed(5000);
            }

            @Override
            public void run() {
                resetField();
                scaleX = (float) getWidth() / mapWidth;
                scaleY = (float) getHeight() / mapHeight;
                start();
            }
        }

    }
}