package com.example.hr.service;

import com.example.hr.config.FileProps;
import com.example.hr.dto.StoredFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorage {

    private final FileProps props;

    public StoredFile store(MultipartFile file) {
        try {
            String origin = file.getOriginalFilename();
            String ext = extensionOf(origin);                       // ".png"
            String storedName = UUID.randomUUID() + ext;

            LocalDate d = LocalDate.now();
            Path dir = Path.of(props.baseDir(),
                    String.valueOf(d.getYear()),
                    String.format("%02d", d.getMonthValue()));
            Files.createDirectories(dir);

            Path base = Path.of(props.baseDir()).toAbsolutePath().normalize();
            Path target = dir.resolve(storedName).toAbsolutePath().normalize();
            if (!target.startsWith(base)) {
                throw new IllegalArgumentException("잘못된 저장 경로");
            }

            file.transferTo(target);
            return new StoredFile(origin, storedName, target.toString(),
                    file.getSize(), file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    private String extensionOf(String name) {
        if (name == null) return "";
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(i).toLowerCase() : "";
    }
}
