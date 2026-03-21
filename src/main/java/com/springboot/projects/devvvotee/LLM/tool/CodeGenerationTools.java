package com.springboot.projects.devvvotee.LLM.tool;

import com.springboot.projects.devvvotee.Dto.File.FileContentResponse;
import com.springboot.projects.devvvotee.Service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class CodeGenerationTools {

    private final ProjectFileService projectFileService;
    private final Long projectId;

    @Tool(
            name = "read_project_files",
            description = """
                    Read the content of files.  Only input the file names present inside the FILE_TREE. 
                    DO NOT input any path which is not present under the FILE_TREE.
                    """
    )
    public List<String> readProjectFiles(
            @ToolParam(description = "List of relative paths (eg: ['/src/App.tsx'])")
            List<String> filePaths
    ){
        List<String> result = new ArrayList<>();
        log.info("read_project_files tool called for projectId: {}", projectId);

        for(String path : filePaths){
            String cleanPath = path.startsWith("/") ? path.substring(1) : path;
            String content = projectFileService.getFileContent(projectId, cleanPath).content();
            log.info("reading project files for {}/{}", projectId, cleanPath);
            result.add(String.format(
                    "--- START OF FILE: %s ---\n%s\n--- END OF FILE ---", path, content
            ));
        }

        log.info("read_project_files tool returning response for projectId: {}", projectId);
        return result;
    }
}
