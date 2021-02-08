package ru.daniilazarnov.data;

public class AuthData extends CommonData implements iData  {

    private String login;
    private String password;
    private int command;

    public AuthData(String login, String password) {
        super(TypeMessages.AUTH);
    }

    public AuthData setType (int i) {
        this.command = i; return this;
    }

    public int getCommand () {
        return this.command;
    }

    public String getLogin () {
        return this.login;
    }

    public String getPassword () {
        return this.password;
    }
}
