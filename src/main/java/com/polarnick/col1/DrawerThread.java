package com.polarnick.col1;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DrawerThread extends Thread {

    private final SurfaceHolder surfaceHolder;

    private final static int THREAD_COUNT = Runtime.getRuntime().availableProcessors() + 1;
    private final static int FIELD_UPDATER_COUNT = THREAD_COUNT;

    private final static int FOOTER_HEIGHT = 128;
    //Default:     240 x 320
    //Xperia Sola: 480 x 854
    //Galaxy S3:   720 x 1280
    private final static int WIDTH = HelloAndroidActivity.getScreenWidth();
    private final static int HEIGHT = HelloAndroidActivity.getScreenHeight() - FOOTER_HEIGHT;

    private static final int UPDATE_FPS_AFTER_MS = 239;
    private static final int MS_IN_SECOND = 1000;
    private static final Paint FPS_COLOR_TEXT = new Paint();

    private static final float TEXT_SIZE = 30f;
    private static final int TEXT_ALPHA = 255;

    private static final int TEXT_OFFSET_X = 15;
    private static final int TEXT_OFFSET_Y = 35;

    private final static Random random = new Random();

    public static final int POINT_COLOUR = Color.WHITE;
    public static final int BACKGROUND_COLOUR = Color.BLACK;

    private final Profiler<WhirlViewFunctions> profiler = new Profiler<WhirlViewFunctions>(this.getClass());

    private final ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
    private final List<FieldUpdater> updaters = new ArrayList<FieldUpdater>(FIELD_UPDATER_COUNT);

    private int[] field = new int[WIDTH * HEIGHT];
    private boolean[] wasBlocked = new boolean[WIDTH * HEIGHT];
    private boolean[] willBlocked = new boolean[WIDTH * HEIGHT];
    private int[] pointsCount = new int[WIDTH * HEIGHT];
    private Queue<Point> points = new ConcurrentLinkedQueue<Point>();

    private boolean runFlag;

    public DrawerThread(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
        initFieldUpdaters();
        initStartState();
        addPoints(1000);
    }

    private void addPoints(int count) {
        for (int i = 0; i < count; i++) {
            int index = random.nextInt(WIDTH * HEIGHT);
            while (wasBlocked[index]) {
                index = random.nextInt(WIDTH * HEIGHT);
            }
            Point p = new Point(index, false);
            points.add(p);
            ++pointsCount[index];
        }
    }

    private void initStartState() {
        final int index = HEIGHT / 2 * WIDTH + WIDTH / 2;
        wasBlocked[index] = true;
        ++pointsCount[index];
        field[index] = POINT_COLOUR;

        Arrays.fill(field, BACKGROUND_COLOUR);
        field[index] = POINT_COLOUR;
    }

    private void initFieldUpdaters() {
        for (int i = 0; i < FIELD_UPDATER_COUNT; i++) {
            updaters.add(new FieldUpdater(i, i, FIELD_UPDATER_COUNT, WIDTH, HEIGHT, field, points, wasBlocked, willBlocked, pointsCount));
        }
    }

    @Override
    public void run() {
        while (runFlag) {
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                    if (canvas != null) {
                        onDraw(canvas);
                    }
                }
            } finally {
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
            if (profiler.isToLogNextStep()) {
                Log.d(this.getClass().getName(), profiler.getAverangeLog());
            }
            sleepNano(1);
        }
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

    public void onDraw(final Canvas canvas) {
        profiler.in(WhirlViewFunctions.onDraw);
        update(canvas);
        profiler.out(WhirlViewFunctions.onDraw);
    }

    private void update(final Canvas canvas) {
        final CountDownLatch counter = new CountDownLatch(THREAD_COUNT);
        for (FieldUpdater updater : updaters) {
            updater.setCounter(counter);
            pool.execute(updater);
        }

        pool.execute(new Runnable() {
            @Override
            public void run() {
                counter.countDown();
            }
        });

        try {
            counter.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        renderFieldBitmap(field, canvas);
        updateData();
    }

    private void updateData() {
        Iterator<Point> iterator = points.iterator();
        while (iterator.hasNext()) {
            Point p = iterator.next();
            if (p.isNowFixed()) {
                wasBlocked[p.getIndex()] = true;
                iterator.remove();
                p.updateBlockedState();
            }
        }

        boolean[] tmp = wasBlocked;
        wasBlocked = willBlocked;
        willBlocked = tmp;
    }

    private void sleepNano(int time) {
        try {
            Thread.sleep(0L, time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long fps;

    private long frameStart = System.currentTimeMillis();
    private long frames;

    static {
        FPS_COLOR_TEXT.setARGB(TEXT_ALPHA, 100, 100, 255);
        FPS_COLOR_TEXT.setTextSize(TEXT_SIZE);
    }

    private void renderFieldBitmap(int[] colors, Canvas canvas) {
        profiler.in(WhirlViewFunctions.renderFieldBitmap);
        canvas.scale(1f * HelloAndroidActivity.getScreenWidth() / WIDTH,
                1f * (HelloAndroidActivity.getScreenHeight() - FOOTER_HEIGHT) / HEIGHT);
        canvas.drawBitmap(colors, 0, WIDTH, 0, 0, WIDTH, HEIGHT, false, null);
        final long delta = System.currentTimeMillis() - frameStart;
        frames++;
        if (delta > UPDATE_FPS_AFTER_MS) {
            fps = (frames * MS_IN_SECOND / delta);
            frameStart = System.currentTimeMillis();
            frames = 0;
        }
        canvas.drawText(fps + " FPS", TEXT_OFFSET_X, TEXT_OFFSET_Y, FPS_COLOR_TEXT);
        profiler.out(WhirlViewFunctions.renderFieldBitmap);
    }

    private enum WhirlViewFunctions {

        onDraw, updateField, getNewColor, updateField2, renderFieldBitmap, updateField3, updateField4, renderField

    }

}
