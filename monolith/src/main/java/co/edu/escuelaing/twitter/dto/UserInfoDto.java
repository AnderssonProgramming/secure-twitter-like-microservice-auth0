package co.edu.escuelaing.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response for GET /api/me — current authenticated user profile.
 */
@Schema(description = "Current authenticated user profile")
public record UserInfoDto(

    @Schema(description = "Internal user ID", example = "1")
    Long id,

    @Schema(description = "Auth0 subject claim", example = "auth0|64abc123...")
    String auth0Sub,

    @Schema(description = "User email", example = "user@example.com")
    String email,

    @Schema(description = "Display name", example = "Andersson David")
    String name,

    @Schema(description = "Profile picture URL")
    String picture,

    @Schema(description = "Number of posts authored", example = "5")
    int postCount,

    @Schema(description = "ISO-8601 account creation timestamp")
    LocalDateTime createdAt

) {}
