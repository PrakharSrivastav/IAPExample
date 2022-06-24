package no.sysco.cip.server.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.http.HttpServletResponse;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtTokenFilter jwtTokenFilter;

    public SecurityConfig(JwtTokenFilter jwtTokenFilter) {this.jwtTokenFilter = jwtTokenFilter;}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(this.jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
            .cors().disable()
            .httpBasic().disable()
            .csrf().disable()
            .exceptionHandling()
            .authenticationEntryPoint((__, res, ex) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage()))
            .and().sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Set permissions on endpoints
            .authorizeRequests()
            // Our public endpoints
            .antMatchers("/api/public/**").permitAll()
            // Our private endpoints
            .anyRequest().authenticated();
    }

}