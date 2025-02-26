package net.darkness.mosaic.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import net.darkness.mosaic.entity.AccessCode;
import net.darkness.mosaic.repository.AccessCodeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static net.darkness.mosaic.MosaicApplication.*;


@Service
@Slf4j
public class AccessCodeService {
    private final int pageSize;
    private final AccessCodeRepository repository;

    public AccessCodeService(@Value("${page.size}") int pageSize, AccessCodeRepository repository) {
        this.pageSize = pageSize;
        this.repository = repository;
    }

    public Page<AccessCode> getPage(int pageNumber) {
        return repository.findAllByOrderByCreatedDesc(PageRequest.of(pageNumber, pageSize));
    }

    public String getCodeForCodeId(String codeId) {
        return repository.getReferenceById(UUID.fromString(codeId)).getCode();
    }

    public AccessCode getAccessCode(String code) {
        AccessCode accessCode;
        try {
            accessCode = repository.findById(UUID.fromString(code.trim())).orElse(null);
        } catch (IllegalArgumentException | EntityNotFoundException e) {
            return null;
        }
        return accessCode;
    }

    public Optional<String> getCodeId(String code) {
        return getAccessCodeByCode(code)
                .map(AccessCode::getId)
                .map(UUID::toString);
    }

    public Optional<AccessCode> getAccessCodeByCode(String code) {
        return repository.findByCode(code);
    }

    public boolean isCodeUsedById(String uuid) {
        AccessCode accessCode = repository.getReferenceById(UUID.fromString(uuid));
        return accessCode.getState() == AccessCode.State.USED;
    }

    public void createAccessCode(String code) {
        if (!repository.existsByCode(code))
            repository.save(new AccessCode(code));
    }

    public void createAccessCodes(int amount) {
        for (int i = 0; i < amount; i++)
            createAccessCode(RANDOM_GENERATOR.nextInt(100, 999) + "-" + RANDOM_GENERATOR.nextInt(100, 999));
    }

    public void deleteAccessCode(String uuid) {
        repository.findById(UUID.fromString(uuid)).ifPresentOrElse(code -> {
            if (code.getState() == AccessCode.State.USED)
                deleteAssociatedFiles(code);
            repository.delete(code);
        }, () -> {
        });
    }

    private static void deleteAssociatedFiles(AccessCode code) {
        try {
            Files.deleteIfExists(Path.of(IMAGES_DIRECTORY, code.getOriginalImageName()));
            Files.deleteIfExists(Path.of(IMAGES_DIRECTORY, code.getGeneratedImageName()));
        } catch (IOException e) {
            log.error("Can't delete files", e);
        }
    }

}
