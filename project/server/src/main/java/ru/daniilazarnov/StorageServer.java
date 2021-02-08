package ru.daniilazarnov;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import ru.daniilazarnov.handlers.CommandHandler;
import ru.daniilazarnov.handlers.TempDataHandler;
import ru.daniilazarnov.handlers.WriteDataHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class StorageServer {
    private static final Logger logger = Logger.getLogger(StorageServer.class.getName());
    private ExecutorService executorService;


    private static final HashMap<String, ChannelHandlerContext> execConsMap = new HashMap<>();


    public static final String LOCATION_FILES = "files" + File.separator;
    public static final String LOCATION_TEMP_FILES = LOCATION_FILES + File.separator + "temp" + File.separator;

    public static final int SIZE_META = 10;
    public static final int PORT = 8894;

    public StorageServer () {

        this.loggerSettings();
        this.executorService = Executors.newCachedThreadPool();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            DataBase.connected();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new WriteDataHandler());
                            ch.pipeline().addLast(new TempDataHandler());
                        }
                    });

            ChannelFuture f = b.bind(PORT).sync();
            f.addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        logger.log(Level.INFO, future.channel().localAddress()+" started!");
                    }
                }
            });


            f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }

    }

    public ExecutorService getExecutorService () {
        return this.executorService;
    }

    public static void loggerSettings () {
        try {
            Path properties = Paths.get("logging.properties");
            Path logDir = Paths.get("log");

            if(Files.exists(properties)) {
                LogManager.getLogManager().readConfiguration(new FileInputStream(properties.toString()));
            }

            if (!Files.isDirectory(logDir)) Files.createDirectory(logDir);

            logger.addHandler(new FileHandler("log/log_%g.txt", 10 * 1024, 20, true));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void setExecConsMap (String socketid, ChannelHandlerContext ctx) {
        StorageServer.execConsMap.put(socketid, ctx);
    }

    public static ChannelHandlerContext getExecConsMap (String socketid) {
        return StorageServer.execConsMap.get(socketid);
    }

    public static void removeExecConsMap (String socketid) {
        StorageServer.execConsMap.remove(socketid);
    }





}
