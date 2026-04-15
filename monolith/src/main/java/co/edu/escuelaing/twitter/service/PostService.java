package co.edu.escuelaing.twitter.service;

import co.edu.escuelaing.twitter.dto.PostRequest;
import co.edu.escuelaing.twitter.dto.PostResponse;
import co.edu.escuelaing.twitter.entity.Post;
import co.edu.escuelaing.twitter.entity.User;
import co.edu.escuelaing.twitter.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for creating posts and retrieving the global stream.
 */
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserService    userService;

    /**
     * Creates a new post attributed to the JWT owner.
     * Automatically syncs the user record from Auth0 claims.
     */
    @Transactional
    public PostResponse createPost(PostRequest request, Jwt jwt) {
        User author = userService.syncUser(jwt);
        Post post = Post.builder()
            .content(request.content())
            .author(author)
            .build();
        return toResponse(postRepository.save(post));
    }

    /**
     * Returns a paginated list of all posts, newest first.
     * This endpoint is public — no authentication required.
     */
    @Transactional(readOnly = true)
    public Page<PostResponse> getStream(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(this::toResponse);
    }

    private PostResponse toResponse(Post post) {
        User author = post.getAuthor();
        return new PostResponse(
            post.getId(),
            post.getContent(),
            author.getName(),
            author.getEmail(),
            author.getPicture(),
            post.getCreatedAt()
        );
    }
}
