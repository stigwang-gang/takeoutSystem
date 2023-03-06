package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetMealService extends IService<Setmeal> {
    public  void saveWithDish(SetmealDto setmealDto);

    public  void removeWithDish(List<Long>ids);

    public  void upadateWithDish(Integer status,List<Long>ids);
}
