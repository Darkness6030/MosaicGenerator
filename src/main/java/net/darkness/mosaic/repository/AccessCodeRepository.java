package net.darkness.mosaic.repository;

import net.darkness.mosaic.entity.AccessCode;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface AccessCodeRepository extends JpaRepository<AccessCode, UUID> {
    Optional<AccessCode> findByCode(String code);

    Page<AccessCode> findAllByOrderByCreatedDesc(Pageable pageable);

    boolean existsByCode(String code);
}
