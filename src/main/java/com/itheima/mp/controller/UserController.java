package com.itheima.mp.controller;

import cn.hutool.core.bean.BeanUtil;
import com.itheima.mp.domain.dto.UserFormDTO;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.vo.UserVO;
import com.itheima.mp.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@Api("用户管理接口")
@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    @ApiOperation("新增用户接口")
    @PostMapping
    public void saveUser(@RequestBody UserFormDTO userDTO){
//        1. DTO拷贝到PO
        User user = BeanUtil.copyProperties(userDTO, User.class);
//        2. 新增
        userService.save(user);
    }

    @ApiOperation("删除用户接口")
    @DeleteMapping("{id}")
    public void deleteUser(@ApiParam("用户id") @PathVariable("id") Long id){
        userService.removeById(id);
    }

    @ApiOperation("查询用户接口")
    @GetMapping("{id}")
    public UserVO queryUserById(@ApiParam("用户id") @PathVariable("id") Long id){
//        1. 查询用户PO
        User user = userService.getById(id);
//        2. 把PO拷贝到VO
        return BeanUtil.copyProperties(user, UserVO.class);
    }

    @ApiOperation("根据id批量查询用户接口")
    @GetMapping("/users}")
    public List<UserVO> queryUserByIds(@ApiParam("用户id集合") @RequestParam("ids") List<Long> ids){
//        1. 查询用户PO
        List<User> users = userService.listByIds(ids);
//        2. 把PO拷贝到VO
        return BeanUtil.copyToList(users, UserVO.class);
    }

    @ApiOperation("根据id扣减用户对应账户的余额")
    @PutMapping("{id}/deduction/{money}")
    public void deductBalance(
            @ApiParam("用户id") @PathVariable("id") Long id,
            @ApiParam("扣减量") @PathVariable("money") Integer money
    ){
        userService.deductMoney(id, money);
    }

}
