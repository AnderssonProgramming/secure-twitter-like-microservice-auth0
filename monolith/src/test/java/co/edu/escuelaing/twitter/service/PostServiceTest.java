package co.edu.escuelaing.twitter.service;

import co.edu.escuelaing.twitter.dto.PostRequest;
import co.edu.escuelaing.twitter.dto.PostResponse;
import co.edu.escuelaing.twitter.entity.Post;
import co.edu.escuelaing.twitter.entity.User;
import co.edu.escuelaing.twitter.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService unit tests")
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PostService postService;

    private User mockUser;
    private Jwt  mockJwt;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
            .id(1L)
            .auth0Sub("auth0|test123")
            .email("test@example.com")
            .name("Test User")
            .build();

        mockJwt = Jwt.withTokenValue("mock-token")
            .header("alg", "RS256")
            .claim("sub", "auth0|test123")
            .claim("email", "test@example.com")
            .claim("name", "Test User")
            .build();
    }

    @Test
    @DisplayName("createPost — stores post and returns response with author info")
    void createPost_storesAndReturnsResponse() {
        PostRequest request = new PostRequest("Hello, world!");
        Post savedPost = Post.builder()
            .id(1L)
            .content("Hello, world!")
            .author(mockUser)
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.syncUser(mockJwt)).thenReturn(mockUser);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        PostResponse response = postService.createPost(request, mockJwt);

        assertThat(response.content()).isEqualTo("Hello, world!");
        assertThat(response.authorEmail()).isEqualTo("test@example.com");
        assertThat(response.authorName()).isEqualTo("Test User");
        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getStream — returns page of posts newest first")
    void getStream_returnsPaginatedPosts() {
        Post post = Post.builder()
            .id(1L)
            .content("Stream post")
            .author(mockUser)
            .createdAt(LocalDateTime.now())
            .build();

        Page<Post> postPage = new PageImpl<>(List.of(post), PageRequest.of(0, 20), 1);
        when(postRepository.findAllByOrderByCreatedAtDesc(any())).thenReturn(postPage);

        Page<PostResponse> result = postService.getStream(0, 20);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).content()).isEqualTo("Stream post");
    }

    @Test
    @DisplayName("createPost — 140-char content is accepted")
    void createPost_maxLength_accepted() {
        String maxContent = "A".repeat(140);
        PostRequest request = new PostRequest(maxContent);
        Post savedPost = Post.builder()
            .id(2L)
            .content(maxContent)
            .author(mockUser)
            .createdAt(LocalDateTime.now())
            .build();

        when(userService.syncUser(mockJwt)).thenReturn(mockUser);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        PostResponse response = postService.createPost(request, mockJwt);
        assertThat(response.content()).hasSize(140);
    }
}
