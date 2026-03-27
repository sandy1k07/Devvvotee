package com.springboot.projects.devvvotee.Service;

import com.springboot.projects.devvvotee.Dto.deploy.DeploymentResponse;

public interface DeploymentService {

    DeploymentResponse deploy(Long projectId);
}
