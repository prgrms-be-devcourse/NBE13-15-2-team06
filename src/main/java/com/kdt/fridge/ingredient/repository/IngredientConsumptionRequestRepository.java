package com.kdt.fridge.ingredient.repository;

import com.kdt.fridge.ingredient.dto.IngredientResponse;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.json.JsonMapper;

@Repository
public class IngredientConsumptionRequestRepository {

    private final JdbcTemplate jdbcTemplate;
    private final JsonMapper jsonMapper;

    public IngredientConsumptionRequestRepository(JdbcTemplate jdbcTemplate, JsonMapper jsonMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.jsonMapper = jsonMapper;
    }

    public Optional<CompletedRequest> find(long groupId, long ingredientId, long userId, String key) {
        return jdbcTemplate.query("""
                SELECT requested_quantity, response_body
                FROM ingredient_consumption_requests
                WHERE group_id = ? AND ingredient_id = ? AND user_id = ? AND request_key = ?
                """,
                (rs, rowNumber) -> new CompletedRequest(
                        rs.getLong("requested_quantity"),
                        jsonMapper.readValue(rs.getString("response_body"), IngredientResponse.class)
                ),
                groupId, ingredientId, userId, key
        ).stream().findFirst();
    }

    public void save(
            long groupId, long ingredientId, long userId, String key,
            long requestedQuantity, IngredientResponse response
    ) {
        jdbcTemplate.update("""
                INSERT INTO ingredient_consumption_requests
                    (group_id, ingredient_id, user_id, request_key, requested_quantity, response_body)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                groupId, ingredientId, userId, key, requestedQuantity, jsonMapper.writeValueAsString(response)
        );
    }

    public record CompletedRequest(long requestedQuantity, IngredientResponse response) {
    }
}
