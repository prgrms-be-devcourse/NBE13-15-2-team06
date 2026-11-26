package com.kdt.fridge.groupmember.repository;

import com.kdt.fridge.groupmember.entity.JoinRequest;
import com.kdt.fridge.groupmember.entity.JoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, Long> {
    boolean existsByFridgeIdAndUserIdAndStatus(Long fridgeId, Long userId, JoinRequestStatus status);

    List<JoinRequest> findAllByFridgeIdAndStatusOrderByRequestedAtAsc(Long fridgeId, JoinRequestStatus status);

    Optional<JoinRequest> findByIdAndFridgeId(Long requestId, Long fridgeId);
}
