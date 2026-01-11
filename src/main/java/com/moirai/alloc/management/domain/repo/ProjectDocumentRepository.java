package com.moirai.alloc.management.domain.repo;

import com.moirai.alloc.management.domain.entity.SquadAssignment;
import org.springframework.data.repository.CrudRepository;

public interface ProjectDocumentRepository extends CrudRepository<SquadAssignment, Long> {
}
