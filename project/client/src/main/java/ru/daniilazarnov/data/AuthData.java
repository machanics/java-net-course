package ru.daniilazarnov.data;


public class AuthData extends CommonData implements iData  {

    private String login;
    private String password;
    private int command;

    public AuthData(String login, String password) {
        super(TypeMessages.AUTH);
        this.login = login;
        this.password = password;
    }

    public AuthData setType (int i) {
        this.command = i; return this;
    }



}