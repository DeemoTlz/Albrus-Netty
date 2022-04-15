package com.deemo.netty.hello;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class BIOServer {

	public static void main(String[] args) throws IOException {
		// 线程池思路，当一个连接过来时，便提交一个任务到线程池
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
		try (ServerSocket serverSocket = new ServerSocket(6666);) {
			log.info("succeed to create server...");
			while (!Thread.currentThread().isInterrupted()) {
				log.info("The thread: {} is waiting for connect...", Thread.currentThread().getName());
				Socket accept = serverSocket.accept();

				cachedThreadPool.execute(() -> handler(accept));
			}
		}
	}

	private static void handler(Socket socket) {
		String name = Thread.currentThread().getName();
		log.info("The thread: {} received a connection...", name);
		try (InputStream inputStream = socket.getInputStream();) {
			byte[] bytes = new byte[1024];
			int read;
			StringBuilder content = new StringBuilder();
			while ((read = inputStream.read(bytes)) != -1) {
				log.info("The thread: {} read: {} bytes from socket...", name, read);
				content.append(new String(bytes, 0, read));
			}
			log.info("The thread: {} read end from socket.", name);
			log.info("The thread: {} reads from socket is: {}.", name, content.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
