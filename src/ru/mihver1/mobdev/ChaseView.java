package ru.mihver1.mobdev;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created with IntelliJ IDEA.
 * User: mihver1
 * Date: 27.09.13
 * Time: 13:34
 * To change this template use File | Settings | File Templates.
 */
public class ChaseView extends SurfaceView implements SurfaceHolder.Callback {
    private Drawer drawer;

    public ChaseView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    public ChaseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }

    public ChaseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawer = new Drawer(getHolder(), getResources(), getWidth(), getHeight());
        drawer.setRunning(true);
        drawer.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        // завершаем работу потока
        drawer.setRunning(false);
        while (retry) {
            try {
                drawer.join();
                retry = false;
            } catch (InterruptedException e) {
                // если не получилось, то будем пытаться еще и еще
            }
        }
    }
}