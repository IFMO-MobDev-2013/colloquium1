package md.zoidberg.android.colloquium1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

class AutomataView extends SurfaceView implements SurfaceHolder.Callback {
    int x, y;
    AutomataThread automataThread;

    public AutomataView(Context ctx, AttributeSet set) {
        super(ctx, set);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        x = getWidth();
        y = getHeight();

        automataThread = new AutomataThread(holder, x, y);
        automataThread.setRunning(true);
        automataThread.start();
    }

    public void addCells() {
        automataThread.addCells(1000);
    }

    public void removeCells() {
        automataThread.removeCells(1000);
    }

    public void reset() {
        automataThread.reset();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        automataThread.setRunning(false);
        while (retry) {
            try {
                automataThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // it should finish some time later, I guess
            }
        }
    }
}
