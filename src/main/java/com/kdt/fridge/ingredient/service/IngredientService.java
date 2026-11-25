package com.kdt.fridge.ingredient.service;

import com.kdt.fridge.ingredient.dto.IngredientCreateRequest;
import com.kdt.fridge.ingredient.dto.IngredientResponse;
import com.kdt.fridge.ingredient.entity.Ingredient;
import com.kdt.fridge.ingredient.repository.IngredientRepository;
import com.kdt.fridge.ingredient.dto.IngredientUpdateRequest;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import org.apache.coyote.Response;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final JdbcTemplate jdbcTemplate;

    public IngredientService(
            IngredientRepository ingredientRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.ingredientRepository = ingredientRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public IngredientResponse create(
            long groupId,
            long userId,
            IngredientCreateRequest request
    ) {
        Boolean member = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1 FROM group_members
                    WHERE group_id = ? AND user_id = ?
                )
                """, Boolean.class, groupId, userId);

        if (!Boolean.TRUE.equals(member)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "냉장고 그룹 구성원이 아닙니다."
            );
        }

        Ingredient ingredient = new Ingredient(
                groupId,
                userId,
                request.name().strip(),
                request.quantity(),
                request.unit(),
                request.storageType(),
                Boolean.TRUE.equals(request.shared()),
                request.purchasedAt(),
                request.expiresAt(),
                request.notifyDaysBefore(),
                "STORED",
                request.memo(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return IngredientResponse.from(savedIngredient);
    }
    @Transactional(readOnly = true)
    public List<IngredientResponse> findAll(long groupId, long userId) {
        requireGroupMember(groupId, userId);

        List<Ingredient> ingredients =
                ingredientRepository.findVisibleIngredients(groupId, userId);

        List<IngredientResponse> responses = new ArrayList<>(ingredients.size());

        for (Ingredient ingredient : ingredients) {
            IngredientResponse response = IngredientResponse.from(ingredient);
            responses.add(response);
        }

        return responses;
    }
    @Transactional(readOnly = true)
    public IngredientResponse getDetail(long groupId,long ingredientId,long userId){
        requireGroupMember(groupId,userId);

        Ingredient ingredient = ingredientRepository.findByIdAndGroupId(ingredientId,groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "식재료를 찾을 수 없습니다."
                ));
        return IngredientResponse.from(ingredient);
    }
    @Transactional
    public IngredientResponse update(
            long groupId,
            long ingredientId,
            long userId,
            IngredientUpdateRequest request
    ){
        List<String> roles = jdbcTemplate.queryForList("""
            SELECT role
            FROM group_members
            WHERE group_id = ? AND user_id = ?
            """,String.class,groupId,userId);
        if(roles.isEmpty()){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "냉장고 그룹 구성원이 아닙니다."
            );
        }
        Ingredient ingredient = ingredientRepository.findForUpdate(ingredientId,groupId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"식재료를 찾을 수 없습니다."
        ));

        boolean canUpdate = ingredient.isShared()
                ? roles.contains("ADMIN")
                : Long.valueOf(userId).equals(ingredient.getOwnerId());

        if(!canUpdate) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,"이 식재료를 수정할 권한이 없습니다."
            );
        }
        String name = request.name() == null ? ingredient.getName() : request.name().strip();
        if(name != null && name.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,"식재료 이름을 공백일 수 없습니다."
            );
        }
        String unit = request.unit() == null ? ingredient.getUnit() : request.unit();
        String storageType = request.storageType() == null
                ? ingredient.getStorageType()
                : request.storageType();
        LocalDate purchasedAt = request.purchasedAt() == null
                ? ingredient.getPurchasedAt()
                : request.purchasedAt();
        LocalDate expiresAt = request.expiresAt() == null
                ? ingredient.getExpiresAt()
                : request.expiresAt();
        Integer notifyDaysBefore = request.notifyDaysBefore() == null
                ? ingredient.getNotifyDaysBefore()
                : request.notifyDaysBefore();
        if((request.purchasedAt() != null || request.expiresAt() != null)
               && purchasedAt != null && expiresAt != null
               && expiresAt.isBefore(purchasedAt)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,"유통기한은 구매일보다 빠를 수 없습니다."
            );
        }
        ingredient.updateBasicInfo(
                name,
                unit,
                storageType,
                purchasedAt,
                expiresAt,
                notifyDaysBefore,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        return IngredientResponse.from(ingredient);
    }
    @Transactional
    public IngredientResponse consume(
            long groupId,
            long ingredientId,
            long userId,
            long amount
    ) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        Boolean member = jdbcTemplate.queryForObject("""
                SELECT EXISTS(
                    SELECT 1 FROM group_members
                    WHERE group_id = ? AND user_id = ?
                )
                """,Boolean.class,groupId,userId);
        if (!Boolean.TRUE.equals(member)) {
            throw new BusinessException(ErrorCode.INGREDIENT_GROUP_MEMBER_REQUIRED);
        }

        Ingredient ingredient = ingredientRepository.findForUpdate(ingredientId,groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));
        if (!ingredient.isShared() && !Long.valueOf(userId).equals(ingredient.getOwnerId())) {
            throw new BusinessException(ErrorCode.INGREDIENT_CONSUMPTION_FORBIDDEN);
        }

        if (!"STORED".equals(ingredient.getStatus())) {
            throw new BusinessException(ErrorCode.INGREDIENT_NOT_STORED);
        }

        Long currentQuantity = ingredient.getQuantity();
        if(currentQuantity == null || currentQuantity < amount) {
            throw new BusinessException(ErrorCode.INGREDIENT_INSUFFICIENT_QUANTITY);
        }

        long remainingQuantity = currentQuantity - amount;
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        ingredient.applyConsumption(remainingQuantity,now);

        jdbcTemplate.update("""
            INSERT INTO ingredient_histories
                (ingredient_id,user_id,action,quantity_change,created_at)
            VALUES (?,?,?,?,?)
            """,ingredientId,userId,"CONSUME",-amount,now);

        return IngredientResponse.from(ingredient);
    }

    private void requireGroupMember(long groupId, long userId) {
        Boolean member = jdbcTemplate.queryForObject("""
            SELECT EXISTS (
                SELECT 1 FROM group_members
                WHERE group_id = ? AND user_id = ?
            )
            """, Boolean.class, groupId, userId);

        if (!Boolean.TRUE.equals(member)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "냉장고 그룹 구성원이 아닙니다."
            );
        }
    }
}