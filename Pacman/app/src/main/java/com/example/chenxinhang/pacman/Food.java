package com.example.chenxinhang.pacman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Food implements GameObject {
    private int xPos;
    private int yPos;
    private Rect rectangle;
    private int height;
    private int width;
    private int color;
    private String type;

    public Food(int xPos, int yPos, String type) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.height = 10;
        this.width = 10;
        this.type = type;
        if (type.equals("NORMAL")) {
            this.color = Color.rgb(252, 148, 3);
        } else if (type.equals("INVISIBLE")) {
            this.color = Color.BLUE;
        }
        this.rectangle = new Rect(xPos - width / 2, yPos - height / 2, xPos + width / 2, yPos + height / 2);
    }

    public Rect getRectangle() {
        return this.rectangle;
    }

    public Rect getLogicRectangle() {
        return new Rect(rectangle.left, rectangle.top, rectangle.right, rectangle.bottom);
    }

    public String getType() {
        return type;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(rectangle, paint);
    }
}
