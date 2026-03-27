package com.springboot.projects.devvvotee.Controller;

import com.springboot.projects.devvvotee.Dto.Project.ProjectRequest;
import com.springboot.projects.devvvotee.Dto.Project.ProjectResponse;
import com.springboot.projects.devvvotee.Dto.Project.ProjectSummaryResponse;
import com.springboot.projects.devvvotee.Dto.deploy.DeploymentResponse;
import com.springboot.projects.devvvotee.Service.DeploymentService;
import com.springboot.projects.devvvotee.Service.Implementation.DeploymentServiceImpl;
import com.springboot.projects.devvvotee.Service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final DeploymentService deploymentService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getMyProjects(){
        log.info("Request to get all projects received at controller");
        return ResponseEntity.ok(projectService.getProjects());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable(name = "id") Long projectId){
        return ResponseEntity.ok(projectService.getUserProjectById(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable(name = "id") Long projectId, @Valid @RequestBody ProjectRequest request){
        return ResponseEntity.ok(projectService.updateProject(projectId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable(name = "id") Long projectId){
        projectService.softDelete(projectId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<DeploymentResponse> deployProject(@PathVariable(name = "id") Long projectId){
        return ResponseEntity.ok(deploymentService.deploy(projectId));
    }
}
