package com.CodeEvalCrew.AutoScore.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SourceCheckUtil {

    public Optional<String> checkConnectionStrings() {
        return Optional.empty();
    }

    public Optional<String> checkSolutionName() {
        return Optional.empty();
    }

    public static void analyzeCSharpStructure(String directoryPath) throws IOException {
        try {
            Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".cs")) // Only C# source files
                    .forEach(file -> {
                        try {
                            analyzeFile(file.toFile());
                        } catch (IOException e) {
                            System.err.println("Error reading file: " + file);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Error walking through directory: " + directoryPath);
        }
    }

    private static void analyzeFile(File file) throws IOException {
        System.out.println("Analyzing file: " + file.getName());
        List<String> lines = Files.readAllLines(file.toPath());

        for (String line : lines) {
            checkForNamespace(line);
            checkForClass(line);
            checkForMethod(line);
        }
    }

    private static void checkForNamespace(String line) {
        Pattern namespacePattern = Pattern.compile("\\bnamespace\\s+([a-zA-Z_][a-zA-Z0-9_\\.]*)");
        Matcher matcher = namespacePattern.matcher(line);
        if (matcher.find()) {
            System.out.println("Namespace found: " + matcher.group(1));
            System.out.println("");
        }
    }

    private static void checkForClass(String line) {
        Pattern classPattern = Pattern.compile("\\bclass\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher matcher = classPattern.matcher(line);
        if (matcher.find()) {
            System.out.println("Class found: " + matcher.group(1));
            System.out.println("");
        }
    }

    private static void checkForMethod(String line) {
        Pattern methodPattern = Pattern.compile("\\b(public|private|protected|internal)?\\s+\\w+\\s+(\\w+)\\s*\\(");
        Matcher matcher = methodPattern.matcher(line);
        if (matcher.find()) {
            System.out.println("Method found: " + matcher.group(2));
            System.out.println("");
        }
    }

    public static Optional<String> findSolutionName(String directoryPath, String solutionName) {
        try {
            // Search for .sln files that contain the specified string in their names
            return Files.walk(Paths.get(directoryPath), 1)
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".sln"))
                    .filter(file -> file.getFileName().toString().contains(solutionName)) // Match the string part
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .map(fileName -> fileName.replace(".sln", "")) // Remove the .sln extension
                    .findFirst();

        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static void analyzeAppSettings(String filePath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Parse the JSON file
            JsonNode rootNode = mapper.readTree(new File(filePath));

            // Check for known sections, e.g., "ConnectionStrings"
            if (rootNode.has("ConnectionStrings")) {
                System.out.println("ConnectionStrings section found.");
                JsonNode connectionStringsNode = rootNode.get("ConnectionStrings");
                displaySection(connectionStringsNode, "ConnectionStrings");
            } else {
                System.out.println("No ConnectionStrings section found.");
            }

            // Add checks for other known sections as needed
            // Display all root keys
            System.out.println("Root Keys in appsettings.json:");
            Iterator<String> rootKeys = rootNode.fieldNames();
            while (rootKeys.hasNext()) {
                System.out.println(" - " + rootKeys.next());
            }

        } catch (IOException e) {
            System.err.println("Error reading or parsing file: " + filePath);
            e.printStackTrace();
        }
    }

    // Display nested JSON sections
    private static void displaySection(JsonNode sectionNode, String sectionName) {
        System.out.println("Contents of section: " + sectionName);
        Iterator<Entry<String, JsonNode>> fields = sectionNode.fields();
        while (fields.hasNext()) {
            Entry<String, JsonNode> field = fields.next();
            System.out.println(" - " + field.getKey() + ": " + field.getValue());
        }
    }

    public static Optional<String> findAppsettingsJsonPath(String directoryPath) {
        try {
            // Search for the appsettings.json file in the specified directory and its subdirectories
            return Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.getFileName().toString().equals("appsettings.json")) // Find appsettings.json
                    .map(Path::toString) // Convert Path to String
                    .findFirst();                              // Return the first matching file path as String

        } catch (IOException e) {
            System.err.println("Error searching directory: " + directoryPath);
            e.printStackTrace();
            return Optional.empty();
        }
    }

    // Function to get all folder names in the directory containing a .sln file
    public static List<String> getAllFolderNamesInSolutionDirectory(String directoryPath) {
        File solutionFolder = findSolutionFolder(directoryPath);
        List<String> folderNames = new ArrayList<>();

        if (solutionFolder != null) {
            // List all subdirectories in the solution folder
            File[] subdirectories = solutionFolder.listFiles(File::isDirectory);
            if (subdirectories != null) {
                for (File subdirectory : subdirectories) {
                    folderNames.add(subdirectory.getName()); // Get only the folder name
                }
            }
        }
        return folderNames;
    }

    // Helper function to find the folder containing the .sln file
    private static File findSolutionFolder(String directoryPath) {
        try {
            return Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".sln"))
                    .map(Path::getParent)
                    .findFirst()
                    .map(Path::toFile)
                    .orElse(null);

        } catch (IOException e) {
            System.err.println("Error searching for .sln file: " + e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) throws IOException {
        String projectPath = "C:\\Project\\PE_PRN231_SU24_009909\\StudentSolution\\1\\vuongvtse160599\\0\\PEPRN231_SU24_009909_VoTrongVuong_BE";  // Replace with the path to your project directory

        
        Optional<String> appSettingsPath = findAppsettingsJsonPath(projectPath);

        appSettingsPath.ifPresentOrElse(
            path -> System.out.println("Path to appsettings.json: " + path),
            () -> System.out.println("appsettings.json file not found.")
            );
            String solutionNamePart = "PEPRN231_SU24_009909_VoTrongVuong_BE";   //?? Replace with part of the solution name you're looking for ??
            Optional<String> solutionName = findSolutionName(projectPath, solutionNamePart);
            String path = appSettingsPath.orElseThrow(() -> new RuntimeException("Path not found"));
            analyzeAppSettings(path);
            solutionName.ifPresentOrElse(
                name -> System.out.println("Solution found: " + name),
                () -> System.out.println("No matching solution (.sln) file found.")
            );


        // analyzeCSharpStructure(projectPath);

        List<String> folderNames = getAllFolderNamesInSolutionDirectory(projectPath);

        if (folderNames.isEmpty()) {
            System.out.println("No folders found in the directory containing the .sln file.");
        } else {
            System.out.println("Folders found in the solution directory:");
            for (String folderName : folderNames) {
                System.out.println(folderName);
            }
        }
    }
}
