package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品加入口味数据 需要两张表 dish 和dish_falvor
    public void saveWithFlavor(DishDto dishDto);

    //根据id 查询菜品信息和口味信息
    public DishDto getByIdWithFlavor(Long id);
    //更新菜品以及口味信息
    public void updateWithFlavor(DishDto dishDto);
}
