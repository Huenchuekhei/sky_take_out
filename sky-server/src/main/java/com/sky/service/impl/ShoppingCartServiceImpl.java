package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Override
    public void save(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        /* 设置用户id，指定当前是哪个用户的购物车数据 */
        shoppingCart.setUserId(BaseContext.getCurrentId());
        /* 查询当前菜品或套餐是否在购物车中 */
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (! shoppingCartList.isEmpty()){
            shoppingCart=shoppingCartList.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCartMapper.updateNumberById(shoppingCart);
        }else {
            //判断当前添加到购物车的是菜品还是套餐
            Long dishId = shoppingCart.getDishId();
            if (dishId != null) {
                Dish dish = dishMapper.getByid(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else {
                Setmeal setmeal = setmealMapper.getByid(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        Long id = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(id)
                .build();
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    @Override
    public void deleteAll() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        /* 设置用户id，指定当前是哪个用户的购物车数据 */
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList.isEmpty()){
            throw new RuntimeException("购物车为空，无法删除");
    }
        shoppingCart=shoppingCartList.get(0);
        Integer number = shoppingCart.getNumber();
        if (number > 1){
            shoppingCart.setNumber(number - 1);
            shoppingCartMapper.updateNumberById(shoppingCart);
        }else {
            //TODO 删除购物车中的该商品
            throw new RuntimeException("删除购物车中的该商品");
        }
    }
}
