package com.moirai.alloc.management.domain.repo;

import com.moirai.alloc.project.command.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project,Long> {
}
