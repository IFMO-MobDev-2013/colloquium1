package ru.mihver1.mobdev;

import android.*;
import android.R;
import android.content.res.Resources;
import android.graphics.*;
import android.view.Display;
import android.view.SurfaceHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.graphics.Color.*;
import static java.lang.Long.toString;

/**
 * Created with IntelliJ IDEA.
 * User: mihver1
 * Date: 27.09.13
 * Time: 13:06
 * To change this template use File | Settings | File Templates.
 */
class Pnt {
    public int x, y;
    public boolean fixed;

    Pnt(int x, int y) {
        this.x = x;
        this.y = y;
        fixed = false;
    }
};

class Drawer extends Thread{
    private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;
    Long frames, fixedNum;
    Long startMilis, lastMilis;


    Random rand;
    final int[][] moves = {
            {0, 0},
            {0, 1},
            {1, 1},
            {1, 0},
            {0, -1},
            {-1, -1},
            {-1, 0},
            {1, -1},
            {-1, 1}
    };

    int X, Y;
    List<Pnt> list;
    int[] colors;
    boolean[][] hasFixed;
    Paint p;

    public Drawer(SurfaceHolder surfaceHolder, Resources resources, int X, int Y){
        frames = 0l;
        fixedNum = 1l;
        startMilis = System.currentTimeMillis();
        this.surfaceHolder = surfaceHolder;
        list = new ArrayList<Pnt>();
        rand = new Random();
        for(int i = 0; i < 5000; i++) {
            list.add(new Pnt(rand.nextInt(X), rand.nextInt(Y)));
        }
        Pnt fixed = new Pnt(X/2, Y/2);
        fixed.fixed = true;
        list.add(fixed);
        this.X = X;
        this.Y = Y;
        colors = new int[X*Y];
        hasFixed = new boolean[X][Y];
        p = new Paint();
        p.setARGB(255, 255, 0, 0);
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

    public void add(int num) {
        if(num > 0) {
            for(int i = 0; i < num; i++) {
                list.add(new Pnt(rand.nextInt(X), rand.nextInt(Y)));
            }
        } else {
            int t = list.size()-1;
            while(t < list.size() && num < 0 && t >= 0) {
                if(!list.get(t).fixed){
                    num++;
                    list.remove(t);
                    --t;
                } else {
                    --t;
                }
            }
        }
    }

    public void reset() {
        list.clear();
        for(int i = 0; i < X; i++) {
            for(int j = 0; j < Y; j++) {
                hasFixed[i][j] = false;
            }
        }
        for(int i = 0; i < 5000; i++) {
            list.add(new Pnt(rand.nextInt(X), rand.nextInt(Y)));
        }

        fixedNum = 1l;
        Pnt fixed = new Pnt(X/2, Y/2);
        fixed.fixed = true;
        list.add(fixed);
    }

    public void process() {
        for(int i = 0; i < X*Y; ++i) {
            colors[i] = 0;
        }
        fixedNum = 0l;
        for(int i = 0; i < list.size(); ++i) {
            if(list.get(i).fixed) {
                colors[list.get(i).y*X + list.get(i).x] = 0xff0000;
                hasFixed[list.get(i).x][list.get(i).y] = true;
                fixedNum++;
            } else {
                int move = rand.nextInt(moves.length);
                list.get(i).x += moves[move][0];
                list.get(i).y += moves[move][1];
                if(list.get(i).x < 0)
                    list.get(i).x = X-1;
                if(list.get(i).x >= X)
                    list.get(i).x = 0;
                if(list.get(i).y < 0)
                    list.get(i).y = Y-1;
                if(list.get(i).y >= Y)
                    list.get(i).y = 0;
                colors[list.get(i).y*X + list.get(i).x] = 0xffffff;
            }
        }
        for(int i = 0; i < list.size(); ++i) {
            Pnt temp = list.get(i);
            for(int j = 0; j < moves.length; ++j) {
                int newX = temp.x + moves[j][0];
                int newY = temp.y + moves[j][1];
                if(newX < 0) newX = X-1;
                if(newX >= X) newX = 0;
                if(newY < 0) newY = Y-1;
                if(newY >= Y) newY = 0;
                if(hasFixed[newX][newY]) {
                    temp.fixed = true;
                    colors[temp.y*X + temp.x] = 0xff0000;
                    hasFixed[temp.x][temp.y] = true;

                    break;
                }
            }
        }
    }

    @Override
    public void run() {
        Canvas canvas;
        while (runFlag) {
            canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas(null);
                if(canvas == null) return;
                synchronized (surfaceHolder) {
                    process();
                    canvas.drawBitmap(colors, 0, X, 0, 0, X, Y, false, p);
                    lastMilis = System.currentTimeMillis();
                    canvas.drawText(String.format("FPS:%s", Long.toString(1000/((lastMilis-startMilis)/++frames))), 10, 10, p);
                    canvas.drawText(String.format("DOTS total:%s", Long.toString(list.size())), 10, 20, p);
                    canvas.drawText(String.format("DOTS fixed:%s", Long.toString(fixedNum)), 10, 30, p);
                }
            }
            finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
