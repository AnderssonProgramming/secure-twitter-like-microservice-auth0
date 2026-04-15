package co.edu.escuelaing.twitter.repository;

import co.edu.escuelaing.twitter.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /** Returns all posts sorted by most recent first (for the global stream). */
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
