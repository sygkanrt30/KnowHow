package ru.tbank.knowhow.core_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DeferredCsrfToken;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class GetCsrfTokenFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private CsrfTokenRepository csrfTokenRepository;

    @Mock
    private DeferredCsrfToken deferredCsrfToken;

    @Mock
    private RequestMatcher requestMatcher;

    private GetCsrfTokenFilter filter;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() {
        var objectMapper = new ObjectMapper();
        responseWriter = new StringWriter();

        filter = new GetCsrfTokenFilter()
                .csrfTokenRepository(csrfTokenRepository)
                .requestMatcher(requestMatcher)
                .objectMapper(objectMapper);
    }

    @Test
    void doFilterInternal_whenRequestMatchesCsrfEndpoint_shouldReturnCsrfToken() throws Exception {
        when(requestMatcher.matches(request)).thenReturn(true);
        when(csrfTokenRepository.loadDeferredToken(request, response)).thenReturn(deferredCsrfToken);
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        filter.doFilterInternal(request, response, filterChain);

        verify(requestMatcher).matches(request);
        verify(csrfTokenRepository).loadDeferredToken(request, response);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenRequestDoesNotMatchCsrfEndpoint_shouldContinueFilterChain() throws Exception {
        when(requestMatcher.matches(request)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        verify(requestMatcher).matches(request);
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(csrfTokenRepository);
        verify(response, never()).setStatus(anyInt());
        verify(response, never()).setContentType(anyString());
    }

    @Test
    void doFilterInternal_whenCsrfTokenIsNull_shouldThrowNPE() throws Exception {
        when(requestMatcher.matches(request)).thenReturn(true);
        when(csrfTokenRepository.loadDeferredToken(request, response)).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));


        assertThrows(NullPointerException.class,
                () -> filter.doFilterInternal(request, response, filterChain));
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_whenIOExceptionOccurs_shouldThrowException() throws Exception {
        when(requestMatcher.matches(request)).thenReturn(true);
        when(response.getWriter()).thenThrow(new IOException("Test IO exception"));

        try {
            filter.doFilterInternal(request, response, filterChain);
            assert false : "Expected IOException";
        } catch (IOException ignored) {}
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_withDefaultConfiguration_shouldWorkCorrectly() throws Exception {
        var defaultFilter = new GetCsrfTokenFilter();

        defaultFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}