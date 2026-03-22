package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.File.FileContentResponse;
import com.springboot.projects.devvvotee.Dto.File.FileNode;
import com.springboot.projects.devvvotee.Dto.File.FileTreeResponse;

import java.util.List;

public interface ProjectFileService {
    FileTreeResponse getFileTree(Long projectId);

    FileContentResponse getFileContent(Long projectId, String path);

    void saveFile(Long projectId, String filePath, String fileContent);
}
