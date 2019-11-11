package com.example.chenxinhang.pacman;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class MainThread extends Thread {
    public static final int MAX_FPS = 60;
    private GamePanel gamePanel;
    private SurfaceHolder surfaceHolder;
    private boolean running;
    public static Canvas canvas;

    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel){
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }
    @Override
    public void run(){
        long startTime;
        long timeMillis;
        long waitTime;
        long targetTime = 1000/MAX_FPS;
        while(running){
            startTime = System.nanoTime();
            canvas = null;
            try{
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder){
                    this.gamePanel.update();
                    this.gamePanel.draw(canvas);
                }
            }catch(Exception e){
                e.printStackTrace();
            }finally{
                if(canvas !=null){
                    try{
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                timeMillis = (System.nanoTime()-startTime)/1000000;
                waitTime = targetTime-timeMillis;
                try{
                    if(waitTime>0) {
                        sleep(waitTime);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void setRunning(boolean running){
        this.running = running;
    }

}
