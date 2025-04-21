package com.dev.e_shop.dto;

public record PaginationResponse (
        int currentPage,
        int totalPage,
        long totalItems,
        int pageSize){}
