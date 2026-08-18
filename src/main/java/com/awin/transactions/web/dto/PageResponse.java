package com.awin.transactions.web.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * @param content       the transactions on this page.
 * @param page          the current page number, zero-based.
 * @param size          the requested page size.
 * @param totalElements the total number of matching transactions across all pages.
 * @param totalPages    the total number of pages.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    /** @return a response built from a Spring Data {@link Page}. */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
