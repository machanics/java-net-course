package ru.daniilazarnov.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import ru.daniilazarnov.StorageServer;
import ru.daniilazarnov.data.CommandData;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TempDataHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = Logger.getLogger(TempDataHandler.class.getName());
    CommandHandler connHand;

    private volatile boolean isAccepted = false;
    private volatile long readsSize = 0;
    private int size = 0;
    private String tempFile = "";
    private String metaData = "";


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        byte[] data = new byte[buf.readableBytes()];
        this.readsSize = this.readsSize + data.length;
        buf.readBytes(data);

        try (FileOutputStream fos = new FileOutputStream(this.tempFile, true)) {
            fos.write(data);
        }

        if (buf.readableBytes() == 0) {
            buf.release();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (this.size == 0 && this.readsSize >= 10) {
            FileInputStream fRead = new FileInputStream(this.tempFile);
            try {
                byte[] sizeByte = new byte[StorageServer.SIZE_META];
                fRead.read(sizeByte, 0, StorageServer.SIZE_META);
                this.size = Integer.parseInt(new String(sizeByte));
            } finally {
                fRead.close();
            }
        }

        if (this.metaData.length() == 0 && this.size <= this.readsSize) {
            FileInputStream fRead = new FileInputStream(this.tempFile);
            try {
                fRead.skip((long) StorageServer.SIZE_META);
                byte[] sizeMetaByte = new byte[this.size];
                fRead.read(sizeMetaByte, 0, this.size);
                this.metaData = new String(sizeMetaByte);
            } finally {
                fRead.close();
            }
        }

        if (this.metaData.length() > 0 && !this.getIsAccepted()) {
            this.connHand.addCommand();
        }

        ctx.fireChannelReadComplete();
    }

    public long getReadsSize() {
        return this.readsSize;
    }

    public void clear () {

        this.setIsAccepted(false);
        this.readsSize = 0;
        this.size = 0;
        this.metaData = "";

        try (FileOutputStream writer = new FileOutputStream(this.tempFile)){
            writer.write(("").getBytes());
            writer.close();
        } catch (IOException e) {
            this.logger.log(Level.INFO, "Error clear file : "+this.connHand.getTempFile());
            this.tempFile = this.connHand.getTempFile();
        }

    }

    public String getMeta() {
        return this.metaData;
    }


    public void setIsAccepted (boolean b) {
        this.isAccepted = b;
    }

    public boolean getIsAccepted () {
        return this.isAccepted;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.connHand = new CommandHandler(this, ctx.channel().id().toString());
        this.tempFile = connHand.createTempFile();

//        FileData welcome = new FileData();
//        welcome.addFile(Path.of("files/1/photo/x3m_1.jpg"));
//        welcome.setCalalog("/Users/r.shafikov/Downloads/1111111.jpeg");
        CommandData welcome = new CommandData();
        welcome.addEcho("Welcome to Storage Server, please auth! Show HELP -h");
        ctx.write(welcome);

        StorageServer.setExecConsMap(ctx.channel().id().toString(), ctx);
        logger.log(Level.INFO, "Connected client socketid: "+ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        StorageServer.removeExecConsMap(ctx.channel().id().toString());
        logger.log(Level.INFO, "Disconnect client socketid: "+ctx.channel().id());
        super.channelInactive(ctx);
    }

}
