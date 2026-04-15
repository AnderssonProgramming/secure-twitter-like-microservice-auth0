package co.edu.escuelaing.twitter.controller;

import co.edu.escuelaing.twitter.dto.PostRequest;
import co.edu.escuelaing.twitter.dto.PostResponse;
import co.edu.escuelaing.twitter.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for post creation and public stream retrieval.
 *
 * GET  /api/posts — public, no auth required
 * POST /api/posts — protected, requires valid Auth0 JWT
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Posts", description = "Create posts and browse the public stream")
public class PostController {

    private final PostService postService;

    // ── Public ─────────────────────────────────────────────────

    @Operation(
        summary = "Get public stream",
        description = "Returns all posts ordered by most recent first. No authentication required."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Page of posts",
            content = @Content(schema = @Schema(implementation = PostResponse.class)))
    })
    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponse>> getPosts(
        @Parameter(description = "Zero-based page index", example = "0")
        @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size (max 50)", example = "20")
        @RequestParam(defaultValue = "20") int size
    ) {
        int safeSize = Math.min(size, 50);
        return ResponseEntity.ok(postService.getStream(page, safeSize));
    }

    @Operation(
        summary = "Get global stream (alias)",
        description = "Alias for GET /api/posts — returns all posts newest-first."
    )
    @GetMapping("/stream")
    public ResponseEntity<Page<PostResponse>> getStream(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return getPosts(page, size);
    }

    // ── Protected ──────────────────────────────────────────────

    @Operation(
        summary = "Create a post",
        description = "Creates a new post in the global stream. Requires a valid Auth0 JWT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Post created",
            content = @Content(schema = @Schema(implementation = PostResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error (e.g. content > 140 chars)"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(
        @Valid @RequestBody PostRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        PostResponse response = postService.createPost(request, jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
