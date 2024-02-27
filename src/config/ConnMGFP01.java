/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import javax.swing.JOptionPane;

/**
 *
 * @author TomMe
 */
public class ConnMGFP01 {

    public ConnMGFP01(Connection connection) {
        this.connection = connection;
    }

    // O endereço da base de dados
    private static final String USER = "sysdba";
    private static final String PASSWORD = "masterkey";
    private static String ip;
    private static String folderToBd;
    private static String url;
    private Connection connection;

    public ConnMGFP01() {
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public static void getHost() {
        try {
            File myObj = new File(System.getProperty("user.dir") + "/setup.ini");
            try (Scanner myReader = new Scanner(myObj)) {
                int i = 0;
                while (myReader.hasNextLine()) {
                    ++i;
                    String data = myReader.nextLine();
                    if (i == 1 && data.substring(0, 12).equals("@connString ")) {
                        setIp(data.substring(12).trim());
                    } else {
                        setIp("localhost");
                    }
                    if (i == 2 && data.substring(0, 12).equals("@folderToBd ")) {
                        setFolderToBd(data.substring(12).trim());
                    } else {
                        setFolderToBd(System.getProperty("user.dir"));
                        // setFolderToBd("c:/windows/mgfolha");
                    }
                }
                System.out.println(getFolderToBd());
                setUrl("jdbc:firebirdsql:" + getIp() + "/3050:" + getFolderToBd()
                        + "/MGFP01.fdb?charSet=utf-8&defaultHoldable=true");
            }
        } catch (FileNotFoundException e) {
            JOptionPane.showMessageDialog(null, "O sistema não pode encontrar o arquivo setup.ini");
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Conectar ao BD
     */
    public void conectar() {
        try {
            getHost();
            System.out.println(getUrl());
            mensagem("Conectar a: " + getUrl());
            Class.forName("org.firebirdsql.jdbc.FBDriver");
            Connection conn = DriverManager.getConnection(getUrl(), USER, PASSWORD);
            this.setConnection(conn);
            mensagem("Conectado");
        } catch (ClassNotFoundException | SQLException e) {
            mensagem("Erro: " + e.getMessage());
        } 
    }

    /**
     * Desconectar do BD
     */
    public void desconectar() {
        try {
            mensagem("Desconectar");
            if (!this.getConnection().isClosed()) {
                this.getConnection().close();
            }
        } catch (SQLException e) {
            mensagem("Erro: " + e.getMessage());
        } finally {
            mensagem("Desconectado");
        }
    }

    public String mensagem(String texto) {
        String ret = texto;
        System.out.println(ret);
        return ret;
    }

    public static String getIp() {
        return ip;
    }

    public static void setIp(String ip) {
        ConnMGFP01.ip = ip;
    }

    public static String getFolderToBd() {
        return folderToBd;
    }

    public static void setFolderToBd(String folderToBd) {
        ConnMGFP01.folderToBd = folderToBd;
    }

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String url) {
        ConnMGFP01.url = url;
    }

}
