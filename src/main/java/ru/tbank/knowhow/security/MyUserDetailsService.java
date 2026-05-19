package ru.tbank.knowhow.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.tbank.knowhow.service.user.GetUserInfoService;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

    private final GetUserInfoService getUserInfoService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return getUserInfoService.getByUsernameOrElseThrow(username);
    }
}
