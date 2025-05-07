package com.dev.e_shop.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class PaginationDto {

    public static final Map<String ,String> fieldMap = new HashMap<>();


    // Trường String để nhận dữ liệu đầu vào và kiểm tra định dạng
    @Pattern(regexp = "\\d+", message = "Page must be a valid non-negative number")
    private String page = "0";

    @Pattern(regexp = "\\d+", message = "Size must be a valid positive number")
    private String size = "1";

    @Min(value = 0, message = "Page must be greater than or equal to 0")
    private Integer pageInt;

    @Min(value = 1, message = "Size must be greater than or equal to 1")
    @Max(value = 20, message = "Size must not exceed 20")
    private Integer sizeInt;

    PaginationDto() {
        this.pageInt = Integer.parseInt(this.page);
        this.sizeInt = Integer.parseInt(this.size);

        fieldMap.put("sizeInt", "size");
        fieldMap.put("size", "size");
        fieldMap.put("pageInt", "page");
        fieldMap.put("page", "page");
    }

    public void setPage(String page) {
        this.page = (page != null) ? page : "0";
        try {
            this.pageInt = Integer.parseInt(this.page);
        } catch (NumberFormatException e) {
            this.pageInt = null;
        }
    }

    public void setSize(String size) {
        this.size = (size != null) ? size : "5";
        try {
            this.sizeInt = Integer.parseInt(this.size);
        } catch (NumberFormatException e) {
            this.sizeInt = null;
        }
    }

    public static boolean contains(String name) {
        return fieldMap.containsKey(name);
    }

    public static String getFieldName(String name) {
        return fieldMap.get(name);
    }
}
