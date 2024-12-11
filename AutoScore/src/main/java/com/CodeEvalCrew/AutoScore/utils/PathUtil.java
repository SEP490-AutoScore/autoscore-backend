package com.CodeEvalCrew.AutoScore.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PathUtil {

    @Value("${app.newman.cmd_path}")
    private String newmanCmdPath;

    @Value("${app.database.url}")
    private String databaseUrl;

    @Value("${app.database.driver}")
    private String databaseDriver;

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public String getNewmanCmdPath() {
        if (!"AUTOMATIC".equalsIgnoreCase(newmanCmdPath)) {
            return newmanCmdPath;
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

        return newmanCmdPath;
    }
}
