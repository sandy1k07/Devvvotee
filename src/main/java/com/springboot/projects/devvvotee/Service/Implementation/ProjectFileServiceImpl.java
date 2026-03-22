package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Dto.File.FileContentResponse;
import com.springboot.projects.devvvotee.Dto.File.FileNode;
import com.springboot.projects.devvvotee.Dto.File.FileTreeResponse;
import com.springboot.projects.devvvotee.Entity.Project;
import com.springboot.projects.devvvotee.Entity.ProjectFile;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.BadRequestException;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.ResourceNotFoundException;
import com.springboot.projects.devvvotee.Mapper.FileContentResponseMapper;
import com.springboot.projects.devvvotee.Mapper.ProjectFileMapper;
import com.springboot.projects.devvvotee.Repository.ProjectFileRepository;
import com.springboot.projects.devvvotee.Repository.ProjectRepository;
import com.springboot.projects.devvvotee.Service.ProjectFileService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectFileServiceImpl implements ProjectFileService {

    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;
    private final MinioClient minioClient;
    private final ProjectFileMapper projectFileMapper;

    @Value("${minio.project-bucket}")
    private String projectBucket;

    @Override
    public FileTreeResponse getFileTree(Long projectId) {

        List<ProjectFile> projectFileList = projectFileRepository.findByProjectId(projectId);
        return new FileTreeResponse(
                projectFileList.stream()
                        .map(projectFileMapper::toFileNode)
                        .toList()
        );
    }

    @Override
    public FileContentResponse getFileContent(Long projectId, String path) {
        String objectName =  "project_" + projectId + "/" + path;

        try(
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(projectBucket)
                            .object(objectName)
                            .build()
            )){
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return new FileContentResponse(path, content);
        } catch (Exception e) {
            log.error("Failed to read file content of {}/{}", projectId, path);
            throw new RuntimeException("Failed to read file content" + e.getMessage());
        }
    }

    @Override
    public void saveFile(Long projectId, String filePath, String fileContent) {
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("Project ", projectId.toString())
        );

        String cleanPath = filePath.startsWith("/") ? filePath.substring(1) : filePath;
        String objectKey = "project_" + projectId + "/" + cleanPath;
        log.info("Saving file content of {}/{}", projectId, cleanPath);

        try{
            byte[] bytes = fileContent.getBytes(StandardCharsets.UTF_8);   // for image support
            InputStream inputStream = new ByteArrayInputStream(bytes);
            minioClient.putObject(   // put object argument to be provided (from minio docs)
                    PutObjectArgs.builder()
                            .bucket(projectBucket)
                            .object(objectKey)
                            .stream(inputStream, bytes.length, -1)
                            .contentType(getContentType(cleanPath))
                            .build()
            );
            log.info("File content saved to minIo of {}/{}", projectId, cleanPath);

            ProjectFile file = projectFileRepository.findByProjectIdAndPath(projectId, cleanPath)
                    .orElseGet(() -> ProjectFile.builder()
                            .project(project)
                            .path(cleanPath)
                            .miniIoObjectKey(objectKey)
                            .createdAt(Instant.now())
                            .build()
                    );

            file.setUpdatedAt(Instant.now());
            projectFileRepository.save(file);
            log.info("File Metadata saved to db of {}/{}", projectId, cleanPath);
            log.info("Project file has been saved successfully : {}", objectKey);
        } catch (Exception e) {
            log.error("Failed to save file {}/{}", projectId, cleanPath, e);
            throw new RuntimeException("Failed to save file project_" + projectId + "/" + cleanPath + " : msg: " + e);
        }
    }

    private String getContentType(String filePath) {
        String type = URLConnection.guessContentTypeFromName(filePath);
        if(type != null) return type;
        else if(filePath.endsWith(".jsx") || filePath.endsWith(".tsx") || filePath.endsWith(".ts")) return "text/javascript";
        else if(filePath.endsWith("/json")) return "application/json";
        else if(filePath.endsWith(".css")) return "text/css";
        else return "text/plain";
    }
}
