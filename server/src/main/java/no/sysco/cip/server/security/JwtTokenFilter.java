package no.sysco.cip.server.security;

import com.google.auth.oauth2.TokenVerifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        System.out.println("-----HEADERS------");
        request.getHeaderNames().asIterator().forEachRemaining(System.out::println);
        System.out.println("-----HEADERS------");

        // Get authorization header and validate
        final var header = request.getHeader(HttpHeaders.AUTHORIZATION);
        // todo : GCP alters the Authorization : Bearer to x-goog-iap-jwt-assertion header so that should be changed
        // at server side
        if (header == null || header.isBlank() || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        // Get jwt token and validate
        final var split = header.split(" ");
        if (split.length != 2) {
            chain.doFilter(request, response);
            return;
        }
        var token = split[1].trim();

        final var verifier = TokenVerifier.newBuilder()
            // todo : how to resolve this?
            .setCertificatesLocation("https://www.googleapis.com/robot/v1/metadata/x509/dev-iap-user%40cip-kubernetes-dev.iam.gserviceaccount.com")
            .build();

        try {
            var payload = verifier.verify(token);
            var userDetails = new User(payload.getPayload().getSubject(), "", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
            var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);

        } catch (TokenVerifier.VerificationException e) {
            e.printStackTrace();
            chain.doFilter(request, response);
        }
    }

}
