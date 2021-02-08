package ru.daniilazarnov;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ru.daniilazarnov.data.CommandData;
import ru.daniilazarnov.data.CommonData;
import ru.daniilazarnov.data.FileData;
import ru.daniilazarnov.data.TypeMessages;

import java.io.*;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataHandler {
    private static final Logger logger = Logger.getLogger(DataHandler.class.getName());
    public static final String LOCATION_FILES = "files" + File.separator;
    public static final String LOCATION_TEMP_FILES = LOCATION_FILES + File.separator + "temp_client" + File.separator;

    public Path path;
    public int size = 0;
    public String metaData = "";
    private volatile boolean isAccepted = false;
    public volatile long readsSize = 0;



    private ArrayList<? super CommonData> accepted = new ArrayList<>();
    private Gson gson = new Gson();


    public DataHandler () {

    }

    public void setIsAccepted (boolean b) {
        this.isAccepted = b;
    }

    public boolean getIsAccepted () {
        return this.isAccepted;
    }


    public void addCommand () {
        JsonObject jsonObject = JsonParser.parseString(this.metaData).getAsJsonObject();
        String type = jsonObject.get("type").getAsString();
        this.setIsAccepted(true);

        if (type.equals(TypeMessages.FILE.toString())) {
            FileData fileData = (FileData) gson.fromJson(this.metaData, FileData.class);
            this.accepted.add(fileData);
            long monitor = fileData.getLengthByte() + Client.SIZE_META + this.metaData.length();
            new Thread(new ReadsSizeHandler(this, monitor)).start();

        } else if (type.equals(TypeMessages.COMMAND.toString())) {
            this.accepted.add((CommandData) gson.fromJson(this.metaData, CommandData.class));
            this.run();
        }

    }

    public CommonData getCommand () {
        if (this.accepted.size() == 0) return null;
        return (CommonData) this.accepted.get(this.accepted.size()-1);
    }

    public void run () {
        if (this.accepted.size() == 0) return;
        CommonData command = (CommonData) this.accepted.get(this.accepted.size()-1);

        if (command.getType() == TypeMessages.FILE) {
            this.tempToFile((FileData) command);
        } else {
            ((CommandData) command).run();
        }

        this.size = 0;
        this.readsSize = 0;
        this.metaData = "";
        this.setIsAccepted(false);

        try (FileOutputStream writer = new FileOutputStream(this.path.toString())){
            writer.write(("").getBytes());
            writer.close();
        } catch (IOException e) {
            this.logger.log(Level.INFO, "Error clear file : "+this.path.toString());
            this.createTempFile();
        }

    }

    public String createTempFile () {
        this.path = Paths.get(DataHandler.LOCATION_TEMP_FILES + UUID.randomUUID().toString());
        File pathFile = new File(this.path.toString());

        try {
            pathFile.createNewFile();
        } catch (IOException e) {
            return createTempFile();
        }

        return pathFile.toString();
    }


    public static String getFilePath (FileData f) {
        File pathFile = new File(f.getToCatalog());

        if (!pathFile.isFile()) {
            try {
                pathFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return f.getToCatalog();
    }

    public static long getSizeFilePath (String path) {
        return new File(path).length();
    }

    private void tempToFile (FileData f) {
        byte[] byteF = new byte[(int) f.getLengthByte()];
        int metaSize = this.metaData.length();
        File pathFile = new File(f.getToCatalog());

        try {

            if (!pathFile.isFile()) {
                pathFile.createNewFile();
            }

            FileInputStream fRead = new FileInputStream(this.path.toString());
            FileOutputStream fWrite = new FileOutputStream(pathFile, false);

            fRead.skip((long) Client.SIZE_META + this.metaData.length());
            fRead.read(byteF, 0, byteF.length);
            fWrite.write(byteF);

            fWrite.close();
            fRead.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
