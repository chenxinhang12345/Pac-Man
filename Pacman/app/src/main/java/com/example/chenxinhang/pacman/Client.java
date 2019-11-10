package com.example.chenxinhang.pacman;

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

public class Client  {
    DatagramSocket ds;
    InetAddress ip;
    byte buf[];
    DatagramPacket DPreceive;
    byte receiveBuf[];
    DataOutputStream outToServer;
    Socket socket;
    BufferedReader inFromServer;
    String receivedBytes = "None";
    public static final String SERVER_IP = "10.0.2.2";
    public static final int TCP_SERVER_PORT = 4321;
    public static final int UDP_SERVER_PORT = 1234;
    public Client() throws IOException{
        this.ds = new DatagramSocket();
        this.ip = InetAddress.getByName(SERVER_IP);
        this.buf = null;
        this.receiveBuf = new byte[1024];
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    socket = new Socket(ip, TCP_SERVER_PORT);
                    outToServer = new DataOutputStream(socket.getOutputStream());
                    inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String Bytes;
                        while ((Bytes = inFromServer.readLine()) != null) {
                            receivedBytes = Bytes;
                            System.out.println(receivedBytes);
                        }

                    //Your code goes here
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();



    }
    public void send (int xPos, int yPos) throws IOException{
        buf = (xPos+"\n"+yPos).getBytes();
        DatagramPacket DPsend = new DatagramPacket(buf, buf.length,ip,UDP_SERVER_PORT);
        ds.send(DPsend);
    }
    public String receive() throws IOException{
        this.DPreceive = new DatagramPacket(receiveBuf, receiveBuf.length,ip,UDP_SERVER_PORT);
        ds.receive(DPreceive);
        String data = new String( DPreceive.getData(), 0,
                DPreceive.getLength());
        return data;
    }
    public String receiveInitialization(){
            while(receivedBytes.equals(" ")) {
                System.out.printf("waiting");
            }
        return receivedBytes;
    }
//    public static void main(String args[]) throws IOException {
//        Scanner sc = new Scanner(System.in);
//
//        // Step 1:Create the socket object for
//        // carrying the data.
//        DatagramSocket ds = new DatagramSocket();
//
//        InetAddress ip = InetAddress.getLocalHost();
//        byte buf[] = null;
//
//        // loop while user not enters "bye"
//        while (true) {
//            String inp = sc.nextLine();
//
//            // convert the String input into the byte array.
//            buf = inp.getBytes();
//
//            // Step 2 : Create the datagramPacket for sending
//            // the data.
//            DatagramPacket DpSend =
//                    new DatagramPacket(buf, buf.length, ip, 1234);
//
//            // Step 3 : invoke the send call to actually send
//            // the data.
//            ds.send(DpSend);
//
//            // break the loop if user enters "bye"
//            if (inp.equals("bye"))
//                break;
//        }
//    }
}
