package com.itheima.mp.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mp.domain.dto.PageDTO;
import com.itheima.mp.domain.dto.UserFormDTO;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.query.UserQuery;
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
    @GetMapping("/users")
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

    @ApiOperation("根据指定复杂条件查询用户列表")
    @GetMapping("/lists")
    public List<UserVO> queryUsers(UserQuery userQuery){
        List<User> userList = userService.queryUsers(userQuery.getName(), userQuery.getStatus(), userQuery.getMinBalance(), userQuery.getMaxBalance());
        return BeanUtil.copyToList(userList, UserVO.class);
    }

    @ApiOperation("查询用户同时查询其对应地址信息接口")
    @GetMapping("/useraddress/{id}")
    public UserVO queryUserAndAddressById(@ApiParam("用户id") @PathVariable("id") Long id) {
        return userService.queryUserAndAddressById(id);
    }

    @ApiOperation("根据id批量查询用户及其地址接口")
    @GetMapping("/batchusers")
    public List<UserVO> queryUserAndAddressByIds(@ApiParam("用户id集合") @RequestParam("ids") List<Long> ids){
        return userService.queryBatchUserAndAddressByIds(ids);
    }

    @ApiOperation("以分页形式根据指定条件查询用户信息")
    @GetMapping("/page")
    public PageDTO<UserVO> queryUsersPage(UserQuery query){
        return userService.queryUserPage(query);
    }

}
