# MyBatis-Plus Demo
## 1. MyBatis-Plus的使用
在`src/main/java/com/itheima/mp/mapper/UserMapper.java`下，使类继承`BaseMapper<User>`，就可以使用常见的一些语句

测试见`src/test/java/com/itheima/mp/MpDemoApplicationTests.java`下的：
- `testInsert`
- `testSelectById`
- `testQueryByIds`
- `testUpdateById`
- `testDeleteUser`

## 2. MyBatis-Plus常用注解
见`src/main/java/com/itheima/mp/domain/po/User.java`实体类

这里使用了：
- `@TableName()`指定表名
- `@TableId()`指定Id类型的一些属性
- `@TableField()`指定对应字段名

## 3. MyBatis-Plus条件构造器
`src/test/java/com/itheima/mp/MpDemoApplicationTests.java`下演示了复杂的条件构造器模式：
- `testQueryWrapper`
- `testUpdateByQueryWrapper`
- `testUpdateWrapper`

这里只展示了非lambda形式。推荐使用lambda形式，将column修改为：
User::getId
User::getName
User::getInfo
User::getBalance
等等等等......

这样的好处是假如在日后更新了数据库字段名，就不需要修改具体语句中的内容，只需要使用`@TableField`改一下实体类的映射就可以

## 4. 自定义Sql
`src/test/java/com/itheima/mp/MpDemoApplicationTests.java`下演示了,即`testUpdateCustomSql`

需求：更新id为 1,2,4 的⽤⼾的余额，扣200

语句为：UPDATE user SET balance = balance - 200 WHERE id in (1, 2, 4)

假设`testUpdateWrapper`是业务层，不推荐将Sql语句写在业务层，而应该写在数据持久层，也就是Mapper中

通过Wrapper自定义条件，并将参数传入到自己自定义的语句中

在自己自定义的语句中通过`@Param("ew")`来指明wrapper对象（这个是固定的！），通过其他名字来指定传入的参数

## 4.1 自定义Sql用于多表查询

见`src/test/java/com/itheima/mp/MpDemoApplicationTests.java`下的`testQueryMultiTable`

我尝试使用LambdaQueryWrapper，但是好像并不好使，在定义：
```java
LambdaQueryWrapper<User> users = new LambdaQueryWrapper<User>
        .in(User::getId, List.of(1L, 2L, 4L))
        .eq(Address::getCity, "北京") // 这里会出问题，因为上面定义了类是User，这里好像找不到这个getCity方法
```

但是可以通过`.lambda`转换：
```java
LambdaQueryWrapper<User> userLambdaQueryWrapper = new QueryWrapper<User>()
        .in("u.id", ids)
        .eq("a.city", "北京")
        .lambda();
```

# 5. IService接口
## 5.1 IService接口的使用
在`src/main/java/com/itheima/mp/service/IUserService.java`接口下继承IService

在`src/main/java/com/itheima/mp/service/impl/UserServiceImpl.java`下继承ServiceImpl<UserMapper, User>，其中第一个类是Mapper类，第二个是对应的实体类，这个类里边实现了IService要求的许多方法，接着实现IUserService

测试CRUD见`src/test/java/com/itheima/mp/service/UserServiceTest.java`

## 5.2 IService做简单业务开发
在Controller做基础业务开发见`src/main/java/com/itheima/mp/controller/UserController.java`

## 5.3 IService做复杂业务开发
在Controller做复杂业务开发见`src/main/java/com/itheima/mp/controller/UserController.java`下的`deductBalance`

在这部分中主要就是在业务层对用户合法性进行校验，并对余额进行检查

对于Mapper中之前会使用的`${ew.customSqlSegment}`，简单的条件可以不使用wrapper来实现，复杂的条件比如`id in (ids)`这些需要使用一些循环来写的，才需要使用wrapper，简单的可以自己手写

在这个开发过程中，遇到的问题包括：
1. 请求参数如果是单个参数不可以使用`@RequestParam`，否则会报错无法解析条件的问题：`Required request parameter 'id' for method parameter type long is not presen...`
2. 使用swapper时记得将参数勾选上，如果勾选上提示`Request method 'PUT' not supported`的话，可以检查前后端的请求和规定链接是否一致

## 5.4 IService的Lambda方法
在`src/main/java/com/itheima/mp/controller/UserController.java`下的复杂条件查询方法`queryUsers`就使用了Lambda查询

同时在`src/main/java/com/itheima/mp/service/impl/UserServiceImpl.java`中的`deductMoney`也给出了更新余额的Lambda实现形式，
在这种实现形式中还使用了乐观锁来保证有多个线程访问同一个用户的方法时不会出现问题。

lambda适用于一些原本需要再Mapper.xml中写的一些判断语句，如：
```xml
<if test="name != null">
    AND userName LIKE #{name}
</if>
```

## 5.5 IService中的批量新增
做批量新增的三种方案：
1. 普通for循环逐条插入，速度很慢，不推荐（因为一条语句要发送一个网络请求）
2. MP的批量新增，基于预编译的批处理，性能不错（MP会将所有的插入请求打包起来，比如1000条打包一份，一次网络请求发送1000条插入语句）
3. 配置JDBC参数，开启rewriteBatchedStatements，性能最好（这会将所有的请求转换成一条Sql语句进行执行，效率最高）

