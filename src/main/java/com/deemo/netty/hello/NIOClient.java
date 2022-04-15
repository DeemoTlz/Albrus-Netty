package com.deemo.netty.hello;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * 网络客户端程序
 */
public class NIOClient {

	public static void main(String[] args) throws Exception {
		// 1. 得到一个网络通道
		try (SocketChannel channel = SocketChannel.open()) {
			// 2. 设置非阻塞方式
			channel.configureBlocking(false);
			// 3. 提供服务器端的IP地址和端口号
			InetSocketAddress address = new InetSocketAddress("127.0.0.1", 6666);
			// 4. 连接服务器端
			if (!channel.connect(address)) {
				while (!channel.finishConnect()) {
					// nio非阻塞式
					System.out.println("客户端: 因为连接需要时间，客户端不会阻塞，可以做个计算工作...");
				}
			}
			// 连接成功了..
			// 5. 得到一个缓冲区并存入数据
			ByteBuffer writeBuf = ByteBuffer.wrap("hello, Deemo".getBytes(StandardCharsets.UTF_8));
			// 6. 发送数据
			channel.write(writeBuf);
		}

		System.in.read();
	}

}
