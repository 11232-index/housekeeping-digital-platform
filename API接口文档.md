# smart-housekeeping-system 后端接口文档

> 项目：smart-housekeeping-system（智能家政服务）  
> 文档生成日期：2026-07-24

---

## 目录

- [一、管理端 - 管理员认证](#一管理端---管理员认证)
- [二、管理端 - 员工管理](#二管理端---员工管理)
- [三、管理端 - 员工详细信息](#三管理端---员工详细信息)
- [四、管理端 - 分类管理](#四管理端---分类管理)
- [五、管理端 - 文件上传](#五管理端---文件上传)
- [六、管理端 - AI对话会话](#六管理端---ai对话会话)
- [七、管理端 - 店铺营业状态管理](#七管理端---店铺营业状态管理)
- [八、员工端 - 员工认证](#八员工端---员工认证)
- [九、员工端 - 服务项目管理](#九员工端---服务项目管理)
- [十、员工端 - 服务套餐管理](#十员工端---服务套餐管理)
- [十一、员工端 - 店铺营业状态](#十一员工端---店铺营业状态)
- [十二、C端 - 用户登录](#十二c端---用户登录)
- [十三、C端 - 地址簿管理](#十三c端---地址簿管理)
- [十四、C端 - 购物车管理](#十四c端---购物车管理)
- [十五、管理端 - 动态消息管理](#十五管理端---动态消息管理)
- [十六、管理端 - 关于我们管理](#十六管理端---关于我们管理)
- [十七、管理端 - 服务介绍管理](#十七管理端---服务介绍管理)
- [十八、C端 - 搜索记录管理](#十八c端---搜索记录管理)
- [十九、管理端 - 服务推荐管理](#十九管理端---服务推荐管理)
- [二十、C端 - 最新动态搜索记录管理](#二十c端---最新动态搜索记录管理)
- [二十一、C端 - 服务介绍搜索记录管理](#二十一c端---服务介绍搜索记录管理)
- [二十二、管理端 - 家政员管理](#二十二管理端---家政员管理)
- [二十六、C端 - 家政员可预约时段查询](#二十六c端---家政员可预约时段查询)
- [二十七、C端 - 用户订单管理](#二十七c端---用户订单管理)
- [二十八、员工端 - 订单管理](#二十八员工端---订单管理)
- [二十九、员工端 - 优惠券管理](#二十九员工端---优惠券管理)
- [三十、C端 - 优惠券（领取/秒杀）](#三十c端---优惠券领取秒杀)

---

## 一、管理端 - 管理员认证

**Controller:** `AdminController`  
**Base URL:** `/admin/auth`

| 序号 | 方法   | 完整路径                          | 请求参数                      | 说明                                         |
|------|--------|-----------------------------------|-------------------------------|----------------------------------------------|
| 1    | `POST` | `/admin/auth/login`               | Body: EmployeeLoginDTO        | 管理员登录，返回 JWT 令牌及基本信息           |
| 2    | `POST` | `/admin/auth/logout`              | —                             | 管理员退出                                   |
| 3    | `PUT`  | `/admin/auth/editPassword`        | Body: PasswordEditDTO         | 修改管理员密码（需登录）                     |
| 4    | `PUT`  | `/admin/auth/editName`            | Body: String (name)           | 修改管理员姓名（需登录）                     |
| 5    | `PUT`  | `/admin/auth/security`            | Body: { question, answer }    | 设置密保（需登录）                           |
| 6    | `GET`  | `/admin/auth/securityQuestion`    | Query: username               | 获取密保问题（无需登录）                     |
| 7    | `POST` | `/admin/auth/resetPassword`       | Body: { username, answer, newPassword } | 重置密码（忘记密码，通过密保验证，无需登录） |

---

## 二、管理端 - 员工管理

**Controller:** `EmployeeController`  
**Base URL:** `/admin/employee`

| 序号 | 方法     | 完整路径                              | 请求参数                      | 说明                   |
|------|----------|---------------------------------------|-------------------------------|------------------------|
| 1    | `POST`   | `/admin/employee`                     | Body: EmployeeDTO             | 新增员工信息           |
| 2    | `GET`    | `/admin/employee/page`                | Query: EmployeePageQueryDTO   | 分页查询员工           |
| 3    | `POST`   | `/admin/employee/status/{status}`     | Path: status, Query: id       | 启用/禁用员工账号      |
| 4    | `GET`    | `/admin/employee/{id}`                | Path: id                      | 根据ID查询员工信息     |
| 5    | `PUT`    | `/admin/employee`                     | Body: EmployeeDTO             | 修改员工信息           |
| 6    | `DELETE` | `/admin/employee/{id}`                | Path: id                      | 删除员工信息           |

---

## 三、管理端 - 员工详细信息

**Controller:** `EmployeeDetailController`  
**Base URL:** `/admin/employee/detail`

| 序号 | 方法     | 完整路径                                  | 请求参数                      | 说明                       |
|------|----------|-------------------------------------------|-------------------------------|----------------------------|
| 1    | `GET`    | `/admin/employee/detail/{employeeId}`     | Path: employeeId              | 根据员工ID查询详细信息     |
| 2    | `POST`   | `/admin/employee/detail`                  | Body: EmployeeDetailDTO       | 新增员工详细信息           |
| 3    | `PUT`    | `/admin/employee/detail`                  | Body: EmployeeDetailDTO       | 修改员工详细信息           |
| 4    | `DELETE` | `/admin/employee/detail/{employeeId}`     | Path: employeeId              | 根据员工ID删除详细信息     |

---

## 四、管理端 - 分类管理

**Controller:** `CategoryController`  
**Base URL:** `/admin/category`

| 序号 | 方法     | 完整路径                              | 请求参数                      | 说明                      |
|------|----------|---------------------------------------|-------------------------------|---------------------------|
| 1    | `POST`   | `/admin/category`                     | Body: CategoryDTO             | 新增分类                  |
| 2    | `GET`    | `/admin/category/page`                | Query: CategoryPageQueryDTO   | 分页查询分类              |
| 3    | `GET`    | `/admin/category/list`                | Query: type (optional)        | 查询全部分类（下拉列表用）|
| 4    | `GET`    | `/admin/category/{id}`                | Path: id                      | 根据ID查询分类信息        |
| 5    | `PUT`    | `/admin/category`                     | Body: CategoryDTO             | 修改分类信息              |
| 6    | `POST`   | `/admin/category/status/{status}`     | Path: status, Query: id       | 启用/禁用分类             |
| 7    | `DELETE` | `/admin/category/{id}`                | Path: id                      | 删除分类信息              |

---

## 五、管理端 - 文件上传

**Controller:** `CommonController`  
**Base URL:** `/admin/common`

| 序号 | 方法   | 完整路径                    | 请求参数        | 说明                       |
|------|--------|-----------------------------|-----------------|----------------------------|
| 1    | `POST` | `/admin/common/upload`      | FormData: file  | 文件上传到阿里云 OSS       |

---

## 六、管理端 - AI对话会话

**Controller:** `ChatSessionController`  
**Base URL:** `/admin/chat`

| 序号 | 方法     | 完整路径                        | 请求参数                        | 说明                          |
|------|----------|---------------------------------|---------------------------------|-------------------------------|
| 1    | `POST`   | `/admin/chat/session`           | Body: ChatSessionDTO            | 创建会话（首次提问时调用）    |
| 2    | `GET`    | `/admin/chat/session/page`      | Query: ChatSessionPageQueryDTO  | 分页查询历史会话              |
| 3    | `PUT`    | `/admin/chat/session/{id}`      | Path: id, Body: ChatSessionDTO  | 重命名会话                    |
| 4    | `DELETE` | `/admin/chat/session/{id}`      | Path: id                        | 删除指定会话                  |
| 5    | `DELETE` | `/admin/chat/session`           | —                               | 删除当前管理员所有会话        |

---

## 七、管理端 - 店铺营业状态管理

**Controller:** `ShopControllor`（adminShopControllor）  
**Base URL:** `/admin/shop`

| 序号 | 方法   | 完整路径                             | 请求参数              | 说明                                         |
|------|--------|--------------------------------------|-----------------------|----------------------------------------------|
| 1    | `GET`  | `/admin/shop/setStatus/{status}`     | Path: status (Integer) | 设置店铺营业状态（1-营业中，0-打烊中）       |
| 2    | `GET`  | `/admin/shop/getStatus`              | —                     | 获取店铺当前营业状态                         |

> **说明：** 营业状态存储在 Redis 中，key 为 `SHOP_STATUS`。返回统一格式 `Result`。

---

## 八、员工端 - 员工认证

**Controller:** `EmployeeAuthController`  
**Base URL:** `/employee/auth`

| 序号 | 方法   | 完整路径                          | 请求参数                 | 说明             |
|------|--------|-----------------------------------|--------------------------|------------------|
| 1    | `POST` | `/employee/auth/login`            | Body: EmployeeLoginDTO   | 员工登录，返回 JWT 令牌及基本信息 |
| 2    | `POST` | `/employee/auth/logout`           | —                        | 员工退出         |
| 3    | `PUT`  | `/employee/auth/editPassword`     | Body: PasswordEditDTO    | 修改员工密码     |

---

## 九、员工端 - 服务项目管理

**Controller:** `ServiceItemController`  
**Base URL:** `/employee/service-item`

| 序号 | 方法     | 完整路径                                | 请求参数                          | 说明                   |
|------|----------|-----------------------------------------|-----------------------------------|------------------------|
| 1    | `POST`   | `/employee/service-item`                 | Body: ServiceItemDTO             | 新增服务项目           |
| 2    | `GET`    | `/employee/service-item/page`            | Query: ServiceItemPageQueryDTO   | 分页查询服务项目       |
| 3    | `GET`    | `/employee/service-item/{id}`            | Path: id                         | 根据ID查询服务项目     |
| 4    | `PUT`    | `/employee/service-item`                 | Body: ServiceItemDTO             | 修改服务项目           |
| 5    | `POST`   | `/employee/service-item/status/{status}` | Path: status, Query: id          | 启用/禁用服务项目      |
| 6    | `DELETE` | `/employee/service-item/{id}`            | Path: id                         | 删除服务项目           |
| 7    | `DELETE` | `/employee/service-item/batch`           | Body: List\<Long\> (ids)         | 批量删除服务项目       |

---

## 十、员工端 - 服务套餐管理

**Controller:** `ServicePackageController`  
**Base URL:** `/employee/service-package`

| 序号 | 方法     | 完整路径                                  | 请求参数                            | 说明                   |
|------|----------|-------------------------------------------|-------------------------------------|------------------------|
| 1    | `POST`   | `/employee/service-package`                 | Body: ServicePackageDTO           | 新增服务套餐           |
| 2    | `GET`    | `/employee/service-package/page`            | Query: ServicePackagePageQueryDTO | 分页查询套餐           |
| 3    | `GET`    | `/employee/service-package/{id}`            | Path: id                          | 根据ID查询套餐         |
| 4    | `PUT`    | `/employee/service-package`                 | Body: ServicePackageDTO           | 修改套餐               |
| 5    | `POST`   | `/employee/service-package/status/{status}` | Path: status, Query: id           | 启用/禁用套餐          |
| 6    | `DELETE` | `/employee/service-package/{id}`            | Path: id                          | 删除套餐               |
| 7    | `DELETE` | `/employee/service-package/batch`           | Body: List\<Long\> (ids)          | 批量删除套餐           |

---

## 十一、员工端 - 店铺营业状态

**Controller:** `ShopControllor`（employeeShopControllor）  
**Base URL:** `/employee/shop`

| 序号 | 方法  | 完整路径                | 请求参数 | 说明                       |
|------|-------|-------------------------|----------|----------------------------|
| 1    | `GET` | `/employee/shop/get`    | —        | 获取店铺当前营业状态       |

> **说明：** 营业状态存储在 Redis 中，key 为 `SHOP_STATUS`。返回统一格式 `Result`。

---

## 十二、C端 - 用户登录

**Controller:** `UserController`  
**Base URL:** `/user/user`

| 序号 | 方法   | 完整路径               | 请求参数              | 说明                                             |
|------|--------|------------------------|-----------------------|--------------------------------------------------|
| 1    | `POST` | `/user/user/login`     | Body: UserLoginDTO    | 用户微信登录，返回 JWT 令牌及用户基本信息         |

> **说明：** `UserLoginDTO` 包含微信授权码（code），后端通过微信接口获取 openid 完成登录。

---

## 十三、C端 - 地址簿管理

**Controller:** `AddressBookController`  
**Base URL:** `/user/address-book`

| 序号 | 方法     | 完整路径                              | 请求参数                       | 说明                   |
|------|----------|---------------------------------------|--------------------------------|------------------------|
| 1    | `POST`   | `/user/address-book`                  | Body: AddressBook              | 新增地址               |
| 2    | `GET`    | `/user/address-book/page`             | Query: AddressBookPageQueryDTO | 分页查询地址列表       |
| 3    | `GET`    | `/user/address-book/{id}`             | Path: id                       | 根据ID查询地址         |
| 4    | `PUT`    | `/user/address-book`                  | Body: AddressBook              | 修改地址               |
| 5    | `DELETE` | `/user/address-book/{id}`             | Path: id                       | 删除地址               |
| 6    | `PUT`    | `/user/address-book/default/{id}`     | Path: id                       | 设置默认地址           |
| 7    | `GET`    | `/user/address-book/default`          | —                              | 查询默认地址           |

> **说明：** 用户需登录后操作自己的地址簿。新增地址时若为第一条地址，自动设为默认地址。删除默认地址后，自动将剩余地址中的最新一条设为默认。接口使用 Redis 缓存（key 前缀为 `addressBook`）。

### AddressBook 实体字段说明

| 字段         | 类型    | 必填 | 说明                        |
|--------------|---------|------|-----------------------------|
| id           | Long    | —    | 主键（自动生成）            |
| userId       | Long    | —    | 用户id（后端自动填充）      |
| consignee    | String  | 否   | 联系人                      |
| phone        | String  | 是   | 手机号                      |
| sex          | String  | 是   | 性别（0 女，1 男）          |
| provinceCode | String  | 否   | 省级区划编号                |
| provinceName | String  | 否   | 省级名称                    |
| cityCode     | String  | 否   | 市级区划编号                |
| cityName     | String  | 否   | 市级名称                    |
| districtCode | String  | 否   | 区级区划编号                |
| districtName | String  | 否   | 区级名称                    |
| detail       | String  | 是   | 详细地址                    |
| label        | String  | 否   | 标签（如：家、公司、学校）  |
| isDefault    | Integer | 否   | 是否默认（0 否，1 是）      |

### AddressBookPageQueryDTO 查询参数

| 参数      | 类型   | 必填 | 说明                     |
|-----------|--------|------|--------------------------|
| page      | int    | 是   | 页码                     |
| pageSize  | int    | 是   | 每页记录数               |
| consignee | String | 否   | 联系人（模糊搜索）       |

---

## 十四、C端 - 购物车管理

**Controller:** `ShoppingCartController`  
**Base URL:** `/user/shopping-cart`

| 序号 | 方法     | 完整路径                       | 请求参数              | 说明                                                         |
|------|----------|--------------------------------|-----------------------|--------------------------------------------------------------|
| 1    | `POST`   | `/user/shopping-cart/add`      | Body: ShoppingCartDTO | 新增购物车（已存在则数量+1）                                 |
| 2    | `GET`    | `/user/shopping-cart/list`     | —                     | 查询当前用户的购物车列表                                     |
| 3    | `POST`   | `/user/shopping-cart/sub`      | Body: ShoppingCartDTO | 减少购物车数量（数量>1减1，数量=1删除）                      |
| 4    | `DELETE` | `/user/shopping-cart/clean`    | —                         | 清空当前用户购物车                                           |
| 5    | `GET`    | `/user/shopping-cart/requirements` | Query: serviceItemId, servicePackageId | 查询服务需求（根据服务项目id或服务套餐id）    |

> **说明：** 用户需登录后操作自己的购物车，用户id从 ThreadLocal（`BaseContext`）获取。接口使用 Redis 缓存（key 前缀为 `shoppingCart`）。

---

## 接口汇总统计

| 序号 | 模块                         | Base URL                     | 接口数量 |
|------|------------------------------|------------------------------|----------|
| 一   | 管理端 - 管理员认证          | `/admin/auth`                | 7        |
| 二   | 管理端 - 员工管理            | `/admin/employee`            | 6        |
| 三   | 管理端 - 员工详细信息        | `/admin/employee/detail`     | 4        |
| 四   | 管理端 - 分类管理            | `/admin/category`            | 7        |
| 五   | 管理端 - 文件上传            | `/admin/common`              | 1        |
| 六   | 管理端 - AI对话会话          | `/admin/chat`                | 5        |
| 七   | 管理端 - 店铺营业状态管理    | `/admin/shop`                | 2        |
| 八   | 员工端 - 员工认证            | `/employee/auth`             | 3        |
| 九   | 员工端 - 服务项目管理        | `/employee/service-item`     | 7        |
| 十   | 员工端 - 服务套餐管理        | `/employee/service-package`  | 7        |
| 十一 | 员工端 - 店铺营业状态        | `/employee/shop`             | 1        |
| 十二 | C端 - 用户登录               | `/user/user`                 | 1        |
| 十三 | C端 - 地址簿管理             | `/user/address-book`         | 7        |
| 十四 | C端 - 购物车管理             | `/user/shopping-cart`        | 5        |
| 十五 | 管理端 - 动态消息管理          | `/admin/updates`             | 5        |
| 十六 | 管理端 - 关于我们管理          | `/admin/about`               | 5        |
| 十七 | 管理端 - 服务介绍管理          | `/admin/service-intro`       | 5        |
| 十八 | C端 - 搜索记录管理             | `/user/search-history`       | 3        |
| 十九 | 管理端 - 服务推荐管理          | `/admin/hot-picks`                | 6        |
| 二十 | C端 - 最新动态搜索记录管理     | `/user/updates-search-history`         | 3        |
| 二十一 | C端 - 服务介绍搜索记录管理    | `/user/service-intro-search-history`  | 3        |
| 二十二 | 管理端 - 家政员管理            | `/admin/housekeeper` / `/user/housekeeper`  | 7        |
| 二十三 | C端 - 家政员搜索记录管理       | `/user/housekeeper-search-history`          | 3        |
| 二十四 | C端 - 资讯中心                 | `/user/feed`                                | 1        |
| 二十五 | 员工端 - 家政员预约时间管理    | `/employee/housekeeper-schedule`            | 5        |
| 二十六 | C端 - 家政员可预约时段查询     | `/user/housekeeper-schedule`               | 1        |
| 二十七 | C端 - 订单管理                 | `/user/order`                              | 5        |
| 二十八 | 员工端 - 订单管理              | `/employee/order`                           | 4        |
| 二十九 | 员工端 - 优惠券管理            | `/employee/voucher`                        | 6        |
| 三十   | C端 - 优惠券（领取/秒杀）      | `/user/voucher`                            | 2        |
|      | **合计**                     |                                             | **127**   |

---

## 十五、管理端 - 动态消息管理

**Controller:** `UpdatesController`  
**Base URL:** `/admin/updates`

| 序号 | 方法       | 完整路径                        | 请求参数                  | 说明             |
|------|------------|---------------------------------|---------------------------|------------------|
| 1    | `POST`     | `/admin/updates`                | Body: UpdatesDTO          | 新增动态消息     |
| 2    | `GET`      | `/admin/updates/page`           | Query: UpdatesPageQueryDTO| 分页查询动态消息 |
| 3    | `GET`      | `/admin/updates/{id}`           | Path: id                  | 根据ID查询动态消息 |
| 4    | `PUT`      | `/admin/updates`                | Body: UpdatesDTO          | 修改动态消息     |
| 5    | `DELETE`   | `/admin/updates/{id}`           | Path: id                  | 删除动态消息     |

**UpdatesDTO 字段说明：**

| 字段    | 类型   | 必填 | 说明     |
|---------|--------|------|----------|
| id      | Long   | 否   | 动态ID   |
| title   | String | 是   | 动态标题 |
| picture | String | 否   | 图片路径 |
| content | String | 否   | 动态内容 |

**UpdatesPageQueryDTO 字段说明：**

| 字段     | 类型    | 必填 | 说明             |
|----------|---------|------|------------------|
| title    | String  | 否   | 动态标题（模糊搜索） |
| page     | int     | 是   | 页码             |
| pageSize | int     | 是   | 每页记录数       |

---

## 十六、管理端 - 关于我们管理

**Controller:** `AboutController`  
**Base URL:** `/admin/about`

| 序号 | 方法       | 完整路径                        | 请求参数                | 说明               |
|------|------------|---------------------------------|-------------------------|--------------------|
| 1    | `POST`     | `/admin/about`                  | Body: AboutDTO          | 新增关于我们       |
| 2    | `GET`      | `/admin/about/page`             | Query: AboutPageQueryDTO| 分页查询关于我们   |
| 3    | `GET`      | `/admin/about/{id}`             | Path: id                | 根据ID查询关于我们 |
| 4    | `PUT`      | `/admin/about`                  | Body: AboutDTO          | 修改关于我们       |
| 5    | `DELETE`   | `/admin/about/{id}`             | Path: id                | 删除关于我们       |

**AboutDTO 字段说明：**

| 字段    | 类型   | 必填 | 说明         |
|---------|--------|------|--------------|
| id      | Long   | 否   | 关于我们ID   |
| title     | String  | 是   | 题目               |
| sortOrder | Integer | 否   | 排序（越大越靠前） |
| content   | String  | 否   | 具体内容           |

**AboutPageQueryDTO 字段说明：**

| 字段     | 类型    | 必填 | 说明             |
|----------|---------|------|------------------|
| title    | String  | 否   | 题目（模糊搜索） |
| page     | int     | 是   | 页码             |
| pageSize | int     | 是   | 每页记录数       |

---

## 十七、管理端 - 服务介绍管理

**Controller:** `ServiceIntroController`  
**Base URL:** `/admin/service-intro`

| 序号 | 方法       | 完整路径                              | 请求参数                        | 说明               |
|------|------------|---------------------------------------|---------------------------------|--------------------|
| 1    | `POST`     | `/admin/service-intro`                | Body: ServiceIntroDTO           | 新增服务介绍       |
| 2    | `GET`      | `/admin/service-intro/page`           | Query: ServiceIntroPageQueryDTO | 分页查询服务介绍   |
| 3    | `GET`      | `/admin/service-intro/{id}`           | Path: id                        | 根据ID查询服务介绍 |
| 4    | `PUT`      | `/admin/service-intro`                | Body: ServiceIntroDTO           | 修改服务介绍       |
| 5    | `DELETE`   | `/admin/service-intro/{id}`           | Path: id                        | 删除服务介绍       |

**ServiceIntroDTO 字段说明：**

| 字段    | 类型   | 必填 | 说明     |
|---------|--------|------|----------|
| id      | Long   | 否   | 服务介绍ID |
| title   | String | 是   | 服务标题   |
| picture | String | 否   | 图片路径   |
| content | String | 否   | 服务内容   |

**ServiceIntroPageQueryDTO 字段说明：**

| 字段     | 类型    | 必填 | 说明               |
|----------|---------|------|--------------------|
| title    | String  | 否   | 服务标题（模糊搜索） |
| page     | int     | 是   | 页码               |
| pageSize | int     | 是   | 每页记录数         |

---

## 十八、C端 - 搜索记录管理

**Controller:** `SearchHistoryController`  
**Base URL:** `/user/search-history`

| 序号 | 方法     | 完整路径                         | 请求参数            | 说明                |
|------|----------|----------------------------------|---------------------|---------------------|
| 1    | `POST`   | `/user/search-history`           | Query: keyword      | 新增搜索记录        |
| 2    | `GET`    | `/user/search-history/list`      | —                   | 查询搜索记录（最新10条） |
| 3    | `DELETE` | `/user/search-history/clean`     | —                   | 清空搜索记录        |

---

## 十九、管理端 - 服务推荐管理

**Controller:** `HotPicksController`  
**Base URL:** `/admin/hot-picks`

| 序号 | 方法     | 完整路径                              | 请求参数                    | 说明                   |
|------|----------|---------------------------------------|-----------------------------|------------------------|
| 1    | `POST`   | `/admin/hot-picks`                    | Body: HotPicksDTO           | 新增服务推荐           |
| 2    | `GET`    | `/admin/hot-picks/page`               | Query: HotPicksPageQueryDTO | 分页查询服务推荐       |
| 3    | `GET`    | `/admin/hot-picks/{id}`               | Path: id                    | 根据ID查询服务推荐     |
| 4    | `PUT`    | `/admin/hot-picks`                    | Body: HotPicksDTO           | 修改服务推荐           |
| 5    | `POST`   | `/admin/hot-picks/status/{status}`    | Query: id                   | 启用/禁用服务推荐      |
| 6    | `DELETE` | `/admin/hot-picks/{id}`               | Path: id                    | 删除服务推荐           |

**HotPicksDTO 字段说明：**

| 字段      | 类型    | 必填 | 说明                 |
|-----------|---------|------|----------------------|
| id        | Long    | 否   | 推荐ID               |
| name      | String  | 是   | 服务名称             |
| picture   | String  | 是   | 服务推荐图片路径     |
| status    | Integer | 否   | 状态 0关闭 1启用     |
| sortOrder | Integer | 否   | 排序（越小越靠前）   |

**HotPicksPageQueryDTO 字段说明：**

| 字段     | 类型    | 必填 | 说明               |
|----------|---------|------|--------------------|
| name     | String  | 否   | 服务名称（模糊搜索） |
| status   | Integer | 否   | 按状态筛选          |
| page     | int     | 是   | 页码                |
| pageSize | int     | 是   | 每页记录数          |

---

## 二十、C端 - 最新动态搜索记录管理

**Controller:** `UpdatesSearchHistoryController`  
**Base URL:** `/user/updates-search-history`

| 序号 | 方法     | 完整路径                                  | 请求参数       | 说明                     |
|------|----------|-------------------------------------------|----------------|--------------------------|
| 1    | `POST`   | `/user/updates-search-history`            | Query: keyword | 新增搜索记录             |
| 2    | `GET`    | `/user/updates-search-history/list`       | —              | 查询搜索记录（最新10条） |
| 3    | `DELETE` | `/user/updates-search-history/clean`      | —              | 清空搜索记录             |

---

## 二十一、C端 - 服务介绍搜索记录管理

**Controller:** `ServiceIntroSearchHistoryController`  
**Base URL:** `/user/service-intro-search-history`

| 序号 | 方法     | 完整路径                                        | 请求参数       | 说明                     |
|------|----------|-------------------------------------------------|----------------|--------------------------|
| 1    | `POST`   | `/user/service-intro-search-history`            | Query: keyword | 新增搜索记录             |
| 2    | `GET`    | `/user/service-intro-search-history/list`       | —              | 查询搜索记录（最新10条） |
| 3    | `DELETE` | `/user/service-intro-search-history/clean`      | —              | 清空搜索记录             |

---

## 二十二、管理端 - 家政员管理

**Controller:** `HousekeeperController`  
**Base URL:** `/admin/housekeeper`

| 序号 | 方法   | 完整路径                           | 请求参数                      | 说明                                                      |
|------|--------|------------------------------------|-------------------------------|-----------------------------------------------------------|
| 1    | `GET`  | `/admin/housekeeper/page`          | Query: HousekeeperPageQueryDTO| 分页查询家政员                                            |
| 2    | `GET`  | `/admin/housekeeper/{id}`          | Path: id                      | 根据ID查询家政员                                          |
| 3    | `PUT`  | `/admin/housekeeper`               | Body: HousekeeperDTO          | 修改家政员                                                |
| 4    | `GET`  | `/user/housekeeper/top10`          | —                             | C端查询前10条家政员                                       |
| 5    | `GET`  | `/user/housekeeper/page`           | Query: HousekeeperPageQueryDTO| C端分页查询家政员（支持名字搜索）                         |
| 6    | `GET`  | `/user/housekeeper/{id}`           | Path: id                      | C端根据ID查询家政员详情                                   |
| 7    | `GET`  | `/user/housekeeper/{id}/employee`  | Path: id                      | C端根据家政员ID查询关联的员工信息（含详细信息，供详情页下盒子） |

> **说明：** 家政员的新增和删除由员工管理联动控制——新增员工时若 role=cleaner 则自动创建家政员记录，修改员工角色时自动新增/删除对应家政员记录，删除员工时自动删除家政员记录。

**HousekeeperDTO 字段说明：**

| 字段       | 类型      | 必填 | 说明                         |
|------------|-----------|------|------------------------------|
| id         | Long      | 否   | 家政员ID（修改时必填）       |
| employeeId | Long      | 否   | 关联员工表ID                 |
| name       | String    | 否   | 家政员名字（关联员工name）   |
| price      | BigDecimal| 否   | 单价                         |
| rating     | BigDecimal| 否   | 评分（0-5）                  |
| carousel   | String    | 否   | 轮播图（多图URL，分号分隔）  |
| rank       | Integer   | 否   | 身价 0无牌 1金牌 2银牌 3铜牌 |
| review     | String    | 否   | 综合评价                     |

**GET /user/housekeeper/{id}/employee 响应字段（HousekeeperEmployeeVO）：**

| 字段              | 类型    | 说明                                     | 数据来源       |
|-------------------|---------|------------------------------------------|----------------|
| employeeId        | Long    | 员工ID                                   | employee       |
| name              | String  | 员工姓名                                 | employee       |
| phone             | String  | 手机号                                   | employee       |
| sex               | String  | 性别                                     | employee       |
| age               | String  | 年龄                                     | employee       |
| education         | String  | 学历（如：本科、大专、高中）             | employee_detail|
| pastExperience    | String  | 过往经历（如：3年家政经验）               | employee_detail|
| position          | String  | 职位（如：高级家政员）                    | employee_detail|
| workStatus        | String  | 在职状态（如：在职、离职）                | employee_detail|
| healthCertificate | String  | 健康证（如：已办理、未办理）              | employee_detail|

> **说明：** 该接口使用 Redis 缓存（`@Cacheable`），首次请求查 MySQL 并写入 Redis，后续请求直接从 Redis 返回。缓存 TTL 默认 30 分钟。

---

## 二十三、C端 - 家政员搜索记录管理

**Controller:** `HousekeeperSearchHistoryController`  
**Base URL:** `/user/housekeeper-search-history`

| 序号 | 方法     | 完整路径                                    | 请求参数       | 说明                     |
|------|----------|---------------------------------------------|----------------|--------------------------|
| 1    | `POST`   | `/user/housekeeper-search-history`          | Query: keyword | 新增搜索记录             |
| 2    | `GET`    | `/user/housekeeper-search-history/list`     | —              | 查询搜索记录（最新10条） |
| 3    | `DELETE` | `/user/housekeeper-search-history/clean`    | —              | 清空搜索记录             |

---

## 二十四、C端 - 资讯中心

**Controller:** `FeedUserController`  
**Base URL:** `/user/feed`

| 序号 | 方法  | 完整路径           | 请求参数 | 说明                                           |
|------|-------|--------------------|----------|------------------------------------------------|
| 1    | `GET` | `/user/feed/list`  | —        | 查询资讯列表（最多20条，按排序+时间降序）       |

> **说明：** feed 表为联表结构（只存 updates_id、service_intro_id + 时间），查询时 UNION ALL + LEFT JOIN 两张原表获取 title/picture/content。updates 和 service_intro 增删时自动同步 feed。

---

## 二十五、员工端 - 家政员预约时间管理

**Controller:** `HousekeeperScheduleEmployeeController`  
**Base URL:** `/employee/housekeeper-schedule`

| 序号 | 方法     | 完整路径                                | 请求参数                              | 说明               |
|------|----------|-----------------------------------------|---------------------------------------|--------------------|
| 1    | `POST`   | `/admin/housekeeper-schedule`           | Body: HousekeeperScheduleDTO          | 新增预约时间       |
| 2    | `GET`    | `/admin/housekeeper-schedule/page`      | Query: HousekeeperSchedulePageQueryDTO| 分页查询预约时间   |
| 3    | `GET`    | `/admin/housekeeper-schedule/{id}`      | Path: id                              | 根据ID查询         |
| 4    | `PUT`    | `/admin/housekeeper-schedule`           | Body: HousekeeperScheduleDTO          | 修改预约时间       |
| 5    | `DELETE` | `/admin/housekeeper-schedule/{id}`      | Path: id                              | 删除               |

**HousekeeperScheduleDTO 字段说明：**

| 字段           | 类型    | 必填 | 说明                         |
|----------------|---------|------|------------------------------|
| id             | Long    | 否   | 主键ID（修改时必填）         |
| housekeeperId  | Long    | 是   | 关联家政员ID                 |
| scheduleDate   | String  | 是   | 预约日期 yyyy-MM-dd          |
| startTime      | String  | 是   | 开始时间 HH:mm               |
| endTime        | String  | 是   | 结束时间 HH:mm               |
| status         | Integer | 否   | 0可约 1已约（默认0）         |

---

## 认证说明

- 管理端接口（`/admin/**`）使用管理员 JWT Token 认证。
- 员工端接口（`/employee/**`）使用员工 JWT Token 认证。
- C端接口（`/user/**`）使用用户 JWT Token 认证。
- 部分接口无需登录（如：获取密保问题 `/admin/auth/securityQuestion`，重置密码 `/admin/auth/resetPassword`）。

---

## 二十六、C端 - 家政员可预约时段查询

**Controller:** `HousekeeperScheduleUserController`
**Base URL:** `/user/housekeeper-schedule`

| 序号 | 方法  | 完整路径                                    | 请求参数                   | 说明                              |
|------|-------|---------------------------------------------|----------------------------|-----------------------------------|
| 1    | `GET` | `/user/housekeeper-schedule/available`      | Query: housekeeperId (Long) | 查询某家政员所有可预约时段(status=0) |

> **说明：** 按日期+开始时间升序排列，仅返回状态为0（可约）的时段。使用 Redis 缓存（@Cacheable key="available:家政员ID"）。

---

## 二十七、C端 - 用户订单管理

**Controller:** `OrderUserController`
**Base URL:** `/user/order`

| 序号 | 方法   | 完整路径               | 请求参数                | 说明             |
|------|--------|------------------------|-------------------------|------------------|
| 1    | `POST` | `/user/order/submit`   | Body: OrderSubmitDTO    | 提交订单（从购物车生成） |
| 2    | `GET`  | `/user/order/page`     | Query: OrderPageQueryDTO| 分页查询自己的订单       |
| 3    | `GET`  | `/user/order/{id}`     | Path: id                | 查询订单详情（含明细列表）|
| 4    | `PUT`  | `/user/order/cancel`   | Body: OrderCancelDTO    | 取消订单                 |
| 5    | `PUT`  | `/user/order/pay/{id}` | Path: id                | 支付订单                 |

**OrderSubmitDTO 字段：**

| 字段                | 类型          | 必填 | 说明                     |
|---------------------|---------------|------|--------------------------|
| addressBookId       | Long          | 是   | 地址ID                   |
| remark              | String        | 否   | 备注                     |
| estimatedServiceTime| LocalDateTime | 否   | 预计上门时间             |
| payMethod           | Integer       | 否   | 支付方式 1微信 2支付宝   |

**OrderPageQueryDTO 字段：**

| 字段        | 类型    | 必填 | 说明               |
|-------------|---------|------|--------------------|
| page        | Integer | 否   | 页码（默认1）      |
| pageSize    | Integer | 否   | 每页条数（默认10） |
| status      | Integer | 否   | 订单状态筛选       |
| number      | String  | 否   | 订单号模糊搜索     |

**OrderSubmitVO 响应字段：**

| 字段        | 类型          | 说明     |
|-------------|---------------|----------|
| orderId     | Long          | 订单ID   |
| orderNumber | String        | 订单号   |
| amount      | BigDecimal    | 订单金额 |
| orderTime   | LocalDateTime | 下单时间 |

**OrderVO 响应字段（extends Order）：**

| 字段             | 类型                   | 说明         |
|------------------|------------------------|--------------|
| (Order全部字段)  | —                      | 订单基本信息 |
| orderDetailList  | List\<OrderDetail\>     | 订单明细列表 |

**OrderDetail 明细字段：**

| 字段             | 类型        | 说明                             |
|------------------|-------------|----------------------------------|
| id               | Long        | 明细ID                           |
| orderId          | Long        | 订单ID                           |
| housekeeperId    | Long        | 家政员ID（预约家政员时有值）     |
| scheduleId       | Long        | 时段ID                           |
| serviceItemId    | Long        | 服务项目ID（预约服务时有值）     |
| servicePackageId | Long        | 服务套餐ID（预约套餐时有值）     |
| name             | String      | 名称                             |
| image            | String      | 图片                             |
| serviceDuration  | String      | 服务时长（如：2小时、半天、全天）|
| requirement      | String      | 服务要求                         |
| amount           | BigDecimal  | 金额                             |

> **下单流程：** 用户添加服务到购物车 → POST /user/order/submit（传地址+支付方式）→ 后端从购物车读取明细生成订单 → 保存订单+明细 → 清空购物车 → 返回订单号。
> **状态流转：** 1待付款 → 支付 → 2待接单 → 接单 → 3已接单 → 开始服务 → 4服务中 → 完成 → 5已完成；1/2 → 取消 → 6已取消。
> **Redis缓存：** 读 @Cacheable(value="order", key="one:订单id")；写 @CacheEvict(value="order", allEntries=true)。

---

## 二十八、员工端 - 订单管理

**Controller:** `OrderEmployeeController`
**Base URL:** `/employee/order`

| 序号 | 方法  | 完整路径                               | 请求参数                | 说明                          |
|------|-------|----------------------------------------|-------------------------|-------------------------------|
| 1    | `GET` | `/employee/order/page`                 | Query: OrderPageQueryDTO| 分页查询所有订单              |
| 2    | `GET` | `/employee/order/{id}`                 | Path: id                | 查询订单详情                  |
| 3    | `PUT` | `/employee/order/status/{id}`          | Path: id, Query: status | 更新订单状态（接单/服务/完成）|
| 4    | `PUT` | `/employee/order/estimated-time/{id}`  | Path: id, Body: OrderDTO| 设置预计上门时间              |

> **说明：** 所有接口需要 admin/manager 角色（@RoleCheck）

---

## 二十九、员工端 - 优惠券管理

**Controller:** `VoucherController`
**Base URL:** `/employee/voucher`

| 序号 | 方法     | 完整路径                             | 请求参数                      | 说明                                                 |
|------|----------|--------------------------------------|-------------------------------|------------------------------------------------------|
| 1    | `POST`   | `/employee/voucher`                  | Body: VoucherDTO              | 新增优惠券（可设置普通券/秒杀券、起止时间、库存）     |
| 2    | `GET`    | `/employee/voucher/page`             | Query: VoucherPageQueryDTO    | 分页查询优惠券（支持标题/类型/状态筛选）              |
| 3    | `GET`    | `/employee/voucher/{id}`             | Path: id                      | 根据ID查询优惠券（含秒杀信息）                        |
| 4    | `PUT`    | `/employee/voucher`                  | Body: VoucherDTO              | 修改优惠券（起止时间、库存、status等）                |
| 5    | `POST`   | `/employee/voucher/status/{status}`  | Path: status, Query: id       | 上架/下架/过期（1上架；2下架；3过期）                 |
| 6    | `DELETE` | `/employee/voucher/{id}`             | Path: id                      | 删除优惠券（同步删除秒杀信息）                        |

> **说明：** 需要 admin/manager 角色（@RoleCheck）。优惠券 id 由 RedisIdWorker 全局ID生成器生成（非自增）。优惠券对所有服务/套餐订单通用；`orderId` 在用户使用该券支付订单时回填（关联 orders 表）。秒杀券（type=1）联动 tb_seckill_voucher 表（库存+起止时间），普通券不涉及该表。秒杀券的生效时间必须早于失效时间，否则后端返回"生效时间必须早于失效时间"。状态只有上架/下架，过期由截止时间自动判断。

**VoucherDTO 字段说明：**

| 字段             | 类型          | 必填 | 说明                               |
|------------------|---------------|------|------------------------------------|
| id               | Long          | 否   | 优惠券ID（新增时后端生成）         |
| orderId          | Long          | 否   | 使用该券的订单id（支付时用券后回填）|
| title            | String        | 是   | 代金券标题                         |
| subTitle         | String        | 否   | 副标题                             |
| rules            | String        | 否   | 使用规则                           |
| payValue         | Long          | 否   | 原价，单位分（可选，默认0）        |
| actualValue      | Long          | 是   | 折扣率（八折=80，八五折=85）       |
| type             | Integer       | 是   | 0普通券；1秒杀券                   |
| status           | Integer       | 否   | 1上架；2下架（默认1，过期由截止时间自动判断）|
| stock            | Integer       | 否   | 秒杀券专属：库存                   |
| beginTime        | LocalDateTime | 否   | 秒杀券专属：生效时间               |
| endTime          | LocalDateTime | 否   | 秒杀券专属：失效时间               |

**VoucherPageQueryDTO 字段说明：**

| 字段     | 类型    | 必填 | 说明               |
|----------|---------|------|--------------------|
| title    | String  | 否   | 标题（模糊搜索）   |
| type     | Integer | 否   | 类型筛选           |
| status   | Integer | 否   | 状态筛选           |
| page     | int     | 是   | 页码               |
| pageSize | int     | 是   | 每页记录数         |

---

## 三十、C端 - 优惠券（领取/秒杀）

**Controller:** `VoucherUserController`
**Base URL:** `/user/voucher`

| 序号 | 方法   | 完整路径                 | 请求参数                | 说明                                                 |
|------|--------|--------------------------|-------------------------|------------------------------------------------------|
| 1    | `GET`  | `/user/voucher/list`     | —                       | 查询上架中的优惠券列表（含秒杀库存/起止时间）         |
| 2    | `POST` | `/user/voucher/seckill`  | Body: VoucherSeckillDTO | 领取/秒杀优惠券（普通券直接领取；秒杀券校验+扣库存） |

> **说明：** 需用户登录。秒杀券领取逻辑：查询优惠券 → 判断秒杀是否开始 → 判断库存是否充足 → 判断用户id是否存在 → 判断是否在有效时间内且上架(status=1) → Redisson 分布式锁（RLock）锁用户id解决一人一单 → 乐观锁扣减库存（防超卖）→ 领取成功。领取记录存于 Redis（key=claim:用户id），每人每券限领一单。超过结束时间的秒杀券由后端自动置为过期（status=3）。

**VoucherSeckillDTO 字段说明：**

| 字段      | 类型 | 必填 | 说明             |
|-----------|------|------|------------------|
| voucherId | Long | 是   | 要领取的优惠券id |

**VoucherVO 响应字段（含秒杀信息）：**

| 字段             | 类型          | 说明                               |
|------------------|---------------|------------------------------------|
| id               | Long          | 优惠券ID                           |
| orderId          | Long          | 使用该券的订单id（支付时用券后回填）|
| title            | String        | 代金券标题                         |
| subTitle         | String        | 副标题                             |
| rules            | String        | 使用规则                           |
| payValue         | Long          | 原价，单位分（展示用）             |
| actualValue      | Long          | 折扣率（八折=80，八五折=85）       |
| type             | Integer       | 0普通券；1秒杀券                   |
| status           | Integer       | 1上架；2下架（过期由截止时间自动判断）|
| createTime       | LocalDateTime | 创建时间                           |
| updateTime       | LocalDateTime | 更新时间                           |
| stock            | Integer       | 秒杀券专属：库存                   |
| beginTime        | LocalDateTime | 秒杀券专属：生效时间               |
| endTime          | LocalDateTime | 秒杀券专属：失效时间               |
