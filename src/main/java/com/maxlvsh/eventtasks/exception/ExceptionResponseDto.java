package com.maxlvsh.eventtasks.exception;

import java.time.LocalDateTime;

public record ExceptionResponseDto (
        String massage,
        String detailMassage,
        LocalDateTime time
){
}
