package com.peti.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

//    @GetMapping("/")
//    public String root() {
//        // Redirect to /page/login
//        return "redirect:/page/login";
//    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        // Redirect authenticated users to wardrobe, unauthenticated to login
        return authentication != null ? "redirect:/wardrobe" : "redirect:/public/login";
    }

    @GetMapping("/public/login")
    public String loginPage() {
        return "login"; // Renders login.html
    }

    @GetMapping("/public/register")
    public String registerPage() {
        return "register"; // Renders register.html
    }
    @GetMapping("/public/forgotPassword")
    public String ForgotPasswordPage() {
        return "forgot-password"; // Renders forgot-password.html
    }
    @GetMapping("/public/resetPassword")
    public String ResetPasswordPage() {
        return "reset-password"; // Renders reset-password.html
    }
    @GetMapping("/public/verifyOTP")
    public String VerifyOTPage() {
        return "verify-otp"; // Renders verify-otp.html
    }

    @GetMapping("/api/page/dashboard")
    public String dashboardPage() {
        return "dashboard"; // Renders dashboard.html
    }

    @GetMapping("/api/page/addItem")
    public String AddItemPage() {
        return "upload";
    }


    @GetMapping("/api/page/category")
    public String CategoryPage() {
        return "categories";
    }

    @GetMapping("/api/page/users")
    public String UserPage() {
        return "user-managment"; // Renders user-management.html
    }
    @GetMapping("/api/page/outfits")
    public String OutfitPage() {
        return "outfit"; // Renders outfits.html
    }
    @GetMapping("/api/page/reports")
    public String ReportPage() {
        return "reports"; // Renders reports.html
    }
    @GetMapping("/api/page/sugesstion")
    public String SuggestionPage() {
        return "suggestion"; // Renders suggestion.html
    }
    @GetMapping("/api/page/adminDashboard")
    public String AdminDashboardPage() {
        return "admin-dashboard"; // Renders admin-dashboard.html
    }
    @GetMapping("/api/page/analytics")
    public String AnalyticsPage() {
        return "analytics"; // Renders analytics.html
    }
    @GetMapping("/api/page/profile")
    public String ProfilePage() {
        return "profile"; // Renders profile.html
    }

}