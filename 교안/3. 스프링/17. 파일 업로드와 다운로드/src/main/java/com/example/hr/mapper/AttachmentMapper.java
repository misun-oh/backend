package com.example.hr.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.hr.dto.AttachmentDto;

@Mapper
public interface AttachmentMapper {

	int insert(AttachmentDto dto);

	List<AttachmentDto> selectByRef(String refType, String refId);

	@Select("select * from attachment where attachment_id = #{attachmentId}")
	AttachmentDto selectById(int attachmentId);
}
