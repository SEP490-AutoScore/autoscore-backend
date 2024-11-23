package com.CodeEvalCrew.AutoScore.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.CodeEvalCrew.AutoScore.exceptions.NotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SourceCheckUtil {

    public Optional<String> checkImportant(String sourcePath, String studentCode, String examPaperCode, String examCode) throws Exception {
        try {
            //check solution name
            String solutionName = examCode + "_" + examPaperCode + "_" + studentCode;
            checkSolutionName(solutionName, sourcePath);
            //check connection string
            checkConnectionStrings(sourcePath, "ConnectionStrings", "MyDB", "appsettings.json");
            //chheck source structure
            checkSourceStructure(sourcePath);

            //check db conenct on api layer
            checkAPILayer();

        } catch (NotFoundException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw e;
        }

        return Optional.empty();
    }

    private Optional<String> checkConnectionStrings(String sourcePath, String section, String dbNode, String fileName) throws Exception, NotFoundException {
        Optional<String> jsonPath = findAppsettingsJsonPath(sourcePath);
        if (jsonPath.isEmpty()) {
            throw new NotFoundException(fileName + " not found");
        }
        // analyzeAppSettings(jsonPath.toString(), "ConnectionStrings", "MyDB");
        analyzeAppSettings(jsonPath.toString(), section, dbNode);
        return Optional.empty();
    }

    private Optional<String> checkSolutionName(String solutionName, String sourcePath) throws NotFoundException {
        try {
            Optional<String> solution = findSolutionName(sourcePath, solutionName);
            if (solution.isEmpty()) {
                throw new NotFoundException("Solution not found");
            }
            return solution;
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            System.out.println(e.getCause());
            throw e;
        }
    }

    private Optional<String> checkSourceStructure(String projectRoot) { 
        // Step 1: Identify the "main" .csproj file
        String mainCsproj = findMainCsproj(projectRoot);
        if (mainCsproj == null) {
            System.out.println("Main .csproj file not found.");
            return Optional.empty();
        }

        System.out.println("Main .csproj file: " + new File(mainCsproj).getName());

        // Step 2: Find all .csproj files in the project
        List<String> allCsprojFiles = new ArrayList<>();
        findAllCsprojFiles(new File(projectRoot), allCsprojFiles);

        // Step 3: Check if each .csproj has references
        for (String csprojFile : allCsprojFiles) {
            if (csprojFile.equals(mainCsproj)) {
                continue;
            }

            if (hasReferences(csprojFile)) {
                System.out.println(new File(csprojFile).getName() + " has references.");
            } else {
                System.out.println(new File(csprojFile).getName() + " does NOT have references.");
            }
        }
        return Optional.of(mainCsproj);
    }

    private Optional<String> checkAPILayer() {
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

    //read appsetting
    public static void analyzeAppSettings(String filePath, String sectionName, String dbNode) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Parse the JSON file
            JsonNode rootNode = mapper.readTree(new File(filePath));

            // Check for known sections, e.g., "ConnectionStrings"
            if (rootNode.has(sectionName)) {
                System.out.println(sectionName + " section found.");
                JsonNode sectionNode = rootNode.get(sectionName);

                // Check if the ConnectionStrings section has a DefaultConnection node
                if (sectionName.equals(sectionName) && sectionNode.has(dbNode)) {
                    System.out.println(dbNode + " found in " + sectionName + ".");
                    displaySection(sectionNode, sectionName);
                } else if (sectionName.equals(sectionName)) {
                    System.out.println("No " + dbNode + " found in " + sectionName + ".");
                }
            } else {
                System.out.println("No " + sectionName + " section found.");
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

    //find appsettign func
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

    // Method to get all .csproj files and their references
    public static Map<String, List<String>> getCsprojFilesAndReferences(String projectPath) {
        Map<String, List<String>> projectReferences = new HashMap<>();
        List<File> csprojFiles = collectCsprojFiles(new File(projectPath));
        System.out.println(".csproj file found in the solution "+csprojFiles.size());
        for (File csprojFile : csprojFiles) {
            List<String> references = getProjectReferences(csprojFile);
            projectReferences.put(csprojFile.getName(), references);
        }

        return projectReferences;
    }

    // Helper function to recursively collect all .csproj files
    private static List<File> collectCsprojFiles(File dir) {
        List<File> csprojFiles = new ArrayList<>();
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                csprojFiles.addAll(collectCsprojFiles(file));
            } else if (file.getName().endsWith(".csproj")) {
                csprojFiles.add(file);
            }
        }
        return csprojFiles;
    }

    // Function to parse a .csproj file and extract its project references
    private static List<String> getProjectReferences(File csprojFile) {
        List<String> references = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(csprojFile);
            doc.getDocumentElement().normalize();

            NodeList projectReferences = doc.getElementsByTagName("ProjectReference");

            for (int i = 0; i < projectReferences.getLength(); i++) {
                Element reference = (Element) projectReferences.item(i);
                String includePath = reference.getAttribute("Include");
                references.add(includePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error parsing references in " + csprojFile.getName());
        }
        return references;
    }

    // Method to find the "main" .csproj (the one in the same directory as Program.cs)
    private static String findMainCsproj(String rootDir) {
        File root = new File(rootDir);
        File[] programFiles = root.listFiles((dir, name) -> name.equals("Program.cs"));

        List<String> mainCsprojFiles = new ArrayList<>();

        if (programFiles != null) {
            for (File programFile : programFiles) {
                String parentDir = programFile.getParent();
                File csprojFile = new File(parentDir, programFile.getName().replace(".cs", ".csproj"));
                if (csprojFile.exists()) {
                    mainCsprojFiles.add(csprojFile.getAbsolutePath());
                }
            }
        }

        // Log all found main csproj files
        if (!mainCsprojFiles.isEmpty()) {
            System.out.println("Found main .csproj files:");
            for (String csproj : mainCsprojFiles) {
                System.out.println(" - " + new File(csproj).getName());
            }
            return mainCsprojFiles.get(0); // Return the first found one
        }

        return null;
    }

    // Method to find all .csproj files in the project
    private static void findAllCsprojFiles(File dir, List<String> csprojFiles) {
        File[] files = dir.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findAllCsprojFiles(file, csprojFiles);
                } else if (file.getName().endsWith(".csproj")) {
                    csprojFiles.add(file.getAbsolutePath());
                }
            }
        }
    }

    // Method to check if a .csproj file has any project references
    private static boolean hasReferences(String csprojPath) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(csprojPath));

            NodeList projectReferences = doc.getElementsByTagName("ProjectReference");
            return projectReferences.getLength() > 0;

        } catch (Exception e) {
            System.out.println("Error reading " + csprojPath + ": " + e.getMessage());
            return false;
        }
    }

    //main check
    public static void main(String[] args) throws IOException {
        String projectPath = "C:\\Project\\PE_PRN231_SU24_009909\\StudentSolution\\1\\vuongvtse160599\\0\\PEPRN231_SU24_009909_VoTrongVuong_BE";  // Replace with the path to your project directory

        Optional<String> appSettingsPath = findAppsettingsJsonPath(projectPath);
        //json file
        appSettingsPath.ifPresentOrElse(
                path -> System.out.println("Path to appsettings.json: " + path),
                () -> System.out.println("appsettings.json file not found.")
        );
        String solutionNamePart = "PEPRN231_SU24_009909_VoTrongVuong_BE";   //?? Replace with part of the solution name you're looking for ??
        Optional<String> solutionName = findSolutionName(projectPath, solutionNamePart);
        String path = appSettingsPath.orElseThrow(() -> new RuntimeException("Path not found"));
        analyzeAppSettings(path, "ConnectionStrings", "MyDB");
        solutionName.ifPresentOrElse(
                name -> System.out.println("Solution found: " + name),
                () -> System.out.println("No matching solution (.sln) file found.")
        );
        Optional<String> test = findSolutionName(projectPath, solutionNamePart);
        System.out.println(test.toString());
        analyzeCSharpStructure(projectPath);
        List<String> folderNames = getAllFolderNamesInSolutionDirectory(projectPath);
        if (folderNames.isEmpty()) {
            System.out.println("No folders found in the directory containing the .sln file.");
        } else {
            System.out.println("Folders found in the solution directory:");
            for (String folderName : folderNames) {
                System.out.println(folderName);
            }
        }
        Map<String, List<String>> projectReferences = getCsprojFilesAndReferences(projectPath);

        // Output each .csproj file and its references
        for (Map.Entry<String, List<String>> entry : projectReferences.entrySet()) {
            System.out.println("Project: " + entry.getKey());
            if (entry.getValue().isEmpty()) {
                System.out.println("   No references found.");
            } else {
                System.out.println("   References:");
                for (String reference : entry.getValue()) {
                    System.out.println("     - " + reference);
                }
            }
        }

    }
}
