package id.ac.ui.cs.advprog.auth.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import id.ac.ui.cs.advprog.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private FilterChain filterChain;
    @Mock private Claims claims;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter counter;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.<String>any()))
                .thenReturn(counter);
        when(meterRegistry.counter(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.<String>any(),
                org.mockito.ArgumentMatchers.<String>any()))
                .thenReturn(counter);
        when(meterRegistry.counter(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.<String>any(),
                org.mockito.ArgumentMatchers.<String>any(),
                org.mockito.ArgumentMatchers.<String>any(),
                org.mockito.ArgumentMatchers.<String>any()))
                .thenReturn(counter);
        filter = new JwtAuthenticationFilter(jwtService, meterRegistry);
        SecurityContextHolder.clearContext();
    }

    @Test
    void passesRequestThroughWhenNoAuthHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void passesRequestThroughWhenAuthHeaderNotBearer() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void passesRequestThroughWhenTokenInvalid() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractAllClaims("invalid-token")).thenThrow(new JwtException("invalid"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void setsAuthenticationWhenTokenValid() throws Exception {
        UUID userId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(claims.getSubject()).thenReturn(userId.toString());
        when(claims.get("role", String.class)).thenReturn("BURUH");

        when(jwtService.extractAllClaims("valid-token")).thenReturn(claims);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(userId, auth.getPrincipal());
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BURUH")));
    }

    @Test
    void shouldNotFilterPublicEndpoints() {
        MockHttpServletRequest registerReq = new MockHttpServletRequest();
        registerReq.setServletPath("/api/v1/auth/register");
        assertTrue(filter.shouldNotFilter(registerReq));

        MockHttpServletRequest loginReq = new MockHttpServletRequest();
        loginReq.setServletPath("/api/v1/auth/login");
        assertTrue(filter.shouldNotFilter(loginReq));

        MockHttpServletRequest googleReq = new MockHttpServletRequest();
        googleReq.setServletPath("/api/v1/auth/google");
        assertTrue(filter.shouldNotFilter(googleReq));
    }

    @Test
    void shouldFilterProtectedEndpoints() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/v1/auth/logout");

        // shouldNotFilter returns false → the filter DOES run
        assertTrue(!filter.shouldNotFilter(request));
    }
}
