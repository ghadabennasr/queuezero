package com.queuezero.backend.category;

public record CategoryResponse(Long id, String code, String name, String icon) {

    static CategoryResponse from(ServiceCategory c) {
        return new CategoryResponse(c.getId(), c.getCode(), c.getName(), c.getIcon());
    }
}
