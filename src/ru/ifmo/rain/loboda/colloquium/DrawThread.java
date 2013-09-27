package ru.ifmo.rain.loboda.colloquium;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.Random;

public class DrawThread extends Thread {
    private Canvas canvas;
    private int[][] old;
    private int[][] newState;
    private int[] dx;
    private int[] dy;
    private int[] drawable;
    private Paint paint;
    DrawThread(Canvas canvas, int[][] old,int[][] newState, int dx[], int dy[], int[] drawable, Paint paint){
        this.canvas = canvas;
        this.old = old;
        this.newState = newState;
        this.dx = dx;
        this.dy = dy;
        this.drawable = drawable;
        this.paint = paint;
    }
    @Override
    public void run() {
        int in, jn;
        Random random = new Random();
        for(int i = 0; i < newState.length; ++i){
            for(int j = 0; j < newState[0].length; ++j){
                newState[i][j] = 0;
            }
        }
        for(int i = 0; i < newState.length; ++i){
            for(int j = 0; j < newState[0].length; ++j){
                if(old[i][j] == -1){
                    newState[i][j] = -1;
                    continue;
                }
                for(int h = 0; h < old[i][j]; ++h){
                    boolean isStuck = false;
                    for(int k = 0; k < 8; ++k){
                        in = i + dx[k];
                        jn = j + dy[k];
                        if(in >= newState.length){
                            in = 0;
                        }
                        if(in < 0){
                            in = newState.length - 1;
                        }
                        if(jn >= newState[0].length){
                            jn = 0;
                        }
                        if(jn < 0){
                            jn = newState[0].length - 1;
                        }
                        if(old[in][jn] == -1){
                            newState[i][j] = -1;
                            isStuck = true;
                            break;
                        }
                    }
                    if(!isStuck){
                        int a = random.nextInt(8);
                        in = i + dx[a];
                        jn = j + dy[a];
                        if(in >= newState.length){
                            in = 0;
                        }
                        if(in < 0){
                            in = newState.length - 1;
                        }
                        if(jn >= newState[0].length){
                            jn = 0;
                        }
                        if(jn < 0){
                            jn = newState[0].length - 1;
                        }
                        ++newState[in][jn];
                    }
                }
            }
        }
        for(int i = 0; i < old.length; ++i){
            for(int j = 0; j < old[0].length; ++j){
                if(newState[i][j] == -1){
                    drawable[i * newState[0].length + j] = 0xFFFF0000;
                    continue;
                }
                if(newState[i][j] == 0){
                    drawable[i * newState[0].length + j] = 0;
                    continue;
                }
                drawable[i * newState[0].length + j] = 0xFFFFFFFF;
            }
        }
        canvas.drawBitmap(drawable, 0, newState[0].length, 0, 0, newState[0].length, newState.length, true, paint);
    }
}
