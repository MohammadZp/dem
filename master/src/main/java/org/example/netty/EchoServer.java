package org.example.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class EchoServer {

    public static void main(String[] args) throws Exception {

        NioEventLoopGroup bossGroup =
                new NioEventLoopGroup(1);

        NioEventLoopGroup workerGroup =
                new NioEventLoopGroup();

        try {

            ServerBootstrap bootstrap = new ServerBootstrap();

            bootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) {

                            ChannelPipeline pipeline =
                                    ch.pipeline();

                            pipeline.addLast(
                                    new LineBasedFrameDecoder(1024)
                            );

                            pipeline.addLast(
                                    new StringDecoder()
                            );

                            pipeline.addLast(
                                    new StringEncoder()
                            );

                            pipeline.addLast(
                                    new EchoHandler()
                            );
                        }
                    });

            Channel serverChannel =
                    bootstrap
                            .bind(8080)
                            .sync()
                            .channel();

            System.out.println(
                    "Server started on port 8080"
            );

            serverChannel
                    .closeFuture()
                    .sync();

        } finally {

            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}