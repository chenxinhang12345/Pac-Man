package com.example.chenxinhang.pacman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Point;

public class Player implements GameObject {
    private int ID; //network ID
    private Rect rectangle;
    private int color ;
    private Point target;
    private int xPos;
    private int yPos;
    private int oldxPos;
    private int oldyPos;
    private int speed;
    public GamePanel gamePanel;

    public Player( int color,int xPos,int yPos,int width, int height,int speed,int ID, GamePanel gamePanel){
        this.gamePanel = gamePanel;
        this.color = color;
        this.xPos = xPos;
        this.yPos = yPos;
        this.oldxPos = xPos;
        this.oldyPos = yPos;
        this.rectangle = new Rect(xPos-width/2,yPos-height/2,xPos+width/2,yPos+height/2);
        this.speed = speed;
        this.ID = ID;
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public void setxPos(int xPos){ this.xPos = xPos; }

    public void setyPos(int yPos){this.yPos = yPos;}

    public void setColor(int color){
        this.color = color;
    }
    public int getID(){
        return this.ID;
    }
    public int getColor(){
        return color;
    }

    public void undoMove(){
        xPos = oldxPos;
        yPos = oldyPos;
        changePosition(xPos,yPos);
    }

    public Rect getRectangle() {
        return rectangle;
    }

    @Override
    public void draw(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(rectangle,paint);
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
            oldxPos = xPos;
            xPos += speed*xDiff/displacement;
        }
        if(Math.abs(yDiff)>=speed){
            oldyPos = yPos;
            yPos += speed*yDiff/displacement;
        }
        changePosition(xPos,yPos);
    }

}
