package ru.tbank.knowhow.service.token;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.tbank.knowhow.model.Token;
import ru.tbank.knowhow.service.user.GetUserInfoService;

@RequiredArgsConstructor
public class TokenAuthenticationUserDetailsService implements
        AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private final GetUserInfoService getUserInfoService;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken)
            throws UsernameNotFoundException {
        if (authenticationToken.getPrincipal() instanceof Token token) {
            return getUserInfoService.findByUsername(token.username()).orElseThrow(
                    () ->  new UsernameNotFoundException("Username " + token.username() + " not found")
            );
        }
        throw new UsernameNotFoundException("Principal must be of type Token");
    }
}
