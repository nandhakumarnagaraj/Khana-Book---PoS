package com.khanabook.pos.service;

import com.khanabook.pos.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {

	List<Category> getAllCategories();

	List<Category> getActiveCategories();

	Optional<Category> getCategoryById(Long id);

	Category createCategory(Category category);

	Category updateCategory(Long id, Category category);

	void deleteCategory(Long id);

	Category deactivateCategory(Long id);

	Category activateCategory(Long id);
}
