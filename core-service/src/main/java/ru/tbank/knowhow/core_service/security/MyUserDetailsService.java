package ru.tbank.knowhow.core_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.core_service.service.users.GetUserService;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final GetUserService getUserService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserService.getByUsernameOrElseThrow(username);
    }
}
