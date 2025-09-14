package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.result.Result;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;


public interface DishService {

    public void saveWithFlavor(@RequestBody DishDTO dishDTO);
}
