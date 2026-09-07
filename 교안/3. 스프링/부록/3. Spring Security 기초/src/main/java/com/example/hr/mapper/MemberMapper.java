package com.example.hr.mapper;

import com.example.hr.domain.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 부록 3. MEMBER / MEMBER_ROLE 매퍼.
 */
@Mapper
public interface MemberMapper {
    boolean existsById(String memberId);
    int insert(Member m);                    // Day 12와 같은 MEMBER 테이블 (MEMBER_ID는 자연 키)
    int insertRole(@Param("memberId") String memberId, @Param("role") String role);
    Member findById(String memberId);
    List<String> findRoles(String memberId);
}
