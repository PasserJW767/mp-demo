package com.itheima.mp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.itheima.mp.domain.dto.PageDTO;
import com.itheima.mp.domain.po.Address;
import com.itheima.mp.domain.po.User;
import com.itheima.mp.domain.query.UserQuery;
import com.itheima.mp.domain.vo.AddressVO;
import com.itheima.mp.domain.vo.UserVO;
import com.itheima.mp.enums.UserStatus;
import com.itheima.mp.mapper.UserMapper;
import com.itheima.mp.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Override
    public void deductMoney(Long id, Integer money) {
//        1. 校验用户账户合法性
        User user = baseMapper.selectById(id);
        if (user == null || user.getStatus() == UserStatus.FROZEN){
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
                .set(result_balance == 0, User::getStatus, UserStatus.FROZEN)
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

    @Override
    public UserVO queryUserAndAddressById(Long id) {
//        1. 查询用户
        User user = baseMapper.selectById(id);
        if (user == null || user.getStatus() == UserStatus.FROZEN)
            throw new RuntimeException("用户不存在或账号被冻结！");
        // 封装用户VO
        UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
//        2. 用户存在则查询地址
        List<Address> list = Db.lambdaQuery(Address.class)
                .eq(Address::getUserId, id).list();
        System.out.println(list);
        // 若地址列表不为空则封装地址VO
        if (!list.isEmpty()){
            List<AddressVO> addressVOS = BeanUtil.copyToList(list, AddressVO.class);
            userVO.setAddress(addressVOS);
        }

        return userVO;
    }

    @Override
    public List<UserVO> queryBatchUserAndAddressByIds(List<Long> ids) {
//        1. 先批量查询用户
        List<User> users = baseMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(users)){
            return Collections.emptyList();
        }

//        2. 使用in语句来批量查询
//        根据查询的用户，获取用户id集合（不用ids的原因是，可能有的ids是不存在的）
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
//        再根据这些Id查询用户地址 -> in语句查询
        List<Address> addresses = Db.lambdaQuery(Address.class).in(Address::getUserId, userIds).list();
//        转换成AddressVO
        List<AddressVO> addressVOList = BeanUtil.copyToList(addresses, AddressVO.class);

//        用户地址分集合处理，同一个用户的地址放入一个集合中，HashMap的Key是用户ID，Value是用户对应的地址集合
        Map<Long, List<AddressVO>> addressMap = new HashMap<>();
        if (CollUtil.isNotEmpty(addressVOList)){
            addressMap = addressVOList.stream().collect(Collectors.groupingBy(AddressVO::getUserId));
        }

//        3. 转换VO返回
        List<UserVO> list = new ArrayList<>();
        for (User user : users){
//            转换成UserVO
            UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
            userVO.setAddress(addressMap.get(userVO.getId()));
            list.add(userVO);
        }

        return list;
    }

    @Override
    public PageDTO<UserVO> queryUserPage(UserQuery query) {
////        1. 设置page
////        1.1 设置Page的当前页码和大小
//        Page<User> page = Page.of(query.getPageNo(), query.getPageSize());
////        1.2 设置page的排序条件
//        if (query.getSortBy() != null){
//            page.addOrder(new OrderItem(query.getSortBy(), query.getIsAsc()));
//        } else {
//            page.addOrder(new OrderItem("update_time", false));
//        }
        Page<User> page = query.toMpPageDefaultSortByUpdateTimeDesc();

//        2. 根据page进行查询
        Page<User> userPage = lambdaQuery()
                .like(query.getName() != null, User::getUsername, query.getName())
                .eq(query.getStatus() != null, User::getStatus, query.getStatus())
                .gt(query.getMinBalance() != null, User::getBalance, query.getMinBalance())
                .lt(query.getMaxBalance() != null, User::getBalance, query.getMaxBalance())
                .page(page);

////        2-3 判空操作，容易漏掉，注意执行判空！但是感觉判断不判断好像差不多……
//        if (CollUtil.isEmpty(userPage.getRecords())){
//            return new PageDTO<>(userPage.getTotal(), userPage.getPages(), Collections.emptyList());
//        }
//
////        3. 查询出了User信息后，给返回值设置这些信息
//        PageDTO<UserVO> userVOPage = new PageDTO<>();
//        userVOPage.setTotal(userPage.getTotal());
//        userVOPage.setPages(userPage.getPages());
//
//        userVOPage.setList(BeanUtil.copyToList(userPage.getRecords(), UserVO.class));

//        传入BeanUtil的copy属性方法
        PageDTO<UserVO> userVOPageDTO1 = PageDTO.change(userPage, user -> BeanUtil.copyProperties(user, UserVO.class));
//        传入自定义的方法，先转换成UserVO，然后隐藏用户名的后两位
        PageDTO<UserVO> userVOPageDTO2 = PageDTO.change(userPage, user -> {
            UserVO userVO = BeanUtil.copyProperties(user, UserVO.class);
            String username = userVO.getUsername();
            userVO.setUsername(username.substring(0, username.length() - 2) + "**");
            return userVO;
        });
        return userVOPageDTO2;
    }
}
