package com.moirai.alloc.user.command.repository;

import com.moirai.alloc.user.command.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
