package com.maxlvsh.eventtasks.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

public record EventRequest (
        @Null(message = "id should be empty")
        Long id,

        @NotNull(message = "externalId is mandatory")
        String externalId,

        @NotNull(message = "type is mandatory")
        String type,

        @NotNull(message = "payload is mandatory")
        String payload
) {}


