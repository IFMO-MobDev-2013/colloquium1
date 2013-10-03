package com.polarnick.col1;

import android.util.Log;

import java.util.Iterator;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class FieldUpdater implements Runnable {

    private final Random r = new Random();

    private Profiler<FieldUpdaterThreadFunctions> profiler;

    private final int width;
    private final int height;

    private final int[] fieldColour;
    private final Queue<Point> points;

    private final int[] count;

    private boolean[] wasBlocked;
    private boolean[] willBlocked;

    private final int yourPartOfWork;
    private final int countOfWorkParts;

    private CountDownLatch counter;

    private final int[] dIndex;
    private final int[] dx = new int[]{-1, 0, 1, -1, 0, 1, -1, 0, 1};
    private final int[] dy = new int[]{-1, -1, -1, 1, 1, 1, 0, 0, 0};

    public FieldUpdater(int threadNum, int yourPartOfWork, int countOfWorkParts, int width, int height,
                        int[] fieldColour, Queue<Point> points, boolean[] wasBlocked, boolean[] willBlocked, int[] count) {
        this.profiler = new Profiler<FieldUpdaterThreadFunctions>(this.getClass(), "thread=" + threadNum);

        this.yourPartOfWork = yourPartOfWork;
        this.countOfWorkParts = countOfWorkParts;

        this.width = width;
        this.height = height;

        this.fieldColour = fieldColour;
        this.points = points;

        this.dIndex = new int[]{-width - 1, -width, -width + 1, width - 1, width, width + 1, -1, 0, 1};

        this.wasBlocked = wasBlocked;
        this.willBlocked = willBlocked;

        this.count = count;
    }

    public void setCounter(CountDownLatch counter) {
        this.counter = counter;
    }

    @Override
    public void run() {
        updateField5();
        counter.countDown();
        if (profiler.isToLogNextStep()) {
            Log.d(this.getClass().getName(), profiler.getAverangeLog());
        }
    }

    private void updateField5() {
        profiler.in(FieldUpdaterThreadFunctions.updateField5);
        Iterator<Point> iterator = points.iterator();
        int fromPoint = yourPartOfWork * points.size() / countOfWorkParts;
        int toPoint = (yourPartOfWork + 1) * points.size() / countOfWorkParts;
        skip(iterator, fromPoint);
        int cur = fromPoint;
        while (iterator.hasNext() && cur < toPoint) {
            Point p = iterator.next();
            if (!p.isWasFixed()) {
                int dTo = r.nextInt(dIndex.length);
                int newIndex = updateIndex(p.getIndex(), dTo);
                for (int i = 0; i < dIndex.length; i++) {
                    if (wasBlocked[updateIndex(newIndex, i)]) {
                        p.setNowFixed(true);
                        willBlocked[newIndex] = true;
                        break;
                    }
                }
                --count[p.getIndex()];
                if (count[p.getIndex()] == 0) {
                    fieldColour[p.getIndex()] = DrawerThread.BACKGROUND_COLOUR;
                }
                ++count[newIndex];
                fieldColour[newIndex] = DrawerThread.POINT_COLOUR;
                p.setIndex(newIndex);
            }
            cur++;
        }
        profiler.out(FieldUpdaterThreadFunctions.updateField5);
    }

    private int updateIndex(int index, int deltaI) {
        int delta = dIndex[deltaI];
        int res = -1;
        if (index % width != 0 && index % width != width - 1 && index >= width && index < (width - 1) * height) {
            res = index + delta;
        } else {
            int x = index % width;
            int y = index / width;
            x = (x + dx[deltaI] + width) % width;
            y = (y + dy[deltaI] + height) % height;
            res = y * width + x;
        }
        return res;
    }

    private void skip(Iterator iterator, int count) {
        for (int i = 0; i < count; i++) {
            iterator.next();
        }
    }

    private enum FieldUpdaterThreadFunctions {
        updateField5
    }

}
