package ru.daniilazarnov;

public class ReadsSizeHandler implements Runnable {

    DataHandler dataHandler;
    long monitor;

    public ReadsSizeHandler (DataHandler datahandler, long m) {
        this.dataHandler = datahandler;
        this.monitor = m;
    }

    @Override
    public void run() {
        while (true) {
            if (this.dataHandler.readsSize >= this.monitor) {
                this.dataHandler.run(); break;
            }
        }
    }
}
