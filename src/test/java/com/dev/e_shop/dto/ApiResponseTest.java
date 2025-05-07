package com.dev.e_shop.dto;

import com.dev.e_shop.exception.custom.InvalidHttpStatusException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    @Test
    void createApiResponse_ValidStatus_ShouldNotThrowException() {
        String data = "test";
        int validStatus = 200;

        ApiResponse<String> success = new ApiResponse<>(validStatus, "success", data);

        assertThat(success.status()).isEqualTo(200);
        assertThat(success.message()).isEqualTo("success");
        assertThat(success.data()).isEqualTo("test");
    }

    @Test
    void createApiResponse_InvalidStatus_ShouldThrowException() {
        String data = "test";
        int invalidStatus = 900;

        InvalidHttpStatusException exception = assertThrows(InvalidHttpStatusException.class, () -> {
            ApiResponse<String> success = new ApiResponse<>(invalidStatus, "success", data);
        });

        assertThat(exception.getErrorDetail()).isEqualTo("Invalid HTTP status code: " + invalidStatus);
    }
}