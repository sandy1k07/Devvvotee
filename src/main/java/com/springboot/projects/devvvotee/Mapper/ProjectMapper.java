package com.springboot.projects.devvvotee.Mapper;

import com.springboot.projects.devvvotee.Dto.Project.ProjectResponse;
import com.springboot.projects.devvvotee.Dto.Project.ProjectSummaryResponse;
import com.springboot.projects.devvvotee.Entity.Project;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);

    ProjectSummaryResponse toProjectSummaryResponseList(Project project);

    List<ProjectSummaryResponse> toProjectSummaryResponseList(List<Project> projects);
}
