package com.example.Colloquium1;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class MyActivity extends Activity {

    /*   lets imagine that there are enum here
        FIXED(1),
        MOVING(2),
        NONE(3); */
    public class Pair {
        public int x;
        public int y;

        public Pair(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public int[][] field;
    public ArrayList<Pair> points;
    public int width;
    public int height;
    public int[] pixels;
    public int redLimit = 5000;
    Random rand = new Random();

    class FieldView extends View {

        public FieldView(Context context) {
            super(context);
        }

        @Override
        public void onDraw(Canvas canvas) {
            preparePixels();
            canvas.drawBitmap(pixels, 0, width, 0, 0, width, height, false, null);
            movePixels();
            invalidate();
        }
    }

    public void preparePixels() {
        int cell;
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                cell = field[i][j];
                pixels[i * width + j] = cell == 3 ? Color.BLACK :
                        cell == 2 ? Color.WHITE :
                                Color.RED;
            }
    }


    private void movePixels() {
        Pair point;
        for (int i = 0; i < points.size(); i++) {
            point = points.get(i);
            int prevx = point.x;
            int prevy = point.y;
            if (field[point.y][point.x] == 2) {
                int move = rand.nextInt(9);
                switch (move) {
                    case (0): {
                        point.x = point.x == 0 ? width - 1 : point.x - 1;
                        point.y = point.y == 0 ? height - 1 : point.y - 1;
                        break;
                    }
                    case (1): {
                        point.y = point.y == 0 ? height - 1 : point.y - 1;
                        break;
                    }
                    case (2): {
                        point.x = point.x == width - 1 ? 0 : point.x + 1;
                        point.y = point.y == 0 ? height - 1 : point.y - 1;
                        break;
                    }
                    case (3): {
                        point.x = point.x == 0 ? width - 1 : point.x - 1;

                        break;
                    }
                    case (4): {
                        break;
                    }
                    case (5): {
                        point.x = point.x == width - 1 ? 0 : point.x + 1;
                        break;
                    }
                    case (6): {
                        point.x = point.x == 0 ? width - 1 : point.x - 1;
                        point.y = point.y == height - 1 ? 0 : point.y + 1;
                        break;
                    }
                    case (7): {
                        point.y = point.y == height - 1 ? 0 : point.y + 1;
                        break;
                    }
                    case (8): {
                        point.x = point.x == width - 1 ? 0 : point.x + 1;
                        point.y = point.y == height - 1 ? 0 : point.y + 1;
                        break;
                    }
                }

                if (field[point.y][point.x] == 1) {
                    field[prevy][prevx] = 1;
                    points.remove(i);
                } else {
                    field[prevy][prevx] = 3;
                    field[point.y][point.x] = 2;
                }


            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Point point = new Point();
        Display display = getWindowManager().getDefaultDisplay();
        display.getSize(point);
        width = point.x;
        height = point.y;
        pixels = new int[width * height];
        field = new int[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                field[i][j] = 3;
            }
        field[height / 2][width / 2] = 1;
        points = new ArrayList<Pair>();
        int posx;
        int posy;
        int temp = 0;
        int k = 0;
        for (int i = 0; i < redLimit + temp; i++) {
            posy = rand.nextInt(height);
            posx = rand.nextInt(width);
            if (field[posy][posx] == 1) {
                temp++;
            } else {
                field[posy][posx] = 2;
                points.add(new Pair(posx, posy));
            }
            k++;
        }
        setContentView(new FieldView(this));
    }
}
