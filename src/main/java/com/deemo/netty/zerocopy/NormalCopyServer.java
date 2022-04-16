package com.deemo.netty.zerocopy;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class NormalCopyServer {

    public static void main(String[] args) {
        new NormalCopyServer().copy();
    }

    private void copy() {
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();

                // 可以读取Java基本类型 DataInputStream.readLong()
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());

                int read;
                byte[] bytes = new byte[4096];
                while ((read = inputStream.read(bytes)) != -1) {
                    System.out.println("Read " + read + " bytes from socket...");
                }
                inputStream.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
