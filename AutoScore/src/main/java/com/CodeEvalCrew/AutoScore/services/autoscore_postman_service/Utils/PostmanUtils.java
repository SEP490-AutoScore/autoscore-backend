package com.CodeEvalCrew.AutoScore.services.autoscore_postman_service.Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class PostmanUtils {

    public static int BASE_PORT = 10000;

    public static Map.Entry<Path, String> findCsprojAndDotnetVersion(Path dirPath) throws IOException {
        Pattern pattern = Pattern.compile("<TargetFramework>(net\\d+\\.\\d+)</TargetFramework>");
        Pattern beFolderPattern = Pattern.compile(".*_BE$");

        try (Stream<Path> folders = Files.walk(dirPath, 1)) {
            Optional<Path> beFolder = folders
                    .filter(Files::isDirectory)
                    .filter(path -> beFolderPattern.matcher(path.getFileName().toString()).matches())
                    .findFirst();

            if (beFolder.isPresent()) {
                try (Stream<Path> paths = Files.walk(beFolder.get())) {
                    for (Path path : paths.filter(Files::isRegularFile).collect(Collectors.toList())) {
                        if (path.toString().endsWith(".csproj")) {
                            List<String> lines = Files.readAllLines(path);
                            for (String line : lines) {
                                Matcher matcher = pattern.matcher(line);
                                if (matcher.find()) {
                                    String dotnetVersion = matcher.group(1).replace("net", "");
                                    return new AbstractMap.SimpleEntry<>(path, dotnetVersion);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void createDockerfile(Path dirPath, Path csprojPath, String dotnetVersion, int port) throws IOException {
        String csprojName = csprojPath.getFileName().toString();
        String folderName = csprojPath.getParent().getFileName().toString();

        String dockerfileContent = String.format("""
                FROM mcr.microsoft.com/dotnet/aspnet:%s AS base
                WORKDIR /app
                EXPOSE %d

                FROM mcr.microsoft.com/dotnet/sdk:%s AS build
                WORKDIR /src
                COPY ["%s/%s", "./"]
                RUN dotnet restore "./%s"
                COPY . .
                WORKDIR "/src/."
                RUN dotnet build "%s/%s" -c Release -o /app/build

                FROM build AS publish
                RUN dotnet publish "%s/%s" -c Release -o /app/publish

                FROM base AS final
                WORKDIR /app
                COPY --from=publish /app/publish .
                ENTRYPOINT ["dotnet", "%s"]
                """, dotnetVersion, port, dotnetVersion, folderName, csprojName, csprojName, folderName, csprojName, folderName, csprojName, csprojName.replace(".csproj", ".dll"));

        try (BufferedWriter writer = Files.newBufferedWriter(dirPath.resolve("Dockerfile"))) {
            writer.write(dockerfileContent);
        }
    }

    public static void createDockerCompose(Path dirPath, Long studentId, int port) throws IOException {
        String dockerComposeContent = String.format("""
                services:
                  project-studentid-%d-%d:
                    image: project-studentid-%d-%d
                    build:
                      context: .
                      dockerfile: Dockerfile
                    ports:
                      - "%d:%d"
                """, studentId, port, studentId, port, port, port);

        try (BufferedWriter writer = Files.newBufferedWriter(dirPath.resolve("docker-compose.yml"))) {
            writer.write(dockerComposeContent);
        }
    }

    public static void updateAppsettingsJson(Path filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode rootNode = (ObjectNode) objectMapper.readTree(Files.newBufferedReader(filePath, StandardCharsets.UTF_8));

        if (rootNode.has("ConnectionStrings")) {
            ObjectNode connectionStringsNode = (ObjectNode) rootNode.get("ConnectionStrings");
            connectionStringsNode.fieldNames().forEachRemaining(key -> {
                connectionStringsNode.put(key, String.join(";",
                        "Server=192.168.2.16\\SQLEXPRESS",
                        "uid=sa",
                        "pwd=1234567890",
                        "database=EnglishPremierLeague2024DB",
                        "TrustServerCertificate=True"
                ));
            });
        }

        if (rootNode.has("Kestrel")) {
            ObjectNode kestrelNode = (ObjectNode) rootNode.get("Kestrel").get("Endpoints").get("Http");
            String oldUrl = kestrelNode.get("Url").asText();
            String newUrl = oldUrl.replace("{PORT}", "8080");
            kestrelNode.put("Url", newUrl);
        }

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(Files.newBufferedWriter(filePath, StandardCharsets.UTF_8), rootNode);
    }

    public static void findAndUpdateAppsettingsInBE(Path dirPath) throws IOException {
        try (var paths = Files.walk(dirPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith("appsettings.json"))
                    .forEach(path -> {
                        try {
                            updateAppsettingsJson(path);
                        } catch (IOException e) {
                            System.err.println("Error updating: " + path + " - " + e.getMessage());
                        }
                    });
        }
    }

    public static void removeDockerFiles(Path dirPath) throws IOException {
        Files.deleteIfExists(dirPath.resolve("Dockerfile"));
        Files.deleteIfExists(dirPath.resolve("docker-compose.yml"));
    }
}
