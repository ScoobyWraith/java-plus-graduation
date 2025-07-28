package ru.practicum.ewm.category.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.common.dto.event.CategoryDto;

@Component
public class CategoryMapper {

    public static CategoryDto toCategoryDto(Category category) {

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category toCategoryEntity(NewCategoryDto newCategoryDto) {

        return Category.builder()
                .name(newCategoryDto.getName())
                .build();
    }

}

