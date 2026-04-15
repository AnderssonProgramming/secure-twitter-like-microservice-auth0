package co.edu.escuelaing.twitter.posts;

import co.edu.escuelaing.twitter.posts.model.Post;
import co.edu.escuelaing.twitter.posts.util.JwtValidator;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nimbusds.jwt.JWTClaimsSet;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AWS Lambda handler for the Posts microservice.
 *
 * Routes:
 *   GET  /posts          — public, lists all posts newest-first
 *   POST /posts          — protected (JWT required), creates a post
 *
 * DynamoDB table: twitter-posts
 *   pk: postId (String, UUID)
 *   Attributes: content, userId, userEmail, userName, userPicture, createdAt
 *   GSI: createdAt-index on createdAt (for sorting)
 *
 * Lambda handler: co.edu.escuelaing.twitter.posts.PostHandler::handleRequest
 */
public class PostHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME  = System.getenv("POSTS_TABLE");
    private static final String AUTH0_DOMAIN = System.getenv("AUTH0_DOMAIN");
    private static final String AUTH0_AUDIENCE = System.getenv("AUTH0_AUDIENCE");
    private static final int    MAX_CONTENT_LENGTH = 140;

    private final DynamoDbClient  dynamo;
    private final ObjectMapper    mapper;
    private final JwtValidator    jwtValidator;

    public PostHandler() {
        this.dynamo = DynamoDbClient.builder()
            .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-1")))
            .build();

        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.jwtValidator = new JwtValidator(AUTH0_DOMAIN, AUTH0_AUDIENCE);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        String method = event.getHttpMethod();
        context.getLogger().log("PostHandler: " + method + " /posts");

        try {
            return switch (method) {
                case "GET"    -> getPosts(event);
                case "POST"   -> createPost(event);
                case "OPTIONS" -> corsPreflightResponse();
                default       -> response(405, "{\"error\":\"Method not allowed\"}");
            };
        } catch (Exception e) {
            context.getLogger().log("ERROR: " + e.getMessage());
            return response(500, "{\"error\":\"Internal server error\"}");
        }
    }

    // ── GET /posts ─────────────────────────────────────────────

    private APIGatewayProxyResponseEvent getPosts(APIGatewayProxyRequestEvent event) {
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build();

            ScanResponse scanResponse = dynamo.scan(scanRequest);
            List<Map<String, Object>> posts = scanResponse.items().stream()
                .map(this::itemToMap)
                .sorted(Comparator.comparing(
                    m -> String.valueOf(m.get("createdAt")),
                    Comparator.reverseOrder()
                ))
                .collect(Collectors.toList());

            return response(200, mapper.writeValueAsString(posts));
        } catch (Exception e) {
            return response(500, "{\"error\":\"Failed to fetch posts: " + e.getMessage() + "\"}");
        }
    }

    // ── POST /posts ────────────────────────────────────────────

    private APIGatewayProxyResponseEvent createPost(APIGatewayProxyRequestEvent event) {
        // 1. Validate JWT
        JWTClaimsSet claims;
        try {
            String authHeader = Optional.ofNullable(event.getHeaders())
                .map(h -> h.get("Authorization"))
                .orElseThrow(() -> new SecurityException("Missing Authorization header"));
            claims = jwtValidator.validate(authHeader);
        } catch (Exception e) {
            return response(401, "{\"error\":\"Unauthorized: " + e.getMessage() + "\"}");
        }

        // 2. Parse body
        Map<?, ?> body;
        try {
            body = mapper.readValue(event.getBody(), Map.class);
        } catch (Exception e) {
            return response(400, "{\"error\":\"Invalid JSON body\"}");
        }

        // 3. Validate content
        String content = (String) body.get("content");
        if (content == null || content.isBlank()) {
            return response(400, "{\"error\":\"Content must not be blank\"}");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            return response(400, "{\"error\":\"Content must not exceed 140 characters\"}");
        }

        // 4. Build post
        String postId = UUID.randomUUID().toString();
        String userId = claims.getSubject();
        String email  = getStringClaim(claims, "email");
        String name   = getStringClaim(claims, "name");
        String picture = getStringClaim(claims, "picture");
        String now    = java.time.Instant.now().toString();

        // 5. Save to DynamoDB
        try {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("postId",     av(postId));
            item.put("content",    av(content));
            item.put("userId",     av(userId));
            item.put("userEmail",  av(email != null ? email : userId));
            item.put("userName",   av(name  != null ? name  : userId));
            item.put("createdAt",  av(now));
            if (picture != null) item.put("userPicture", av(picture));

            dynamo.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());

            Map<String, Object> responseBody = new LinkedHashMap<>();
            responseBody.put("postId", postId);
            responseBody.put("content", content);
            responseBody.put("userId", userId);
            responseBody.put("userEmail", email);
            responseBody.put("userName", name);
            responseBody.put("createdAt", now);

            return response(201, mapper.writeValueAsString(responseBody));
        } catch (Exception e) {
            return response(500, "{\"error\":\"Failed to save post: " + e.getMessage() + "\"}");
        }
    }

    // ── Helpers ────────────────────────────────────────────────

    private Map<String, Object> itemToMap(Map<String, AttributeValue> item) {
        Map<String, Object> map = new LinkedHashMap<>();
        item.forEach((k, v) -> map.put(k, v.s()));
        return map;
    }

    private AttributeValue av(String value) {
        return AttributeValue.builder().s(value != null ? value : "").build();
    }

    private String getStringClaim(JWTClaimsSet claims, String key) {
        try {
            return (String) claims.getClaim(key);
        } catch (Exception e) {
            return null;
        }
    }

    private APIGatewayProxyResponseEvent response(int statusCode, String body) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Headers", "Content-Type,Authorization");
        headers.put("Access-Control-Allow-Methods", "GET,POST,OPTIONS");

        return new APIGatewayProxyResponseEvent()
            .withStatusCode(statusCode)
            .withHeaders(headers)
            .withBody(body);
    }

    private APIGatewayProxyResponseEvent corsPreflightResponse() {
        return response(200, "{}");
    }
}
