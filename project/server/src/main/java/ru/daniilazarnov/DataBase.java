package ru.daniilazarnov;

import ru.daniilazarnov.data.AuthData;

import java.sql.*;


public class DataBase {
    public static Connection conn;
    public static Statement statmt;
    public static ResultSet resSet;

    public static void connected() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:files/Users.db");
        createTableUsers();
    }

    public static void createTableUsers() throws ClassNotFoundException, SQLException {
        statmt = conn.createStatement();
        statmt.execute("CREATE TABLE if not exists 'users' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'login' text, 'password' text);");
    }

    public static int insertUsers(AuthData authdata) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO users (login, password) VALUES (?,?)");

        System.out.println(authdata.getLogin());
        System.out.println(authdata.getPassword());

        if (getLogin(authdata.getLogin())) {
            return 0;
        } else {
            ps.setString(1, authdata.getLogin());
            ps.setString(2, authdata.getPassword());
            return ps.executeUpdate();
        }
    }

    public static boolean getLogin (String login) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE login = ?");
        ps.setString(1, login);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            return rs.getInt("id") > 0;
        }

        return false;
    }




    public static int getLoginAndPassword (AuthData authdata) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE login = ? and password = ?");
        ps.setString(1, authdata.getLogin());
        ps.setString(2, authdata.getPassword());
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            return rs.getInt("id");
        }

        return 0;
    }

    public static void ReadDB() throws ClassNotFoundException, SQLException {
        resSet = statmt.executeQuery("SELECT * FROM users");

        while(resSet.next())
        {
            int id = resSet.getInt("id");
            String  name = resSet.getString("name");
            String  phone = resSet.getString("phone");
            System.out.println( "ID = " + id );
            System.out.println( "name = " + name );
            System.out.println( "phone = " + phone );
            System.out.println();
        }


    }

    public static void close () throws ClassNotFoundException, SQLException {
        conn.close();
        statmt.close();
        resSet.close();
    }

}

