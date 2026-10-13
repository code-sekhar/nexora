package com.nexora.auth.dto;

import com.nexora.business.entity.Business;
import com.nexora.user.dto.UserResponse;

public class RegisterResponse {

    private String token;
    private UserResponse user;
    private Business business;

    public RegisterResponse() {
    }

    public RegisterResponse(
            String token,
            UserResponse user,
            Business business) {

        this.token = token;
        this.user = user;
        this.business = business;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public Business getBusiness() {
        return business;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }
}