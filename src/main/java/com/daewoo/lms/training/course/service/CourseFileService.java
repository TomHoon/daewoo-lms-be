package com.daewoo.lms.training.course.service;

import com.daewoo.lms.training.course.dto.CourseFileDTO;
import com.daewoo.lms.training.course.mapper.CourseMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CourseFileService {

    private final CourseMapper mapper;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Transactional
    public CourseFileDTO upload(MultipartFile file, Long createdBy) {
        return upload(file, createdBy, "COURSE_BATCH_IMAGE");
    }

    @Transactional
    public CourseFileDTO uploadAdmissionPhoto(MultipartFile file, Long createdBy) {
        return upload(file, createdBy, "ADMISSION_PHOTO");
    }

    private CourseFileDTO upload(MultipartFile file, Long createdBy, String refType) {
        if (file.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지를 선택해 주세요.");
        String type = detectImage(file);
        String extension = switch(type) {
            case "image/png" ->
                "png";
            case "image/jpeg" ->
                "jpg";
            case "image/gif" ->
                "gif";
            default ->
                "webp";
        };
        Path directory = Path.of(uploadDir).toAbsolutePath().normalize().resolve("course");
        String savedName = UUID.randomUUID() + "." + extension;
        Path destination = directory.resolve(savedName);
        try {
            Files.createDirectories(directory);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, destination);
            }
        } catch (IOException e) {
            cleanup(destination);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 저장에 실패했습니다.", e);
        }
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {

                @Override
                public void afterCompletion(int status) {
                    if (status != STATUS_COMMITTED)
                        cleanup(destination);
                }
            }
        );
        CourseFileDTO dto = new CourseFileDTO();
        String original = file.getOriginalFilename();
        if (original == null || original.isBlank())
            original = savedName;
        original = original.replace('\\', '/');
        original = original.substring(original.lastIndexOf('/') + 1);
        if (original.length() > 255)
            original = original.substring(original.length() - 255);
        dto.setOriginalFileName(original);
        dto.setSavedFileName(savedName);
        dto.setFilePath(destination.toString());
        dto.setFileSize(file.getSize());
        dto.setFileExtension(extension);
        dto.setMimeType(type);
        dto.setRefType(refType);
        dto.setCreatedBy(createdBy);
        mapper.insertFile(dto);
        return mapper.file(dto.getFileId());
    }

    public Resource content(Long fileId) {
        CourseFileDTO dto = mapper.file(fileId);
        if (dto == null || dto.getFilePath() == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");
        Path directory = Path.of(uploadDir).toAbsolutePath().normalize().resolve("course");
        try {
            Path path = Path.of(dto.getFilePath()).toAbsolutePath().normalize();
            if (!path.startsWith(directory) || !Files.isRegularFile(path) || !path.toRealPath().startsWith(directory.toRealPath()))
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");
            return new UrlResource(path.toUri());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.", e);
        }
    }

    private String detectImage(MultipartFile file) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            if (header.length >= 8 && header[0] == (byte) 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G' && header[4] == 13 && header[5] == 10 && header[6] == 26 && header[7] == 10)
                return "image/png";
            if (header.length >= 3 && header[0] == (byte) 0xff && header[1] == (byte) 0xd8 && header[2] == (byte) 0xff)
                return "image/jpeg";
            if (header.length >= 6 && new String(
                header,
                0,
                6,
                StandardCharsets.US_ASCII
            ).matches("GIF8[79]a"))
                return "image/gif";
            if (header.length >= 12 && new String(
                header,
                0,
                4,
                StandardCharsets.US_ASCII
            ).equals("RIFF") && new String(
                header,
                8,
                4,
                StandardCharsets.US_ASCII
            ).equals("WEBP"))
                return "image/webp";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PNG, JPEG, GIF, WebP 이미지만 업로드할 수 있습니다.");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지를 읽을 수 없습니다.", e);
        }
    }

    private static void cleanup(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            LoggerFactory.getLogger(CourseFileService.class).warn("업로드 파일 정리 실패: {}", path, e);
        }
    }
}
