package com.example.chenxinhang.pacman;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.SurfaceHolder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.lang.Thread.sleep;

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;// thread that control first player and rendering
    private Player player1;// first player (main player)
    public Player player2;// second player
    public HashMap<Integer, Player> players;//new added players
    private Point player1Point;// navigation point for main user
    private Client playerClient;// client for receiving data
    private Thread receiveThread;// thread for receiving all players position
    private Thread sendThread; //thread for sending players positions
    private ConcurrentHashMap<Integer, Food> food; // store the available food on map
    private CopyOnWriteArrayList<Wall> walls; // store all the walls on map

    public GamePanel(Context context) {
        super(context);
        getHolder().addCallback(this);
        food = new ConcurrentHashMap<>();
        players = new HashMap<>();
        walls = new CopyOnWriteArrayList<>();
//        walls.add(new Wall(0, 970, 1000, 1000));
//        walls.add(new Wall(1000, 970, 1030, 1970));
        thread = new MainThread(getHolder(), this);
        try {
//            playerClient = new Client("18.217.81.167",this);
//            playerClient  = new Client("10.180.157.40");
            playerClient = new Client("10.0.2.2", this);
            while (playerClient.receivedBytes.equals("None")) {
                System.out.println("waiting");
            }
            parseInfo(playerClient.receivedBytes);
            while (playerClient.food.equals("None")) {
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
                        String data = playerClient.receive();
                        JSONObject obj = new JSONObject(data);
                        int x = obj.getInt("X");
                        int y = obj.getInt("Y");
//                        System.out.println(x+", "+y+"type:"+player1.getType());
                        if (players.size() < 1) {
                            player2.changePosition(x, y);
                        } else {
                            int ID = obj.getInt("ID");
                            if (player2.getID() == ID) {
                                player2.changePosition(x, y);
                            } else {
                                if (players.containsKey(ID)) {
                                    players.get(ID).changePosition(x, y);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        sleep(20);
                        playerClient.send(player1.getxPos(), player1.getyPos(), player1.getID());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendThread.start();
        receiveThread.start();
        setFocusable(true);
    }

    /**
     * initialize all the food from server
     *
     * @param food
     */
    public void initializeFood(String food) {
        try {
            JSONArray arr = new JSONArray(food);
            for (int i = 0; i < arr.length(); i++) {
                String json = arr.getString(i);
                JSONObject obj = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                int X = obj.getInt("X");
                int Y = obj.getInt("Y");
                int ID = obj.getInt("ID");
//                String type = obj.getString("Type");
                String type = "";
                this.food.put(ID, new Food(X, Y,type));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initializeWalls(String walls){
        try{
            JSONObject obj = new JSONObject(walls);
            JSONArray rowsArr = obj.getJSONArray("Rows");
            JSONArray colsArr = obj.getJSONArray("Cols");
            for(int i = 0; i < rowsArr.length();i++){
                String json = rowsArr.getString(i);
                JSONObject objRow = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                int x1 = objRow.getInt("X0");
                int x2 = objRow.getInt("X1");
                int y = objRow.getInt("Y0");
                this.walls.add(Wall.getRowWall(x1,x2,y));
            }
            for(int i = 0; i < colsArr.length();i++){
                String json = colsArr.getString(i);
                JSONObject objRow = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                int y1 = objRow.getInt("Y0");
                int y2 = objRow.getInt("Y1");
                int x = objRow.getInt("X0");
                this.walls.add(Wall.getColWall(y1,y2,x));
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
            String type = obj.getString("Type");
            player1 = new Player(color, X, Y, 20, 20, 10, ID,type, this);
            player1Point = new Point(X, Y);
            while (playerClient.newUser.equals("None")) {
                System.out.println("wait another player...");
            }
//            JSONObject objUser = new JSONObject(playerClient.newUser);
//            int X2 = objUser.getInt("X");
//            int Y2 = objUser.getInt("Y");
//            int ID2 = objUser.getInt("ID");
//            int color2 = objUser.getInt("Color");
//            player2 = new Player(color2, X2, Y2, 50, 50, 20, ID2,this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * add the second player
     *
     * @param info
     */

    public void parseInfoAddPlayer2(String info) {
        try {
            JSONObject objUser = new JSONObject(info);
            int X2 = objUser.getInt("X");
            int Y2 = objUser.getInt("Y");
            int ID2 = objUser.getInt("ID");
            int color2 = objUser.getInt("Color");
            String type = objUser.getString("Type");
            player2 = new Player(color2, X2, Y2, 20, 20, 10, ID2, type,this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * add player when the total number
     *
     * @param info
     */

    public void parseInfoAddNewUser(String info) {
        try {
            JSONObject objUser = new JSONObject(info);
            int X = objUser.getInt("X");
            int Y = objUser.getInt("Y");
            int ID = objUser.getInt("ID");
            int color = objUser.getInt("Color");
            String type = objUser.getString("Type");
            Player player = new Player(color, X, Y, 20, 20, 10, ID,type, this);
            players.put(player.getID(), player);
        } catch (Exception e) {

        }
    }

    /**
     * update player's score
     *
     * @param info
     */

    public void parseInfoUpdateScore(String info) {
        try {
            JSONArray arr = new JSONArray(info);
            for (int i = 0; i < arr.length(); i++) {
                String json = arr.getString(i);
                JSONObject obj = new JSONObject(json.substring(json.indexOf("{"), json.lastIndexOf("}") + 1));
                int id = obj.getInt("ID");
                int score = obj.getInt("Score");
                if (id == player1.getID()) {
                    player1.setScore(score);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void parseInfoAddNewFood(String info){
        try{
            JSONObject obj = new JSONObject(info);
            int ID = obj.getInt("ID");
            int X = obj.getInt("X");
            int Y = obj.getInt("Y");
//            String type = obj.getString("Type");
            String type = "";
            food.put(ID,new Food(X,Y,type));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void parseInfoRespawn(String info){
        try{
            JSONObject obj = new JSONObject(info);
            int X = obj.getInt("X");
            int Y = obj.getInt("Y");
//            String type = obj.getString("Type");
            player1.changePosition(X,Y);
        }catch (Exception e){
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

    /**
     * let the Pacman move toward the target every time
     */
    public void update() {
        player1.update(player1Point);


    }

    /**
     * decide whether or not a entry in food map is eat by a player send updated info to server if sendEatInfo is true
     *
     * @param player
     * @param e
     * @param sendEatInfo
     * @return
     */
    private boolean isEat(Player player, Map.Entry<Integer, Food> e, boolean sendEatInfo) {
        if (e.getValue().getRectangle().intersect(player.getRectangle())) {
            if (sendEatInfo) {
                try {
                    playerClient.sendEatData(player.getID(), e.getKey());
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean isEatPlayer(Player player1, Player player2){
        if (player1.getLogicRectangle().intersect(player2.getLogicRectangle())){
            System.out.println("send respawn data");
            try {
                playerClient.sendAttackData(player1.getID(),player2.getID());
            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }else{
            return false;
        }
    }

    /**
     * deal with collision
     */
    public void obstacle() {
        food.entrySet().removeIf(e -> (isEat(player1, e, true)));
        food.entrySet().removeIf(e -> (isEat(player2, e, false)));
//        if(player1.getType().equals("GHOST")) {
//            isEatPlayer(player1, player2);
//            if(players.size()>0) {
//                for (Player mulPlayer : players.values()) {
//                    isEatPlayer(player1, mulPlayer);
//                }
//            }
//        }
        if (players.size() > 0) {
            for (Player mulPlayer : players.values()) {
                food.entrySet().removeIf(e -> (isEat(mulPlayer, e, false)));
            }
        }
        for (Wall wall : walls) {
            if (wall.getLogicRectangle().intersect(player1.getRectangle())) {
                player1.undoMove();
                return;
            }
        }
    }

    /**
     * draw all the walls in the walls list
     * @param canvas
     */
    public void drawWalls(Canvas canvas) {
        for (Wall wall : walls) {
            wall.draw(canvas);
        }
    }

    /**
     * draw all the objects
     * @param canvas
     */
    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(50);
        canvas.drawText("score: " + player1.getScore(), 1100, 2000, paint);
//        canvas.scale(5,5,player1.getxPos(),player1.getyPos());
        canvas.drawColor(Color.rgb(207, 255, 130));
        obstacle();
        player1.draw(canvas);//draw player1
        player2.draw(canvas);//draw player2
//        System.out.println(player2.getxPos()+", "+player2.getyPos()+", "+player2.getType());
        if (players.size() > 0) {
            for (Player player : players.values()) {
                player.draw(canvas);
            }
        }
        for (Food food : food.values()) {
//            System.out.println(food.getRectangle().bottom);
            food.draw(canvas);
        }
        drawWalls(canvas);
    }

}
