package com.example.chenxinhang.pacman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Point;

public class Player implements GameObject {
    private Rect rectangle;
    private int color ;
    private Point target;
    private int xPos;
    private int yPos;
    private int height;
    private int width;
    private int speed;

    public Player( int color,int xPos,int yPos,int width, int height,int speed){
        this.color = color;
        this.xPos = xPos;
        this.yPos = yPos;
        this.rectangle = new Rect(xPos-width/2,yPos-height/2,xPos+width/2,yPos+height/2);
        this.speed = speed;

    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(rectangle,paint);
//        System.out.println(rectangle.height()+", "+rectangle.width());
//        System.out.println(rectangle.left);
    }

    @Override
    public void update(){

    }

    public void changePosition(int x, int y){
        rectangle.set(x-rectangle.width()/2,y-rectangle.height()/2,x+rectangle.width()/2,y+rectangle.height()/2);
    }

    public void update(Point point) {
        target = point;
        int xDiff = target.x-xPos;
        int yDiff = target.y-yPos;
        double displacement = Math.sqrt(xDiff*xDiff+yDiff*yDiff);

        if(Math.abs(xDiff)>=speed){
//            xPos+=(target.x-xPos)/Math.abs(target.x-xPos)*speed;
            xPos += speed*xDiff/displacement;
        }
        if(Math.abs(yDiff)>=speed){
//            yPos+=(target.y-yPos)/Math.abs(target.y-yPos)*speed;
            yPos += speed*yDiff/displacement;
        }


//        rectangle.set(point.x-rectangle.width()/2,point.y-rectangle.height()/2,point.x+rectangle.width()/2,point.y+rectangle.height()/2);
        changePosition(xPos,yPos);
    }

}
