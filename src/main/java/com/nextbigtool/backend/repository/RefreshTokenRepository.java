package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.entity.user.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserAndRevokedFalse(AppUser user);

    void deleteByUser(AppUser user);
}