package com.example.chenxinhang.pacman;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {
    DatagramSocket ds;
    InetAddress ip;
    byte buf[];
    public Client() throws IOException{
        this.ds = new DatagramSocket();
        this.ip = InetAddress.getByName("10.0.2.2");
        this.buf = null;
    }

    public void send (int xPos, int yPos) throws IOException{
        buf = (xPos+"\n"+yPos).getBytes();
        DatagramPacket Dpsend = new DatagramPacket(buf, buf.length,ip,1234);
        ds.send(Dpsend);
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
