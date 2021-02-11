package ru.daniilazarnov.data;

import java.util.*;
import java.util.stream.Collectors;

public enum Command {

    HELP(1, "HelpCommand"),
    LS(2, "LsCommand"),
    MKDIR(3, "MkDirCommand");

    public static Map<Integer, String> mapCommand = Arrays.stream(Command.values())
            .collect(Collectors.toMap(mapCommand -> mapCommand.code, mapCommand -> mapCommand.name));

    public String name;
    public int code;

    Command(Integer code, String name) {
        this.code = code;
        this.name = name;
    }


    public static iCommand getCommand(int code) {
        try {
            return  (iCommand) Class.forName(Command.class.getPackageName() + "." + mapCommand.get(code)).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
