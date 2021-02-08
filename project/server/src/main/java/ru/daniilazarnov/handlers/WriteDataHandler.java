package ru.daniilazarnov.handlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import ru.daniilazarnov.StorageUtil;
import ru.daniilazarnov.data.CommonData;
import ru.daniilazarnov.data.FileData;
import ru.daniilazarnov.data.TypeMessages;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class WriteDataHandler extends ChannelOutboundHandlerAdapter {

    private BufferedInputStream in;
    private BufferedOutputStream out;
    private final ByteBuffer buffer = ByteBuffer.allocate(48);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        CommonData objectData = (CommonData) msg;

        byte[] dataJson = objectData.jsonToString().getBytes();
        byte[] size = String.format("%010d", dataJson.length).getBytes();
        byte[] dataBytes = this.unionByteArray(size, dataJson);

        ByteBuf buf = ctx.alloc().buffer(dataBytes.length);
        buf.writeBytes(dataBytes);
        ctx.writeAndFlush(buf);

        if (objectData.getType() == TypeMessages.FILE) {
            FileData fileData = (FileData) msg;
            FileRegion region = new DefaultFileRegion(new File(fileData.getFromPath()), 0, fileData.getLengthByte());
            ctx.writeAndFlush(region);
        }

    }


    public byte[] unionByteArray (byte[] firstArray, byte[] secondArray) {
        byte[] dataBytes = Arrays.copyOf(firstArray, secondArray.length + firstArray.length);
        System.arraycopy(secondArray, 0, dataBytes, firstArray.length, secondArray.length);
        return dataBytes;
    }

    public byte[] replaceByteArray (byte[] firstArray, byte[] secondArray) {
        System.arraycopy(secondArray, 0, firstArray, firstArray.length - secondArray.length , secondArray.length);
        return firstArray;
    }
}



