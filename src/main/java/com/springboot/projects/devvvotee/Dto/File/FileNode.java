package com.springboot.projects.devvvotee.Dto.File;

import lombok.ToString;

import java.time.Instant;

public record FileNode(
        String path
) {
    @Override
    public String toString() {
        return path;
    }
}
