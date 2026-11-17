package com.queuezero.backend.category;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final ServiceCategoryRepository repository;

    public CategoryService(ServiceCategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return repository.findAll(Sort.by("id")).stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
