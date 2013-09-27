package com.example.Colloquium1;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.Random;
import java.util.Vector;

import static android.graphics.Bitmap.createScaledBitmap;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    class WhirlView extends View {
        private int WIDTH;
        private int HEIGTH;
        private int[][] pole;
        private int[][] pole1;
        private int free_point;
        private int fps = 0;
        private int[] tmp;
        private Bitmap bitmap;
        private Paint paint= new Paint();
        //private Random random = new Random();
        //private int[][] points;
        private Vector<Integer> points_x = new Vector<Integer>();
        private Vector<Integer> points_y = new Vector<Integer>();
        public WhirlView(Context context) {
            super(context);
            paint.setTextSize(50);
            paint.setColor(Color.YELLOW);
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            WIDTH = size.x;
            HEIGTH = size.y;
            pole = new int[WIDTH][HEIGTH];
            pole1 = new int[WIDTH][HEIGTH];
            pole[WIDTH / 2][HEIGTH / 2] = 1;
            pole1[WIDTH / 2][HEIGTH / 2] = 1;
            free_point = WIDTH * HEIGTH - 1;
            tmp = new int[WIDTH * HEIGTH];
            bitmap = Bitmap.createBitmap(WIDTH, HEIGTH, Bitmap.Config.ARGB_8888);
            //points = new int[WIDTH * HEIGTH][];
            set_start_position();
        }
        private void set_start_position(){
            //points_x.setSize(5000);
            //points_y.setSize(5000);
            for(int i = 0; i < 5000; i++){
                int number = (int) (Math.random() * WIDTH * HEIGTH - 1);
                int temp_x = number / HEIGTH;
                int temp_y = number - temp_x * HEIGTH;
                points_y.add(temp_y);
                points_x.add(temp_x);
                //вставка...
            }
        }

        private int x(int a){
            if (a < 0)
                return (a + WIDTH);
            if (a > WIDTH - 1)
                return a - WIDTH;
            return a;
        }

        private int y(int a){
            if (a < 0)
                return (a + HEIGTH);
            if (a > HEIGTH - 1)
                return a - HEIGTH;
            return a;
        }

        private boolean red(int i){
            int x_sup = points_x.get(i);
            int y_sup = points_y.get(i);
            if (pole[x(x_sup - 1)][y(y_sup - 1)] == 1 ||
                    pole[x(x_sup - 1)][y_sup] == 1 ||
                    pole[x(x_sup - 1)][y(y_sup + 1)] == 1 ||
                    pole[x_sup][y(y_sup - 1)] == 1 ||
                    pole[x_sup][y_sup] == 1 ||
                    pole[x_sup][y(y_sup + 1)] == 1 ||
                    pole[x(x_sup + 1)][y(y_sup - 1)] == 1 ||
                    pole[x(x_sup + 1)][y_sup] == 1 ||
                    pole[x(x_sup + 1)][y(y_sup + 1)] == 1){
                pole1[x_sup][y_sup] = 1;
                points_x.set(i, points_x.get(points_x.size() - 1));
                points_y.set(i, points_y.get(points_y.size() - 1));
                points_x.setSize(points_x.size() - 1);
                points_y.setSize(points_y.size() - 1);
                return true;
            }
            return false;
        }

        private void point_walk(){
            for(int j = 0; j < HEIGTH; j++)
                for(int i = 0; i < WIDTH; i++){
                    pole[i][j] = pole1[i][j];
                }
            for(int i = 0; i < points_x.size(); i++){
                int sup = (int) (Math.random() * 8);

                switch (sup){
                    case 0: {
                        points_x.set(i, x(points_x.get(i) - 1));
                        points_y.set(i, y(points_y.get(i) - 1));

                        break;
                    }
                    case 1: {
                        points_x.set(i, x(points_x.get(i) - 1));
                        //points_y.set(i, points_y.get(i) - 1);
                        break;
                    }
                    case 2: {
                        points_x.set(i, x(points_x.get(i) - 1));
                        points_y.set(i, y(points_y.get(i) + 1));
                        break;
                    }
                    case 3: {
                        //points_x.set(i, points_x.get(i) - 1);
                        points_y.set(i, y(points_y.get(i) - 1));
                        break;
                    }
                    case 4: {
                        //points_x.set(i, points_x.get(i) - 1);
                        //points_y.set(i, points_y.get(i) - 1);
                        break;
                    }
                    case 5: {
                        //points_x.set(i, points_x.get(i) - 1);
                        points_y.set(i, y(points_y.get(i) + 1));
                        break;
                    }
                    case 6: {
                        points_x.set(i, x(points_x.get(i) + 1));
                        points_y.set(i, y(points_y.get(i) - 1));
                        break;
                    }
                    case 7: {
                        points_x.set(i, x(points_x.get(i) + 1));
                        //points_y.set(i, points_y.get(i) - 1);
                        break;
                    }
                    case 8: {
                        points_x.set(i, x(points_x.get(i) + 1));
                        points_y.set(i, y(points_y.get(i) + 1));
                        break;
                    }
                }
                if (red(i)){
                    i--;
                    free_point--;
                }
                else{
                    pole[points_x.get(i)][points_y.get(i)] = -1;
                }
            }
            int l = 0;
            for(int j = 0; j < HEIGTH; j++)
                for(int i = 0; i < WIDTH; i++){
                    if(pole1[i][j] == 1){
                        pole[i][j] = 1;
                    }
                    //tmp[l++] = (pole[i][j] != 1) ? Color.BLACK : Color.RED;
                    if (pole[i][j] == -1)
                        tmp[l++] = Color.WHITE;
                    if (pole[i][j] == 1)
                        tmp[l++] = Color.RED;
                    if (pole[i][j] == 0)
                        tmp[l++] = Color.BLACK;

                }
            //int[][] sup = pole;
            //pole =  pole1;
            //pole1 = sup;
        }

        private boolean add_points(int n){
            if(free_point == 0)
                return false;
            for(int i = 0; i < n; i++){
                int number = (int) (Math.random() * free_point - 1);
                //вставка...
            }
            return true;
        }

        private boolean del_points(int n){
            if (WIDTH * HEIGTH - free_point < n)
                return false;
            for(int i = 0; i < n; i++){
                int number = (int) (Math.random() * free_point - 1);
                // удаление
            }
            return true;
        }

        private void reload(){
            pole = new int[WIDTH][HEIGTH];
            pole = new int[WIDTH][HEIGTH];
            pole[WIDTH / 2][HEIGTH / 2] = 1;
            pole[WIDTH / 2][HEIGTH / 2] = 1;
            free_point = WIDTH * HEIGTH - 1;
            set_start_position();
        }

        public void onDraw(Canvas canvas) {
            fps++;
            //вывод
            point_walk();
            bitmap.setPixels(tmp, 0, WIDTH, 0, 0, WIDTH, HEIGTH);
            canvas.drawBitmap(createScaledBitmap(bitmap, WIDTH, HEIGTH, false), 0, 0, null);
            canvas.drawText(" FPS = " + (fps * 1000 / SystemClock.currentThreadTimeMillis()), WIDTH - 250, HEIGTH - 30, paint);
            invalidate();
        }


    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //WhirlView a = ;
        setContentView(new WhirlView(this));
    }
}
