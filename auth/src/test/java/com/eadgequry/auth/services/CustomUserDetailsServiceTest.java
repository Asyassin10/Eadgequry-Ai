package com.eadgequry.auth.services;

import com.eadgequry.auth.model.User;
import com.eadgequry.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setProvider("local");
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("john@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("john@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("$2a$10$encodedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities()).contains(new SimpleGrantedAuthority("USER"));
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();

        verify(userRepository).findByEmail("john@example.com");
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nonexistent@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found with email: nonexistent@example.com");

        verify(userRepository).findByEmail("nonexistent@example.com");
    }

    @Test
    void customUserDetails_GetAuthorities() {
        CustomUserDetailsService.CustomUserDetails customUserDetails =
                new CustomUserDetailsService.CustomUserDetails(testUser);

        assertThat(customUserDetails.getAuthorities()).hasSize(1);
        GrantedAuthority authority = customUserDetails.getAuthorities().iterator().next();
        assertThat(authority.getAuthority()).isEqualTo("USER");
    }

    @Test
    void customUserDetails_GetPassword() {
        CustomUserDetailsService.CustomUserDetails customUserDetails =
                new CustomUserDetailsService.CustomUserDetails(testUser);

        assertThat(customUserDetails.getPassword()).isEqualTo("$2a$10$encodedPassword");
    }

    @Test
    void customUserDetails_GetUsername() {
        CustomUserDetailsService.CustomUserDetails customUserDetails =
                new CustomUserDetailsService.CustomUserDetails(testUser);

        assertThat(customUserDetails.getUsername()).isEqualTo("john@example.com");
    }

    @Test
    void customUserDetails_IsAccountNonExpired() {
        CustomUserDetailsService.CustomUserDetails customUserDetails =
                new CustomUserDetailsService.CustomUserDetails(testUser);

        assertThat(customUserDetails.isAccountNonExpired()).isTrue();
    }

    @Test
    void customUserDetails_IsAccountNonLocked() {
        CustomUserDetailsService.CustomUserDetails customUserDetails =
                new CustomUserDetailsService.CustomUserDetails(testUser);

        assertThat(customUserDetails.isAccountNonLocked()).isTrue();
    }

    @Test
    void customUserDetails_IsCredentialsNonExpired() {
        CustomUserDetailsService.CustomUserDetails customUserDetails =
                new CustomUserDetailsService.CustomUserDetails(testUser);

        assertThat(customUserDetails.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void customUserDetails_IsEnabled() {
        CustomUserDetailsService.CustomUserDetails customUserDetails =
                new CustomUserDetailsService.CustomUserDetails(testUser);

        assertThat(customUserDetails.isEnabled()).isTrue();
    }
}
