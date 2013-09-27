package com.example.colloc;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.View;
import android.view.Display;
import android.view.WindowManager;
import android.view.MotionEvent;
import java.util.Random;

public class MyActivity extends Activity {
    Random rand = new Random();
    private class Whirl extends View {
        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        private final Paint paint = new Paint();
        private final Paint fpsPaint = new Paint();
        long startTime,spentTime;
        public int[][] pointOld = new int[height][width];    // -2 nothing , -1 red
        private int[] colorBit = new int[height * width];
        private int[][] pointNew = new int[height][width];

        public Whirl(Context context) {
            super(context);
            startTime = System.currentTimeMillis();
            fpsPaint.setColor(Color.YELLOW);
            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++)
                    pointOld[i][j] = pointOld[i][j] = -2;
            pointOld[height / 2][width / 2] = -1;
            for (int k = 0; k < 5000; k++) {
                int i = rand.nextInt(height);
                int j = rand.nextInt(width);
                if (pointOld[i][j] > 0)
                    pointOld[i][j]++;
                else
                    pointOld[i][j] = 1;
            }
        }

        @Override
        public void onDraw(Canvas canvas) {
            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++)
                    pointNew[i][j] = -2;
            for (int i = 0; i < height; i++) {
                int nexti = i + 1;
                if (nexti == height) nexti = 0;
                int previ = i - 1;
                if (previ == -1) previ = height - 1;
                for (int j = 0; j < width; j++) {
                    if (pointOld[i][j] == -2)
                        continue;
                    if (pointOld[i][j] < 1) {
                        pointNew[i][j] = pointOld[i][j];
                        continue;
                    }
                    for (int k = 0; k < pointOld[i][j]; k++) {
                        int t = rand.nextInt(9);
                        if (t == 8) {
                            pointNew[i][j] = pointOld[i][j];
                            continue;
                        }
                        int nextj = j + 1;
                        if (nextj == width) nextj = 0;
                        int prevj = j - 1;
                        if (prevj == -1) prevj = width - 1;

                        if (pointOld[previ][prevj] == -1 ||
                                pointOld[previ][j] == -1 ||
                                pointOld[previ][nextj] == -1 ||
                                pointOld[i][prevj] == -1 ||
                                pointOld[i][nextj] == -1 ||
                                pointOld[nexti][prevj] == -1 ||
                                pointOld[nexti][j] == -1 ||
                                pointOld[nexti][nextj] == -1
                                ) {
                                pointNew[i][j] = pointOld[i][j] = -1;
                                break;
                        }


                        int i1 = 0, j1 = 0;
                        if (t == 0) {
                            i1 = previ;
                            j1 = prevj;
                        } else if (t == 1) {
                            i1 = previ;
                            j1 = j;
                        } else if (t == 2) {
                            i1 = previ;
                            j1 = nextj;
                        } else if (t == 3) {
                            i1 = i;
                            j1 = prevj;
                        } else if (t == 4) {
                            i1 = i;
                            j1 = nextj;
                        } else if (t == 5) {
                            i1 = nexti;
                            j1 = prevj;
                        } else if (t == 6) {
                            i1 = nexti;
                            j1 = j;
                        } else if (t == 7) {
                            i1 = nexti;
                            j1 = nextj;
                        }
                        if (pointOld[i1][j1] == -2) {
                            pointNew[i1][j1] = 1;
                            if (pointOld[i][j] > 0)
                                pointOld[i][j]--;
                        } else if (pointOld[i1][j1] == -1)
                            if (pointOld[i][j] > 0)
                                pointOld[i][j]--;
                        else {
                            pointNew[i1][j1]++;
                            if (pointOld[i][j] > 0)
                                pointOld[i][j]--;
                        }
                    }
                }
            }

            int p = 0;
            for (int i = 0; i < height; i++)
                for (int j = 0; j < width; j++) {
                    if (pointOld[i][j] == -2)
                        colorBit[p] = Color.BLACK;
                    else if (pointOld[i][j] == -1)
                        colorBit[p] = Color.RED;
                    else
                        colorBit[p] = Color.WHITE;
                    p++;
                    pointOld[i][j] = pointNew[i][j];
                }
            canvas.drawBitmap(colorBit, 0, width, 0, 0, width, height, false, paint);
            spentTime = System.currentTimeMillis();
            long FPS = 1000 / (spentTime - startTime);
            canvas.drawText("FPS= " + FPS,30,60,fpsPaint);
            startTime = spentTime;
            invalidate();
        }

        public boolean onTouchEvent(MotionEvent event) {
            int x = (int)event.getX();
            int y = (int)event.getY();
            pointOld[y][x] = -1;
            return super.onTouchEvent(event);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new Whirl(this));
    }
}
