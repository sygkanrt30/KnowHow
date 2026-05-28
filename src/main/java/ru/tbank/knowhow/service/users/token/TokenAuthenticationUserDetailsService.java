package ru.tbank.knowhow.service.users.token;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import ru.tbank.knowhow.model.users.auth.Token;
import ru.tbank.knowhow.service.users.GetUserService;

@RequiredArgsConstructor
public class TokenAuthenticationUserDetailsService implements
        AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

    private final GetUserService getUserService;

    @Override
    public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken authenticationToken)
            throws UsernameNotFoundException {
        if (authenticationToken.getPrincipal() instanceof Token token) {
            return getUserService.getByUsernameOrElseThrow(token.username());
        }
        throw new UsernameNotFoundException("Principal must be of type Token");
    }
}
