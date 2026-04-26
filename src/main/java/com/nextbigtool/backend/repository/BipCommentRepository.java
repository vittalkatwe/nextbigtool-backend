package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.bip.BipComment;
import com.nextbigtool.backend.entity.bip.BuildInPublicPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BipCommentRepository extends JpaRepository<BipComment, Long> {
    List<BipComment> findByPostAndDeletedFalseOrderByCreatedAtAsc(BuildInPublicPost post);
    long countByPostAndDeletedFalse(BuildInPublicPost post);
}
