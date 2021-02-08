package ru.daniilazarnov.data;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.gson.*;

public class CommonData implements iData, Serializable {

    private TypeMessages type;

    public CommonData (TypeMessages type) {
        this.type = type;
    }

    @Override
    public TypeMessages getType() {
        return this.type;
    }

    public String jsonToString () {
        Gson gson = new Gson();
        return gson.toJson(this);
    }


    public static CommonData serialization (String[] command) {
        if (command[0].equals("-h")) {
            return new CommandData(1);
        } else if (command[0].equals("ls")) {
            return new CommandData(2);
        } else if (command[0].equals("upload")) {
            if (command.length != 3) return null;
            return new FileData(command[1], command[2]);
        } else if (command[0].equals("reg") || command[0].equals("login")) {
            if (command[0].equals("reg")) {
                return new AuthData(command[1], command[2]).setType(2);
            } else {
                return new AuthData(command[1], command[2]).setType(1);
            }
        } else if (command[0].equals("cd")){
            return new CommandData(4).setParam(command);

        } else if (command[0].equals("mkdir")){
            return new CommandData(3).setParam(command);

        } else if (command[0].equals("remove")){
            return new CommandData(5).setParam(command);

        } else if (command[0].equals("download")){
            return new CommandData(6).setParam(command);

        }

        return null;
    }

}
