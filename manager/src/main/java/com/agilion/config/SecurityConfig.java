package com.agilion.config;

import com.agilion.domain.app.Role;
import com.agilion.services.security.LoggedInUserGetter;
import com.agilion.services.security.SpringSecurityUserGetter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * TODO: SOME THINGS THAT NEED TO BE ANSWERED!
 * 1. Where are the UsernamePasswordAuthentication tokens stored? in memory? Can we store them somewhere else? Should we?
 * 2. Should we store the ENTIRE user object in memory? Or maybe just the ID/username? Maybe we should create a separate
 *    userDetails object?
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public LoggedInUserGetter loggedInUserGetter()
    {
        return new SpringSecurityUserGetter();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers(ignoreedURLS()).permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().loginPage("/login").failureUrl("/login?invalidCredentials=true").defaultSuccessUrl("/home").permitAll()
                .and()
                .logout().permitAll();
    }

    private String[] ignoreedURLS()
    {
        String[] ignoredURLS = {
                "/register",
                "/registerUser",
                "/**/css/**",
                "/**/js/**",
                "/**/webjars/**",
        };

        return ignoredURLS;
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }
}