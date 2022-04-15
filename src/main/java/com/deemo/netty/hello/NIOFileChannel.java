package com.deemo.netty.hello;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

@Slf4j
public class NIOFileChannel {
	private static final String FILE = "D://Albrus-Netty.txt";
	private static final String COPY_FILE = "D://Albrus-Netty-copy.txt";

	public static void main(String[] args) {
		NIOFileChannel nioFileChannel = new NIOFileChannel();
		nioFileChannel.write();
		nioFileChannel.read();
		nioFileChannel.in2Out();
		nioFileChannel.transfer();
	}

	private void write() {
		// 获取文件的输出流，再获取 NIO Channel
		try (FileOutputStream outputStream = new FileOutputStream(FILE);
			 FileChannel channel = outputStream.getChannel();) {
			// 获取字节内容
			byte[] bytes = "Hello, Albrus!".getBytes(StandardCharsets.UTF_8);
			// 创建缓冲区
			ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
			// 存入字节
			byteBuffer.put(bytes);

			// 对 ByteBuffer 进行 切换
			byteBuffer.flip();

			// 写入 FileChannel
			channel.write(byteBuffer);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void read() {
		// 获取文件的输入流，再获取 NIO Channel
		try (FileInputStream inputStream = new FileInputStream(FILE);
			 FileChannel channel = inputStream.getChannel();) {
			// 创建缓冲区
			ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

			// 从 FileChannel 中读取
			int read = channel.read(byteBuffer);
			System.out.println("read: " + read);
			System.out.println("channel.size: " + channel.size());
			System.out.println("channel.position: " + channel.position());

			System.out.println(new String(byteBuffer.array(), 0, read));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void in2Out() {
		// 获取文件的输入流、输出流，再获取 NIO Channel
		try (FileInputStream inputStream = new FileInputStream(FILE);
			 FileChannel inputStreamChannel = inputStream.getChannel();
			 FileOutputStream outputStream = new FileOutputStream(COPY_FILE);
			 FileChannel outputStreamChannel = outputStream.getChannel();) {

			// 创建缓冲区
			ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

			int read;
			while ((read = inputStreamChannel.read(byteBuffer)) != -1) {
				log.info("InputStreamChannel to outputStreamChannel read: {} bytes.", read);
				// 记得转换！！！
				byteBuffer.flip();
				// 写入
				outputStreamChannel.write(byteBuffer);
				// 记得清空！！！
				byteBuffer.clear();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void transfer() {
		// 获取文件的输入流、输出流，再获取 NIO Channel
		try (FileInputStream inputStream = new FileInputStream(FILE);
			 FileChannel inputStreamChannel = inputStream.getChannel();
			 FileOutputStream outputStream = new FileOutputStream(COPY_FILE);
			 FileChannel outputStreamChannel = outputStream.getChannel();) {

			// 使用 transferFrom 完成拷贝
			outputStreamChannel.transferFrom(inputStreamChannel, 0, inputStreamChannel.size());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
