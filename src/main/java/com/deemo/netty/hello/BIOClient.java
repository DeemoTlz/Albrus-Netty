package com.deemo.netty.hello;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
public class BIOClient {

	public static void main(String[] args) {
		try (Socket socket = new Socket("127.0.0.1", 6666);
			 OutputStream outputStream = socket.getOutputStream();) {
			outputStream.write("Hello Albrus!".getBytes(StandardCharsets.UTF_8));
			outputStream.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
