package com.vibelink.exception;

/**
 * 404 (리소스 없음) 예외 전용 커스텀 예외 클래스
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
