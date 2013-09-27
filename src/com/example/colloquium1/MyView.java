package com.example.colloquium1;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;
import java.util.Vector;

public class MyView extends SurfaceView implements Runnable
{
    static class Point
    {
        public int x;
        public int y;
        public int c;
        public Point(int x, int y, int c)
        {
            this.x = x;
            this.y = y;
            this.c = c;
        }
    }

    private final int WIDTH = 200;
    private final int HEIGHT = 200;
    private int w = 1280;
    private int h = 800;
    private int[] bytes;
    int[][][] field;
    private int current = 0;
    private SurfaceHolder holder;
    private Random rand = new Random();
    private int EMPTY = Color.argb(255, 0, 0, 0);
    private int FREE = Color.argb(255, 255, 255, 255);
    private int LOCK = Color.argb(255, 255, 0, 0);

    private Vector<Point> points = new Vector<Point>();

    private int frameCounter;
    private long lastFpsCalcUptime;
    private final long FPS_CALC_INTERVAL = 1000;
    private double fps = 0;

    private Paint p = new Paint();

    public MyView(Context context)
    {
        super(context);
        init();

        holder = getHolder();
        Thread thread = new Thread(this);
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void measureFps()
    {
        frameCounter++;
        long now = SystemClock.uptimeMillis();
        long delta = now - lastFpsCalcUptime;
        if (delta > FPS_CALC_INTERVAL) {
            fps = frameCounter * FPS_CALC_INTERVAL / (double)delta;

            frameCounter = 0;
            lastFpsCalcUptime = now;
        }
    }

    private void init()
    {
        field = new int[2][WIDTH][HEIGHT];
        bytes = new int[WIDTH * HEIGHT];
        add(WIDTH/2, HEIGHT/2, LOCK);
        for (int i = 0 ; i < 5000; i++)
        {
            add(rand.nextInt(WIDTH), rand.nextInt(HEIGHT), FREE);
        }
        p.setColor(Color.GREEN);
    }

    public void add(int x, int y, int color)
    {
        points.add(new Point(x,y,color));
    }

    public void run()
    {
        while (true)
        {
            updateF();
            measureFps();
            if (holder.getSurface().isValid())
            {
                Canvas canvas = holder.lockCanvas();
                onDraw(canvas);
                holder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void updateF()
    {
        int next = 1 - current;
        bytes = new int[WIDTH * HEIGHT];
        for (int i = 0; i < points.size(); i++)
        {
            Point p = points.get(i);
            int x = p.x;
            int y = p.y;
            if (p.c == FREE)
            {
                if (check(x, y))
                {
                    p.c = LOCK;
                }
                else
                {
                    int dx = rand.nextInt(3) - 1;
                    int dy = rand.nextInt(3) - 1;
                    p.x = xx(p.x + dx);
                    p.y = yy(p.y + dy);
                }
            }
            bytes[p.x * HEIGHT + p.y] = field[next][p.x][p.y] = p.c;
        }
        current = next;
    }

    public boolean check(int x, int y)
    {
        if (    cell(x-1, y-1) == LOCK ||
                cell(x, y-1) == LOCK ||
                cell(x-1, y) == LOCK ||
                cell(x+1, y-1) == LOCK ||
                cell(x-1, y+1) == LOCK ||
                cell(x, y+1) == LOCK ||
                cell(x+1, y) == LOCK ||
                cell(x+1, y+1) == LOCK)
        {
            return true;
        }
        return false;
    }

    public int cell(int x, int y)
    {
        return field[current][xx(x)][yy(y)];
    }

    public int xx(int x)
    {
        if (x <= -1) x += WIDTH;
        else if (x >= WIDTH) x -= WIDTH;
        return x;
    }

    public int yy(int y)
    {
        if (y <= -1) y += HEIGHT;
        else if (y >= HEIGHT) y -= HEIGHT;
        return y;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int x = (int)((int)event.getX() * ((float)WIDTH / w));
        int y = (int)((int)event.getY() * ((float)HEIGHT / h));
        add(x, y, LOCK);
        return false;
    }



    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        Matrix matrix = new Matrix();
        matrix.setScale(canvas.getWidth() * 1f / WIDTH, canvas.getHeight() * 1f / HEIGHT);
        canvas.setMatrix(matrix);
        canvas.drawBitmap(bytes, 0, WIDTH, 0, 0, WIDTH, HEIGHT, false, null);
        canvas.drawText((Math.ceil(fps * 10) / 10)+" FPS", 10, 10, p);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        this.w = w;
        this.h = h;
    }
}
