package ru.ifmo.mobdev.MatveevA.colloquium1;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Random;

public class MyActivity extends Activity {

    private static final int POINTS_COUNT = 100000;
    int[][] points;
    int[][] marks;
    int[] saveColors;
    int[][] newPoints;
    int[][] newMarks;
    int[] newSaveColors;

    private int curCount;
    Display display;
    private int displayWidth;
    private int displayHeight;
    private Paint p;
    private long oldTime;

    Paint p1;

    class GameView extends View {

        public GameView(Context context) {
            super(context);
            points = new int[POINTS_COUNT][2];
            newPoints = new int[POINTS_COUNT][2];
            curCount = 5000;

            display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            displayWidth = size.x;
            displayHeight = size.y;


            oldTime = System.currentTimeMillis();

            p = new Paint();
            p1 = new Paint();

            marks = new int[displayHeight][displayWidth];
            newMarks = new int[displayHeight][displayWidth];

            saveColors = new int[displayWidth * displayHeight];

            for (int i = 0; i < displayHeight; i++) {
                for (int j = 0; j < displayWidth; j++) {
                    marks[i][j] = 0;
                    saveColors[i * displayWidth + j] = Color.argb(255, 0, 0, 0);
                }
            }

            Random rand = new Random();
            marks[displayHeight/2][displayWidth/2] = 2;

            saveColors[displayHeight/2 * displayWidth + displayWidth/2] = Color.RED;

            for (int i = 0; i < curCount; i++) {
                int x = rand.nextInt(displayHeight);
                int y = rand.nextInt(displayWidth);

                while (marks[x][y] != 0) {
                    x = rand.nextInt(displayHeight);
                    y = rand.nextInt(displayWidth);
                }
                marks[x][y] = 1;
                points[i][0] = x;
                points[i][1] = y;
                saveColors[x * displayWidth + y] = Color.argb(255, 255, 255, 255);
            }

        }

        int correctX(int x)  {
            if (x < 0) {
                return displayHeight - 1;
            }
            if (x == displayHeight) {
                return 0;
            }
            return x;
        }

        int correctY(int y)  {
            if (y < 0) {
                return displayWidth - 1;
            }
            if (y == displayWidth) {
                return 0;
            }
            return y;
        }

        boolean isRed(int x, int y) {
            if (marks[correctX(x-1)][correctY(y)] == 2) {
                return true;
            }
            if (marks[correctX(x-1)][correctY(y+1)] == 2) {
                return true;
            }
            if (marks[correctX(x)][correctY(y+1)] == 2) {
                return true;
            }
            if (marks[correctX(x+1)][correctY(y+1)] == 2) {
                return true;
            }
            if (marks[correctX(x+1)][correctY(y)] == 2) {
                return true;
            }
            if (marks[correctX(x+1)][correctY(y-1)] == 2) {
                return true;
            }
            if (marks[correctX(x)][correctY(y-1)] == 2) {
                return true;
            }
            if (marks[correctX(x-1)][correctY(y-1)] == 2) {
                return true;
            }
            return false;
        }

        private void change() {
            int x, y, nextX = 0, nextY = 0;

            for (int i = 0; i < curCount; i++) {

                x = points[i][0];
                y = points[i][1];

                if (marks[points[i][0]][points[i][1]] == 2) {
                    continue;
                }

                Random rand = new Random();
                int r = rand.nextInt(9);


                if (r == 0) {
                    continue;
                }

                if (r == 1) {

                    nextX = x - 1;
                    nextX = correctX(nextX);
                    nextY = y;
                    nextY = correctY(nextY);

                }
                if (r == 2) {

                    nextX = x - 1;
                    nextX = correctX(nextX);
                    nextY = y + 1;
                    nextY = correctY(nextY);

                }
                if (r == 3) {

                    nextX = x;
                    nextX = correctX(nextX);
                    nextY = y + 1;
                    nextY = correctY(nextY);

                }
                if (r == 4) {

                    nextX = x + 1;
                    nextX = correctX(nextX);
                    nextY = y + 1;
                    nextY = correctY(nextY);

                }
                if (r == 5) {

                    nextX = x + 1;
                    nextX = correctX(nextX);
                    nextY = y;
                    nextY = correctY(nextY);

                }
                if (r == 6) {

                    nextX = x + 1;
                    nextX = correctX(nextX);
                    nextY = y - 1;
                    nextY = correctY(nextY);

                }
                if (r == 7) {

                    nextX = x;
                    nextX = correctX(nextX);
                    nextY = y - 1;
                    nextY = correctY(nextY);

                }
                if (r == 8) {

                    nextX = x - 1;
                    nextX = correctX(nextX);
                    nextY = y - 1;
                    nextY = correctY(nextY);

                }




                    newPoints[i][0] = nextX;
                    newPoints[i][1] = nextY;
                    marks[nextX][nextY] = 1;
                    if (isRed(nextX, nextY)) {
                        marks[nextX][nextY] = 2;
                        saveColors[nextX * displayWidth + nextY] = Color.RED;
                        marks[x][y] = 0;
                        saveColors[x * displayWidth + y] = Color.BLACK;
                    }
                    else {
                        marks[nextX][nextY] = 1;
                        saveColors[nextX * displayWidth + nextY] = Color.WHITE;
                        marks[x][y] = 0;
                        saveColors[x * displayWidth + y] = Color.BLACK;
                    }

            }

            for (int i = 0; i < curCount; i++) {
                points[i] = newPoints[i];
            }
        }

        private void print(Canvas canvas) {






            canvas.drawBitmap(saveColors, 0, displayWidth, 0, 0, displayWidth, displayHeight, false, p);

            p1.setARGB(255, 255, 255, 0);
            p1.setTextSize(30);
            canvas.drawText("FPS = " + (1000 / (System.currentTimeMillis() - oldTime)), 10, 30, p1);
            oldTime = System.currentTimeMillis();

        }


        @Override
        public void onDraw(Canvas canvas) {
            print(canvas);
            change();
            invalidate();
        }

        public boolean onTouchEvent(MotionEvent event) {
            int y = (int)event.getX();
            int x = (int)event.getY();

            points[curCount][0] = x;
            points[curCount][1] = y;
            curCount++;
            marks[x][y] = 1;

            saveColors[x * displayWidth + y] = Color.WHITE;

            return super.onTouchEvent(event);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GameView(this));
    }
}
