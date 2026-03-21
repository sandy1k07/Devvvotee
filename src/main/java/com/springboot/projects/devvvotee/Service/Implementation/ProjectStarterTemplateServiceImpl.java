package com.springboot.projects.devvvotee.Service.Implementation;

import com.springboot.projects.devvvotee.Entity.Project;
import com.springboot.projects.devvvotee.Entity.ProjectFile;
import com.springboot.projects.devvvotee.ExceptionHandling.Exception.ResourceNotFoundException;
import com.springboot.projects.devvvotee.Repository.ProjectFileRepository;
import com.springboot.projects.devvvotee.Repository.ProjectRepository;
import com.springboot.projects.devvvotee.Service.ProjectStarterTemplateService;
import io.minio.*;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectStarterTemplateServiceImpl implements ProjectStarterTemplateService {

    private final MinioClient minioClient;
    private final ProjectRepository projectRepository;
    private final ProjectFileRepository projectFileRepository;

    private static final String TEMPLATE_BUCKET = "starter-projects";
    private static final String TARGET_BUCKET = "projects";
    private static final String TEMPLATE_NAME = "react-vite-tailwind-daisyui-starter-main";


    @Override
    public void initializeProjectWithTemplate(Long projectId) {
        log.info("Initializing project starter template for project {}", projectId);
        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new ResourceNotFoundException("project ", projectId.toString())
        );

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(TEMPLATE_BUCKET)
                            .prefix(TEMPLATE_NAME + "/")
                            .recursive(true)  // will search all folders and subfolders, but if false then only front layer
                            .build()
            );

            List<ProjectFile> filesToSave = new ArrayList<>(); // for metadata in pgVector

            for (Result<Item> result : results) {
                Item item = result.get();
                String sourceKey = item.objectName();

                String cleanPath = sourceKey.replaceFirst(TEMPLATE_NAME + "/", "");
                String destKey = "project_" + projectId + "/" + cleanPath;

                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(TARGET_BUCKET)
                                .object(destKey)
                                .source(
                                        CopySource.builder()
                                                .bucket(TEMPLATE_BUCKET)
                                                .object(sourceKey)
                                                .build()
                                )
                                .build()
                );

                ProjectFile pf = ProjectFile.builder()
                        .project(project)
                        .path(cleanPath)
                        .miniIoObjectKey(destKey)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

                filesToSave.add(pf);
            }
            log.info("Project template files copied to minio for projectId: {}", projectId);

            projectFileRepository.saveAll(filesToSave);
            log.info("Project template files' metadata saved to db for projectId: {}", projectId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize project from template", e);
        }
    }
}
