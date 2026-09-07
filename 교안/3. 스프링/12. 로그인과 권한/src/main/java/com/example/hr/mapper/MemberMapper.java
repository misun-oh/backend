package com.example.hr.mapper;

import com.example.hr.domain.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * Day 12 — 본문 2절.
 * XML: SELECT MEMBER_ID, PASSWORD, MEMBER_NAME, ROLE FROM MEMBER WHERE MEMBER_ID = #{memberId}
 */
@Mapper
public interface MemberMapper {
    Member findById(String memberId);
}
