package com.peti.security;

import com.peti.security.JwtHelper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter
{

    private Logger logger = LoggerFactory.getLogger(OncePerRequestFilter.class);
    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException
    {
        // Skip authentication for public paths
        String requestPath = request.getRequestURI();
        if (requestPath.equals("/") || requestPath.startsWith("/public/") ||
                requestPath.startsWith("/css/") || requestPath.startsWith("/js/") ||
                requestPath.startsWith("/images/") || requestPath.startsWith("/static/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        String username = null;

        if (token != null) {
            try {
                username = this.jwtHelper.getUsernameFromToken(token);
                logger.debug("Token extracted successfully for user: {}", username);
            } catch (IllegalArgumentException e) {
                logger.warn("Illegal Argument while fetching the username: {}", e.getMessage());
            } catch (ExpiredJwtException e) {
                logger.warn("JWT token is expired: {}", e.getMessage());
            } catch (MalformedJwtException e) {
                logger.warn("JWT token is malformed: {}", e.getMessage());
            } catch (Exception e) {
                logger.error("Error processing JWT token: {}", e.getMessage());
            }
        } else {
            logger.debug("No JWT token found for path: {}", requestPath);
        }

        // Set authentication if username is found and no authentication is set
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (this.jwtHelper.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    logger.debug("Authentication set for user: {}", username);
                } else {
                    logger.info("Token validation failed for user: {}", username);
                }
            } catch (Exception e) {
                logger.error("Error setting authentication for user: {}", username, e);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header, session, or cookies
     */
    private String extractToken(HttpServletRequest request) {
        // First try Authorization header (for API calls)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // Then try session (for template requests after login)
        HttpSession session = request.getSession(false);
        if (session != null) {
            String sessionToken = (String) session.getAttribute("jwtToken");
            if (sessionToken != null) {
                return sessionToken;
            }
        }

        // Try cookies as fallback
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}