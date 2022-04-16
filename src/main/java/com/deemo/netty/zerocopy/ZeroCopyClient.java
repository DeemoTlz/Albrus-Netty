package com.deemo.netty.zerocopy;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.time.Instant;

public class ZeroCopyClient {
    private static final String FILE = "E:\\Resources\\ISO\\cn_office_professional_plus_2019_x86_x64_dvd_5e5be643.iso";

    public static void main(String[] args) {
        new ZeroCopyClient().copy();
    }

    private void copy() {
        try (SocketChannel socketChannel = SocketChannel.open();
             FileInputStream inputStream = new FileInputStream(FILE);
             FileChannel fileChannel = inputStream.getChannel()) {
            socketChannel.connect(new InetSocketAddress("localhost", 8888));

            long start = Instant.now().toEpochMilli();

            int read;
            long total = 0;

            /*ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
            while ((read = fileChannel.read((ByteBuffer) byteBuffer.clear())) != -1) {
                total += read;
                socketChannel.write(byteBuffer);
            }*/

            // fileChannel.transferTo(0, fileChannel.size(), socketChannel);
            // transferTo 在 windows 下一次只能传输 8m，需要手动分段传输，在 linux 下可以一次传输
            int per = 8388607;
            long cnt = (fileChannel.size() + per - 1) / per;
            for (long i = 0; i < cnt; i++) {
                read = (total + per) > fileChannel.size() ? (int) (fileChannel.size() - total) : per;
                total += read;
                fileChannel.transferTo(per * i, read, socketChannel);
            }

            System.out.println("It takes a total of " + (Instant.now().toEpochMilli() - start) + "ms to send " + total + " bytes");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
