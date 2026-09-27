package com.example.hr.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AttachmentDto {
	private int attachmentId;
	// 첨부 대상 종류 (예: "EMP") - 어떤 엔티티든 공통으로 재사용하기 위한 다형 참조
	private String refType;
	// 첨부 대상의 기본키 값
	private String refId;
	private String originalName;
	private String storedName;
	private String storedPath;
	private String contentType;
	private long fileSize;
	private LocalDateTime uploadedAt;
}
