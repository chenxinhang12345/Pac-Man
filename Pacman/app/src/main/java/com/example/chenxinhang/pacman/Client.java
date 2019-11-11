package com.example.chenxinhang.pacman;

import android.graphics.Point;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class Client {
    DatagramSocket ds;
    InetAddress ip;
    byte buf[];
    DatagramPacket DPreceive;
    byte receiveBuf[];
    DataOutputStream outToServer;
    Socket socket;
    BufferedReader inFromServer;
    String receivedBytes = "None";
    String newUser = "None";
    String SERVER_IP;
    public static final int TCP_SERVER_PORT = 4321;
    public static final int UDP_SERVER_PORT = 1234;

    public Client(String SERVER_IP) throws IOException {
        this.ds = new DatagramSocket();
        this.SERVER_IP = SERVER_IP;
        this.ip = InetAddress.getByName(SERVER_IP);
        this.buf = null;
        this.receiveBuf = new byte[1024];
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
                            System.out.println(Bytes);
                            String[] stringlist = Bytes.split(";", 2);
                            if (stringlist[0].equals("USERINFO")) {
                                receivedBytes = stringlist[1];
                            } else if (stringlist[0].equals("NEWUSER")) {
                                newUser = stringlist[1];
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
        this.DPreceive = new DatagramPacket(receiveBuf, receiveBuf.length, ip, UDP_SERVER_PORT);

        ds.receive(DPreceive);
        System.out.println("receive");
        String data = new String(DPreceive.getData(), 0, DPreceive.getLength());
        System.out.println(data);
        return data;
    }

    public String receiveInitialization() {
        while (receivedBytes.equals(" ")) {
            System.out.printf("waiting");
        }
        return receivedBytes;
    }

    public static void main(String args[]) throws Exception {
//        Socket socket = new Socket("localhost", TCP_SERVER_PORT);
//
//        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        String Bytes;
//        while ((Bytes = inFromServer.readLine()) != null) {
//            if(Bytes.length()>2) {
//                System.out.println(Bytes);
//            }
        Client playerClient = new Client("localhost");

        while(playerClient.receivedBytes.equals("None")){

        }
        //initialization finished
        String Bytes = playerClient.receivedBytes;
        System.out.println(Bytes);
        String [] list = Bytes.split(":|,|;",3);
        System.out.println(list.length);
        System.out.println("ID: " +list[1]);
        int ID = Integer.valueOf(list[1]);
        while(true) {
            playerClient.send(5, 5, ID);
            playerClient.receive();
            System.out.println("send");
        }
    }
}

