package com.example.hr.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Day 12 의 MEMBER 테이블(MEMBER_ID/PASSWORD/MEMBER_NAME)을 그대로 쓰되,
 * 부록 3 부터는 단일 ROLE 컬럼 대신 MEMBER_ROLE 테이블(다대다)로 권한을 분리하고
 * ENABLED 컬럼을 추가한다. 권한은 MemberMapper.findRoles(memberId) 로 별도 조회.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    private String memberId;
    private String password;
    private String memberName;
    private boolean enabled;
}
