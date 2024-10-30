package com.CodeEvalCrew.AutoScore.services.autoscore_postman_service.Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoscoreInitUtils {

 
    public static int BASE_PORT = 10000;

    public static Map.Entry<Path, String> findCsprojAndDotnetVersion(Path dirPath) throws IOException {
        Pattern pattern = Pattern.compile("<TargetFramework>(net\\d+\\.\\d+)</TargetFramework>");

        try (Stream<Path> folders = Files.walk(dirPath, 1)) {
            // Find directories that contain a Program.cs file
            Optional<Path> targetDir = folders
                    .filter(Files::isDirectory)
                    .filter(path -> {
                        try (Stream<Path> files = Files.walk(path, 1)) {
                            return files.anyMatch(file -> file.getFileName().toString().equalsIgnoreCase("Program.cs"));
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .findFirst();

            if (targetDir.isPresent()) {
                try (Stream<Path> paths = Files.walk(targetDir.get())) {
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


    public static void removeDockerFiles(Path dirPath) throws IOException {
        Files.deleteIfExists(dirPath.resolve("Dockerfile"));
        Files.deleteIfExists(dirPath.resolve("docker-compose.yml"));
    }

    public static void deleteAllFilesAndFolders(String directoryPath) {
        Path directory = Paths.get(directoryPath);

        try {
            // Duyệt qua tất cả các tệp và thư mục con
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // Xóa từng tệp
                    Files.delete(file);
                    System.out.println("Deleted file: " + file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    // Xóa thư mục sau khi đã xóa hết các tệp bên trong
                    Files.delete(dir);
                    System.out.println("Deleted directory: " + dir);
                    return FileVisitResult.CONTINUE;
                }
            });

            System.out.println("All files and folders deleted successfully.");
        } catch (IOException e) {
            System.err.println("Error while deleting files and folders: " + e.getMessage());
        }
    }

}
