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