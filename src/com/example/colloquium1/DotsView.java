package com.example.colloquium1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;


public class DotsView extends View {
    private final Integer FIRST_FIXED;
    private Paint p;
    private Bitmap bm;
    private int[] colors;
    private int WIDTH;
    private int HEIGHT;
    private ArrayList<Integer> fixedPoints;
    private ArrayList<Integer> floatingPoints;
    private Random r;
    public DotsView(Context context) {
        super(context);
        r = new Random();
        p = new Paint();
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        WIDTH = metrics.widthPixels;
        HEIGHT= metrics.heightPixels - 300;
        FIRST_FIXED = 300 * WIDTH + 300;
        fixedPoints = new ArrayList();
        fixedPoints.add(FIRST_FIXED);
        floatingPoints = new ArrayList();
        initialize_colors();
        addFloatingPoints();
        addFloatingPoints();



        bm = Bitmap.createBitmap(colors, 0, WIDTH, WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);


    }
    private void initialize_colors() {
        colors = new int[WIDTH * HEIGHT];
        for (int i = 0; i < WIDTH * HEIGHT; i++) {
            colors[i] = Color.BLACK;
        }
        for (int i = 0; i < fixedPoints.size(); i++) {
            colors[fixedPoints.get(i).intValue()] = Color.RED;
        }
        for (int i = 0; i < floatingPoints.size(); i++) {
            colors[floatingPoints.get(i).intValue()] = Color.WHITE;
        }

        for (int i =0; i < 100; i++) colors[i] = Color.RED;
    }
    private int chooseNext(int x) {
        int i = x / WIDTH;
        int j = x % WIDTH;
        int r1 = i == 0 ? HEIGHT - 1 : i - 1;
        int r2 = i == HEIGHT - 1 ? 0 : i + 1;
        int c1 = j == 0 ? WIDTH -1 : j - 1;
        int c2 = j == WIDTH - 1 ? 0 : j + 1;
        int rand = r.nextInt(9);
        int ret = x;
        switch (rand) {
            case 0: ret = (r1 * WIDTH + c1); break;
            case 1: ret = (r2 * WIDTH + c2); break;
            case 3: ret = (r1 * WIDTH + c2); break;
            case 4: ret = (r2 * WIDTH + c1); break;
            case 5: ret = (r1 * WIDTH + j); break;
            case 6: ret = (r2 * WIDTH + j); break;
            case 7: ret = (i * WIDTH + c1); break;
            case 8: ret = (i * WIDTH + c2);break;
            case 9: ret = x;break;
        }
        return ret;
    }
    private void update_grid() {
        for (int k = 0; k < floatingPoints.size(); k++) {
            Integer ix = floatingPoints.get(k);
            if (isNeighboorFixed(ix.intValue())){

                colors[ix.intValue()] = Color.RED;
                floatingPoints.remove(k);
                fixedPoints.add(ix);
            }
        }
        for (int k = 0; k < floatingPoints.size(); k++) {
            Integer ix = floatingPoints.get(k);
            int v = chooseNext(ix.intValue());
            floatingPoints.set(k, Integer.valueOf(v));
            colors[ix.intValue()] = Color.BLACK;
            colors[v] = Color.WHITE;

        }
    }

    public void addFloatingPoints() {
        int count = 0;
        while (count < 1000) {
            int tx = r.nextInt(WIDTH * HEIGHT);
            if (!isNeighboorFixed(tx) && !fixedPoints.contains(Integer.valueOf(tx))) {
                floatingPoints.add(Integer.valueOf(tx));
                count++;
            }
        }
    }
    public void removeFloatingPoints() {
        if(floatingPoints.size() > 1000) {
            for (int i =0; i < 1000; i++) {
                floatingPoints.remove(0);
            }
        }
        else {
            floatingPoints.clear();
        }
    }
    private boolean isNeighboorFixed(int x) {
        int i,j;
        i = x / WIDTH;
        j = x % WIDTH;
        //int t = (colors[i * WIDHT + j] + 1) % COLORS;
        int t = Color.RED;
        int r1 = i == 0 ? HEIGHT - 1 : i - 1;
        int r2 = i == HEIGHT - 1 ? 0 : i + 1;
        int c1 = j == 0 ? WIDTH -1 : j - 1;
        int c2 = j == WIDTH - 1 ? 0 : j + 1;
        if (colors[r1 * WIDTH + c1] == t || colors[r2 * WIDTH + c2] == t || colors[r1 * WIDTH + c2] == t || colors[r2 * WIDTH + c1] == t
                || colors[r1 * WIDTH + j] == t || colors[r2 *WIDTH + j] == t || colors [i * WIDTH + c1] == t || colors[i * WIDTH + c2] == t) {
            return true;
        }
        return false;

    }
    @Override
    public void onDraw (Canvas c) {
        bm = Bitmap.createBitmap(colors, 0, WIDTH, WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        c.drawBitmap(bm, 0.0f, 0.0f, p);
        p.setColor(Color.RED);
        update_grid();
        invalidate();
    }



}
