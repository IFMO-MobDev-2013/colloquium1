package com.polarnick.col1;

/**
 * Date: 27.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class Point {

    private int index;

    private boolean wasFixed = false;
    private boolean nowFixed = false;

    public Point(int index) {
        this(index, false);
    }

    public Point(int index, boolean fixed) {
        wasFixed = fixed;
        nowFixed = fixed;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isWasFixed() {
        return wasFixed;
    }

    public void setWasFixed(boolean wasFixed) {
        this.wasFixed = wasFixed;
    }

    public boolean isNowFixed() {
        return nowFixed;
    }

    public void setNowFixed(boolean nowFixed) {
        this.nowFixed = nowFixed;
    }

    public boolean updateBlockedState() {
        if (!wasFixed && nowFixed) {
            wasFixed = nowFixed;
            return true;
        } else {
            wasFixed = nowFixed;
            return false;
        }
    }
}
