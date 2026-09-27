package com.example.hr.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.hr.dto.AttachmentDto;
import com.example.hr.mapper.AttachmentMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AttachmentServiceImpl implements AttachmentService {

	private static final long MAX_SIZE = 5 * 1024 * 1024;
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
	private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

	private final AttachmentMapper mapper;
	private final Path uploadDir;

	public AttachmentServiceImpl(AttachmentMapper mapper, @Value("${hr.upload-dir}") String uploadDir) {
		this.mapper = mapper;
		this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
	}

	@Override
	public AttachmentDto save(String refType, String refId, MultipartFile file) throws IOException {
		validate(file);

		Files.createDirectories(uploadDir);
		String extension = extensionOf(file.getOriginalFilename());
		String storedName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
		Path target = uploadDir.resolve(storedName).normalize();
		if (!target.startsWith(uploadDir)) {
			throw new IllegalArgumentException("잘못된 파일 경로입니다.");
		}
		file.transferTo(target);

		AttachmentDto dto = new AttachmentDto();
		dto.setRefType(refType);
		dto.setRefId(refId);
		dto.setOriginalName(file.getOriginalFilename());
		dto.setStoredName(storedName);
		dto.setStoredPath(target.toString());
		dto.setContentType(file.getContentType());
		dto.setFileSize(file.getSize());
		mapper.insert(dto);
		return dto;
	}

	@Override
	public List<AttachmentDto> selectByRef(String refType, String refId) {
		return mapper.selectByRef(refType, refId);
	}

	@Override
	public AttachmentDto selectById(int attachmentId) {
		return mapper.selectById(attachmentId);
	}

	// 보상 처리용 - 실패해도 등록 흐름 자체를 막지 않도록 예외를 삼키고 로그만 남긴다
	@Override
	public void deleteStoredFile(String storedPath) {
		try {
			Files.deleteIfExists(Path.of(storedPath));
		} catch (IOException e) {
			log.warn("첨부파일 삭제 실패: {}", storedPath, e);
		}
	}

	private void validate(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("첨부할 이미지를 선택하세요.");
		}
		if (file.getSize() > MAX_SIZE) {
			throw new IllegalArgumentException("이미지는 5MB 이하만 업로드할 수 있습니다.");
		}
		String extension = extensionOf(file.getOriginalFilename());
		if (!ALLOWED_EXTENSIONS.contains(extension)) {
			throw new IllegalArgumentException("jpg, jpeg, png, webp 이미지만 업로드할 수 있습니다.");
		}
		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
			throw new IllegalArgumentException("허용되지 않은 이미지 형식입니다.");
		}
	}

	private String extensionOf(String filename) {
		if (filename == null) {
			return "";
		}
		int dot = filename.lastIndexOf('.');
		if (dot < 0 || dot == filename.length() - 1) {
			return "";
		}
		return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
	}
}
