package com.springboot.projects.devvvotee.Mapper;

import com.springboot.projects.devvvotee.Dto.File.FileNode;
import com.springboot.projects.devvvotee.Entity.ProjectFile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectFileMapper {
    FileNode toFileNode(ProjectFile projectFile);
}
