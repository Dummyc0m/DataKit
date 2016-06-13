package com.dummyc0m.bukkit.datakit.client;

import com.dummyc0m.bungeecord.datacord.server.DataPacket;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * Created by Dummyc0m on 3/9/16.
 */
public class DataClient {
    private String host;
    private int port;
    private Channel channel;
    private EventLoopGroup workerGroup = new NioEventLoopGroup();

    public DataClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void start() {
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder());
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            channel = f.channel();
            System.out.println("[DEBUG] Client Started");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void close() {
        channel.close();
        workerGroup.shutdownGracefully();
    }

    public synchronized void write(DataPacket packet) {
        System.out.println("[DEBUG] Writing Packet for " + packet.getUniqueId());
        channel.writeAndFlush(packet);
    }
}
