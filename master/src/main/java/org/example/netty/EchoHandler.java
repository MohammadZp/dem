package org.example.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

class EchoHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(
            ChannelHandlerContext ctx,
            Object msg) {

        String message = (String) msg;

        System.out.println(
                "Received: [" + message + "]"
        );

        ctx.writeAndFlush(
                "Echo: " + message + "\n"
        );
    }
}