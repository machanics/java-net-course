package ru.daniilazarnov;

import ru.daniilazarnov.data.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import com.google.gson.*;



public class Client {

    private SocketChannel socketChannel;
    public static final int PORT = 8894;
    public static final int SIZE_META = 10;
    private static final ByteBuffer buffer = ByteBuffer.allocate(48);

    public Client () {
        this.connect();
        Scanner scan = new Scanner(System.in);
        while(!scan.equals("exit") && scan.hasNext()) {
            String[] consoleString = scan.nextLine().split("\\s+");
            if(consoleString[0].equals("exit"))  break;
            CommonData c = CommonData.serialization(consoleString);
            if (c != null) this.send(c);
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
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


    public void connect () {
        new Thread(() -> {
            try {
                this.socketChannel =  SocketChannel.open();
                this.socketChannel.connect(new InetSocketAddress(PORT));
                this.read(new DataHandler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void read (DataHandler cHandler) {
        ByteBuffer buf = ByteBuffer.allocateDirect(buffer.limit());
        int nRead = 0;
        try {
            buf.clear();

            String tempFile = cHandler.createTempFile();
            while ((nRead = socketChannel.read(buf)) > 0) {

                cHandler.readsSize = cHandler.readsSize + nRead;
                byte[] bytes = new byte[nRead];
                buf.flip();
                buf.get(bytes);

                try (FileOutputStream fos = new FileOutputStream(tempFile, true)) {
                    fos.write(bytes);
                }

                if (cHandler.size == 0 && cHandler.readsSize >= 10) {
                    FileInputStream fRead = new FileInputStream(tempFile);
                    try {
                        byte[] sizeByte = new byte[SIZE_META];
                        fRead.read(sizeByte, 0, SIZE_META);
                        cHandler.size = Integer.parseInt(new String(sizeByte));
                    } finally {
                        fRead.close();
                    }
                }

                if (cHandler.metaData.length() == 0 && (cHandler.size+SIZE_META) <= cHandler.readsSize) {
                    FileInputStream fRead = new FileInputStream(tempFile);
                    try {
                        fRead.skip((long) SIZE_META);
                        byte[] sizeMetaByte = new byte[cHandler.size];
                        fRead.read(sizeMetaByte, 0, cHandler.size);
                        cHandler.metaData = new String(sizeMetaByte);
                    } finally {
                        fRead.close();
                    }
                }

                if (cHandler.metaData.length() > 0 && !cHandler.getIsAccepted()) {
                    cHandler.addCommand();
                }

                buf.clear();
            }

            if (nRead < 0) {
                socketChannel.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T extends CommonData> void  send (T objectData) {
        byte[] dataJson = objectData.jsonToString().getBytes();
        byte[] size = String.format("%010d", dataJson.length).getBytes();
        byte[] dataBytes = this.unionByteArray(size, dataJson);

        int restRead = 0;
        byte[] data = new byte[buffer.limit()];
        try ( InputStream is = new ByteArrayInputStream(dataBytes) ){
            int nRead = 0;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                if ((data.length - nRead) == 0) {
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    this.socketChannel.write(buffer);
                } else {
                    restRead = nRead;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (objectData.getType() != TypeMessages.FILE) {
            try {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                this.socketChannel.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return; // ** последний массив байтов с грезью ** //
        }

        File fromFile = new File(((FileData) objectData).getFromPath());
        try (FileInputStream is = new FileInputStream(fromFile)) {

            if ((data.length - restRead) != 0) {
                byte[] restReadByte = new byte[data.length - restRead];
                is.read(restReadByte, 0, restReadByte.length);
                byte[] oldBytes = this.replaceByteArray(data, restReadByte);
                ByteBuffer buffer = ByteBuffer.wrap(oldBytes);
                this.socketChannel.write(buffer);
            }

            int nRead = 0;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                ByteBuffer buffer = ByteBuffer.wrap(data);
                this.socketChannel.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
