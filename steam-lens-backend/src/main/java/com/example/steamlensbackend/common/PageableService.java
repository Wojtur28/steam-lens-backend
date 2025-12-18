package com.example.steamlensbackend.common;

import com.example.steamlensbackend.common.wrappers.Meta;
import com.example.steamlensbackend.common.wrappers.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;

public class PageableService {
    public static <T>PagedResponse<List<T>> paginate(Collection<T> data, Pageable pageable) {
        int totalPages = (int) Math.ceil((double) data.size() / pageable.getPageSize());
        int offset = pageable.getPageNumber() * pageable.getPageSize();

        List<T> content = data.stream()
                .skip(offset)
                .limit(pageable.getPageSize())
                .toList();

        return PagedResponse.of(content, new Meta(pageable.getPageNumber(), pageable.getPageSize(), totalPages, data.size()));
    }
}
