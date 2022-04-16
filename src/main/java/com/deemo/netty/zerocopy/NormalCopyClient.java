package com.deemo.netty.zerocopy;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.Instant;

public class NormalCopyClient {
    private static final String FILE = "E:\\Resources\\ISO\\cn_office_professional_plus_2019_x86_x64_dvd_5e5be643.iso";

    public static void main(String[] args) {
        new NormalCopyClient().copy();
    }

    private void copy() {
        try (Socket socket = new Socket("localhost", 8888);
             FileInputStream inputStream = new FileInputStream(FILE);
             DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());) {
            int read;
            long total = 0;
            byte[] bytes = new byte[4096];

            long start = Instant.now().toEpochMilli();
            while ((read = inputStream.read(bytes)) != -1) {
                total += read;
                outputStream.write(bytes);
            }
            System.out.println("It takes a total of " + (Instant.now().toEpochMilli() - start) + "ms to send " + total + " bytes");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
