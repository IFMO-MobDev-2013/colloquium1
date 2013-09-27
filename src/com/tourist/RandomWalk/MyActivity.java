package com.tourist.RandomWalk;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.Random;

public class MyActivity extends Activity {

    int buttonHeight;

    class MyButton1 extends Button {
        public MyButton1(Context context) {
            super(context);
            this.setText("Add 1000 points");
            buttonHeight = this.getHeight();
            this.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    myView.add1000();
                }
            });
        }
    }

    class MyButton2 extends Button {
        public MyButton2(Context context) {
            super(context);
            this.setText("Remove 1000 points");
            this.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    myView.remove1000();
                }
            });
        }
    }

    class MyButton3 extends Button {
        public MyButton3(Context context) {
            super(context);
            this.setText("Reset");
            this.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    myView.initField();
                }
            });
        }
    }

    class MyView extends View {

        public MyView(Context context) {
            super(context);
            initField();
        }

        void performGlue() {
            boolean changed = true;
            while (changed) {
                changed = false;
                int i = 0;
                while (i < whiteCount) {
                    int x = white[i].x;
                    int y = white[i].y;
                    boolean redNeighbor = false;
                    for (int dx = -1; dx <= 1; dx++) {
                        int newX = x + dx;
                        if (newX < 0) {
                            newX += width;
                        }
                        if (newX >= width) {
                            newX -= width;
                        }
                        for (int dy = -1; dy <= 1; dy++) {
                            int newY = y + dy;
                            if (newY < 0) {
                                newY += height;
                            }
                            if (newY >= height) {
                                newY -= height;
                            }
                            if (field[newY * width + newX] == 0xFF0000) {
                                redNeighbor = true;
                                break;
                            }
                        }
                        if (redNeighbor) {
                            break;
                        }
                    }
                    if (redNeighbor) {
                        field[y * width + x] = 0xFF0000;
                        changed = true;
                        white[i] = white[whiteCount - 1];
                        whiteCount--;
                    } else {
                        i++;
                    }
                }
            }
        }

        Random rnd = new Random();

        int width;
        int height;
        Point[] white;
        int whiteSize = 100000;
        int whiteCount;
        int[] field;
        long startTime = 0;
        int frames = 0;

        void initField() {
            Display display = getWindowManager().getDefaultDisplay();
            Point p = new Point();
            display.getSize(p);

            width = p.x;
            height = p.y - 3 * buttonHeight;

            field = new int[height * width];
            field[(height / 2) * width + (width / 2)] = 0xFF0000;

            white = new Point[whiteSize];
            for (int i = 0; i < whiteSize; i++) {
                white[i] = new Point();
            }
            whiteCount = 5000;
            for (int i = 0; i < whiteCount; i++) {
                do {
                    white[i].x = rnd.nextInt(width);
                    white[i].y = rnd.nextInt(height);
                } while (field[white[i].y * width + white[i].x] == 0xFF0000);
            }

            for (int i = 0; i < whiteCount; i++) {
                field[white[i].y * width + white[i].x] = 0xFFFFFF;
            }

            performGlue();
        }

        void randomMoves() {
            for (int i = 0; i < whiteCount; i++) {
                field[white[i].y * width + white[i].x] = 0;
            }

            for (int i = 0; i < whiteCount; i++) {
                int dx = rnd.nextInt(3) - 1;
                int dy = rnd.nextInt(3) - 1;
                white[i].x += dx;
                if (white[i].x < 0) {
                    white[i].x = width - 1;
                }
                if (white[i].x >= width) {
                    white[i].x = 0;
                }
                white[i].y += dy;
                if (white[i].y < 0) {
                    white[i].y = height - 1;
                }
                if (white[i].y >= height) {
                    white[i].y = 0;
                }
            }

            for (int i = 0; i < whiteCount; i++) {
                field[white[i].y * width + white[i].x] = 0xFFFFFF;
            }
        }

        public void add1000() {
            if (whiteCount == whiteSize) {
                return;
            }
            for (int i = whiteCount; i < whiteCount + 1000; i++) {
                do {
                    white[i].x = rnd.nextInt(width);
                    white[i].y = rnd.nextInt(height);
                } while (field[white[i].y * width + white[i].x] == 0xFF0000);
            }
            whiteCount += 1000;
        }

        public void remove1000() {
            if (whiteCount == 0) {
                return;
            }
            for (int i = 0; i < 1000; i++) {
                int x = rnd.nextInt(whiteCount);
                field[white[x].y * width + white[x].x] = 0;
                white[x] = white[whiteCount - 1];
                whiteCount--;
            }
        }

        @Override
        public void onDraw(Canvas canvas) {
            if (startTime == 0) {
                startTime = System.currentTimeMillis();
            }

            randomMoves();
            performGlue();
            canvas.drawBitmap(field, 0, width, 0, 0, width, height, false, null);

            long currentTime = System.currentTimeMillis();
            frames++;
            int fps = (int)(1000 * frames / (currentTime - startTime + 1));

            Paint paint = new Paint();
            paint.setColor(0xFF0000FF);
            paint.setTextSize(50);
            canvas.drawText("fps: " + fps, 30, 100, paint);

            invalidate();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_DOWN) {
                int touchX = (int)event.getX();
                int touchY = (int)event.getY();
                if (0 <= touchX && touchX < width && 0 <= touchY && touchY < height) {
                    field[touchY * width + touchX] = 0xFF0000;
                }
            }
            return true;
        }
    }

    MyView myView;
    MyButton1 myButton1;
    MyButton2 myButton2;
    MyButton3 myButton3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        myButton1 = new MyButton1(this);
        myButton2 = new MyButton2(this);
        myButton3 = new MyButton3(this);
        myView = new MyView(this);
        ll.addView(myButton1);
        ll.addView(myButton2);
        ll.addView(myButton3);
        ll.addView(myView);
        setContentView(ll);
    }
}
