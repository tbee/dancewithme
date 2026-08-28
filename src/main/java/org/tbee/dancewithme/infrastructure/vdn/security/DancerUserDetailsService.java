package org.tbee.dancewithme.infrastructure.vdn.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.util.List;

@Service
public class DancerUserDetailsService implements UserDetailsService {

    public static final String ROLE_USER = "USER";

    private final DancerRepository dancerRepository;

    public DancerUserDetailsService(DancerRepository dancerRepository) {
        this.dancerRepository = dancerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Dancer dancer = dancerRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        // a dancer who has not confirmed their email address cannot log in yet; DancerAuthenticationProvider turns
        // that into an EmailNotConfirmedException, but only after the password has been verified
        boolean enabled = dancer.emailConfirmedAt() != null;
        return new User(dancer.email(), dancer.password(), enabled, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + ROLE_USER)));
    }
}
