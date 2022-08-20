package cc.baka9.catseedlogin.bukkit.database;

import cc.baka9.catseedlogin.bukkit.Config;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL extends SQL {
    private Connection connection;

    public MySQL(JavaPlugin javaPlugin){
        super(javaPlugin);
    }

    @Override
    public Connection getConnection() throws SQLException{

        if (this.connection != null && !this.connection.isClosed() && this.connection.isValid(10)) {
            return this.connection;
        }

        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.connection = DriverManager.getConnection(
                    "jdbc:mysql://" + Config.MySQL.host + ":" + Config.MySQL.port + "/" + Config.MySQL.database + "?characterEncoding=UTF-8",
                    Config.MySQL.user, Config.MySQL.password
            );
            return this.connection;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

}