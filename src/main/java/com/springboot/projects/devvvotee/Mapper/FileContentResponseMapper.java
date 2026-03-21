package com.springboot.projects.devvvotee.Mapper;

import com.springboot.projects.devvvotee.Dto.File.FileContentResponse;
import com.springboot.projects.devvvotee.Entity.ProjectFile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FileContentResponseMapper {

    @Mapping(target = "path", source = "path")
    FileContentResponse toFileContentResponseFromProjectFile(ProjectFile projectFile, String path);
}
