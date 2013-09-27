package com.example.colloc;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.graphics.Color;
import android.view.WindowManager;
import android.view.MotionEvent;

import java.util.Random;
import java.util.ArrayList;

public class MyActivity extends Activity {
    public class PhotonView extends View{
        private class Pair{
            int x, y;
            Pair(int x, int y){
                this.x = x;
                this.y = y;
            }
        }

        Paint paint;
        Random rand;
        int fps = 0, cnt = 0;
        final int WIDTH = 720;
        final int HEIGHT = 1280 - 20;
        final int delta = 20;
        ArrayList<Pair> dynamic_points;
        public boolean[] static_points;
        public int[] bitMap;
        long start_time, end_time;
        public PhotonView(Context context){
            super(context);
            paint = new Paint();
            rand = new Random();
            bitMap = new int[WIDTH * (HEIGHT)];
            static_points = new boolean[WIDTH * (HEIGHT)];
            static_points[WIDTH * ((HEIGHT) / 2 - 1) + WIDTH / 2] = true;
            dynamic_points = new ArrayList();
            setDynamicPoints(5000);
            for(int i = 0; i < bitMap.length; i++)
                bitMap[i] = Color.BLACK;
            start_time = System.currentTimeMillis();
        }
        public boolean onTouchEvent(MotionEvent event) {
            int x = (int)event.getX();
            int y = (int)event.getY();
            static_points[y * WIDTH + x] = true;
            return super.onTouchEvent(event);
        }

        public void onDraw(Canvas canvas){
            update();

            canvas.drawBitmap(bitMap, 0, WIDTH, 0, 0, WIDTH, HEIGHT, true, paint);
            end_time = System.currentTimeMillis();
            if(cnt >= fps){
                fps = (int)(1000 / (end_time - start_time));

                cnt = 0;
            }
            paint.setColor(Color.YELLOW);
            paint.setTextSize(50);
            canvas.drawText("FPS: " + fps, 5, 100, paint);
            start_time = end_time;
            cnt++;
            invalidate();
        }

        public void update(){
            int[] new_field = new int[WIDTH * HEIGHT];
            for(int i = 0; i < new_field.length; i++)
                new_field[i] = Color.BLACK;
            for(int i = 0; i < dynamic_points.size(); i++){
                Pair pos = dynamic_points.get(i);
                int r = rand.nextInt(9);
                if (r >= 1 && r <= 3){
                    pos.y--;
                } else if (r >= 5 && r <= 7){
                    pos.y++;
                }
                if (r == 1 || r >= 7){
                    pos.x--;
                } else if (r >= 3 && r <= 5) {
                    pos.x++;
                }
                if (pos.x >= WIDTH)
                    pos.x = 0;
                else if (pos.x < 0)
                    pos.x = WIDTH - 1;
                if (pos.y >= HEIGHT)
                    pos.y = 0;
                else if (pos.y < 0)
                    pos.y = HEIGHT - 1;
                dynamic_points.get(i).x = pos.x;
                dynamic_points.get(i).y = pos.y;

            }
            for(int i = 0; i < dynamic_points.size(); i++){
                Pair pos = dynamic_points.get(i);

                boolean static_point = false;
                for(int dx = -1; dx <= 1 && !static_point; dx++)
                    for(int dy = -1; dy <= 1 && !static_point; dy++){
                        int nx = dx + pos.x;
                        int ny = dx + pos.y;
                        if (nx >= 0 && nx < WIDTH && ny >= 0 && ny < HEIGHT)
                            if (static_points[ny * WIDTH + nx])
                                static_point = true;

                    }
                if (static_point){
                    static_points[pos.y * WIDTH + pos.x] = true;
                    dynamic_points.remove(i);
                    i--;
                }
            }
            for(int i = 0; i < static_points.length; i++){
                if(static_points[i])
                    new_field[i] = Color.RED;
            }
            for(int i = 0; i < dynamic_points.size(); i++){
                Pair coord = dynamic_points.get(i);
                new_field[coord.y * WIDTH + coord.x] = Color.WHITE;
            }
            bitMap = new_field;
        }

        public void setDynamicPoints(int n){
            for(int i = 0; i < n; i++){
                boolean setled = false;
                while(!setled){
                    int x = rand.nextInt(WIDTH);
                    int y = rand.nextInt(HEIGHT);
                    if (!static_points[y * WIDTH + x]){
                        dynamic_points.add(new Pair(x, y));
                        setled = true;
                    }

                }
            }
        }

        public void removeDynamicPoints(int n){
            n = Math.min(dynamic_points.size(),  n);
            for(int i = 0; i < n; i++){
                dynamic_points.remove(0);
            }
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);

        //setContentView(R.layout.main);
        setContentView(new PhotonView(this));

    }


}
