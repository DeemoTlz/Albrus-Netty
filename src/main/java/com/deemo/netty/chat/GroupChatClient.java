package com.deemo.netty.chat;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.UUID;

@Slf4j
public class GroupChatClient {
    private static final String HOST = "localhost";
    private static final Integer PORT = 8888;
    private final Selector selector;
    private final SocketChannel channel;
    private final String username;

    public GroupChatClient(String username) {
        try {
            // 获取 Selector
            this.selector = Selector.open();
            // 获取 Client Channel
            this.channel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            // 设置非阻塞模式！！！
            this.channel.configureBlocking(false);
            // 注册到 Selector！！！
            this.channel.register(this.selector, SelectionKey.OP_READ);
            this.username = username;
            log.info("The client: {} started successfully.", username);
        } catch (IOException e) {
            log.error("Failed to initialize client.");
            throw new RuntimeException(e);
        }
    }

    private void send(String msg) {
        try {
            this.channel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
            log.info("Client: {} says: {} to others.", this.username, msg);
        } catch (IOException e) {
            log.error("Client: {} fails when it says: {} to others caused by: {}.", this.username, msg, e.getMessage());
            e.printStackTrace();
        }
    }

    private void read() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                // 从 Selector 获取可用 Channel
                int select = this.selector.select(8000);
                if (select == 0) {
                    log.debug("Waiting for message...");
                    continue;
                }

                // 有可用的 Channel，迭代
                Iterator<SelectionKey> keyIterator = this.selector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey selectionKey = keyIterator.next();
                    // 可读的 Channel
                    if (selectionKey.isReadable()) {
                        // 因为要复用，现在不能关闭
                        // try (SocketChannel socketChannel = (SocketChannel) selectionKey.channel()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        if (socketChannel == this.channel) {
                            keyIterator.remove();
                            continue;
                        }
                        // 创建 ByteBuffer
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                        // 读取消息
                        int read;
                        StringBuilder msg = new StringBuilder();
                        while ((read = socketChannel.read(byteBuffer)) > 0) {
                            msg.append(new String(byteBuffer.array(), 0, read));
                        }

                        if (msg.length() == 0) {
                            log.info("{} read nothing from: {}.", this.username, socketChannel.getRemoteAddress());
                        } else {
                            log.info("{} read: {} from: {}", this.username, msg, socketChannel.getRemoteAddress());
                        }
                        // }
                    }

                    // 防止重复处理
                    keyIterator.remove();
                }
            } catch (IOException e) {
                log.error("An exception occurred while reading the message caused by: {}.", e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        String username = UUID.randomUUID().toString().substring(0, 5);
        GroupChatClient groupChatClient = new GroupChatClient(username);

        Thread readThread = new Thread(groupChatClient::read);
        readThread.setName("Thread-" + username);
        readThread.start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            groupChatClient.send(scanner.nextLine());
        }
    }
}
