package ru.daniilazarnov.data;

import java.nio.file.Path;
import java.util.ArrayList;

public class HelpCommand implements iCommand {

    @Override
    public ArrayList execute() {
        return null;
    }

    @Override
    public ArrayList execute(ArrayList param) {
        return null;
    }

    @Override
    public ArrayList execute(Path current, ArrayList param) {
        ArrayList<String> echo = new ArrayList<String>();

        echo.add("[-h]: Show help list");
        echo.add("[reg -lLogin -pPaswoord]: User registration");
        echo.add("[login -lLogin -pPaswoord]: User authorization");
        echo.add("[ls]: show folder");
        echo.add("[mkdir -p NAME]: create folder");
        echo.add("[cd -p URL]: transition to url");
        echo.add("[upload -p from -p to]: upload file to Storage Server, (*from): absolute path. (upload /Users/r.shafikov/Downloads/bmw_x3m_2021.jpeg /photo/x3m1.jpg)");
        echo.add("[download -p From -p To]: download file to Storage Server (*to): absolute path.");

        return echo;
    }





}
