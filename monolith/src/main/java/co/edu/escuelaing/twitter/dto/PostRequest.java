package co.edu.escuelaing.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a new post.
 */
@Schema(description = "Payload for creating a new post")
public record PostRequest(

    @NotBlank(message = "Content must not be blank")
    @Size(min = 1, max = 140, message = "Content must be between 1 and 140 characters")
    @Schema(description = "Post text content", example = "Hello, Twitter-like world!", maxLength = 140)
    String content

) {}
