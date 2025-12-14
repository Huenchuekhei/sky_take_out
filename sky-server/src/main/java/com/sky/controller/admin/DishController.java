package com.sky.controller.admin;

import ch.qos.logback.core.status.StatusBase;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品信息:{}", dishDTO);
        dishService.save(dishDTO);
        //清理缓存数据
        String key = "dish_" + dishDTO.getId();
        clearCache(key);
        return Result.success();
    }
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        log.info("分页查询:{}", dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result delete(@RequestParam List<Long> ids){
        log.info("删除菜品信息：{}",ids);
        dishService.deleteBatch(ids);
        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        clearCache("dish_*");
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result getById(@PathVariable Long id) {
        dishService.getByid(id);
        return Result.success();
    }
    @PutMapping
    @ApiOperation("修改菜品")
    public Result update( DishDTO dishDTO) {
        log.info("修改菜品:{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        clearCache("dish_*");
        return Result.success();
    }
    @PostMapping("/status/{status}")
    @ApiOperation("菜品状态")
    public Result StartOrStop(@PathVariable Integer status, Long id) {
        log.info("菜品状态:{},{}",status,id);
        dishService.StartOrStop(status,id);
        //将所有的菜品缓存数据清理掉，所有以dish_开头的key
        clearCache("dish_*");
        return Result.success();
    }
    @GetMapping("list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        //构造redis中的key，规则：dish_分类id
        String key = "dish_" + categoryId;
        List<DishVO> list = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if (list !=null && list.size()>0) {
            //如果存在，直接返回，无须查询数据库
            return Result.success(list);
        }
        list= dishService.list(categoryId);
        return Result.success(list);
    }
    private void clearCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
