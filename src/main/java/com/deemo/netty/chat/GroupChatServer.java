package com.deemo.netty.chat;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@Slf4j
public class GroupChatServer {
	private final Selector selector;
	private final ServerSocketChannel channel;

	public GroupChatServer() {
		try {
			// 获取 Selector
			selector = Selector.open();
			// 获取 Channel
			channel = ServerSocketChannel.open();
			// 绑定监听、端口
			channel.socket().bind(new InetSocketAddress(8888));
			// 设置非阻塞模式！！！
			channel.configureBlocking(false);
			// 注册到 Selector
			channel.register(selector, SelectionKey.OP_ACCEPT);
			log.info("The server started successfully and is listening on port 8888.");
		} catch (IOException e) {
			log.error("Failed to initialize server.");
			throw new RuntimeException(e);
		}
	}

	public void listen() {
		// 从 selector 接收连接
		while (!Thread.currentThread().isInterrupted()) {
			try {
				int select = this.selector.select(6000);
				if (select == 0) {
					log.debug("Waiting for connect...");
					continue;
				}

				// 迭代 key
				Iterator<SelectionKey> keyIterator = this.selector.selectedKeys().iterator();
				while (keyIterator.hasNext()) {
					SelectionKey selectionKey = keyIterator.next();
					// 连接
					if (selectionKey.isAcceptable()) {
						SocketChannel socketChannel = this.channel.accept();
						socketChannel.configureBlocking(false);
						// 为该连接注册 Channel
						socketChannel.register(this.selector, SelectionKey.OP_READ);
						log.info("Client: {} online...", socketChannel.getRemoteAddress());
					}
					// 可读
					if (selectionKey.isReadable()) {
						// 处理数据
						this.read(selectionKey);
					}

					// 防止重复处理
					keyIterator.remove();
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void read(SelectionKey key) {
		SocketAddress remoteAddress = null;
		try {
			// 因为要复用，现在不能关闭
			SocketChannel socketChannel = (SocketChannel) key.channel();
			remoteAddress = socketChannel.getRemoteAddress();
			// 创建 ByteBuffer
			ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

			// 读取、组装消息
			int read;
			StringBuilder msg = new StringBuilder();
			while ((read = socketChannel.read(byteBuffer)) > 0) {
				msg.append(new String(byteBuffer.array(), 0, read));
				byteBuffer.clear();
			}

			if (msg.length() == 0) {
				log.info("Received nothing from the client: {}.", remoteAddress);
			} else {
				String content = msg.toString();
				log.info("Received: {} from the client: {}", content, remoteAddress);
				// 转发
				this.forward(content, key);
			}
		} catch (IOException e) {
			if (remoteAddress != null) {
				log.error("Client: {} offline.", remoteAddress);
			}

			// 取消注册
			key.cancel();
		}
	}

	private void forward(String content, SelectionKey key) {
		log.debug("Server forwards the message to others...");

		SelectableChannel targetChannel;
		ByteBuffer byteBuffer;
		for (SelectionKey selectionKey : this.selector.keys()) {
			if (selectionKey == key) {
				// 无需转发给自己
				log.debug("no needed to forward it to self.");
				continue;
			}

			// 转发给其他客户端
			targetChannel = selectionKey.channel();
			if (targetChannel instanceof SocketChannel) {
				byteBuffer = ByteBuffer.wrap(content.getBytes(StandardCharsets.UTF_8));
				try {
					((SocketChannel) targetChannel).write(byteBuffer);
				} catch (IOException e) {
					log.error("Failed to forward the message: {} to others because: {}.", content, e.getMessage());
				}
			}
		}
	}

	public static void main(String[] args) {
		GroupChatServer groupChatServer = new GroupChatServer();
		groupChatServer.listen();
	}

}
