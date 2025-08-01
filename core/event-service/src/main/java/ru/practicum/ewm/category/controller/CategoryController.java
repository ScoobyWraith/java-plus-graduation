package ru.practicum.ewm.category.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.category.dto.CategoryParams;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.service.CategoryService;
import ru.practicum.ewm.common.dto.event.CategoryDto;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CategoryController {
    private static final String ADMIN_API_PREFIX = "/admin/categories";
    private static final String PUBLIC_API_PREFIX = "/categories";
    private static final String CAT_ID_PATH = "/{cat-id}";
    private static final String CAT_ID = "cat-id";

    private final CategoryService categoryService;

    @GetMapping(PUBLIC_API_PREFIX)
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryDto> getCategory(@RequestParam(defaultValue = "0") Long from,
                                         @RequestParam(defaultValue = "10") Long size) {
        log.info("Received GET request for all category with param: from: {}, size: {}", from, size);

        CategoryParams categoryParams = CategoryParams.builder()
                .from(from)
                .size(size)
                .build();

        return categoryService.getCategory(categoryParams);
    }

    @GetMapping(PUBLIC_API_PREFIX + CAT_ID_PATH)
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto getCategoryById(@PathVariable(CAT_ID) Long catId) {
        log.info("Received GET request for category with id: {}", catId);
        return categoryService.getCategoryById(catId);
    }

    @PostMapping(ADMIN_API_PREFIX)
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody NewCategoryDto newCategoryDto) {
        log.info("Received POST request to create category: {}", newCategoryDto);
        return categoryService.createCategory(newCategoryDto);
    }

    @DeleteMapping(ADMIN_API_PREFIX + CAT_ID_PATH)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable(CAT_ID) Long catId) {
        log.info("Received DELETE request to delete category with id: {}", catId);
        categoryService.deleteCategory(catId);
    }

    @PatchMapping(ADMIN_API_PREFIX + CAT_ID_PATH)
    @ResponseStatus(HttpStatus.OK)
    public CategoryDto updateCategory(@PathVariable(CAT_ID) Long catId, @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Received PATCH request to update category with id : {}, update : {}", catId, newCategoryDto);

        CategoryParams categoryParams = CategoryParams.builder()
                .newCategoryDto(newCategoryDto)
                .catId(catId)
                .build();

        return categoryService.updateCategory(categoryParams);
    }
}
