package com.itheima.mp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.po.UserInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private IUserService userService;

    @Test
    public void testQuery(){

        List<User> users = userService.listByIds(List.of(1L, 2L, 4L));
        users.forEach(System.out::println);

    }

    @Test
    public void testInsert(){
        User user = new User();
        user.setUsername("Yoyo");
        user.setPassword("123");
        user.setPhone("18688990011");
        user.setBalance(200);
        user.setInfo(new UserInfo(24, "英文老师", "female"));
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        userService.save(user);

    }

    @Test
    public void testUpdate(){
        User user = new User();
        user.setPassword("456");
        user.setBalance(2000);
        user.setUpdateTime(LocalDateTime.now());

        LambdaUpdateWrapper<User> userLambdaUpdateWrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, 5L);

        userService.update(user, userLambdaUpdateWrapper);

    }

    @Test
    public void testDelete(){

        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<User>()
                .eq(User::getId, 1793102078061608961L);

        userService.remove(userLambdaQueryWrapper);
    }

    @Test
    public void testPageQuery(){
        int pageNo = 1, pageSize = 2;
//        1. 准备分页条件
        Page<User> page = Page.of(pageNo, pageSize);

//        2. 排序条件
        page.addOrder(new OrderItem("balance", true));
        page.addOrder(new OrderItem("id", true));

//        3. 查询
        Page<User> p = userService.page(page);

//        4. 解析
        long total = p.getTotal();
        System.out.println("total = " + total);
        long pages = p.getPages();
        System.out.println("pages = " + pages);
        List<User> records = p.getRecords();
        records.forEach(System.out::println);
    }

    @Test
    public void testPageQueryUpdate(){
        int pageNo = 1, pageSize = 2;
//        1. 准备分页条件
        Page<User> page = Page.of(pageNo, pageSize);

//        2. 排序条件
        page.addOrder(new OrderItem("balance", true));
        page.addOrder(new OrderItem("id", true));

//        3. 查询
        Page<User> p = userService.page(page);

//        4. 解析
        long total = p.getTotal();
        System.out.println("total = " + total);
        long pages = p.getPages();
        System.out.println("pages = " + pages);
        List<User> records = p.getRecords();
        records.forEach(System.out::println);
    }

}
