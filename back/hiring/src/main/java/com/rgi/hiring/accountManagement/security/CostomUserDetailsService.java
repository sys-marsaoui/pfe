package com.rgi.hiring.accountManagement.security;

import com.rgi.hiring.accountManagement.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



@Service
public class CostomUserDetailsService implements UserDetailsService {

    private final UserRepository repository;

    public CostomUserDetailsService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) repository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
