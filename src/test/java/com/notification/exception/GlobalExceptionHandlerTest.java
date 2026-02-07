package com.notification.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.format.DateTimeParseException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler
 */
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleIllegalArgument() {
        // Arrange
        String errorMessage = "Invalid argument provided";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleIllegalState() {
        // Arrange
        String errorMessage = "Invalid state detected";
        IllegalStateException exception = new IllegalStateException(errorMessage);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalState(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertEquals(HttpStatus.CONFLICT.value(), response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleValidationErrors() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError1 = new FieldError("notificationRequest", "email", "Email is required");
        FieldError fieldError2 = new FieldError("notificationRequest", "subject", "Subject cannot be empty");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Request validation failed", response.getBody().getMessage());
        assertEquals("Validation Failed", response.getBody().getError());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals(2, response.getBody().getValidationErrors().size());
        assertEquals("Email is required", response.getBody().getValidationErrors().get("email"));
        assertEquals("Subject cannot be empty", response.getBody().getValidationErrors().get("subject"));
    }

    @Test
    void testHandleDateTimeParseException() {
        // Arrange
        DateTimeParseException exception = new DateTimeParseException("Invalid format", "2026-13-45", 0);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDateTimeParseException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("ISO 8601 format"));
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatus());
        assertEquals("Bad Request", response.getBody().getError());
    }

    @Test
    void testHandleGenericException() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected error occurred");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void testHandleIllegalArgument_NullMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException((String) null);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalArgument(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalState_EmptyMessage() {
        // Arrange
        IllegalStateException exception = new IllegalStateException("");

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleIllegalState(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("", response.getBody().getMessage());
    }

    @Test
    void testHandleValidationErrors_SingleError() {
        // Arrange
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError = new FieldError("user", "username", "Username is required");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidationErrors(exception);

        // Assert
        assertEquals(1, response.getBody().getValidationErrors().size());
        assertTrue(response.getBody().getValidationErrors().containsKey("username"));
    }

    @Test
    void testErrorResponseBuilder() {
        // Test ErrorResponse builder pattern
        ErrorResponse error = ErrorResponse.builder()
            .status(400)
            .error("Test Error")
            .message("Test Message")
            .build();

        assertNotNull(error);
        assertEquals(400, error.getStatus());
        assertEquals("Test Error", error.getError());
        assertEquals("Test Message", error.getMessage());
    }
}
