package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

// 修正：改为接口
@Mapper
public interface DishFlavorMapper {  // 此处将 class 改为 interface

    @Delete("delete from dish_flavor where id =#{id}")
    public void deleteByDishId(Long id);

    @Insert("insert into dish_flavor(dish_id, name, value) VALUES " +
            "(#{dishId},#{name},#{value})")
    @AutoFill(OperationType.INSERT)
    public void insertBatch(List<DishFlavor> flavors);

    @Select("select * from dish_flavor where dish_id =#{id}")
    List<DishFlavor> getByDishId(Long id);
}