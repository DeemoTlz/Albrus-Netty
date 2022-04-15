package com.deemo.netty.hello;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * 网络服务器端程序
 */
public class NIOServer {

	public static void main(String[] args) throws Exception {
		// 1. 得到一个ServerSocketChannel对象
		try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
			// 2. 得到一个Selector对象
			Selector selector = Selector.open();
			// 3. 绑定一个端口号, 在服务器的6666监听
			// serverSocketChannel.bind(new InetSocketAddress(6666));
			serverSocketChannel.socket().bind(new InetSocketAddress(6666));
			// 4. 设置非阻塞方式
			serverSocketChannel.configureBlocking(false);
			// 5. 把ServerSocketChannel对象注册给Selector对象
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			// 6. 干活
			while (!Thread.currentThread().isInterrupted()) {
				// 6.1 监控客户端
				// 如果使用 selector.select() 就会阻塞在这里的
				if (selector.select(1000) == 0) {
					// nio非阻塞式的优势
					System.out.println("Server: 等待了1秒，无客户端连接...");
					continue;
				}
				// 6.2 得到SelectionKey,判断通道里的事件
				Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
				while (keyIterator.hasNext()) {
					SelectionKey key = keyIterator.next();
					if (key.isAcceptable()) {
						// 客户端连接请求事件
						SocketChannel socketChannel = serverSocketChannel.accept();
						socketChannel.configureBlocking(false);
						// 将客户端的 Channel 注册到 selector
						socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
					}
					if (key.isReadable()) {
						// 读取客户端数据事件，这个时候获取到的 Channel 就是刚刚注册的客户端 Channel
						try (SocketChannel channel = (SocketChannel) key.channel()) {
							ByteBuffer buffer = (ByteBuffer) key.attachment();
							int read = channel.read(buffer);
							System.out.println("接收到客户端数据：" + new String(buffer.array(), 0, read));
						}
					}
					// 6.3 手动从集合中移除当前key,防止重复处理
					keyIterator.remove();
				}
			}
		}
	}

}
