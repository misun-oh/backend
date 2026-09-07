package com.example.hr.security;

import com.example.hr.domain.Member;
import com.example.hr.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 부록 3. 시큐리티가 "이 아이디 사용자 정보 줘" 할 때 호출.
 * 파라미터 이름은 시큐리티 인터페이스가 정한 username 이지만, 넘어오는 값은 우리 MEMBER_ID.
 */
@Service
@RequiredArgsConstructor
public class DbUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
        Member m = memberMapper.findById(memberId);
        if (m == null) throw new UsernameNotFoundException(memberId);

        List<String> roles = memberMapper.findRoles(m.getMemberId());   // ["USER", "ADMIN"]

        return User.builder()
                .username(m.getMemberId())
                .password(m.getPassword())                              // 저장된 해시 그대로
                .disabled(!m.isEnabled())
                .authorities(roles.stream()
                        .map(r -> "ROLE_" + r)                          // ★ ROLE_ 접두어
                        .toArray(String[]::new))
                .build();
    }
}
