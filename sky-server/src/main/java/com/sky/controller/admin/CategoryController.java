package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Api(tags = "分类相关接口")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    @ApiOperation("分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO) {
        log.info("分页查询分类信息:{}", categoryPageQueryDTO);
        PageResult pageResult = categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);
    }
    @PostMapping
    @ApiOperation("新增分类")
    public Result save(@RequestBody CategoryDTO CategoryDTO) {
        log.info("新增分类信息:{}", CategoryDTO);
        categoryService.save(CategoryDTO);
        return Result.success();
    }
    @DeleteMapping
    @ApiOperation("删除发类")
    public Result deleteByid(Long id) {
        log.info("根据id删除分类信息:{}",id);
        categoryService.deleteByid(id);
        return Result.success();
    }
    @PutMapping
    @ApiOperation("修改分类")
    public Result update(@RequestBody CategoryDTO CategoryDTO) {
        log.info("修改套餐信息:{}", CategoryDTO);
        categoryService.update(CategoryDTO);
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result StartOrStop(@PathVariable("status") Integer status,Long id) {
        categoryService.StartOrStop(status,id);
        return Result.success();
    }
    @GetMapping("list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(Integer type){
        log.info("根据分类展示菜品：{}",type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
