package net.darkness.mosaic.configuration;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(requests ->
                        requests
                                .requestMatchers("/admin/**").authenticated()
                                .requestMatchers("/**").permitAll()
                                .anyRequest().permitAll()
                )
                .formLogin(AbstractAuthenticationFilterConfigurer::permitAll)
                .logout(LogoutConfigurer::permitAll)
                .build();
    }
}
