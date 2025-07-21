package com.peti.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LogoutController {

    @GetMapping("/public/logout")
    public String logoutPage() {
        return "public/logout";
    }

    @GetMapping("/logout")
    public String logoutGet(HttpServletRequest request, HttpServletResponse response) {
        return logout(request, response);
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        // Clear security context
        SecurityContextHolder.clearContext();
        
        // Invalidate session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        
        // Clear JWT cookie
        Cookie jwtCookie = new Cookie("jwtToken", "");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immediately
        response.addCookie(jwtCookie);

        ResponseCookie jwtCookie1 = ResponseCookie.from("jwtToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie1.toString());
        return "redirect:/public/login?logout=true";
    }
}
