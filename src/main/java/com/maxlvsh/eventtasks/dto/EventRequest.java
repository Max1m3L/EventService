package com.maxlvsh.eventtasks.dto;

import jakarta.validation.constraints.NotBlank;

public class EventRequest {

    @NotBlank(message = "externalId is mandatory")
    private String externalId;

    @NotBlank(message = "type is mandatory")
    private String type;

    @NotBlank(message = "payload is mandatory")
    private String payload;

    // Геттеры и сеттеры
    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
