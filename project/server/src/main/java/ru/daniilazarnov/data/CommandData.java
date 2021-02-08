package ru.daniilazarnov.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class CommandData extends CommonData implements iData {
    private ArrayList<String> echo = new ArrayList<String>();
    private ArrayList<String> param = new ArrayList<String>();
    private int command = 0;
    private int completed = 0;

    public CommandData() {
        super(TypeMessages.COMMAND);
    }

    public void addEcho (String text) {
        this.echo.add(text);
    }

    public CommandData setCommand(int i){
        this.command = i;
        return this;
    }


    public CommandData ls (Path path){
        File currentDir = new File(path.toString());

        for (File file : currentDir.listFiles()) {
            if (file.isDirectory()) {
                this.addEcho("Directory: " + file.getName());
            } else {
                this.addEcho("file: " + file.getName());
            }
        }

        return this;
    }

    public CommandData mkdir (Path path, ArrayList<String> param) {
        String dirPath = path.toString() + "" +File.separator+ "" + param.get(0);

        if (!Files.isDirectory(Path.of(dirPath))) {
            try {
                Files.createDirectories(Path.of(dirPath));
                this.addEcho("Success mkdir : "+ param.get(0));
            } catch (IOException e) {
                this.addEcho("failed mkdir : "+ param.get(0));
            }
        }

        return this;
    }

    public ArrayList<String> getParam() {
        return this.param;
    }
    public int getCommand() {
        return this.command;
    }

    public CommandData writeInHelp () {
        this.echo.add("[-h]: Show help list");
        this.echo.add("[reg -lLogin -pPaswoord]: User registration");
        this.echo.add("[login -lLogin -pPaswoord]: User authorization");
        this.echo.add("[ls]: show folder");
        this.echo.add("[mkdir -p NAME]: create folder");
        this.echo.add("[cd -p URL]: transition to url");
        this.echo.add("[upload -p from -p to]: upload file to Storage Server, (*from): absolute path. (upload /Users/r.shafikov/Downloads/bmw_x3m_2021.jpeg /photo/x3m1.jpg)");
        this.echo.add("[download -p From -p To]: download file to Storage Server (*to): absolute path.");

        return this;
    }

    public CommandData notAuthorized() {
        this.completed = 0;
        this.addEcho("Please log in, otherwise session kill 120 sec.");

        return this;
    }

    public CommandData authFailed() {
        this.completed = 0;
        this.addEcho("Authorized failed, please try again!");
        return this;
    }

    public CommandData authSuccess() {
        this.completed = 0;
        this.addEcho("Authorized Success!");
        return this;
    }

    public CommandData uploadSuccess() {
        this.completed = 0;
        this.addEcho("Upload Success!");
        return this;
    }



}
