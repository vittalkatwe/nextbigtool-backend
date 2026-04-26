package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.bip.BuildInPublicPost;
import com.nextbigtool.backend.entity.bip.PostLike;
import com.nextbigtool.backend.entity.user.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostAndUser(BuildInPublicPost post, AppUser user);
    long countByPost(BuildInPublicPost post);
    boolean existsByPostAndUser(BuildInPublicPost post, AppUser user);
}
