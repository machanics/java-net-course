package ru.daniilazarnov;

import ru.daniilazarnov.data.Command;
import ru.daniilazarnov.data.iCommand;

public class test {


    public static void main(String[] args) {


        iCommand c = Command.getCommand(1);
        System.out.println(c);

    }

}
