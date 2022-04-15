package com.deemo.netty.hello;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class NIOByteBuffer {
	private static final String FILE = "D://Albrus-Netty.txt";

	public static void main(String[] args) {
		NIOByteBuffer nioByteBuffer = new NIOByteBuffer();
		nioByteBuffer.putAndGet();
		nioByteBuffer.readonly();
		nioByteBuffer.mappedByteBuffer();
		nioByteBuffer.scatteringAndGathering();
	}

	private void putAndGet() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

		// 存放 和 读取 必须对应！！！
		byteBuffer.putInt(1);
		byteBuffer.putFloat(1.1F);
		byteBuffer.putLong(111L);
		byteBuffer.putChar('A');

		// 转换
		byteBuffer.flip();

		// 存放 和 读取 必须对应！！！
		// System.out.println(byteBuffer.getChar());
		System.out.println(byteBuffer.getInt());
		System.out.println(byteBuffer.getFloat());
		System.out.println(byteBuffer.getLong());
		System.out.println(byteBuffer.getChar());
	}

	private void readonly() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

		for (int i = 0; i < 128; i++) {
			byteBuffer.putInt(i);
		}

		// 转换
		byteBuffer.flip();

		ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();
		while (readOnlyBuffer.hasRemaining()) {
			System.out.println(readOnlyBuffer.getInt());
		}

		// java.nio.ReadOnlyBufferException
		// readOnlyBuffer.putChar('S');
	}

	private void mappedByteBuffer() {
		// 获取对应 Channel
		try (RandomAccessFile randomAccessFile = new RandomAccessFile(FILE, "rw");
			 FileChannel channel = randomAccessFile.getChannel();) {

			// 映射直接内存
			MappedByteBuffer mappedByteBuffer = channel.map(FileChannel.MapMode.READ_WRITE, 7, 6);

			for (int i = 0; i < 6; i++) {
				System.out.println("Before: " + (char) mappedByteBuffer.get(i));
				mappedByteBuffer.put(i, (byte) ('A' + i));
				System.out.println("After: " + (char) mappedByteBuffer.get(i));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Scattering：将数据写入到 buffer 时，可以采用 buffer 数组，依次写入 [分散]
	 * Gathering：从 buffer 读取数据时，可以采用 buffer 数组，依次读
	 */
	private void scatteringAndGathering() {
		// Buffer 支持 Buffer （数组）完成读写操作
		ByteBuffer[] byteBuffers = new ByteBuffer[6];
		// 1 + 2 + 3 + ... + 6 = 21
		for (int i = 0; i < byteBuffers.length; i++) {
			byteBuffers[i] = ByteBuffer.allocate(i + 1);
		}

		// 获取文件的输入流，再获取 NIO Channel
		try (FileInputStream inputStream = new FileInputStream(FILE);
			 FileChannel channel = inputStream.getChannel();) {

			long read;
			while ((read = channel.read(byteBuffers)) != -1) {
				System.out.println("read bytes: " + read);
			}

			// 也可以继续写出到网络或磁盘，但记得 flip() ！！！
			Arrays.stream(byteBuffers).map(byteBuffer -> "position: " + byteBuffer.position() + ", limit: " + byteBuffer.limit()).forEach(System.out::println);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
