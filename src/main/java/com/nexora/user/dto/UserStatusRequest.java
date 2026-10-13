//package com.nexora.user.dto;
//
//public class UserStatusRequest {
//
//    private boolean isActive;
//
//    public UserStatusRequest() {
//    }
//
//    public UserStatusRequest(boolean isActive) {
//        this.isActive = isActive;
//    }
//
//    public boolean isActive() {
//        return isActive;
//    }
//
//    public void setActive(boolean active) {
//        isActive = active;
//    }
//}

package com.nexora.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserStatusRequest {

    private boolean active;

    public UserStatusRequest() {
    }

    public UserStatusRequest(boolean active) {
        this.active = active;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return active;
    }

    @JsonProperty("isActive")
    public void setActive(boolean active) {
        this.active = active;
    }
}