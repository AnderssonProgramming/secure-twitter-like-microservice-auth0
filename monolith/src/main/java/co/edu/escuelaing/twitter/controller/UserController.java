package co.edu.escuelaing.twitter.controller;

import co.edu.escuelaing.twitter.dto.UserInfoDto;
import co.edu.escuelaing.twitter.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing the current authenticated user's profile.
 *
 * GET /api/me — protected, requires valid Auth0 JWT
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Current user profile")
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "Get current user profile",
        description = "Returns the profile of the currently authenticated user, " +
                      "derived from the Auth0 JWT claims. " +
                      "Creates the user record on first access.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User profile",
            content = @Content(schema = @Schema(implementation = UserInfoDto.class))),
        @ApiResponse(responseCode = "401", description = "Missing or invalid JWT")
    })
    @GetMapping("/me")
    public ResponseEntity<UserInfoDto> getMe(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getCurrentUserProfile(jwt));
    }
}
