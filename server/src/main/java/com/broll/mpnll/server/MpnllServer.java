package com.broll.mpnll.server;

import com.broll.mpnll.message.MessageRegistrySetup;
import com.broll.mpnll.message.MessageRegistryImpl;
import com.broll.mpnll.server.inbound.SetupContext;
import com.broll.mpnll.server.inbound.TcpServerSetup;
import com.broll.mpnll.server.inbound.WebsocketServerSetup;

import java.util.function.Consumer;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;

public class MpnllServer {

    private final MessageRegistryImpl messageRegistry = new MessageRegistryImpl();
    private Channel tcpChannel, wsChannel;
    private EventLoopGroup bossGroup , workerGroup;

    public void registerMessages(Consumer<MessageRegistrySetup> registry){
        registry.accept(messageRegistry::register);
    }

    public void open(int tcpPort, int websocketPort) throws InterruptedException {
        if(bossGroup!= null && !bossGroup.isTerminated()){
            return;
        }
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        SetupContext context = new SetupContext(
            bossGroup,
            workerGroup,
            null,
            messageRegistry,
            null
        );
        tcpChannel = TcpServerSetup.init(context, tcpPort);
        wsChannel = WebsocketServerSetup.init(context, websocketPort);
    }

    public void close()  {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        tcpChannel.close().syncUninterruptibly();
        wsChannel.close().syncUninterruptibly();
    }
}
