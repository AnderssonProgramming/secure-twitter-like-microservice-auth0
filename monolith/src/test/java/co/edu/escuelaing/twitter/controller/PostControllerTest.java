package co.edu.escuelaing.twitter.controller;

import co.edu.escuelaing.twitter.config.SecurityConfig;
import co.edu.escuelaing.twitter.dto.PostRequest;
import co.edu.escuelaing.twitter.dto.PostResponse;
import co.edu.escuelaing.twitter.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
@DisplayName("PostController integration tests")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    private PostResponse sampleResponse() {
        return new PostResponse(1L, "Hello!", "Test User", "test@example.com", null, LocalDateTime.now());
    }

    // ── GET /api/posts — public ─────────────────────────────

    @Test
    @DisplayName("GET /api/posts — returns 200 without authentication")
    void getPostsPublic_returns200() throws Exception {
        when(postService.getStream(anyInt(), anyInt()))
            .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].content").value("Hello!"));
    }

    @Test
    @DisplayName("GET /api/stream — alias returns 200 without authentication")
    void getStreamPublic_returns200() throws Exception {
        when(postService.getStream(anyInt(), anyInt()))
            .thenReturn(new PageImpl<>(List.of(sampleResponse())));

        mockMvc.perform(get("/api/stream"))
            .andExpect(status().isOk());
    }

    // ── POST /api/posts — protected ─────────────────────────

    @Test
    @DisplayName("POST /api/posts — returns 201 with valid JWT")
    void createPost_withJwt_returns201() throws Exception {
        when(postService.createPost(any(), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/posts")
                .with(jwt().jwt(j -> j.subject("auth0|test").claim("email", "test@example.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PostRequest("Hello!"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.content").value("Hello!"));
    }

    @Test
    @DisplayName("POST /api/posts — returns 401 without JWT")
    void createPost_withoutJwt_returns401() throws Exception {
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PostRequest("Hello!"))))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/posts — returns 400 when content exceeds 140 chars")
    void createPost_tooLong_returns400() throws Exception {
        String tooLong = "X".repeat(141);

        mockMvc.perform(post("/api/posts")
                .with(jwt().jwt(j -> j.subject("auth0|test")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PostRequest(tooLong))))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/posts — returns 400 when content is blank")
    void createPost_blank_returns400() throws Exception {
        mockMvc.perform(post("/api/posts")
                .with(jwt().jwt(j -> j.subject("auth0|test")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PostRequest(""))))
            .andExpect(status().isBadRequest());
    }
}
