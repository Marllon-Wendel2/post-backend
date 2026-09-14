package com.natura.post.domain.exception;

import java.time.LocalDateTime;

public record ErrorResponse(
                int status,
                String message,
                LocalDateTime timestamp) {
}
