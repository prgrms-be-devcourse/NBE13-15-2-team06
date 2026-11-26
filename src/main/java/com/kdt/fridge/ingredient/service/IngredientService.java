package com.kdt.fridge.ingredient.service;

import com.kdt.fridge.ingredient.dto.IngredientCreateRequest;
import com.kdt.fridge.ingredient.dto.IngredientResponse;
import com.kdt.fridge.ingredient.entity.Ingredient;
import com.kdt.fridge.ingredient.repository.IngredientRepository;
import com.kdt.fridge.ingredient.dto.IngredientUpdateRequest;
import com.kdt.fridge.ingredient.dto.IngredientStatusUpdateRequest.Status;
import com.kdt.fridge.ingredient.dto.IngredientHistoryResponse;
import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import org.springframework.data.domain.Sort;
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
    public List<IngredientResponse> findAll(
            long groupId,
            long userId,
            String keyword,
            Integer expiringWithinDays,
            String sort)
    {
        requireGroupMember(groupId, userId);

        if(expiringWithinDays != null && expiringWithinDays < 0){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,"유통기한 조회 일수는 0 이상이어야 합니다."
            );
        }

        String searchKeyword = keyword == null ? "" : keyword.strip();
        Sort ingredientSort = parseIngredientSort(sort);
        List<Ingredient> ingredients;

        if(expiringWithinDays == null) {
            if (searchKeyword.isEmpty()) {
                ingredients = ingredientRepository.findAllByGroupId(groupId,ingredientSort);
            } else {
                ingredients = ingredientRepository.findAllByGroupIdAndNameContaining(
                        groupId, searchKeyword,ingredientSort
                );
            }
        } else {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(expiringWithinDays);

            if(searchKeyword.isEmpty()){
                ingredients = ingredientRepository.findAllByGroupIdAndExpiresAtBetween(
                        groupId,today,endDate,ingredientSort
                );
            } else {
                ingredients = ingredientRepository
                        .findAllByGroupIdAndNameContainingAndExpiresAtBetween(
                                groupId,searchKeyword,today,endDate,ingredientSort
                        );
            }
        }
        List<IngredientResponse> responses = new ArrayList<>(ingredients.size());
        for (Ingredient ingredient : ingredients) {
            responses.add(IngredientResponse.from(ingredient));
        }

        return responses;
    }

    private Sort parseIngredientSort(String sort) {
        if (sort == null) {
            return Sort.unsorted();
        }

        String[] parts = sort.split(",", -1);
        if (parts.length != 2) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,"정렬은 필드,방향 형식으로 입력해주세요."
            );
        }

        String field = parts[0].strip();
        String direction = parts[1].strip();

        if (!"expiresAt".equals(field) && !"createdAt".equals(field)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,"정렬 필드는 expiresAt 또는 createdAt이어야 합니다."
            );
        }

        if("asc".equals(direction)) {
            return Sort.by(Sort.Direction.ASC,field);
        }

        if("desc".equals(direction)) {
            return Sort.by(Sort.Direction.DESC,field);
        }

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,"정렬 방향은 내림차순 혹은 오름차순이어야합니다."
        );
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
                    HttpStatus.BAD_REQUEST,"식재료 이름은 공백일 수 없습니다."
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
    @Transactional
    public IngredientResponse updateStatus(
            long groupId,
            long ingredientId,
            long userId,
            Status targetStatus
    ) {
        if (targetStatus == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        List<String> roles = jdbcTemplate.queryForList("""
            SELECT role
            FROM group_members
            WHERE group_id = ? AND user_id = ?
            """, String.class, groupId, userId);

        if (roles.isEmpty()) {
            throw new BusinessException(ErrorCode.INGREDIENT_GROUP_MEMBER_REQUIRED);
        }

        Ingredient ingredient = ingredientRepository.findForUpdate(ingredientId, groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));

        boolean isOwner = Long.valueOf(userId).equals(ingredient.getOwnerId());

        if (targetStatus == Status.CONSUMED) {
            if (!ingredient.isShared() && !isOwner) {
                throw new BusinessException(ErrorCode.INGREDIENT_CONSUMPTION_FORBIDDEN);
            }
        } else {
            boolean canUpdate = roles.contains("ADMIN")
                    || (!ingredient.isShared() && isOwner);

            if (!canUpdate) {
                throw new BusinessException(ErrorCode.INGREDIENT_STATUS_UPDATE_FORBIDDEN);
            }
        }

        if (targetStatus.name().equals(ingredient.getStatus())) {
            return IngredientResponse.from(ingredient);
        }

        if (!Status.STORED.name().equals(ingredient.getStatus())
                || targetStatus == Status.STORED) {
            throw new BusinessException(ErrorCode.INGREDIENT_INVALID_STATUS_TRANSITION);
        }

        Long currentQuantity = ingredient.getQuantity();
        if (currentQuantity == null || currentQuantity < 0) {
            throw new BusinessException(ErrorCode.INGREDIENT_INVALID_STORED_QUANTITY);
        }

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        String action;

        if (targetStatus == Status.CONSUMED) {
            ingredient.applyConsumption(0L, now);
            action = "CONSUME";
        } else {
            ingredient.discard(now);
            action = "DISCARD";
        }

        jdbcTemplate.update("""
            INSERT INTO ingredient_histories
                (ingredient_id, user_id, action, quantity_change, created_at)
            VALUES (?, ?, ?, ?, ?)
            """, ingredientId, userId, action, -currentQuantity, now);

        return IngredientResponse.from(ingredient);
    }
    @Transactional(readOnly = true)
    public List<IngredientHistoryResponse> getHistories(
            long groupId,
            long ingredientId,
            long userId
    ){
        requireGroupMember(groupId,userId);

        ingredientRepository.findByIdAndGroupId(ingredientId,groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));
        return jdbcTemplate.query("""
                SELECT id,user_id,action,quantity_change,detail,created_at
                FROM ingredient_histories
                WHERE ingredient_id = ?
                ORDER BY created_at DESC,id DESC
                """,
                (rs,rowNum) -> new IngredientHistoryResponse(
                        rs.getLong("id"),
                        rs.getObject("user_id",Long.class),
                        rs.getString("action"),
                        rs.getObject("quantity_change",Long.class),
                        rs.getString("detail"),
                        rs.getObject("created_at",LocalDateTime.class)
                ),
                ingredientId
        );
    }

    @Transactional
    public IngredientResponse updateMemo(
            long groupId,
            long ingredientId,
            long userId,
            String memo
    ){
        List<String> roles = jdbcTemplate.queryForList("""
            SELECT role
            FROM group_members
            WHERE group_id = ? AND user_id = ?
            """,String.class,groupId,userId);
        if(roles.isEmpty()){
            throw new BusinessException(ErrorCode.INGREDIENT_GROUP_MEMBER_REQUIRED);
        }

        Ingredient ingredient = ingredientRepository.findForUpdate(ingredientId,groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));

        boolean isOwner = Long.valueOf(userId).equals(ingredient.getOwnerId());
        boolean canUpdate = roles.contains("ADMIN")
                || (!ingredient.isShared() && isOwner);

        if(!canUpdate) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,"이 식재료의 메모를 수정할 권한이 없습니다."
            );
        }

        ingredient.updateMemo(memo,LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

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
