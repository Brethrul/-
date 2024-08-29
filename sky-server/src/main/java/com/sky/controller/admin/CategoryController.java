package com.sky.controller.admin;



import com.sky.context.BaseContext;
import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/category")
@Slf4j
public class CategoryController {

    @Autowired
    CategoryService categoryService;

    @PostMapping
    @ApiOperation("新建分类")
    public Result create(@RequestBody CategoryDTO categoryDTO){
        log.info("分类新增，{}",categoryDTO);
        categoryService.create(categoryDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("修改分类状态")
    public Result startOrStop(@PathVariable Integer status, Long id){
        log.info("设置分类状态,{}{}",status,id);
        categoryService.startOrStop(status,id);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation("进行分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){
        log.info("分类分页查询，参数为:{}",categoryPageQueryDTO);
        PageResult pageResult = categoryService.pageQuery(categoryPageQueryDTO);
        return Result.success(pageResult);
    }
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类")
    public Result<List<Category>> list(@RequestParam Integer type){
        List<Category> list = categoryService.listQuery(type);
        return Result.success(list);
    }
    @DeleteMapping
    @ApiOperation("根据id删除分类")
    public Result delete(Long id){
        categoryService.delete(id);
        return Result.success();
    }
    @PutMapping
    @ApiOperation("修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO){
        log.info("用户修改分类信息，{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }
}
