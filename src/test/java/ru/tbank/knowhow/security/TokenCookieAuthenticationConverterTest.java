package ru.tbank.knowhow.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.tbank.knowhow.model.users.auth.Token;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class TokenCookieAuthenticationConverterTest {
    @Mock
    private HttpServletRequest request;

    @Mock
    private Function<String, Token> tokenCookieStringDeserializer;

    @Mock
    private Token token;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private TokenCookieAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TokenCookieAuthenticationConverter(tokenCookieStringDeserializer, jdbcTemplate);
    }

    @Test
    void convert_whenCookiesAreNull_shouldReturnNull() {
        when(request.getCookies()).thenReturn(null);

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_whenCookiesAreEmpty_shouldReturnNull() {
        when(request.getCookies()).thenReturn(new Cookie[0]);

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_whenHostAuthTokenCookieExists_shouldReturnAuthentication() {
        var cookieValue = "test-token-value";
        var authCookie = new Cookie(CookieName.HOST_AUTH_TOKEN.name(), cookieValue);
        var otherCookie = new Cookie("other-cookie", "other-value");
        when(request.getCookies()).thenReturn(new Cookie[]{authCookie, otherCookie});
        when(tokenCookieStringDeserializer.apply(cookieValue)).thenReturn(token);

        Authentication result = converter.convert(request);

        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(PreAuthenticatedAuthenticationToken.class);
        assertThat(result.getPrincipal()).isEqualTo(token);
        assertThat(result.getCredentials()).isEqualTo(cookieValue);
    }

    @Test
    void convert_whenMultipleCookiesButNoHostAuthToken_shouldReturnNull() {
        var cookie1 = new Cookie("cookie1", "value1");
        var cookie2 = new Cookie("cookie2", "value2");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie1, cookie2});

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_whenHostAuthTokenCookieHasNullValue_shouldReturnNull() {
        var authCookie = new Cookie(CookieName.HOST_AUTH_TOKEN.name(), null);
        when(request.getCookies()).thenReturn(new Cookie[]{authCookie});

        Authentication result = converter.convert(request);

        assertThat(result).isNull();
    }

    @Test
    void convert_whenHostAuthTokenCookieExistsAndDeserializerReturnsNull_shouldNotThrowException() {
        var cookieValue = "test-token-value";
        var authCookie = new Cookie(CookieName.HOST_AUTH_TOKEN.name(), cookieValue);
        when(request.getCookies()).thenReturn(new Cookie[]{authCookie});
        when(tokenCookieStringDeserializer.apply(cookieValue)).thenReturn(null);

        assertDoesNotThrow(() -> converter.convert(request));
    }

    @Test
    void convert_whenMultipleHostAuthTokenCookiesExist_shouldUseFirstOne() {
        var firstCookieValue = "first-token-value";
        var secondCookieValue = "second-token-value";
        var firstAuthCookie = new Cookie(CookieName.HOST_AUTH_TOKEN.name(), firstCookieValue);
        var secondAuthCookie = new Cookie(CookieName.HOST_AUTH_TOKEN.name(), secondCookieValue);
        when(request.getCookies()).thenReturn(new Cookie[]{firstAuthCookie, secondAuthCookie});
        when(tokenCookieStringDeserializer.apply(firstCookieValue)).thenReturn(token);

        Authentication result = converter.convert(request);

        assertThat(result).isNotNull();
        assertThat(result.getPrincipal()).isEqualTo(token);
        assertThat(result.getCredentials()).isEqualTo(firstCookieValue);
    }
}