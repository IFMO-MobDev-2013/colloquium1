package com.example.colloquim1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.*;

import java.util.Random;

public class MyActivity extends Activity {

    class Field extends View implements View.OnTouchListener {

        Paint p;
        int count = 0;
        boolean drw = true;
        long fpscount = 0;
        long lastTime = System.currentTimeMillis();
        int w, h;
        int RED = 0xFF0000;
        int WHT = 0xFFFFFF;
        int[] field;
        int[] fixed;
        int[] walking;
        int[] newWalking;
        int nfixed;
        int nwalk, newwalk;
        int fixNum = 5000;
        int[] x = {-1, 0, 1, -1, 0, 1, 1, -1, 0};
        int[] y = {-1, -1, -1, 1 , 1, 1, 0, 0, 0};
        long counter = 0;
        long begTime;
        Random generator;

        public Field(Context contex) {
            super(contex);
            this.setOnTouchListener(this);
        }

        public void fps(Canvas canvas) {
            long currTime = System.currentTimeMillis();
            count++;
            long time = currTime - lastTime;
            if (time > 1000) {
                lastTime = currTime;
                fpscount = count / (time / 1000);
                count = 0;
            }
            canvas.drawText("FPS:" + Long.toString(fpscount, 10), w - 80, h - 40, p);

            int t = (int)((currTime - begTime) / 1000);
            if (t != 0) {
                int ifps = (int)(counter / t);
                canvas.drawText("iFPS:" + Long.toString(ifps, 10), w - 80, h - 20, p);
            }
        }

        public void setFieldSize() {
            Display display =((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
            w = display.getWidth();
            h = display.getHeight();
        }

        public void firstFilling() {
            generator = new Random();
            field = new int[w * h];
            fixed = new int[fixNum];
            walking = new int[fixNum];
            newWalking = new int[fixNum];

            begTime = System.currentTimeMillis();

            p = new Paint();
            p.setARGB(255, 255, 255, 255);
            p.setTextSize(20);

            fixed[0] = h * w / 2 + w/2;           //set central RED point
            nfixed = 1;
            field[fixed[0]] = RED;

            int k;
            newwalk = 0;

            for (int i = 0; i < fixNum; i++) { //set random WHITE points
                k = generator.nextInt(w * h);
                walking[nwalk++] = k;
                newWalking[newwalk++] = k;
                field[k] = WHT;
            }
        }

        public void recalcField() {
            int j;
            int newI, newJ, nextInd;
            int newII, newJJ;
            boolean push;
            newwalk = 0;
            for (int i = 0; i < nwalk; i++) {
                push = true;
                j = generator.nextInt(9);
                newI = (walking[i] / w + y[j] + h) % h;
                newJ = (walking[i] % w + x[j] + w) % w;
                nextInd = newI * w + newJ;
                for (int k = 0; k < 8; k++) {
                    newII = (newI + y[k] + h) % h;
                    newJJ = (newJ + x[k] + w) % w;
                    if (field[newII * w + newJJ] == RED) {
                        push = false;
                        fixed[nfixed++] = nextInd;
                        break;
                    }
                }
                if (push) {
                    newWalking[newwalk++] = nextInd;
                }
            }
        }

        public void redrawField() {
            for (int i = 0; i < nwalk; i++) {        //old points are black
                field[walking[i]] = 0;
            }

            for (int i = 0; i < newwalk; i++) {   //new points are WHITE
                field[newWalking[i]] = WHT;
            }

            for (int i = 0; i < nfixed; i++) {    //fixed points are RED
                field[fixed[i]] = RED;
            }

            int[] temp = walking;
            walking = newWalking;
            newWalking = temp;
            nwalk = newwalk;
        }

        @Override
        public void onDraw(Canvas canvas) {
            counter++;
            if (drw) {
                setFieldSize();
                firstFilling();
                drw = false;
            }

            canvas.drawBitmap(field, 0, w, 0, 0, w, h, false, null);

            if (nwalk > 0) {
                recalcField();
                redrawField();
            }

            fps(canvas);
            invalidate();
        }

        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            fixed[nfixed++] = ((int) y) * w + ((int)x);
            return true;
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(new Field(this));
    }
}
