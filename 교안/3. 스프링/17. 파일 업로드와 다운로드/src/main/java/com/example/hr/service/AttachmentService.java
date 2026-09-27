package com.example.hr.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.hr.dto.AttachmentDto;

public interface AttachmentService {

	// file을 검증·저장하고 refType/refId로 메타데이터를 저장한다.
	AttachmentDto save(String refType, String refId, MultipartFile file) throws IOException;

	List<AttachmentDto> selectByRef(String refType, String refId);

	AttachmentDto selectById(int attachmentId);

	void deleteStoredFile(String storedPath);
}
