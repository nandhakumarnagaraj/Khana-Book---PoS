package com.khanabook.pos.service.impl;

import com.khanabook.pos.exception.ResourceNotFoundException;
import com.khanabook.pos.model.Category;
import com.khanabook.pos.repository.CategoryRepository;
import com.khanabook.pos.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override @Cacheable("categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override @Cacheable("categories")
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override @Transactional @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Category with name " + category.getName() + " already exists.");
        }
        category.setCreatedAt(LocalDateTime.now());
        category.setActive(true);
        return categoryRepository.save(category);
    }

    @Override @Transactional @CacheEvict(value = "categories", allEntries = true)
    public Category updateCategory(Long id, Category updatedCategory) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!existingCategory.getName().equals(updatedCategory.getName()) &&
            categoryRepository.existsByName(updatedCategory.getName())) {
            throw new IllegalArgumentException("Category with name " + updatedCategory.getName() + " already exists.");
        }

        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        existingCategory.setDisplayOrder(updatedCategory.getDisplayOrder());
        existingCategory.setActive(updatedCategory.getActive()); // Allow updating active status

        return categoryRepository.save(existingCategory);
    }

    @Override @Transactional @CacheEvict(value = "categories", allEntries = true)
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override @Transactional @CacheEvict(value = "categories", allEntries = true)
    public Category deactivateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setActive(false);
        return categoryRepository.save(category);
    }

    @Override @Transactional @CacheEvict(value = "categories", allEntries = true)
    public Category activateCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        category.setActive(true);
        return categoryRepository.save(category);
    }
}
