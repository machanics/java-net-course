package ru.daniilazarnov;

import ru.daniilazarnov.data.FileData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

public class StorageUtil {



    public static Path getUserPath (int userid) {
        Path path = Paths.get(StorageServer.LOCATION_FILES + "" +userid+ "" +File.separator+ "");

        if (!Files.isDirectory(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return path;
    }



    public static String pathFileStorage (FileData f, String id) {
        Path path = Paths.get(StorageServer.LOCATION_FILES + "" +id+ "" +File.separator+ "" + f.getToCatalog());
        File pathFile = new File(path.toString());

        if (pathFile.isFile()) {
            return path.toString();
        }

        if (!Files.isDirectory(path.getParent())) {
            try {
                Files.createDirectories(path.getParent());
                pathFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            pathFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return path.toString();
    }


    public static String createTempFile () {
        Path path = Paths.get(StorageServer.LOCATION_TEMP_FILES + UUID.randomUUID().toString());
        File pathFile = new File(path.toString());

        try {
            pathFile.createNewFile();
        } catch (IOException e) {
            return createTempFile();
        }

        return pathFile.toString();
    }













}
