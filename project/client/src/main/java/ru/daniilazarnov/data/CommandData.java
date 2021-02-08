package ru.daniilazarnov.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class CommandData extends CommonData implements iData {
    private ArrayList<String> echo = new ArrayList<String>();
    private ArrayList<String> param = new ArrayList<String>();
    private int command = 0;
    private int completed = 0;

    public CommandData() {
        super(TypeMessages.COMMAND);
    }

    public CommandData(int command) {
        super(TypeMessages.COMMAND);
        this.command = command;
    }

    public void run () {
        if (this.completed == 1) return;
        this.showEcho();
        this.completed = 1;
    }

    public CommandData setParam (String[] param) {
        IntStream.range(0, param.length).filter(i -> i > 0).mapToObj(i -> param[i]).forEach(this.param::add);
        return this;
    }

    private void showEcho () {
        if (this.echo.size() == 0) return;
        this.echo.forEach((n) -> System.out.println(n));
    }

}
