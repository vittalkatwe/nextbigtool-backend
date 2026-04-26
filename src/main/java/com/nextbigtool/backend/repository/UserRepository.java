package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.entity.user.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByVerificationToken(String verificationToken);

    boolean existsByEmail(String email);
    List<AppUser> findByRole(UserRole role);
}
