package ru.daniilazarnov.data;

import java.nio.file.Path;
import java.util.ArrayList;

public interface iCommand {

    public ArrayList execute ();
    public ArrayList execute (ArrayList param);
    public ArrayList execute (Path current, ArrayList param);
}
