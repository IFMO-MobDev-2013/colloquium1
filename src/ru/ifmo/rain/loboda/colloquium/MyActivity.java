package ru.ifmo.rain.loboda.colloquium;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;

import java.util.Random;

public class MyActivity extends Activity {
    private int[][] firstArray;
    private int[][] secondArray;
    private int width;
    private int height;
    private int[] colours;
    private Paint paint;
    long mills;
    private boolean who;
    private int draws = 0;
    private int[] deltaX = {-1, 0, 1, 1, 1, 0, -1, -1};
    private int[] deltaY = {1, 1, 1, 0, -1, -1, -1, 0};
    private boolean isInit = false;
    class myView extends View {
         myView(Context context){
            super(context);
         }
        @Override
        public void onDraw(Canvas canvas){
            long start = System.currentTimeMillis();
            if(!isInit){
                paint = new Paint();
                colours = new int[width * height];
                firstArray = new int[height][width];
                secondArray = new int[height][width];
                who = true;
                Random random = new Random();
                for(int i = 0; i < 5000; ++i){
                    int x = random.nextInt(width);
                    int y = random.nextInt(height);
                    ++firstArray[y][x];
                }
                firstArray[height / 2][width / 2] = -1;
                isInit = true;
                draws = 0;
                mills = 0;
            }
            ++draws;
            Thread thread;
            //DrawThread(Canvas canvas, int[][] old,int[][] newState, int dx[], int dy[], int[] drawable, Paint paint){
            if(who){
                thread = new DrawThread(canvas, firstArray, secondArray, deltaX, deltaY, colours, paint);
            } else {
                thread = new DrawThread(canvas, secondArray, firstArray, deltaX, deltaY, colours, paint);
            }
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            who = !who;
            mills += System.currentTimeMillis() - start;
            int fps = (int)((float)draws/((float)(mills)/(float)(1000)));
            paint.setARGB(255, 255, 255, 255);
            paint.setTextSize(50);
            canvas.drawText("FPS: " + (new Integer(fps).toString()), 100, 100, paint);
            invalidate();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        height = dm.heightPixels;
        width = dm.widthPixels;
        setContentView(new myView(this));
    }
}
