package org.example.auctionbackend.service;

import org.example.auctionbackend.model.User;
import org.example.auctionbackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Utilisateur non trouvé avec le nom d'utilisateur : " + username
                ));

        // Conversion des rôles (String) en GrantedAuthority
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        // Construire le UserDetails en tenant compte du verrouillage de compte
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                /* enabled */               true,
                /* accountNonExpired */     true,
                /* credentialsNonExpired */ true,
                /* accountNonLocked */      !user.isAccountLocked(),
                authorities
        );
    }
}
