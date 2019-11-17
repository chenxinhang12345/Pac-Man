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

    public Food(int xPos, int yPos){
        this.xPos = xPos;
        this.yPos = yPos;
        this.height = 30;
        this.width = 30;
        this.color = Color.CYAN;
        this.rectangle = new Rect(xPos-width/2,yPos-height/2,xPos+width/2,yPos+height/2);
    }
    public Rect getRectangle(){
        return this.rectangle;
    }
    @Override
    public void draw(Canvas canvas){
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(rectangle,paint);
    }
}
