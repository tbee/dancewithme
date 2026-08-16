package org.tbee.dancewithme.infrastructure.vdn.security;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.DancerRepository;

import java.util.Optional;

@Component
public class SecurityService {

    private final DancerRepository dancerRepository;

    public SecurityService(DancerRepository dancerRepository) {
        this.dancerRepository = dancerRepository;
    }

    public Optional<Dancer> currentDancer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return dancerRepository.findByEmail(authentication.getName());
    }

    public boolean isLoggedIn() {
        return currentDancer().isPresent();
    }

    public void logout() {
        UI.getCurrent().getPage().setLocation("/");
        new SecurityContextLogoutHandler().logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
    }
}
