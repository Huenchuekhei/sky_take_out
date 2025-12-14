package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.vo.EmployeeLoginVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    @AutoFill(OperationType.INSERT)
    void insert(Employee employee);


    Page<Employee> pagequery(EmployeePageQueryDTO employeePageQueryDTO);


    @Select("select * from employee where id =#{id}")
    Employee getByid(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Employee employee);
}
