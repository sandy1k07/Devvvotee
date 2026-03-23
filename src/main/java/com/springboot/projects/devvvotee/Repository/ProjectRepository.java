package com.springboot.projects.devvvotee.Repository;

import com.springboot.projects.devvvotee.Dto.Project.ProjectWithRole;
import com.springboot.projects.devvvotee.Entity.Project;
import com.springboot.projects.devvvotee.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {


    @Query("""
            SELECT p as project, pm.projectRole as projectRole
            from Project p
            JOIN ProjectMember pm on pm.project.id = p.id
            where p.deletedAt is NULL
            and pm.user.id = :userId
            ORDER BY p.createdAt DESC
            """
    )
    List<ProjectWithRole> findProjectsAccessibleByUser(@Param("userId") Long userId);

    @Query("""
            SELECT p from Project p
            where p.id = :projectId
            and EXISTS(
                SELECT 1 from ProjectMember m
                where m.id.projectId = :projectId and m.id.userId = :userId
            )
            and p.deletedAt is NULL
            """)
    Optional<Project> findAccessibleProjectById(@Param("projectId") Long projectId, @Param("userId") Long userId);

    @Query("""
            SELECT p as project, pm.projectRole as projectRole
            from Project p
            JOIN ProjectMember pm on p.id = pm.project.id
            where p.id = :projectId
            and pm.user.id = :userId
            and p.deletedAt is NULL
            """)
    Optional<ProjectWithRole> findAccessibleProjectByIdWithRole(@Param("projectId") Long projectId, @Param("userId") Long userId);


}
