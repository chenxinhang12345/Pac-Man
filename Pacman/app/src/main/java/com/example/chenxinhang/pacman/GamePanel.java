package com.example.chenxinhang.pacman;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import org.json.JSONObject;

import java.io.IOException;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;
    private Player player1;
    private Player player2;
    private Point player1Point;
    private Point player2Point;
    private Client playerClient;
    private Thread receiveThread;

    public GamePanel(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
//        player1Point = new Point(150,150);
        player2Point = new Point(200, 200);
        try {
            playerClient = new Client("10.0.2.2");
            while (playerClient.receivedBytes.equals("None")) {
                System.out.println("waiting");
            }
            parseInfo(playerClient.receivedBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        playerClient.send(player1.getxPos(), player1.getyPos(), player1.getID());
//                        String data = playerClient.receive();
//                        System.out.println(data);
//                        String pos = data.split(";")[1];
//                        JSONObject obj = new JSONObject(pos);
//                        int x = obj.getInt("X");
//                        int y = obj.getInt("Y");
//                        player2.changePosition(x, y);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        receiveThread.start();
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void parseInfo(String info) {
        try {
            JSONObject obj = new JSONObject(info);
            int X = obj.getInt("X");
            int Y = obj.getInt("Y");
            int ID = obj.getInt("ID");
            int color = obj.getInt("Color");
            player1 = new Player(color, X, Y, 50, 50, 10, ID);
            player1Point = new Point(X, Y);
            while (playerClient.newUser.equals("None")) {
                System.out.println("wait another player...");
            }
            JSONObject objUser = new JSONObject(playerClient.newUser);
            int X2 = objUser.getInt("X");
            int Y2 = objUser.getInt("Y");
            int ID2 = objUser.getInt("ID");
            int color2 = objUser.getInt("Color");
            player2 = new Player(color2, X2, Y2, 50, 50, 10, ID2);
            player2Point = new Point(X2, Y2);
            System.out.println(X);

        } catch (Exception e) {
            e.printStackTrace();
        }

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
                receiveThread.join();
            } catch (Exception e) {
                e.printStackTrace();
                retry = false;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                int XPos = (int) event.getX();
                int YPos = (int) event.getY();
                player1Point.set(XPos, YPos);
//                try {
//                    playerClient.send(XPos, YPos, player1.getID());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                System.out.println((int)event.getX());
        }
        return true;

    }

    public void update() {
        player1.update(player1Point);


    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.YELLOW);
        player1.draw(canvas);
        player2.draw(canvas);
    }
}
