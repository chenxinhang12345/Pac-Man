package com.example.chenxinhang.pacman;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import java.io.IOException;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;
    private Player player1;
    private Player player2;
    private Point player1Point;
    private Point player2Point;
    private Client playerClient;

    public GamePanel(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);


        player1 = new Player( Color.RED , 50,50,50,50, 10);
        player2 = new Player(Color.BLUE,200,200,50,50,20);
        player1Point = new Point(150,150);
        player2Point = new Point(200,200);
        try {
            playerClient = new Client();
            while(playerClient.receivedBytes.equals("None")){
                System.out.println("waiting");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new MainThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (true) {
            try {
                thread.setRunning(false);
                thread.join();
            } catch (Exception e) {
                e.printStackTrace();
                retry = false;
            }
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                player1Point.set((int)event.getX(),(int)event.getY());
                player2Point.set((int)event.getX(),(int)event.getY());
//                System.out.println((int)event.getX());
        }
        return true;

    }

    public void update(){
        player1.update(player1Point);
        player2.update(player2Point);
//        try {
//            playerClient.send(player1.getxPos(), player1.getyPos());
//            String data = playerClient.receive();
//            System.out.println("receive:"+data);
////            System.out.println("1");
//        }catch (Exception e){
//            e.printStackTrace();
//        }
    }
    @Override
    public void draw(Canvas canvas){
        super.draw(canvas);
        canvas.drawColor(Color.YELLOW);
        player1.draw(canvas);
        player2.draw(canvas);
    }
}
