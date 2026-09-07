package com.example.hr.security;

import com.example.hr.domain.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 부록 4. Member 를 감싼 커스텀 UserDetails — 화면에서 이름 등 도메인 정보를 쓰기 위함.
 */
@Getter
public class MemberPrincipal implements UserDetails {

    private final Member member;                 // 도메인 객체 통째로
    private final List<GrantedAuthority> authorities;

    public MemberPrincipal(Member member, List<String> roles) {
        this.member = member;
        this.authorities = roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toList());
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public String getPassword() { return member.getPassword(); }
    @Override public String getUsername() { return member.getMemberId(); }   // 부록 3: MEMBER_ID를 그대로 씀
    @Override public boolean isEnabled() { return member.isEnabled(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    public String getDisplayName() { return member.getMemberName(); }   // 편의 메서드
}
