package co.edu.escuelaing.twitter.posts.util;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import java.net.URL;
import java.util.List;

/**
 * Validates Auth0 JWT tokens using the tenant's JWKS endpoint.
 * Used by all Lambda functions to authenticate protected operations.
 */
public class JwtValidator {

    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final String audience;
    private final String issuer;

    public JwtValidator(String auth0Domain, String audience) {
        this.audience = audience;
        this.issuer   = "https://" + auth0Domain + "/";

        try {
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(
                new URL("https://" + auth0Domain + "/.well-known/jwks.json")
            );
            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize JWT validator: " + e.getMessage(), e);
        }
    }

    /**
     * Validates the token and returns the claims set.
     *
     * @param bearerToken  The raw "Bearer xxx" or just the token value
     * @return             Validated JWT claims
     * @throws Exception   If the token is invalid, expired, or has wrong audience/issuer
     */
    public JWTClaimsSet validate(String bearerToken) throws Exception {
        String token = bearerToken.startsWith("Bearer ")
            ? bearerToken.substring(7)
            : bearerToken;

        JWTClaimsSet claims = jwtProcessor.process(token, null);

        // Validate issuer
        if (!issuer.equals(claims.getIssuer())) {
            throw new Exception("Invalid issuer: " + claims.getIssuer());
        }

        // Validate audience
        List<String> aud = claims.getAudience();
        if (aud == null || !aud.contains(audience)) {
            throw new Exception("Invalid audience");
        }

        return claims;
    }
}
