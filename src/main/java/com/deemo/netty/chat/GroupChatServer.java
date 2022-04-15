package com.deemo.netty.chat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class GroupChatServer {
	private final ServerSocketChannel channel;
	private final Selector selector;

	public GroupChatServer() throws IOException {
		channel = ServerSocketChannel.open();
		channel.socket().bind(new InetSocketAddress(8888));
		selector = Selector.open();
		channel.register(selector, SelectionKey.OP_ACCEPT);
	}

	public void listen() {

	}

	private void read() {

	}

	private void forward() {

	}

}
