package co.edu.escuelaing.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response payload representing a post in the public stream.
 */
@Schema(description = "A post in the public stream")
public record PostResponse(

    @Schema(description = "Unique post ID", example = "1")
    Long id,

    @Schema(description = "Post text content", example = "Hello from Auth0!")
    String content,

    @Schema(description = "Author display name", example = "Andersson David")
    String authorName,

    @Schema(description = "Author email", example = "user@example.com")
    String authorEmail,

    @Schema(description = "Author profile picture URL")
    String authorPicture,

    @Schema(description = "ISO-8601 creation timestamp")
    LocalDateTime createdAt

) {}
