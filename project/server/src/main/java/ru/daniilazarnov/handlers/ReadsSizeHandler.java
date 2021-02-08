package ru.daniilazarnov.handlers;

public class ReadsSizeHandler implements Runnable {

    long monitor;
    CommandHandler commandHandler;
    TempDataHandler temp;

    public ReadsSizeHandler (TempDataHandler temp, CommandHandler commandhandler, long m) {
        this.temp = temp;
        this.commandHandler = commandhandler;
        this.monitor = m;
    }

    @Override
    public void run() {
        while (true) {
            if (this.temp.getReadsSize() >= this.monitor) {
                this.commandHandler.run(); break;
            }
        }
    }
}
