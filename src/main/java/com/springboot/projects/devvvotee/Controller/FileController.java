package com.springboot.projects.devvvotee.Controller;

import com.springboot.projects.devvvotee.Dto.File.FileContentResponse;
import com.springboot.projects.devvvotee.Dto.File.FileNode;
import com.springboot.projects.devvvotee.Dto.File.FileTreeResponse;
import com.springboot.projects.devvvotee.Service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/files")
@Slf4j
public class FileController {
    private final ProjectFileService projectFileService;

    @GetMapping
    public ResponseEntity<FileTreeResponse> getFileTree(@PathVariable("projectId") Long projectId){
        Long userId = 1L;
        return ResponseEntity.ok(projectFileService.getFileTree(projectId));
    }

    @GetMapping("/content")
    public ResponseEntity<FileContentResponse> getFile(
            @PathVariable Long projectId,
        @RequestParam String path
    ){
        log.info("File request received at FIleController for project {} path {}", projectId, path);
        Long userId = 1L;
        return ResponseEntity.ok(projectFileService.getFileContent(projectId, path));
    }
}
