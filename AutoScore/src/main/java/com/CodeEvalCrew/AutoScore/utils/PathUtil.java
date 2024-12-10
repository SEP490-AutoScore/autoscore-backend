package com.CodeEvalCrew.AutoScore.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PathUtil {

    //postman 
    public static final String NEWMAN_CMD_PATH = "AUTOMATIC";

    //sql server
    public static final String DATABASE_URL = "jdbc:sqlserver://MSI\\SQLSERVER;databaseName=master;user=sa;password=123456;encrypt=false;trustServerCertificate=true;";
    // public static final String DATABASE_URL = "jdbc:sqlserver://ADMIN-PC\\SQLEXPRESS;databaseName=master;user=sa;password=1234567890;encrypt=false;trustServerCertificate=true;";
    public static final String DATABASE_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

     public static String getNewmanCmdPath() {
        if (!"AUTOMATIC".equalsIgnoreCase(NEWMAN_CMD_PATH)) {
            return NEWMAN_CMD_PATH;
        }

        try {
            Process process = new ProcessBuilder("where", "newman").start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".cmd") || line.endsWith(".exe")) {
                    return line.trim(); // Return the first valid path
                }
            }
        } catch (IOException e) {
            System.err.println("Error while trying to locate newman: " + e.getMessage());
        }

        return NEWMAN_CMD_PATH;
    }
}
