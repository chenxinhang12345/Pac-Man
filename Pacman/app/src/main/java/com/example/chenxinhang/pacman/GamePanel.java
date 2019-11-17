package com.example.chenxinhang.pacman;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static java.lang.Thread.sleep;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;// thread that control first player and rendering
    private Player player1;// first player (main player)
    public Player player2;// second player
    public List<Player> players;
    private Point player1Point;// navigation point for main user
    private Client playerClient;// client for receiving data
    private Thread receiveThread;// thread for updating second player position
    private HashMap<Integer,Food> food;
    private Wall wall;

    public GamePanel(Context context) {
        super(context);
        getHolder().addCallback(this);
        food = new HashMap<>();
        wall = new Wall();
        thread = new MainThread(getHolder(), this);
        try {
//            playerClient = new Client("18.217.81.167");
//            playerClient  = new Client("10.180.157.40");
            playerClient = new Client("10.0.2.2");
            while (playerClient.receivedBytes.equals("None")) {
                System.out.println("waiting");
            }
            parseInfo(playerClient.receivedBytes);
            while(playerClient.food.equals("None")){
                System.out.println("waiting food");
            }
            initializeFood(playerClient.food);
            System.out.println(playerClient.food);
        } catch (Exception e) {
            e.printStackTrace();
        }
        receiveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(50);
                        playerClient.send(player1.getxPos(), player1.getyPos(), player1.getID());
                        String data = playerClient.receive();
                        JSONObject obj = new JSONObject(data);
                        int x = obj.getInt("X");
                        int y = obj.getInt("Y");
                        player2.changePosition(x, y);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        receiveThread.start();
        setFocusable(true);
    }


    public void initializeFood(String food){
        try {
            JSONArray arr = new JSONArray(food);
            for (int i = 0; i< arr.length();i++){
                String json = arr.getString(i);
                JSONObject obj = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                int X = obj.getInt("X");
                int Y = obj.getInt("Y");
                int ID = obj.getInt("ID");
                this.food.put(ID,new Food(X,Y));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    /**
     * parse information from initialization in json format
     *
     * @param info
     */
    public void parseInfo(String info) {
        try {
            JSONObject obj = new JSONObject(info);
            int X = obj.getInt("X");
            int Y = obj.getInt("Y");
            int ID = obj.getInt("ID");
            int color = obj.getInt("Color");
            player1 = new Player(color, X, Y, 50, 50, 20, ID,this);
            player1Point = new Point(X, Y);
            while (playerClient.newUser.equals("None")) {
                System.out.println("wait another player...");
            }
            JSONObject objUser = new JSONObject(playerClient.newUser);
            int X2 = objUser.getInt("X");
            int Y2 = objUser.getInt("Y");
            int ID2 = objUser.getInt("ID");
            int color2 = objUser.getInt("Color");
            player2 = new Player(color2, X2, Y2, 50, 50, 20, ID2,this);
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
        while (true) {
            try {
                thread.setRunning(false);
                thread.join();
                receiveThread.join();
            } catch (Exception e) {
                e.printStackTrace();
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
                player1Point.set(XPos, YPos);// whenever user touch a point, the pacman will follow that point
        }
        return true;

    }

    public void update() {
        player1.update(player1Point);


    }

    public void obstacle(){
        food.entrySet().removeIf(e->(e.getValue().getRectangle().intersect(player1.getRectangle())));
        food.entrySet().removeIf(e->(e.getValue().getRectangle().intersect(player2.getRectangle())));
        if(wall.getLogicRectangle().intersect(player1.getRectangle())){
            player1.undoMove();
            System.out.println("1");
        }

    }
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.YELLOW);
        obstacle();
        player1.draw(canvas);//draw player1
        player2.draw(canvas);//draw player2
        for (Food food : food.values()){
//            System.out.println(food.getRectangle().bottom);
            food.draw(canvas);
        }
        wall.draw(canvas);
    }
}
