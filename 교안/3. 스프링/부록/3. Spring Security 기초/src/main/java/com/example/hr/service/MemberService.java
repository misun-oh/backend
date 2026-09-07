package com.example.hr.service;

import com.example.hr.domain.Member;
import com.example.hr.dto.SignupForm;
import com.example.hr.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 부록 3. 회원가입 — 비밀번호는 반드시 BCrypt 로 해시 후 저장.
 */
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public String signup(SignupForm form) {
        if (memberMapper.existsById(form.memberId()))
            throw new BusinessException("DUP_MEMBER_ID", "이미 사용 중인 아이디입니다");

        Member m = Member.builder()
                .memberId(form.memberId())
                .password(passwordEncoder.encode(form.password()))   // ★ 해시
                .memberName(form.memberName())
                .enabled(true)
                .build();
        memberMapper.insert(m);                                 // Day 12와 같은 MEMBER 테이블
        memberMapper.insertRole(m.getMemberId(), "USER");        // 부록 3에서 추가된 MEMBER_ROLE
        return m.getMemberId();
    }
}
