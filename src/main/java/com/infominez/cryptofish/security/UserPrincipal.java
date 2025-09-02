package com.infominez.cryptofish.security;


import com.infominez.cryptofish.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;



import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements  UserDetails {

    private Integer id;
    private String username;
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Integer id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.authorities = authorities;
    }

    // Factory method to build UserPrincipal from your User entity
    public static UserPrincipal create( User user) {
        // If roles/authorities are needed, map them here
        // For now assuming simple USER role
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                Collections.emptyList() // or fetch roles if you have
        );
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // customize if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // customize if needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // customize if needed
    }

    @Override
    public boolean isEnabled() {
        return true;  // customize if needed
    }
}
