package com.finance.dto.user;

import jakarta.validation.constraints.NotNull;

public class UpdateStatusRequest {

    @NotNull
    private Boolean active;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