对于第一种方案：
```java
void testsaveOneByone(){
    long b = system.currentTimeMillis();
    for(int i=1;i<=100000;i++){
        userService.save(buildUser(i));
    };
    long e = system.currentTimeMillis();
    System.out.println("耗时:"+(e- b));
}
```

对于第二种方案：
```java
    // 准备一个容量为1000的集合
    List<User> list = new ArrayList<>(1000);
    long b = system.currentTimeMillis();
    
    for (int i = 1; i <= 100000; i++){
        // 向集合中加入新用户
        list.add(builderUser(i));
        if (i % 1000 == 0){
            // 发送一次网络请求
            userService.saveBatch(list);
            // 清空集合
            list.clear();
        }
    }
    
    long e = system.currentTimeMillis();
    System.out.println("耗时:"+(e- b));
```

对于第三种方案：

在第二种方案的基础上只需要在`application.yml`后拼接一个参数：`rewriteBatchedStatements=True`即可。

# 6. DB静态工具
## 6.1 为什么需要DB静态工具？
思考一个场景：查询用户时，我们需要同时返回用户的地址信息

在这种情况下，我们可能还需要在`UserService`业务类中额外地加入如`AddressService`或者`AddressMapper`等`User`无关的内容，
假如说加入的是`AddressService`，未来`AddressService`又依赖于`UserService`，就会导致循环依赖的问题。

虽然Spring能够处理好循环依赖，但是我们应当尽量避免循环依赖的出现，这时候就需要使用DB静态工具

## 6.2 案例一：给定用户id，查询用户时同时查询用户地址
对应于`service/impl/UserServiceImpl.java`下的`queryUserAndAddressById`方法

步骤：
1. 给`UserVO`新建一个`List<AddressVO> addresses`属性，用于存放用户地址（因为这个属性只是面向输出的，我们不需要在po里面加，只需要在面向输出的实体中加即可）
2. 根据给定的用户id查询用户信息得到`User`类，可以使用`BeanUtil`转换为`UserVO`（面向输出的实体）
3. 根据`User`的id在`Address`表中查询对应地址，这时候就使用到DB静态工具类：`Db.lambdaQuery(Address.class).eq(Address::getUserId, id).list();`，在`lambdaQuery`中指定要查询的类，后面跟平时一样接一些等式（`Where`条件），`.list()`表示返回列表
4. 得到`Address`列表后，使用`BeanUtil`将列表转换为`AddressVO`，并将这个地址属性设置到`UserVO`中去
5. 返回`UserVO`

## 6.3 案例二：给定批量用户ids，查询这一些用户的信息及用户地址
对应于`service/impl/UserServiceImpl.java`下的`queryBatchUserAndAddressByIds`方法

步骤：
1. 根据`ids`查询用户信息得到列表`users`：`baseMapper.selectBatchIds(ids)`，将`users`使用`BeanUtil`转换为`userVOs`
2. ！注意条件的判断，此处使用了一个判空，假如用户列表为空的话，直接返回空
3. 将`users`的`ids`批量提取出来得到`userIds`，这里使用到了`collections`的方法：`users.stream().map(User::getId).collect(Collectors.toList());`
4. 使用`in`语句在`Address`表中批量查询用户的地址信息，并使用`BeanUril`转换得到地址列表`addressVOList`
5. `addressVOList`的结果不利于我们将信息设置到对应的用户里去，如果能够转成`userId`为K，`addresses`为V的HashMap是最好的，所以再次使用`collections`方法：`addressVOList.stream().collect(Collectors.groupingBy(AddressVO::getUserId));`
6. 使用`forEach`遍历`userVOs`，将每一个`userId`对应的`AddressVO`设置到`userVO`中
7. 返回`userVOs`

# 7. 逻辑删除
对于⼀些⽐较重要的数据，我们往往会采⽤逻辑删除的⽅案：
- 在表中添加⼀个字段标记数据是否被删除
- 当删除数据时把标记置为true
- 查询时过滤掉标记为true的数据

MP支持逻辑删除，只需要在`application.yml`中配置逻辑删除字段：
```yaml
mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: deleted # 配置逻辑删除字段
      logic-delete-value: 1
      logic-not-delete-value: 0
```
配置完逻辑删除字段后，之后执行删除操作都会变成`update`，执行查询操作都会额外加上条件`AND 删除字段=未删除标记`

# 8. 枚举处理器
在`User`中有一个`UserStatus`状态，在表中是0和1，但是编码时候一直`set`为0/1，或者`get`为0/1会导致意义不明的情况，导致代码可读性降低

所以这里将`UserStatus`处理成一个枚举类型：
```java
package com.itheima.mp.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum UserStatus {
    NORMAL(1, "正常"),
    FROZEN(2, "冻结")
    ;

    @EnumValue // 此注解指明哪个值对应数据库中的值
    private final int value;
    @JsonValue // 该注解加在哪个属性上边，未来返回的时候就会显示哪个属性
    private final String desc;

    UserStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
```
注意，使用这种枚举处理还要在yaml中配置：
```yaml
mybatis-plus:
  configuration:
    default-enum-type-handler: com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler
```
这里代码的核心为：
1. `@EnumVlue`，这个注解指明清楚哪个字段对应于数据库中的值
2. `@JsonValue`，表明返回时显示哪个属性的值，若不备注默认显示上边的`NORMAL/FROZEN`，可能导致可读性不强的问题