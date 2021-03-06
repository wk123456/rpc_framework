package com.acme.rpc.demo.framework.protocol.netty;

import com.acme.rpc.demo.framework.serialize.handler.BaseCodec;
import com.acme.rpc.demo.framework.serialize.tranobject.RpcRequest;
import com.acme.rpc.demo.framework.URL;
import com.acme.rpc.demo.register.zk.RegisterForServer;
import com.acme.rpc.demo.register.zk.heartbeat.BeatDataSender;
import com.acme.rpc.demo.utils.RpcThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.util.Map;
import java.util.concurrent.*;


/**
 * @author K.
 * @Date 2021/1/15 22:41
 */
public class NettyServer {
    BeatDataSender beatDataSender;
    private ExecutorService serverTask=new ThreadPoolExecutor(
            10,10,600,TimeUnit.SECONDS,new LinkedBlockingQueue<>(),
            new RpcThreadFactoryBuilder().setNamePrefix("serverTask").build()
    );

    private ExecutorService beatDataTask = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1));

    public void start(String servicName, URL url, Map<String, Object> serviceMap) throws InterruptedException {
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    //存放已完成三次握手的请求的队列的最大长度
                    .option(ChannelOption.SO_BACKLOG, 128)
                    //启用心跳保活
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //解码器
                            ch.pipeline()
                                    /* //把request转为字节
                                     .addLast(new RpcDecoder(RpcRequest.class))
                                     //把本地执行的response对象转为字节
                                     .addLast(new RpcEncoder(RpcResponse.class))*/
                                    //统一的编解码器
                                    .addLast(new LengthFieldBasedFrameDecoder(1024, 0, 4, 0, 0))
                                    .addLast(new BaseCodec(RpcRequest.class))
                                    //把本地执行的response对象转为字节
                                    .addLast(new NettyServerHandler(serviceMap,serverTask));

                        }
                    });
            //bind初始化端口是异步的，但调用sync则会同步阻塞等待端口绑定成功
            ChannelFuture future = bootstrap.bind(url.getHostname(), url.getPort()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    //绑定成功后，修改子节点
                    if(future.isSuccess()){
                        RegisterForServer.getInstance().serverOn(servicName,url);
                    }
                }
            });
            //添加发送心跳
//            beatDataTask.submit(() -> beatDataSender = new BeatDataSender(url.toString(), ZKConsts.KEEPALIVEMONITOR_ADDRESS, ZKConsts.KEEPALIVEMONITOR_PORT, servicName));

            future.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            beatDataSender.close();
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
