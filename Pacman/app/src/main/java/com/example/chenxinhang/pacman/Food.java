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

    public Food(int xPos, int yPos, String type ){
        this.xPos = xPos;
        this.yPos = yPos;
        this.height = 10;
        this.width = 10;
        this.type = type;
        this.color = Color.rgb(252, 148, 3);
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
