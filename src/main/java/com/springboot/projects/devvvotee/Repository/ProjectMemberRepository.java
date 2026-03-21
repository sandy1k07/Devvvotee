package com.springboot.projects.devvvotee.Repository;

import com.springboot.projects.devvvotee.Entity.ProjectMember;
import com.springboot.projects.devvvotee.Entity.ProjectMemberId;
import com.springboot.projects.devvvotee.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    List<ProjectMember> findByIdProjectId(Long projectId);

    @Query("""
            SELECT p.projectRole from ProjectMember p
            where p.id.userId = :userId and p.id.projectId = :projectId
            """)
    Optional<ProjectRole> findRoleByProjectIdAndUserId(@Param("projectId") Long projectId,
                                                       @Param("userId") Long userId);

    @Query("""
            SELECT count(p) from ProjectMember p
            where p.id.userId = :userId and p.projectRole = 'OWNER'
            """)
    int getCountOfUserOwnedProjects(Long userId);
}
