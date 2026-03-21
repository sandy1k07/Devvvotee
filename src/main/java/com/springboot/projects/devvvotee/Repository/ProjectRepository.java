package com.springboot.projects.devvvotee.Repository;

import com.springboot.projects.devvvotee.Entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {


    @Query("""
            SELECT p from Project p
            where p.deletedAt is NULL
            and EXISTS(
                SELECT 1 from ProjectMember m
                where m.id.projectId = p.id and m.id.userId = :userId
            )
            ORDER BY p.createdAt DESC
            """
    )
    List<Project> findProjectsAccessibleByUser(@Param("userId") Long userId);

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

}
