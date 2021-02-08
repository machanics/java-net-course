package ru.daniilazarnov.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.channel.ChannelHandlerContext;
import ru.daniilazarnov.DataBase;
import ru.daniilazarnov.StorageServer;
import ru.daniilazarnov.StorageUtil;
import ru.daniilazarnov.data.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class CommandHandler {

    private int id;
    private boolean isAuthorized = false;

    private Path userPath;
    private String currentPath;

    private TempDataHandler tempDataHandler;
    private ArrayList<? super CommonData> accepted = new ArrayList<>();
    private Gson gson = new Gson();
    private Path tempPath;
    private String socketId;

    public CommandHandler (TempDataHandler temp, String socketid) {
        this.socketId = socketid;
        this.tempDataHandler = temp;
    }


    public void addCommand () {
        String stringJson = tempDataHandler.getMeta();
        JsonObject jsonObject = JsonParser.parseString(stringJson).getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        tempDataHandler.setIsAccepted(true);

        if (type.equals(TypeMessages.FILE.toString())) {
            FileData fileData = (FileData) gson.fromJson(stringJson, FileData.class);
            long monitor = fileData.getLengthByte() + StorageServer.SIZE_META + stringJson.length();
            this.accepted.add(fileData);
            new Thread(new ReadsSizeHandler(tempDataHandler,this, monitor)).start();
        } else if (type.equals(TypeMessages.COMMAND.toString())) {
            this.accepted.add((CommandData) gson.fromJson(stringJson, CommandData.class));
        } else if (type.equals(TypeMessages.AUTH.toString())) {
            this.accepted.add((AuthData) gson.fromJson(stringJson, AuthData.class));
        }

        if (!type.equals(TypeMessages.FILE.toString())) {
            this.run();
        }
    }

    public void run () {
        if (this.accepted.size() == 0) return;
        CommonData common = (CommonData) this.accepted.get(this.accepted.size()-1);

        if (!this.isAuthorized && common.getType() != TypeMessages.AUTH) {
            ChannelHandlerContext ctx = StorageServer.getExecConsMap(this.socketId);
            CommandData command = (CommandData) common;
            if (command.getCommand() == 1) {
                ctx.write(new CommandData().setCommand(1).writeInHelp());
            } else {
                ctx.write(new CommandData().notAuthorized());
            }
        }

        if (!this.isAuthorized && common.getType() == TypeMessages.AUTH) {
            ChannelHandlerContext ctx = StorageServer.getExecConsMap(this.socketId);
            AuthData authData = (AuthData) common;
            try {
                if (authData.getCommand() == 1) {
                    this.id = DataBase.getLoginAndPassword(authData);
                } else {
                    this.id = DataBase.insertUsers(authData);
                }

                if (this.id > 0) {
                    this.isAuthorized = true;
                    this.userPath = StorageUtil.getUserPath(this.id);
                    ctx.write(new CommandData().authSuccess());
                } else {
                    ctx.write(new CommandData().authFailed());
                }

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

        if (this.isAuthorized && common.getType() == TypeMessages.COMMAND) {
            CommandData commandIn = (CommandData) common;
            CommandData commandOut = new CommandData();
            ChannelHandlerContext ctx = StorageServer.getExecConsMap(this.socketId);

            switch (commandIn.getCommand()) {
                case 1 : ctx.write(commandOut.writeInHelp()); break;
                case 2 : ctx.write(commandOut.ls(this.currentUserPath())); break;
                case 3 : ctx.write(commandOut.mkdir(this.currentUserPath(), commandIn.getParam())); break;
            }


        }

        if (this.isAuthorized && common.getType() == TypeMessages.FILE) {
            ChannelHandlerContext ctx = StorageServer.getExecConsMap(this.socketId);
            this.tempToFile((FileData) common);
            ctx.write(new CommandData().uploadSuccess());
        }

        this.tempDataHandler.clear();
    }

    private Path currentUserPath () {
        if (currentPath != null) {
            return Path.of(this.userPath.toString() + currentPath);
        } else {
            return this.userPath;
        }
    }


    private void tempToFile (FileData f) {
        byte[] byteF = new byte[(int) f.getLengthByte()];
        int metaSize = this.tempDataHandler.getMeta().length();
        String path = StorageUtil.pathFileStorage(f, ""+this.id);

        try {

            FileInputStream fRead = new FileInputStream(this.getTempFile());
            FileOutputStream fWrite = new FileOutputStream(path, false);

            fRead.skip((long) StorageServer.SIZE_META + metaSize);
            fRead.read(byteF, 0, byteF.length);
            fWrite.write(byteF);

            fWrite.close();
            fRead.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String createTempFile () {
        this.tempPath = Paths.get(StorageServer.LOCATION_TEMP_FILES + UUID.randomUUID().toString());
        File pathFile = new File(tempPath.toString());

        try {
            pathFile.createNewFile();
        } catch (IOException e) {
            this.createTempFile();
        }

        return pathFile.toString();
    }


    public String getTempFile () {
        return tempPath.toString();
    }

}
