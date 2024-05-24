package com.itheima.mp.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.mapper.UserMapper;
import com.itheima.mp.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public void deductMoney(Long id, Integer money) {
//        1. 校验用户账户合法性
        User user = baseMapper.selectById(id);
        if (user == null || user.getStatus() == 2){
            throw new RuntimeException("用户状态异常");
        }
//        2. 校验用户余额是否大于扣减数量
        if (user.getBalance() < money){
            throw new RuntimeException("用户余额不足");
        }
//        3. 执行操作
//        这里没有使用wrapper是因为，我们希望wrapper帮我们做一些复杂的选择操作，比如`in (ids)`等，这里简单的一个查询可以自己写

//        方法1，通过自定义的mapper语句操作
//        baseMapper.deductMoney(id, money);

//        方法2，通过lambda表达式操作
        int result_balance = user.getBalance() - money;
        lambdaUpdate()
                .set(User::getBalance, result_balance)
                .set(result_balance == 0, User::getStatus, 0)
                .eq(User::getId, id)
                .eq(User::getBalance, user.getBalance()) // 乐观锁（Compare and Set），保证当一个用户有两个线程在访问同一个方法时候，不会出现问题。即用户更新余额时，要求数据库的余额和当初拿到的余额相等
                .update();
    }

    @Override
    public List<User> queryUsers(String name, Integer status, Integer minBalance, Integer maxBalance) {
        return lambdaQuery()
                .like(name != null, User::getUsername, name)
                .eq(status != null, User::getStatus, status)
                .gt(minBalance != null, User::getBalance, minBalance)
                .lt(maxBalance != null, User::getBalance, maxBalance)
                .list();
    }
}
