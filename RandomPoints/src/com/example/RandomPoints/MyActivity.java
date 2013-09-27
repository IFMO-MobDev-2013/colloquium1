package com.example.RandomPoints;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import java.util.Random;

public class MyActivity extends Activity {
    Display display;
    Point size;

    private static final int WIDTH = 480;
    private static final int HEIGHT = 800;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int RED = 0xFFFF0000;
    private static final int BLACK = 0x00000000;
    private static final int ALL = WIDTH*HEIGHT;

    private int ground[] = new int[ALL];
    private int pozx[] = {-1, -1, 0, 1, 1, 1, 0, -1, 0};
    private int pozy[] = {0, -1, -1, -1, 0, 1, 1, 1, 0};

    private int numberOfFixPoints;
    private int numberOfFreePoints;

    private Random rand = new Random();
    Paint paint =  new Paint();
    public Thread mythread1;
    public Thread mythread2;
    public Thread mythread3;
    public Thread mythread4;

    private boolean flag = false;
    long endOfTime, startOfTime;

    public class Recalc implements Runnable {
        int from, to;
        public Recalc(int x, int y) {
            from = x;
            to = y;
        }

        public void check(int poz) {
            if (ground[poz] == RED || ground[poz] == BLACK) return;
            int newx, newy;
            int newpoz;
            int x = poz / WIDTH;
            int y = poz % WIDTH;

            for (int i = 0; i < 8; i++) {
                newx = x + pozx[i];
                newy = y + pozy[i];
                if (newx >= HEIGHT) newx = 0;
                if (newx < 0) newx = HEIGHT - 1;
                if (newy >= WIDTH) newy = 0;
                if (newy < 0) newx = WIDTH - 1;
                newpoz = newx*WIDTH + newy;
                if (ground[newpoz] == RED) {
                    ground[poz] = RED;
                    return;
                }

            }
        }

        public void run() {
            int act;
            int x, y;
            int newx, newy;
            int poz;
            for (int i = from; i < to; i++) {
                if (ground[i] == WHITE) {
                    act = rand.nextInt(9);
                    x = i / WIDTH;
                    y = i % WIDTH;
                    newx = x + pozx[act];
                    newy = y + pozy[act];

                    if (newx >= HEIGHT) newx = 0;
                    if (newx < 0) newx = HEIGHT - 1;
                    if (newy >= WIDTH) newy = 0;
                    if (newy < 0) newx = WIDTH - 1;
                    poz = newx*WIDTH + newy;
                    if (ground[poz] == WHITE) {
                        int tmp = ground[poz];
                        ground[poz] = ground[i];
                        ground[i] = tmp;
                    }
                    if (ground[poz] != WHITE && ground[poz] != RED) {
                        ground[poz] = WHITE;
                        ground[i] = BLACK;
                    }
                }
            }
            for (int i = from; i < to; i++)
                check(i);
        }
    }


    public class Painting extends View {
        Recalc rec1 = new Recalc(0, ALL/4);
        Recalc rec2 = new Recalc(ALL/4 + 1, ALL/4*2);
        Recalc rec3 = new Recalc(ALL/4*2 + 1, ALL/4*3);
        Recalc rec4 = new Recalc(ALL/4*3 + 1, ALL);

        public Painting(Context context) {
            super(context);
        }

        private void prepare(Canvas canvas) {
            paint.setARGB(255, 255, 0, 255);
            paint.setTextSize(15);
            for (int i = 0; i < ALL; i++) {
                ground[i] = BLACK;
            }
            ground[HEIGHT/2 * WIDTH + WIDTH / 2] = RED;
            numberOfFixPoints = 1;
            numberOfFreePoints = 0;
            startOfTime = System.currentTimeMillis();
        }

        private void addPoints(int number) {
            if (numberOfFixPoints + numberOfFreePoints > ALL) {
                return;
            }
            numberOfFreePoints += number;
            int poz;
            for (int i = 0; i < number; i++) {
                poz = rand.nextInt(ALL);
                while (ground[poz] == RED) {
                    poz = rand.nextInt(ALL);
                }
                ground[poz] = WHITE;
            }
        }


        public boolean onTouchEvent(MotionEvent event) {
            int x = (int)event.getX();
            int y = (int)event.getY();
            ground[y*WIDTH + x] = RED;
            return super.onTouchEvent(event);
        }

        @Override
        public void onDraw(Canvas canvas) {
            if (flag == false) {
                prepare(canvas);
                addPoints(5000);
                flag = true;
                canvas.drawBitmap(ground, 0, WIDTH, 0, 0, WIDTH, HEIGHT, false, null);
            }

            mythread1 = new Thread(rec1);
            mythread2 = new Thread(rec2);
            mythread3 = new Thread(rec3);
            mythread4 = new Thread(rec4);

            mythread1.start();
            mythread2.start();
            mythread3.start();
            mythread4.start();

            canvas.drawBitmap(ground, 0, WIDTH, 0, 0, WIDTH, HEIGHT, false, null);
            endOfTime = System.currentTimeMillis();
            canvas.drawText("FPS: " + 1000 / (endOfTime - startOfTime), 10, 30, paint);
            startOfTime = System.currentTimeMillis();
            invalidate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new Painting(this));
    }
}
