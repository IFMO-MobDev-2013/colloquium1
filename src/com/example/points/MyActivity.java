package com.example.points;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.util.Pair;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.Random;

public class MyActivity extends Activity {

    public static final int WIDTH = 600;
    public static final int HEIGHT = 800;
    public static final int RED_COUNT = 100000;
    Random random = new Random();

    int pixels[] = new int[WIDTH * HEIGHT];
    int pixelsOld[][] = new int[WIDTH][HEIGHT];
    int pixelsNew[][] = new int[WIDTH][HEIGHT];
    int vx[] = {0, -1, 0, 1, 1, 1, 0, -1, -1};
    int vy[] = {0, -1, -1, -1, 0, 1, 1, 1, 0};
    long timeStart;
    float currentIterations = 0;

    Paint paint = new Paint();

    class MyView extends View {
        public MyView(Context context) {
            super(context);
            this.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    pixelsOld[(int)event.getX()][(int)event.getY()] = RED_COUNT + 5;
                    return true;
                }
            });
        }

        @Override
        public void onDraw(Canvas canvas) {
            currentIterations++;
            makeStep();
            getBitmap();
            canvas.drawBitmap(pixels, 0, WIDTH, 0, 0, WIDTH, HEIGHT, true, null);
            canvas.drawText("fps: " + 1000 * currentIterations / (float)(System.currentTimeMillis() - timeStart), 50, 50, paint);
            invalidate();
        }
    }

    public void getBitmap() {
        for(int i = 0; i < WIDTH; i++)
            for(int j = 0; j < HEIGHT; j++)
            {
                if(pixelsOld[i][j] > RED_COUNT)
                    pixels[j * WIDTH + i] = Color.argb(255, 255, 0, 0);
                else if(pixelsOld[i][j] == 0)
                    pixels[j * WIDTH + i] = Color.argb(255, 0, 0, 0);
                else
                    pixels[j * WIDTH + i] = Color.argb(255, 255, 255, 255);
            }
    }

    public void makeStep()
    {
        int z;
        for(int i = 0; i < WIDTH; i++)
            for(int j = 0; j < HEIGHT; j++)
            {
                if(pixelsOld[i][j] < RED_COUNT)
                {
                    for(int x = 0; x < pixelsOld[i][j]; x++)
                    {
                        z = random.nextInt(9);
                        pixelsNew[(i + vx[z] + WIDTH) % WIDTH][(j + vy[z] + HEIGHT) % HEIGHT]++;
                    }
                }
                else
                {
                    for(int x = 0; x < 9; x++)
                        if(pixelsOld[(i + vx[x] + WIDTH) % WIDTH][(j + vy[x] + HEIGHT) % HEIGHT] > 0)
                            pixelsNew[(i + vx[x] + WIDTH) % WIDTH][(j + vy[x] + HEIGHT) % HEIGHT] = RED_COUNT + 5;
                }
            }
        for(int i = 0; i < WIDTH; i++)
            for(int j = 0; j < HEIGHT; j++)
            {
                pixelsOld[i][j] = pixelsNew[i][j];
                pixelsNew[i][j] = 0;
            }
    }
    void addThousand()
    {
        for(int i = 0; i < 1000; i++)
        {
            int z = random.nextInt(WIDTH * HEIGHT);
            int x = z % WIDTH;
            int y = z / WIDTH;
            pixelsOld[x][y]++;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        paint.setTextSize(30);
        paint.setARGB(255, 0, 255, 0);
        timeStart = System.currentTimeMillis();
        for(int i = 0; i < 5; i++)
            addThousand();
        pixelsOld[WIDTH / 2][HEIGHT / 2] = RED_COUNT * 2;
        for(int i = 0; i < WIDTH; i++)
            for(int j = 0; j < HEIGHT; j++)
                pixelsNew[i][j] = 0;

        setContentView(new MyView(this));
    }
}
