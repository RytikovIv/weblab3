package com.example.casdoor_demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@RestController
public class AuthController {

    @Value("${casdoor.endpoint}")
    private String casdoorEndpoint;
    @Value("${casdoor.client-id}")
    private String clientId;
    @Value("${casdoor.client-secret}")
    private String clientSecret;
    @Value("${casdoor.redirect-uri}")
    private String redirectUri;

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String authorizeUrl = casdoorEndpoint + "/login/oauth/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + redirectUri
                + "&scope=openid profile email";
        response.sendRedirect(authorizeUrl);
    }

    @GetMapping("/callback")
    public void callback(@RequestParam String code, HttpServletResponse response) throws IOException {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(
                casdoorEndpoint + "/api/login/oauth/access_token",
                request,
                Map.class
        );

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        Cookie cookie = new Cookie("access_token", accessToken);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(true);
        response.addCookie(cookie);

        response.sendRedirect("/");
    }

    @GetMapping("/logout")
    public void logout(HttpServletResponse response) throws IOException {
        Cookie cookie = new Cookie("access_token", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(false);
        cookie.setSecure(true);
        response.addCookie(cookie);

        response.sendRedirect("https://localhost:8443/logout?redirect_uri=https://localhost:7443/");
    }

}