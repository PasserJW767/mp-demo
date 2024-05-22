package com.itheima.mp.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.mp.domain.po.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserMapper extends BaseMapper<User> {

//    @Select("UPDATE user SET balance = balance - #{money} ${ew.customSqlSegment}")
    @Update("update tb_user set balance = balance - #{money} ${ew.customSqlSegment}")
    void deductBalanceAccordingIds(@Param("money") int money, @Param("ew") LambdaUpdateWrapper<User> userQueryWrapper);

    @Select("select u.* from tb_user u inner join address a on u.id = a.user_id ${ew.customSqlSegment}")
    List<User> testQueryMultiTable(@Param("ew") QueryWrapper<User> queryWrapper);
}
