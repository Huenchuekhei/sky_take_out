package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.CategoryMapper;
import com.sky.result.PageResult;
import com.sky.service.CategoryService;
import org.apache.commons.lang3.exception.ContextedException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl  implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public PageResult page(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(),categoryPageQueryDTO.getPageSize());
        Page<Category> categoryPage  = categoryMapper.pagequery(categoryPageQueryDTO);
        return new PageResult(categoryPage.getTotal(),categoryPage.getResult());
    }

    @Override
    public void save(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        category.setStatus(StatusConstant.DISABLE);
        categoryMapper.insert(category);
    }

    @Override
    public void deleteByid(Long id) {
        Category category = categoryMapper.getByid(id);
        if(category.getStatus().equals(StatusConstant.ENABLE)){
            throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
        }
        categoryMapper.deleteByid(id);
    }

    @Override
    public void StartOrStop(Integer status, Long id) {
        Category existcategory = categoryMapper.getByid(id);
        if (existcategory == null) {
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_NOT_FOUND);
        }
       Category category = Category.builder()
               .id(id)
               .status(status)
               .build();
        categoryMapper.update(category);
    }

    @Override
    public void update(CategoryDTO categoryDTO) {
        Category category = new Category();
        BeanUtils.copyProperties(categoryDTO,category);
        categoryMapper.update(category);
    }

    @Override
    public List<Category> list(Integer type) {
        return categoryMapper.list(type);
    }
}
