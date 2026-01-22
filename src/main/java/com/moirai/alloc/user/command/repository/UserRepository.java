package com.moirai.alloc.user.command.repository;

import com.moirai.alloc.user.command.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLoginId(String loginId);
    boolean existsByLoginId(String loginId);

    Optional<User> findById(Long userId);

    boolean existsByEmail(String userEmail);

    //수정 시 이메일 중복 체크
    boolean existsByEmailAndUserIdNot(String email, Long userId);

}

