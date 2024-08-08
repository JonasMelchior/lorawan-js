package com.github.jonasmelchior.js.json;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class JsonPage<T> extends org.springframework.data.domain.PageImpl<T> {
    public JsonPage(final Page<T> page, final Pageable pageable) {
        super(page.getContent(), pageable, page.getTotalElements());
    }

    @JsonView(Views.Public.class)
    public int getTotalPages() {
        return super.getTotalPages();
    }

    @JsonView(Views.Public.class)
    public long getTotalElements() {
        return super.getTotalElements();
    }

    @JsonView(Views.Public.class)
    public boolean hasNext() {
        return super.hasNext();
    }

    @JsonView(Views.Public.class)
    public boolean isLast() {
        return super.isLast();
    }

    @JsonView(Views.Public.class)
    public boolean hasContent() {
        return super.hasContent();
    }

    @JsonView(Views.Public.class)
    public List<T> getContent() {
        return super.getContent();
    }
}