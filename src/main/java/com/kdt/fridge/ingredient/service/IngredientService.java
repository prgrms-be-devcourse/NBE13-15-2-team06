package com.kdt.fridge.ingredient.service;

import com.kdt.fridge.global.exception.BusinessException;
import com.kdt.fridge.global.exception.ErrorCode;
import com.kdt.fridge.ingredient.dto.IngredientCreateRequest;
import com.kdt.fridge.ingredient.dto.IngredientHistoryResponse;
import com.kdt.fridge.ingredient.dto.IngredientResponse;
import com.kdt.fridge.ingredient.dto.IngredientStatusUpdateRequest.Status;
import com.kdt.fridge.ingredient.dto.IngredientUpdateRequest;
import com.kdt.fridge.ingredient.entity.Ingredient;
import com.kdt.fridge.ingredient.repository.IngredientConsumptionRequestRepository;
import com.kdt.fridge.ingredient.repository.IngredientRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngredientService {

    private static final Pattern IDEMPOTENCY_KEY_PATTERN = Pattern.compile("[A-Za-z0-9._-]{1,128}");

    private final IngredientRepository ingredientRepository;
    private final JdbcTemplate jdbcTemplate;
    private final IngredientConsumptionRequestRepository consumptionRequestRepository;

    public IngredientService(
            IngredientRepository ingredientRepository,
            JdbcTemplate jdbcTemplate,
            IngredientConsumptionRequestRepository consumptionRequestRepository
    ) {
        this.ingredientRepository = ingredientRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.consumptionRequestRepository = consumptionRequestRepository;
    }

    // 식재료 등록 API — 그룹 구성원을 확인한 뒤 새 식재료를 보관 상태로 저장합니다.
    @Transactional
    public IngredientResponse create(
            long groupId,
            long userId,
            IngredientCreateRequest request
    ) {
        requireGroupMember(groupId, userId);

        LocalDate purchasedAt = request.purchasedAt();
        LocalDate expiresAt = request.expiresAt();

        if (purchasedAt != null && expiresAt != null
                && expiresAt.isBefore(purchasedAt)){
            throw new BusinessException(ErrorCode.INGREDIENT_INVALID_EXPIRATION_DATE);
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
                Status.STORED.name(),
                request.memo()
        );

        Ingredient savedIngredient = ingredientRepository.save(ingredient);
        return IngredientResponse.from(savedIngredient);
    }

    // 식재료 목록 조회 API — 그룹 내 개인·공동 식재료를 조회합니다.
    // 검색어와 유통기한 조건은 선택 사항이며, 정렬 조건을 조회에 함께 전달합니다.
    @Transactional(readOnly = true)
    public List<IngredientResponse> findAll(
            long groupId,
            long userId,
            String keyword,
            Integer expiringWithinDays,
            String sort
    ) {
        requireGroupMember(groupId, userId);

        if (expiringWithinDays != null && expiringWithinDays < 0) {
            throw new BusinessException(ErrorCode.INGREDIENT_INVALID_EXPIRING_WITHIN_DAYS);
        }

        String searchKeyword = keyword == null ? "" : keyword.strip();
        Sort ingredientSort = parseIngredientSort(sort);
        List<Ingredient> ingredients;

        if (expiringWithinDays == null) {
            if (searchKeyword.isEmpty()) {
                ingredients = ingredientRepository.findAllByGroupId(groupId, ingredientSort);
            } else {
                ingredients = ingredientRepository.findAllByGroupIdAndNameContaining(
                        groupId, searchKeyword, ingredientSort
                );
            }
        } else {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(expiringWithinDays);

            if (searchKeyword.isEmpty()) {
                ingredients = ingredientRepository.findAllByGroupIdAndExpiresAtBetween(
                        groupId, today, endDate, ingredientSort
                );
            } else {
                ingredients = ingredientRepository
                        .findAllByGroupIdAndNameContainingAndExpiresAtBetween(
                                groupId, searchKeyword, today, endDate, ingredientSort
                        );
            }
        }

        List<IngredientResponse> responses = new ArrayList<>(ingredients.size());
        for (Ingredient ingredient : ingredients) {
            responses.add(IngredientResponse.from(ingredient));
        }
        return responses;
    }

    // 식재료 상세 조회 API — 요청한 그룹에 속한 식재료 한 건을 조회합니다.
    @Transactional(readOnly = true)
    public IngredientResponse getDetail(long groupId, long ingredientId, long userId) {
        requireGroupMember(groupId, userId);

        Ingredient ingredient = findIngredient(groupId, ingredientId);
        return IngredientResponse.from(ingredient);
    }

    // 식재료 기본정보 수정 API — 공동 식재료는 관리자, 개인 식재료는 소유자가 수정합니다.
    @Transactional
    public IngredientResponse update(
            long groupId,
            long ingredientId,
            long userId,
            IngredientUpdateRequest request
    ) {
        List<String> roles = requireGroupMemberRoles(groupId, userId);
        Ingredient ingredient = findIngredientForUpdate(groupId, ingredientId);

        boolean canUpdate = ingredient.isShared()
                ? roles.contains("ADMIN")
                : isOwner(ingredient, userId);

        if (!canUpdate) {
            throw new BusinessException(ErrorCode.INGREDIENT_UPDATE_FORBIDDEN);
        }

        // 요청에 값이 들어온 항목만 바꾸고, 나머지는 현재 값을 사용합니다.
        String name = request.name() == null ? ingredient.getName() : request.name().strip();
        if (name != null && name.isBlank()) {
            throw new BusinessException(ErrorCode.INGREDIENT_BLANK_NAME);
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

        // 날짜가 수정되면 기존 값과 새 요청 값을 합친 최종 날짜를 비교합니다.
        if ((request.purchasedAt() != null || request.expiresAt() != null)
                && purchasedAt != null
                && expiresAt != null
                && expiresAt.isBefore(purchasedAt)) {
            throw new BusinessException(ErrorCode.INGREDIENT_INVALID_EXPIRATION_DATE);
        }

        ingredient.updateBasicInfo(
                name, unit, storageType, purchasedAt, expiresAt, notifyDaysBefore);
        return IngredientResponse.from(ingredient);
    }

    // 잠금 대기 후 조회한 요청 기록에 먼저 끝난 트랜잭션의 커밋이 반영되어야 합니다.
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public IngredientResponse consume(
            long groupId,
            long ingredientId,
            long userId,
            long amount,
            String idempotencyKey
    ) {
        if (amount <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        if (idempotencyKey != null && !IDEMPOTENCY_KEY_PATTERN.matcher(idempotencyKey).matches()) {
            throw new BusinessException(ErrorCode.INGREDIENT_INVALID_IDEMPOTENCY_KEY);
        }

        requireGroupMember(groupId, userId);
        Ingredient ingredient = findIngredientForUpdate(groupId, ingredientId);
        requireConsumptionPermission(ingredient, userId);

        if (idempotencyKey != null) {
            var previousRequest = consumptionRequestRepository.find(
                    groupId, ingredientId, userId, idempotencyKey
            );
            if (previousRequest.isPresent()) {
                var completedRequest = previousRequest.get();
                if (completedRequest.requestedQuantity() != amount) {
                    throw new BusinessException(ErrorCode.INGREDIENT_IDEMPOTENCY_KEY_CONFLICT);
                }
                return completedRequest.response();
            }
        }

        if (!Status.STORED.name().equals(ingredient.getStatus())) {
            throw new BusinessException(ErrorCode.INGREDIENT_NOT_STORED);
        }

        Long currentQuantity = ingredient.getQuantity();
        if (currentQuantity == null || currentQuantity < amount) {
            throw new BusinessException(ErrorCode.INGREDIENT_INSUFFICIENT_QUANTITY);
        }

        long remainingQuantity = currentQuantity - amount;
        LocalDateTime now = nowInSeconds();

        // 엔티티에 남은 수량을 전달하고, 이력에는 줄어든 양을 음수로 기록합니다.
        ingredient.applyConsumption(remainingQuantity);
        saveQuantityHistory(ingredientId, userId, "CONSUME", -amount, now);


        ingredientRepository.flush();
        IngredientResponse response = IngredientResponse.from(ingredient);
        if (idempotencyKey != null) {
            consumptionRequestRepository.save(
                    groupId, ingredientId, userId, idempotencyKey, amount, response);
        }
        return response;
    }

    // 식재료 상태 변경 API — 보관 중인 식재료를 소비 또는 폐기 처리하고 이력을 저장합니다.
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

        List<String> roles = requireGroupMemberRoles(groupId, userId);
        Ingredient ingredient = findIngredientForUpdate(groupId, ingredientId);

        if (targetStatus == Status.CONSUMED) {
            requireConsumptionPermission(ingredient, userId);
        } else if (!isAdminOrPrivateIngredientOwner(roles, ingredient, userId)) {
            throw new BusinessException(ErrorCode.INGREDIENT_STATUS_UPDATE_FORBIDDEN);
        }

        // 같은 상태를 다시 요청하면 현재 값을 반환해 이력이 중복되는 것을 막습니다.
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

        LocalDateTime now = nowInSeconds();
        String action;

        if (targetStatus == Status.CONSUMED) {
            ingredient.applyConsumption(0L);
            action = "CONSUME";
        } else {
            ingredient.discard();
            action = "DISCARD";
        }

        saveQuantityHistory(ingredientId, userId, action, -currentQuantity, now);
        return IngredientResponse.from(ingredient);
    }

    // 식재료 변경 이력 조회 API — 식재료의 그룹 소속을 확인한 뒤 최신 이력부터 반환합니다.
    @Transactional(readOnly = true)
    public List<IngredientHistoryResponse> getHistories(
            long groupId,
            long ingredientId,
            long userId
    ) {
        requireGroupMember(groupId, userId);
        findIngredient(groupId, ingredientId);

        return jdbcTemplate.query("""
                SELECT id, user_id, action, quantity_change, detail, created_at
                FROM ingredient_histories
                WHERE ingredient_id = ?
                ORDER BY created_at DESC, id DESC
                """,
                (rs, rowNum) -> new IngredientHistoryResponse(
                        rs.getLong("id"),
                        rs.getObject("user_id", Long.class),
                        rs.getString("action"),
                        rs.getObject("quantity_change", Long.class),
                        rs.getString("detail"),
                        rs.getObject("created_at", LocalDateTime.class)
                ),
                ingredientId
        );
    }

    // 식재료 메모 수정 API — 관리자 또는 개인 식재료의 소유자가 메모를 수정합니다.
    @Transactional
    public IngredientResponse updateMemo(
            long groupId,
            long ingredientId,
            long userId,
            String memo
    ) {
        List<String> roles = requireGroupMemberRoles(groupId, userId);
        Ingredient ingredient = findIngredientForUpdate(groupId, ingredientId);

        if (!isAdminOrPrivateIngredientOwner(roles, ingredient, userId)) {
            throw new BusinessException(ErrorCode.INGREDIENT_MEMO_UPDATE_FORBIDDEN);
        }

        ingredient.updateMemo(memo);
        ingredientRepository.flush();
        return IngredientResponse.from(ingredient);
    }

    // 식재료 공동 여부 변경 API — 권한을 확인하고 개인·공동 설정을 변경합니다.
    @Transactional
    public IngredientResponse updateOwnership(
            long groupId,
            long ingredientId,
            long userId,
            boolean shared
    ) {
        List<String> roles = requireGroupMemberRoles(groupId, userId);
        Ingredient ingredient = findIngredientForUpdate(groupId, ingredientId);

        if (!isAdminOrPrivateIngredientOwner(roles, ingredient, userId)) {
            throw new BusinessException(ErrorCode.INGREDIENT_OWNERSHIP_UPDATE_FORBIDDEN);
        }

        if (ingredient.isShared() != shared) {
            ingredient.updateShared(shared);
        }

        return IngredientResponse.from(ingredient);
    }

    // 목록 요청의 "필드,방향" 문자열을 저장소에 전달할 Sort 객체로 바꿉니다.
    private Sort parseIngredientSort(String sort) {
        if (sort == null) {
            return Sort.unsorted();
        }

        String[] parts = sort.split(",", -1);
        if (parts.length != 2) {
            throw new BusinessException(ErrorCode.INGREDIENT_INVALID_SORT_FORMAT);
        }

        String field = parts[0].strip();
        String direction = parts[1].strip();

        if (!"expiresAt".equals(field) && !"createdAt".equals(field)) {
            throw new BusinessException(ErrorCode.INGREDIENT_INVALID_SORT_FIELD);
        }

        return switch (direction) {
            case "asc" -> Sort.by(Sort.Direction.ASC, field);
            case "desc" -> Sort.by(Sort.Direction.DESC, field);
            default -> throw new BusinessException(ErrorCode.INGREDIENT_INVALID_SORT_DIRECTION);
        };
    }

    // 역할 정보가 필요 없는 API에서 그룹 가입 여부를 확인합니다.
    private void requireGroupMember(long groupId, long userId) {
        Boolean member = jdbcTemplate.queryForObject("""
                SELECT EXISTS (
                    SELECT 1
                    FROM group_members
                    WHERE group_id = ? AND user_id = ?
                )
                """, Boolean.class, groupId, userId);

        if (!Boolean.TRUE.equals(member)) {
            throw new BusinessException(ErrorCode.INGREDIENT_GROUP_MEMBER_REQUIRED);
        }
    }

    // 수정 권한을 판단할 API에 그룹 안에서 사용자가 가진 역할을 돌려줍니다.
    private List<String> requireGroupMemberRoles(long groupId, long userId) {
        List<String> roles = jdbcTemplate.queryForList("""
                SELECT role
                FROM group_members
                WHERE group_id = ? AND user_id = ?
                """, String.class, groupId, userId);

        if (roles.isEmpty()) {
            throw new BusinessException(ErrorCode.INGREDIENT_GROUP_MEMBER_REQUIRED);
        }
        return roles;
    }

    // 상세·이력 조회에 사용하는 식재료 조회입니다.
    private Ingredient findIngredient(long groupId, long ingredientId) {
        return ingredientRepository.findByIdAndGroupId(ingredientId, groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));
    }

    // 쓰기 API에서는 저장소의 수정용 조회를 사용합니다.
    private Ingredient findIngredientForUpdate(long groupId, long ingredientId) {
        return ingredientRepository.findForUpdate(ingredientId, groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INGREDIENT_NOT_FOUND));
    }

    private boolean isOwner(Ingredient ingredient, long userId) {
        return Long.valueOf(userId).equals(ingredient.getOwnerId());
    }

    private boolean isAdminOrPrivateIngredientOwner(
            List<String> roles,
            Ingredient ingredient,
            long userId
    ) {
        return roles.contains("ADMIN") || (!ingredient.isShared() && isOwner(ingredient, userId));
    }

    // 그룹 구성원 확인을 통과한 뒤, 개인 식재료라면 소유자인지 확인합니다.
    private void requireConsumptionPermission(Ingredient ingredient, long userId) {
        if (!ingredient.isShared() && !isOwner(ingredient, userId)) {
            throw new BusinessException(ErrorCode.INGREDIENT_CONSUMPTION_FORBIDDEN);
        }
    }

    // 소비·폐기 이력을 식재료 변경과 같은 트랜잭션에서 저장합니다.
    private void saveQuantityHistory(
            long ingredientId,
            long userId,
            String action,
            long quantityChange,
            LocalDateTime createdAt
    ) {
        jdbcTemplate.update("""
                INSERT INTO ingredient_histories
                    (ingredient_id, user_id, action, quantity_change, created_at)
                VALUES (?, ?, ?, ?, ?)
                """, ingredientId, userId, action, quantityChange, createdAt);
    }

    private LocalDateTime nowInSeconds() {
        return LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }
}