package com.itheima.mp.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.itheima.mp.domain.po.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testInsert() {
        User user = new User();
        user.setId(5L);
        user.setUsername("Lucy");
        user.setPassword("123");
        user.setPhone("18688990011");
        user.setBalance(200);
        user.setInfo("{\"age\": 24, \"intro\": \"英文老师\", \"gender\": \"female\"}");
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Test
    void testSelectById() {
        User user = userMapper.selectById(5L);
        System.out.println("user = " + user);
    }


    @Test
    void testQueryByIds() {
        List<User> users = userMapper.selectBatchIds(List.of(1L, 2L, 3L, 4L));
        users.forEach(System.out::println);
    }

    @Test
    void testUpdateById() {
        User user = new User();
        user.setId(5L);
        user.setBalance(20000);
        userMapper.updateById(user);
    }

    @Test
    void testDeleteUser() {
        userMapper.deleteById(5L);
    }

    @Test
    void testQueryWrapper(){
//        查询出名字中带 o 的，存款⼤于等于1000元的⼈
//        select id,username,info,balance
//        from user
//        where username like 'o' and balance >= 1000

        QueryWrapper<User> wrapper = new QueryWrapper<User>()
                .select("id", "username", "info", "balance")
                .like("username", "o")
                .ge("balance", 1000);

        List<User> users = userMapper.selectList(wrapper);
        System.out.println(users);
    }

    @Test
    void testUpdateByQueryWrapper(){
//        更新⽤⼾名为jack的⽤⼾的余额为2000
//        update user
//        set balance = 2000
//        where username = "jack"

//        写法1：
//        User user = new User();
//        user.setBalance(2000);
//
//        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<User>()
//                .eq("username", "jack");
//
//        userMapper.update(user, userUpdateWrapper);

//        写法2：setSql中的内容通常写死，不太推荐，如果日后数据库字段名称改变不易维护
        UpdateWrapper<User> userUpdateWrapper1 = new UpdateWrapper<User>()
                .setSql("balance = 20000")
                .eq("username", "jack");

        userMapper.update(null, userUpdateWrapper1);
    }

    @Test
    void testUpdateWrapper(){
//        更新id为 1,2,4 的⽤⼾的余额，扣200
//        UPDATE user SET balance = balance - 200 WHERE id in (1, 2, 4)

        List<Long> ids = List.of(1L, 2L, 4L);
        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<User>()
                .setSql("balance = balance - 200")
                .in("id", ids);

        userMapper.update(null, userUpdateWrapper);
    }

}