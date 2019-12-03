package com.example.chenxinhang.pacman;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.Point;

public class Player implements GameObject {
    private int ID; //network ID
    private Rect rectangle;
    private int color;
    private Point target;
    private int xPos;
    private int yPos;
    private int oldxPos;
    private int oldyPos;
    private int speed;
    private int score;
    private Bitmap pacmanTexture;
    private Paint paint;
    private int width;
    private int height;
    private String type;
    private boolean visible;
    public GamePanel gamePanel;

    public Player(int color, int xPos, int yPos, int width, int height, int speed, int ID, String type, GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.score = 0;
        this.color = color;
        this.xPos = xPos;
        this.yPos = yPos;
        this.oldxPos = xPos;
        this.oldyPos = yPos;
        this.type = type;
        this.width = 20;
        this.height = 20;
        this.visible = true;
        this.rectangle = new Rect(xPos - this.width / 2, yPos - this.height / 2, xPos + this.width / 2, yPos + this.height / 2);
        this.speed = speed;
        this.ID = ID;
//        this.pacmanTexture = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(gamePanel.getResources(), R.drawable.pacman),(int)(1.5*width),(int)(1.5*height),false);
        this.paint = new Paint();
        paint.setColor(color);
    }

    public int getxPos() {
        return xPos;
    }

    public int getyPos() {
        return yPos;
    }

    public void setxPos(int xPos) {
        this.xPos = xPos;
    }

    public void setyPos(int yPos) {
        this.yPos = yPos;
    }

    public String getType() {
        return type;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getID() {
        return this.ID;
    }

    public int getColor() {
        return color;
    }

    public void undoMove() {
        xPos = oldxPos;
        yPos = oldyPos;
        changePosition(xPos, yPos);
    }

    public Rect getRectangle() {
        return rectangle;
    }

    public Rect getLogicRectangle() {
        return new Rect(rectangle.left, rectangle.top, rectangle.right, rectangle.bottom);
    }

    @Override
    public void draw(Canvas canvas) {
//        Paint paint = new Paint();


        if (!visible) {
            if (ID == gamePanel.player1.getID()) {
                paint.setColor(Color.WHITE);
            } else {
                paint.setColor(Color.argb(0, 255, 255, 255));
            }
        } else {
            paint.setColor(color);
        }
        if (this.type.equals("PACMAN")) {
            canvas.drawRect(rectangle, paint);
        } else {
            int x = (rectangle.left + rectangle.right) / 2;
            int y = (rectangle.top + rectangle.bottom) / 2;
            canvas.drawCircle(x, y, rectangle.width() - 5, paint);
        }

//        canvas.drawBitmap(pacmanTexture, xPos-width, yPos-height, paint);
    }

    public void drawScore(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(10);
        canvas.drawText("score: " + score, xPos, yPos, paint);
    }

    public void drawEndInfo(Canvas canvas) {
        paint.setColor(Color.argb(90, 255, 28, 28));
        paint.setTextSize(15);
        String message = "Your team Win!";
        if (gamePanel.getGhostScore() > gamePanel.getPacmanScore()) {
            if (gamePanel.player1.getType().equals("PACMAN")) {
                message = "Your team Lose!";
            }
        } else if (gamePanel.getGhostScore() < gamePanel.getPacmanScore()) {
            if (gamePanel.player1.getType().equals("GHOST")) {
                message = "Your team Lose!";
            }
        } else {
            message = "Draw!";
        }
        canvas.drawText(message, xPos, yPos + 300, paint);
        canvas.drawText("Ghost: " + gamePanel.getGhostScore() + " Pacman: " + gamePanel.getPacmanScore(), xPos, yPos + 100, paint);
    }


    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public void changePosition(int x, int y) {
        rectangle.set(x - rectangle.width() / 2, y - rectangle.height() / 2, x + rectangle.width() / 2, y + rectangle.height() / 2);
    }

    public void update(Point point) {
        target = point;
        int xDiff = target.x - xPos;
        int yDiff = target.y - yPos;
        double displacement = Math.sqrt(xDiff * xDiff + yDiff * yDiff);

        if (Math.abs(xDiff) >= speed) {
            oldxPos = xPos;
            xPos += speed * xDiff / displacement;
        }
        if (Math.abs(yDiff) >= speed) {
            oldyPos = yPos;
            yPos += speed * yDiff / displacement;
        }
        changePosition(xPos, yPos);
    }

}
