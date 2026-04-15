package co.edu.escuelaing.twitter.stream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AWS Lambda handler for the Stream (feed) microservice.
 *
 * Routes:
 *   GET /stream — public, returns all posts sorted by newest first
 *
 * Reads from the shared twitter-posts DynamoDB table.
 * Fully public — no JWT required.
 *
 * Lambda handler: co.edu.escuelaing.twitter.stream.StreamHandler::handleRequest
 */
public class StreamHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private static final String TABLE_NAME = System.getenv("POSTS_TABLE");

    private final DynamoDbClient dynamo;
    private final ObjectMapper   mapper;

    public StreamHandler() {
        this.dynamo = DynamoDbClient.builder()
            .region(Region.of(System.getenv().getOrDefault("AWS_REGION", "us-east-1")))
            .build();
        this.mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
            APIGatewayProxyRequestEvent event, Context context) {

        context.getLogger().log("StreamHandler: GET /stream");

        if ("OPTIONS".equals(event.getHttpMethod())) return cors200();
        if (!"GET".equals(event.getHttpMethod()))
            return response(405, "{\"error\":\"Method not allowed\"}");

        try {
            // Parse optional pagination query params
            Map<String, String> params = event.getQueryStringParameters();
            int limit = params != null && params.containsKey("limit")
                ? Math.min(Integer.parseInt(params.get("limit")), 50)
                : 20;

            // Scan all posts (for production you'd use a GSI with sort key on createdAt)
            ScanResponse scanResponse = dynamo.scan(ScanRequest.builder()
                .tableName(TABLE_NAME)
                .build());

            List<Map<String, Object>> stream = scanResponse.items().stream()
                .map(this::itemToMap)
                .sorted(Comparator.comparing(
                    m -> String.valueOf(m.getOrDefault("createdAt", "")),
                    Comparator.reverseOrder()
                ))
                .limit(limit)
                .collect(Collectors.toList());

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("items", stream);
            result.put("count", stream.size());
            result.put("total", scanResponse.count());

            return response(200, mapper.writeValueAsString(result));
        } catch (Exception e) {
            context.getLogger().log("ERROR: " + e.getMessage());
            return response(500, "{\"error\":\"Failed to load stream: " + e.getMessage() + "\"}");
        }
    }

    private Map<String, Object> itemToMap(Map<String, AttributeValue> item) {
        Map<String, Object> map = new LinkedHashMap<>();
        item.forEach((k, v) -> map.put(k, v.s()));
        return map;
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
