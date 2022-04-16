package com.deemo.netty.zerocopy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ZeroCopyServer {

    public static void main(String[] args) {
        new ZeroCopyServer().copy();
    }

    private void copy() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.socket().bind(new InetSocketAddress(8888));

            while (!Thread.currentThread().isInterrupted()) {
                SocketChannel socketChannel = serverSocketChannel.accept();
                int read;
                long total = 0;
                ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
                while ((read = socketChannel.read((ByteBuffer) byteBuffer.clear())) != -1) {
                    total += read;
                    System.out.println("Read " + read + " bytes from socket...");
                }
                System.out.println(total);
                socketChannel.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
