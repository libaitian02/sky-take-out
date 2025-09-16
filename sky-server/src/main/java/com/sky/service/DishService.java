package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


public interface DishService {

    public void saveWithFlavor(@RequestBody DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void delete(List<Long> ids);
}
