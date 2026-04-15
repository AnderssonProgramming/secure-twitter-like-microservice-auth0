package co.edu.escuelaing.twitter.users;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.net.URL;
import java.time.Instant;
import java.util.*;

/**
 * AWS Lambda handler for the Users microservice.
 *
 * Routes:
 *   GET /me   — protected, returns/syncs the current user profile
 *
 * DynamoDB table: twitter-users
 *   pk: auth0Sub (String)
 *   Attributes: email, name, picture, createdAt
 *
 * Lambda handler: co.edu.escuelaing.twitter.users.UserHandler::handleRequest
 */
public class UserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME    = System.getenv("USERS_TABLE");
    private static final String AUTH0_DOMAIN  = System.getenv("AUTH0_DOMAIN");
    private static final String AUTH0_AUDIENCE = System.getenv("AUTH0_AUDIENCE");

    private final DynamoDbClient dynamo;
    private final ObjectMapper   mapper;
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private final String issuer;

    public UserHandler() {
        this.dynamo = DynamoDbClient.builder()
            .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-1")))
            .build();
        this.mapper = new ObjectMapper();
        this.issuer = "https://" + AUTH0_DOMAIN + "/";

        try {
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(
                new URL("https://" + AUTH0_DOMAIN + "/.well-known/jwks.json")
            );
            this.jwtProcessor = new DefaultJWTProcessor<>();
            this.jwtProcessor.setJWSKeySelector(
                new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, keySource)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to init JWT processor", e);
        }
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        String method = event.getHttpMethod();
        context.getLogger().log("UserHandler: " + method);

        if ("OPTIONS".equals(method)) return cors200();

        // All user endpoints require authentication
        JWTClaimsSet claims;
        try {
            Map<String, String> headers = Optional.ofNullable(event.getHeaders())
                .orElse(Collections.emptyMap());

            String auth = Optional.ofNullable(headers.get("Authorization"))
                .orElse(headers.get("authorization"));

            if (auth == null || auth.isBlank()) {
                throw new SecurityException("Missing Authorization header");
            }

            String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
            claims = jwtProcessor.process(token, null);

            if (!issuer.equals(claims.getIssuer()))
                throw new SecurityException("Invalid issuer");
            if (claims.getAudience() == null || !claims.getAudience().contains(AUTH0_AUDIENCE))
                throw new SecurityException("Invalid audience");

        } catch (Exception e) {
            return response(401, "{\"error\":\"Unauthorized: " + e.getMessage() + "\"}");
        }

        try {
            if ("GET".equals(method)) return getOrCreateUser(claims);
            return response(405, "{\"error\":\"Method not allowed\"}");
        } catch (Exception e) {
            context.getLogger().log("ERROR: " + e.getMessage());
            return response(500, "{\"error\":\"Internal server error\"}");
        }
    }

    private APIGatewayProxyResponseEvent getOrCreateUser(JWTClaimsSet claims) throws Exception {
        String sub     = claims.getSubject();
        String email   = (String) claims.getClaim("email");
        String name    = (String) claims.getClaim("name");
        String picture = (String) claims.getClaim("picture");

        // Try to get existing user
        GetItemResponse getResp = dynamo.getItem(GetItemRequest.builder()
            .tableName(TABLE_NAME)
            .key(Map.of("auth0Sub", AttributeValue.builder().s(sub).build()))
            .build());

        Map<String, AttributeValue> item;
        if (getResp.hasItem()) {
            // Update profile in case it changed in Auth0
            item = new HashMap<>(getResp.item());
            if (email   != null) item.put("email",   av(email));
            if (name    != null) item.put("name",    av(name));
            if (picture != null) item.put("picture", av(picture));
        } else {
            // First time — create
            item = new HashMap<>();
            item.put("auth0Sub", av(sub));
            item.put("email",    av(email   != null ? email   : sub));
            item.put("name",     av(name    != null ? name    : sub));
            item.put("createdAt", av(Instant.now().toString()));
            if (picture != null) item.put("picture", av(picture));
        }

        dynamo.putItem(PutItemRequest.builder()
            .tableName(TABLE_NAME)
            .item(item)
            .build());

        Map<String, String> responseBody = new LinkedHashMap<>();
        item.forEach((k, v) -> responseBody.put(k, v.s()));

        return response(200, mapper.writeValueAsString(responseBody));
    }

    private AttributeValue av(String v) {
        return AttributeValue.builder().s(v != null ? v : "").build();
    }

    private APIGatewayProxyResponseEvent response(int code, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,Authorization");
        headers.put("Access-Control-Allow-Methods", "GET,OPTIONS");
        return new APIGatewayProxyResponseEvent()
            .withStatusCode(code).withHeaders(headers).withBody(body);
    }

    private APIGatewayProxyResponseEvent cors200() {
        return response(200, "{}");
    }
}
