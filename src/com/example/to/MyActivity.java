package com.example.to;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class MyActivity extends Activity {
    public int w, h, f, ans;
    long time;
    public int[][] a = new int[2000][2000];
    public int[][] b = new int[2000][2000];
    public int[][] count = new int[2000][2000];
    public int[][] old = new int[2000][2000];
    public int[] pi = new int[2000 * 2000];
    public int[][] x = new int[9][2];
    public Paint p = new Paint();
    Random random = new Random();

    class t extends View {

        public t(Context context) {
            super(context);
            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v,MotionEvent event) {
                    push(event.getX(),event.getY());
                    invalidate();
                    return true;
                }

            });
        }

         public void push(float x,float y)
         {
          a[(int)y][(int)x]=2;
         }

        @Override
        public void onDraw(Canvas canvas) {
            ans++;
            for (int i = 0; i < h; i++)
                for (int j = 0; j < w; j++)
                    if (i == 0 || j == 0 || i == h - 1 || j == w - 1) {
                        if (a[i][j] == 1) {

                            int o = count[i][j];
                            for (int p = 0; p < o; p++) {
                                int k = (int) random.nextInt(9);
                                //  b[i][j]=1;
                                b[(i + x[k][0] + h) % h][(j + x[k][1] + w) % w] = 1;
                                old[(i + x[k][0] + h) % h][(j + x[k][1] + w) % w]++;
                            }
                        } else if (a[i][j] == 2) {
                            b[i][j] = a[i][j];
                            old[i][j] = count[i][j];
                        }
                    } else if (a[i][j] == 1) {

                        int k1 = count[i][j];
                        for (int p = 0; p < k1; p++) {
                            int k = (int) random.nextInt(9);
                            //  b[i][j]=1;
                            b[i + x[k][0]][j + x[k][1]] = 1;
                            old[i + x[k][0]][j + x[k][1]]++;
                        }
                    } else if (a[i][j] == 2) {
                        b[i][j] = a[i][j];
                        old[i][j] = count[i][j];
                    }
            for (int i = 0; i < h; i++)
                for (int j = 0; j < w; j++) {
                    a[i][j] = b[i][j];
                    count[i][j] = old[i][j];
                    b[i][j] = 0;
                    old[i][j] = 0;
                }
            for (int i = 0; i < h; i++)
                for (int j = 0; j < w; j++)
                    if (i == 0 || j == 0 || i == h - 1 || j == w - 1) {
                        if (a[i][j] == 1) {
                            f = 0;
                            for (int k = 0; k < 9; k++)
                                if (a[(i + x[k][0] + h) % h][(j + x[k][1] + w) % w] == 2) {
                                    f = 1;
                                    break;
                                }
                            b[i][j] = a[i][j];
                            if (f == 1) b[i][j] = 2;
                        } else {
                            b[i][j] = a[i][j];
                        }
                    } else {
                        if (a[i][j] == 1) {
                            f = 0;
                            for (int k = 0; k < 9; k++)
                                if (a[i + x[k][0]][j + x[k][1]] == 2) {
                                    f = 1;
                                    break;
                                }
                            b[i][j] = a[i][j];
                            if (f == 1) b[i][j] = 2;
                        } else {
                            b[i][j] = a[i][j];
                        }
                    }
            int k = 0;
            for (int i = 0; i < h; i++)
                for (int j = 0; j < w; j++)

                {
                    a[i][j] = b[i][j];
                    b[i][j] = 0;
                    if (a[i][j] == 0)
                        pi[k++] = Color.BLACK;
                    else if (a[i][j] == 1)
                        pi[k++] = Color.WHITE;
                    else
                        pi[k++] = Color.RED;
                }
            canvas.drawBitmap(pi, 0, w, 0, 0, w, h, true, null);
            canvas.drawText("fps=" + ans / ((SystemClock.uptimeMillis() - time + 1000) / 1000), 5, 30, p);
            invalidate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Display display = getWindowManager().getDefaultDisplay();
        w = display.getWidth();
        h = display.getHeight();
        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++) {
                a[i][j] = 0;
                count[i][j] = 0;
                b[i][j] = 0;
                old[i][j] = 0;
            }
        int q, e;
        for (int k = 0; k < 5000; k++) {
            q = (int) random.nextInt(h);
            e = (int) random.nextInt(w);
            a[q][e] = 1;
            count[q][e]++;
        }

        a[h / 2][w / 2] = 2;

        x[0][0] = 0;
        x[0][1] = 1;
        x[1][0] = 0;
        x[1][1] = -1;
        x[2][0] = 1;
        x[2][1] = 0;
        x[3][0] = -1;
        x[3][1] = 0;
        x[4][0] = 1;
        x[4][1] = 1;
        x[5][0] = -1;
        x[5][1] = 1;
        x[6][0] = 1;
        x[6][1] = -1;
        x[7][0] = -1;
        x[7][1] = -1;
        x[8][0] = 0;
        x[8][1] = 0;
        p.setARGB(255, 0, 255, 0);
        p.setTextSize(20);
        time = SystemClock.uptimeMillis();
        ans = 0;
        super.onCreate(savedInstanceState);
        setContentView(new t(this));
    }
}
