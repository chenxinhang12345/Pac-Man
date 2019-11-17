package com.example.chenxinhang.pacman;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

public class Client {
    DatagramSocket ds;
    InetAddress ip;
    byte buf[];
    byte[] receiveBuf;
    DataOutputStream outToServer;
    Socket socket;
    BufferedReader inFromServer;
    String receivedBytes = "None";
    String newUser = "None";
    String food = "None";
    String SERVER_IP;
    boolean twoPlayerJoined;
    GamePanel gamePanel;
    public static final int TCP_SERVER_PORT = 4321;
    public static final int UDP_SERVER_PORT = 1234;

    public Client(String SERVER_IP, GamePanel gamePanel) throws IOException {
        this.ds = new DatagramSocket();
        this.SERVER_IP = SERVER_IP;
        this.ip = InetAddress.getByName(SERVER_IP);
        this.buf = null;
        this.receiveBuf = new byte[256];
        this.twoPlayerJoined= false;
        this.gamePanel = gamePanel;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, TCP_SERVER_PORT);
                    socket.setKeepAlive(true);
                    outToServer = new DataOutputStream(socket.getOutputStream());
                    inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String Bytes;
                    while (true) {
                        if ((Bytes = inFromServer.readLine()) != null) {
                            String[] stringlist = Bytes.split(";", 2);
                            if (stringlist[0].equals("USERINFO")){
                                receivedBytes = stringlist[1];
                            } else if (stringlist[0].equals("NEWUSER")){
                                newUser = stringlist[1];
                                if(twoPlayerJoined){
                                    gamePanel.parseInfoAddNewUser(newUser);
                                }else{
                                    gamePanel.parseInfoAddPlayer2(newUser);
                                }
                                twoPlayerJoined = true;
                            }else if (stringlist[0].equals("FOOD")){
                                food = stringlist[1];
                            }
                        }
                    }

                    //Your code goes here
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();


    }

    public void send(int xPos, int yPos, int ID) throws IOException {
        buf = ("POS;{\"ID\":" + ID + ", \"X\":" + xPos + ", \"Y\": " + yPos + " }\n").getBytes();
        DatagramPacket DPsend = new DatagramPacket(buf, buf.length, ip, UDP_SERVER_PORT);
        ds.send(DPsend);
    }

    public String receive() throws IOException {
        DatagramPacket DPreceive = new DatagramPacket(receiveBuf, receiveBuf.length, ip, UDP_SERVER_PORT);
        ds.receive(DPreceive);
        String data = new String(DPreceive.getData(), 0, DPreceive.getLength());
        return data;
    }

    public static void main(String args[]) throws Exception {
//        Client playerClient = new Client("localhost");
//
//        while(playerClient.receivedBytes.equals("None")){
//
//        }
//        //initialization finished
//        String Bytes = playerClient.receivedBytes;
//        String [] list = Bytes.split(":|,|;",3);
//        int ID = Integer.valueOf(list[1]);
//        while(true) {
//            playerClient.send(5, 5, ID);
//            playerClient.receive();
//        }
    }
}

