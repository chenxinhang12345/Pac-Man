package com.example.chenxinhang.pacman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Wall implements GameObject {
    Rect rectangle;
    int left;
    int top;
    int right;
    int bottom;
    int color;

    public Wall() {
        this.left = 0;
        this.top = 970;
        this.right = 1000;
        this.bottom = 1000;
        this.rectangle = new Rect(left, top, right, bottom);
        this.color = Color.BLACK;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(rectangle, paint);
    }

    public Rect getLogicRectangle(){
        return new Rect(left,top,right,bottom);
    }

}
