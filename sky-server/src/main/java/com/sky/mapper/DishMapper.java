package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {


    @Insert("insert into dish(name, category_id, price, image, description, create_time, update_time, create_user, update_user) VALUES " +
            " (#{name}, #{categoryId}, #{price}, #{image}, #{description},  #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    Page<Dish> pagequry(DishPageQueryDTO dishPageQueryDTO);

    @Select("select * from  dish where  id =#{id}")
    Dish getByid(Long dishId);

    @Delete("delete  from dish where id =#{id}")
    void deleteById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    List<DishVO> list(Dish dish);

    Integer countByMap(Map map);
}
