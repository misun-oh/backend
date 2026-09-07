package com.example.hr.security;

import com.example.hr.domain.Member;
import com.example.hr.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 부록 4. 부록 3 의 표준 User 대신, 도메인 정보를 담은 MemberPrincipal 을 반환하도록 갱신.
 */
@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String memberId) {
        Member m = memberMapper.findById(memberId);
        if (m == null) throw new UsernameNotFoundException(memberId);
        return new MemberPrincipal(m, memberMapper.findRoles(m.getMemberId()));
    }
}
