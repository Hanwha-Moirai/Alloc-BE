package com.moirai.alloc.calendar.command.repository;

import com.moirai.alloc.calendar.command.domain.entity.PublicEventsMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublicEventsMemberRepository extends JpaRepository<PublicEventsMember, Long> {
    List<PublicEventsMember> findByEventId(Long eventId);

    void deleteByEventId(Long eventId);
}
