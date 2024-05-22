package com.itheima.mp.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.mapper.UserMapper;
import com.itheima.mp.service.IUserService;
import org.springframework.stereotype.Service;

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
        baseMapper.deductMoney(id, money);
    }
}
