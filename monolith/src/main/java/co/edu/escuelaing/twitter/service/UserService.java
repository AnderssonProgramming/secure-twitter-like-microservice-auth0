package co.edu.escuelaing.twitter.service;

import co.edu.escuelaing.twitter.dto.UserInfoDto;
import co.edu.escuelaing.twitter.entity.User;
import co.edu.escuelaing.twitter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages user synchronisation from Auth0 JWT claims and profile retrieval.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Finds or creates the local User record from the Auth0 JWT.
     * Called automatically on every authenticated request so the DB stays
     * in sync with the Auth0 user profile.
     */
    @Transactional
    public User syncUser(Jwt jwt) {
        String sub   = jwt.getSubject();
        String email = jwt.getClaimAsString("email");
        String name  = jwt.getClaimAsString("name");
        String pic   = jwt.getClaimAsString("picture");

        return userRepository.findByAuth0Sub(sub)
            .map(existing -> {
                existing.setEmail(email != null ? email : existing.getEmail());
                existing.setName(name   != null ? name  : existing.getName());
                existing.setPicture(pic != null ? pic   : existing.getPicture());
                return userRepository.save(existing);
            })
            .orElseGet(() -> userRepository.save(
                User.builder()
                    .auth0Sub(sub)
                    .email(email != null ? email : sub)
                    .name(name   != null ? name  : sub)
                    .picture(pic)
                    .build()
            ));
    }

    /**
     * Returns the profile of the currently authenticated user as a DTO.
     */
    @Transactional(readOnly = true)
    public UserInfoDto getCurrentUserProfile(Jwt jwt) {
        User user = syncUser(jwt);
        return toDto(user);
    }

    private UserInfoDto toDto(User user) {
        return new UserInfoDto(
            user.getId(),
            user.getAuth0Sub(),
            user.getEmail(),
            user.getName(),
            user.getPicture(),
            user.getPosts() == null ? 0 : user.getPosts().size(),
            user.getCreatedAt()
        );
    }
}
