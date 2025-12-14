package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface CategoryMapper {

    Page<Category> pagequery(CategoryPageQueryDTO categoryPageQueryDTO);

    @Insert("insert into sky_take_out.category(type, name, sort, status, create_time, update_time, create_user, update_user) VALUES " +
            "(#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    @AutoFill(OperationType.INSERT)
    void insert(Category category);

    @Select("select * from category where id =#{id}")
    Category getByid(Long id);

    @Delete("delete from category where  id =#{id}")
    void deleteByid(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Category category);


    List<Category> list(Integer type);
}
