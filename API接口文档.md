# smart-housekeeping-system 后端接口文档

> 项目：smart-housekeeping-system（智能家政服务）  
> 文档生成日期：2026-07-24  
> 最后更新：2026-10-29（补充管理端工作台统计、员工端工作台、消息通知、家政员端、C端内容查询等缺失接口，按代码实际路由校正并重新统计）

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
- [二十三、C端 - 家政员搜索记录管理](#二十三c端---家政员搜索记录管理)
- [二十四、C端 - 资讯中心](#二十四c端---资讯中心)
- [二十五、员工端 - 家政员预约时间管理](#二十五员工端---家政员预约时间管理)
- [认证说明](#认证说明)
- [二十六、C端 - 家政员可预约时段查询](#二十六c端---家政员可预约时段查询)
- [二十七、C端 - 用户订单管理](#二十七c端---用户订单管理)
- [二十八、员工端 - 订单管理](#二十八员工端---订单管理)
- [二十九、员工端 - 优惠券管理](#二十九员工端---优惠券管理)
- [三十、C端 - 优惠券（领取/秒杀）](#三十c端---优惠券领取秒杀)
- [三十一、管理端 - 工作台统计](#三十一管理端---工作台统计)
- [三十二、员工端 - 家政员查询](#三十二员工端---家政员查询)
- [三十三、员工端 - 订单提醒消息中心](#三十三员工端---订单提醒消息中心)
- [三十四、C端 - 用户消息通知](#三十四c端---用户消息通知)
- [三十五、C端 - 内容查询（关于我们/服务推荐/服务介绍/最新动态）](#三十五c端---内容查询关于我们服务推荐服务介绍最新动态)
- [三十六、家政员端（小程序）- 认证 / 订单 / 个人资料](#三十六家政员端小程序---认证--订单--个人资料)
- [三十七、员工端 - 工作台](#三十七员工端---工作台)
- [接口汇总统计](#接口汇总统计)

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

| 序号 | 方法     | 完整路径                                              | 请求参数                              | 说明               |
|------|----------|-------------------------------------------------------|---------------------------------------|--------------------|
| 1    | `POST`   | `/employee/housekeeper-schedule`                      | Body: HousekeeperScheduleDTO          | 新增预约时间       |
| 2    | `GET`    | `/employee/housekeeper-schedule/page`                 | Query: HousekeeperSchedulePageQueryDTO| 分页查询预约时间   |
| 3    | `GET`    | `/employee/housekeeper-schedule/{id}`                 | Path: id                              | 根据ID查询         |
| 4    | `PUT`    | `/employee/housekeeper-schedule`                      | Body: HousekeeperScheduleDTO          | 修改预约时间       |
| 5    | `DELETE` | `/employee/housekeeper-schedule/{id}`                 | Path: id                              | 删除               |
| 6    | `DELETE` | `/employee/housekeeper-schedule/batch`                | Body: List&lt;Long&gt;                     | 批量删除           |
| 7    | `PUT`    | `/employee/housekeeper-schedule/batch/status/{status}`| Body: List&lt;Long&gt;, Path: status       | 批量更新状态       |

> **说明：** 所有接口需要 admin/manager 角色（`@RoleCheck`）。接口已从早期的 `/admin/housekeeper-schedule` 迁移到员工端前缀。

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

所有登录态接口的令牌统一放在请求头 `token` 中。

- **管理端**（`/admin/**`）：管理员 JWT。
  免登录白名单：`/admin/auth/login`、`/admin/auth/securityQuestion`、`/admin/auth/resetPassword`、`/admin/common/download/**`、`/admin/shop/getStatus`、`/admin/category/list`。
- **员工端 + 家政员端**（`/employee/**`、`/worker/**`）：员工 JWT（家政员小程序登录签发的同样是员工令牌）。
  免登录白名单：`/employee/auth/login`、`/employee/service-item/page`、`/employee/service-package/page`、`/worker/auth/login`。
- **C端**（`/user/**`）：用户 JWT。
  免登录白名单：`/user/user/login`。

> 拦截器配置见 `WebMvcConfiguration#addInterceptors`。

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
| 5    | `PUT`  | `/user/order/pay/{id}` | Path: id, Query: payMethod（可选） | 支付订单      |
| 6    | `POST` | `/user/order/submit-item` | Body: OrderSubmitDTO（含 item） | 提交单条服务预约（仅一条明细，并删除购物车对应项） |
| 7    | `POST` | `/user/order/{id}/remind`  | Path: id              | 客户催单：推送提醒给在线员工端（仅限自己的订单） |

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
| 5    | `GET` | `/employee/order/candidates`           | Query: orderId          | 查询该订单服务时段内空闲的家政员候选 |
| 6    | `POST`| `/employee/order/{id}/assign`          | Path: id, Body: OrderAssignDTO | 指派家政员（订单仍停在待接单，等家政员确认）|
| 7    | `POST`| `/employee/order/{id}/cancel`          | Path: id, Body: OrderCancelDTO | 取消待接单订单（必填 cancelReason）→ 待退款 |
| 8    | `POST`| `/employee/order/{id}/refund`          | Path: id, Body: OrderRefundDTO | 确认退款（必填 refundReason）：待退款 → 已取消 |

> **说明：** 所有接口需要 admin/manager 角色（@RoleCheck）。
> **分页返回：** `/employee/order/page` 返回 `PageResult`（`{ total, records }`），`records` 中每个元素是 `OrderVO`（含订单全部字段 + `orderDetailList` 明细），前端分页取 `data.total / data.records`。
> **列表缓存：** 订单列表为实时管理数据，不做缓存；订单详情 `/{id}` 走 `order` 缓存（`@Cacheable(value="order", key="'one:'+#id")`，允许缓存空值防穿透、5 分钟 TTL、随机抖动防雪崩）。
> **手机号过滤：** 员工端订单页新增“手机号”查询条件，DTO 增加 `phone` 字段（orders.phone 模糊匹配）。

**OrderPageQueryDTO 字段说明：**

| 字段     | 类型    | 必填 | 说明                                        |
|----------|---------|------|---------------------------------------------|
| page     | int     | 是   | 页码（默认1）                               |
| pageSize | int     | 是   | 每页条数（默认10）                          |
| number   | String  | 否   | 订单号（模糊搜索）                          |
| status   | Integer | 否   | 订单状态：1待付款 2待接单 3已接单 4服务中 5已完成 6退款中 7已取消（全部订单不传）|
| phone    | String  | 否   | 手机号（模糊搜索）                          |
| beginTime| String  | 否   | 下单开始时间，如 `2026-09-01` 或 `2026-09-01 00:00:00` |
| endTime  | String  | 否   | 下单截止时间（建议前端传 `yyyy-MM-dd 23:59:59` 以包含结束当天）|

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
| 3    | `GET`  | `/user/voucher/mine`     | —                       | 我的优惠券（含已使用，按领取时间倒序）               |

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

---

## 三十一、管理端 - 工作台统计

**Controller:** `StatisticsController`  
**Base URL:** `/admin/statistics`

> 工作台数据总览，供 Web 管理端「工作台」页面使用。所有接口均为只读聚合查询，需要管理员登录（`/admin/**` 由 token 拦截器校验）。

| 序号 | 方法  | 完整路径                       | 请求参数                            | 说明                                       |
|------|-------|--------------------------------|-------------------------------------|--------------------------------------------|
| 1    | `GET` | `/admin/statistics/turnover`   | Query: beginTime, endTime（均可选） | 营业额统计（总额、有效订单数、客单价、每日趋势）|
| 2    | `GET` | `/admin/statistics/user`       | Query: beginTime, endTime（均可选） | 用户统计（总数、新增数、每日新增趋势）      |
| 3    | `GET` | `/admin/statistics/order`      | Query: beginTime, endTime（均可选） | 订单统计（总数、有效/取消数、每日趋势、状态分布）|
| 4    | `GET` | `/admin/statistics/sales`      | Query: beginTime, endTime（均可选） | 销量排名 Top 10（服务项目/服务套餐）        |

**通用查询参数：**

| 字段      | 类型   | 必填 | 说明                                                        |
|-----------|--------|------|-------------------------------------------------------------|
| beginTime | String | 否   | 开始日期 `yyyy-MM-dd`；不传默认「今天往前 29 天」            |
| endTime   | String | 否   | 结束日期 `yyyy-MM-dd`；不传默认今天                          |

> 区间规则：只传一端时，另一端按 30 天自动补齐；开始日期不能晚于结束日期；单次查询跨度不能超过 366 天，否则返回 `code=0` 及错误信息。

**统计口径：**

- **营业额 / 有效订单**：`orders.status = 5`（已完成）且 `pay_status = 1`（已支付），按 `pay_time` 归日；
- **平均客单价**：总营业额 ÷ 有效订单数（无有效订单时为 0）；
- **订单数**：按 `order_time` 归日；**已取消订单** = 退款中(6) + 已取消(7)；
- **用户**：总数为 `user` 表全量，新增按 `create_time` 归日；
- **销量排名**：已完成且已支付订单的 `order_detail`，只统计服务项目/服务套餐明细（排除家政员明细），按项目/套餐分组取前 10。

**响应字段：**

`/admin/statistics/turnover`

| 字段            | 类型            | 说明                                 |
|-----------------|-----------------|--------------------------------------|
| totalTurnover   | BigDecimal      | 区间总营业额                         |
| validOrderCount | Long            | 有效订单数                           |
| avgOrderAmount  | BigDecimal      | 平均客单价（保留 2 位小数）          |
| dateList        | List&lt;String&gt;  | 日期列表 `yyyy-MM-dd`（区间内每天都有）|
| turnoverList    | List&lt;BigDecimal&gt; | 每日营业额，与 dateList 一一对应（无数据补 0）|

`/admin/statistics/user`

| 字段        | 类型            | 说明                         |
|-------------|-----------------|------------------------------|
| totalUser   | Long            | 用户总数                     |
| newUser     | Long            | 区间新增用户数               |
| dateList    | List&lt;String&gt;  | 日期列表                     |
| newUserList | List&lt;Long&gt;    | 每日新增用户数（无数据补 0） |

`/admin/statistics/order`

| 字段           | 类型                  | 说明                              |
|----------------|-----------------------|-----------------------------------|
| totalOrder     | Long                  | 区间订单总数                      |
| validOrder     | Long                  | 有效订单数（已完成且已支付）      |
| cancelledOrder | Long                  | 已取消订单数（退款中 + 已取消）   |
| dateList       | List&lt;String&gt;        | 日期列表                          |
| orderList      | List&lt;Long&gt;          | 每日订单数（无数据补 0）          |
| statusList     | List&lt;StatusCountVO&gt; | 各状态订单数，仅包含出现过的状态  |

`StatusCountVO`：`status`（状态值）、`name`（状态名称）、`count`（订单数）。

`/admin/statistics/sales`

| 字段       | 类型                | 说明                       |
|------------|---------------------|----------------------------|
| nameList   | List&lt;String&gt;      | 服务名称，按销量从高到低   |
| numberList | List&lt;Long&gt;        | 销量（明细条数）           |
| amountList | List&lt;BigDecimal&gt;  | 销售额（明细金额之和）     |

> 三个列表等长且下标一一对应；区间内没有销量时均为空数组。

---

## 三十二、员工端 - 家政员查询

**Controller:** `HousekeeperEmployeeController`  
**Base URL:** `/employee/housekeeper`

| 序号 | 方法  | 完整路径                        | 请求参数                     | 说明                           |
|------|-------|---------------------------------|------------------------------|--------------------------------|
| 1    | `GET` | `/employee/housekeeper/page`    | Query: HousekeeperPageQueryDTO | 分页查询家政员列表（供下拉选择用）|

> **说明：** 需要 admin/manager 角色（`@RoleCheck`）。返回 `PageResult`。

---

## 三十三、员工端 - 订单提醒消息中心

**Controller:** `OrderNotifyEmployeeController`  
**Base URL:** `/employee/order-notify`

| 序号 | 方法   | 完整路径                              | 请求参数             | 说明                                     |
|------|--------|---------------------------------------|----------------------|------------------------------------------|
| 1    | `GET`  | `/employee/order-notify/list`         | Query: limit（默认30）| 最近 N 条提醒消息（含当前员工是否已读）  |
| 2    | `GET`  | `/employee/order-notify/unread-count` | —                    | 当前员工未读消息数（角标）               |
| 3    | `POST` | `/employee/order-notify/{id}/read`    | Path: id             | 标记某条消息已读                         |
| 4    | `POST` | `/employee/order-notify/read-all`     | —                    | 一键全部已读                             |

> **说明：** 消息按员工区分已读状态，数据来源为来单提醒（`newOrder`）与客户催单。需要 admin/manager 角色。

---

## 三十四、C端 - 用户消息通知

**Controller:** `UserNotifyController`  
**Base URL:** `/user/notify`

| 序号 | 方法   | 完整路径                          | 请求参数             | 说明                             |
|------|--------|-----------------------------------|----------------------|----------------------------------|
| 1    | `GET`  | `/user/notify/list`               | Query: limit（默认30）| 消息列表（默认最近 30 条）       |
| 2    | `GET`  | `/user/notify/unread-count`       | —                    | 未读消息数（小程序红点）         |
| 3    | `POST` | `/user/notify/{id}/read`          | Path: id             | 标记单条已读                     |
| 4    | `POST` | `/user/notify/read-all`           | —                    | 一键全部已读                     |

> **说明：** 需要用户登录（`/user/**` 由 token 拦截器校验，userId 取自 JWT）。

---

## 三十五、C端 - 内容查询（关于我们/服务推荐/服务介绍/最新动态）

| 序号 | 方法  | 完整路径                          | 请求参数                        | 说明                                   |
|------|-------|-----------------------------------|---------------------------------|----------------------------------------|
| 1    | `GET` | `/user/about/list`                | —                               | 查询全部「关于我们」（按排序+创建时间）|
| 2    | `GET` | `/user/hot-picks/list`            | —                               | 查询启用的服务推荐（最多5条）          |
| 3    | `GET` | `/user/service-intro/page`        | Query: ServiceIntroPageQueryDTO | 分页查询服务介绍                       |
| 4    | `GET` | `/user/service-intro/{id}`        | Path: id                        | 服务介绍详情                           |
| 5    | `GET` | `/user/updates/page`              | Query: UpdatesPageQueryDTO      | 分页查询最新动态                       |
| 6    | `GET` | `/user/updates/{id}`              | Path: id                        | 最新动态详情                           |

> **说明：** 对应 Controller 分别为 `AboutUserController`(`/user/about`)、`HotPicksUserController`(`/user/hot-picks`)、`ServiceIntroUserController`(`/user/service-intro`)、`UpdatesUserController`(`/user/updates`)，均为 C 端只读查询。

---

## 三十六、家政员端（小程序）- 认证 / 订单 / 个人资料

**Controllers:** `WorkerAuthController`、`WorkerOrderController`、`WorkerProfileController`

| 序号 | 方法   | 完整路径                              | 请求参数                                | 说明                                             |
|------|--------|---------------------------------------|-----------------------------------------|--------------------------------------------------|
| 1    | `POST` | `/worker/auth/login`                  | Body: EmployeeLoginDTO                  | 家政员登录（账号须在 employee 表且 role=cleaner）|
| 2    | `GET`  | `/worker/order/page`                  | Query: WorkerOrderPageQueryDTO          | 我的订单分页（直接返回 Page 数组，非 PageResult）|
| 3    | `GET`  | `/worker/order/pending-count`         | —                                       | 待接单数量（首页角标）                           |
| 4    | `GET`  | `/worker/order/{id}`                  | Path: id                                | 订单详情（只能查指派给自己的）                   |
| 5    | `POST` | `/worker/order/{id}/accept`           | Path: id, Body: `{estimatedArrivalTime}`| 接单：待接单 → 已接单（必须填写预计到达时间）    |
| 6    | `POST` | `/worker/order/{id}/arrive`           | Path: id                                | 确认到达：已接单 → 服务中（写入实际上门时间）    |
| 7    | `POST` | `/worker/order/{id}/complete`         | Path: id                                | 完成服务：服务中 → 已完成                        |
| 8    | `POST` | `/worker/order/{id}/reject`           | Path: id, Body: `{reason}`              | 不接单：退回「待接单」（必须填写原因）           |
| 9    | `POST` | `/worker/order/{id}/cannot-serve`     | Path: id, Body: `{reason}`              | 上报无法服务：已接单/服务中 → 待退款（必须填写原因）|
| 10   | `GET`  | `/worker/profile`                     | —                                       | 查询本人的资料、排班与可预约时段                 |

> **说明：** 除登录外，所有接口需要 cleaner 角色（`@RoleCheck`），`/worker/**` 由 token 拦截器校验。

**WorkerOrderPageQueryDTO 字段：**

| 字段     | 类型    | 必填 | 说明               |
|----------|---------|------|--------------------|
| page     | Integer | 否   | 页码（默认1）      |
| pageSize | Integer | 否   | 每页条数（默认10） |
| status   | Integer | 否   | 订单状态（空查全部）|

---

## 三十七、员工端 - 工作台

**Controller:** `WorkbenchController`  
**Base URL:** `/employee/workbench`

| 序号 | 方法  | 完整路径                        | 请求参数              | 说明                                             |
|------|-------|---------------------------------|-----------------------|--------------------------------------------------|
| 1    | `GET` | `/employee/workbench/overview`  | Query: date（可选）   | 工作台聚合数据：待办 + 当日指标 + 当日预约时段   |

> **说明：** 一次请求返回工作台全部数据，避免前端并发多个接口。需要员工登录（`/employee/**` 由 token 拦截器校验），不分角色均可访问；**营业额、有效订单数、平均客单价仅 `admin` / `manager` 角色有值，其他角色返回 `null`**（服务端按 `employee.role` 判断，前端同时按角色隐藏）。消息未读数仍使用 `/employee/order-notify/unread-count`。

**查询参数：**

| 字段 | 类型   | 必填 | 说明                                    |
|------|--------|------|-----------------------------------------|
| date | String | 否   | 统计日期 `yyyy-MM-dd`，不传默认今天      |

**响应字段：**

| 字段      | 类型                    | 说明                                       |
|-----------|-------------------------|--------------------------------------------|
| date      | String                  | 实际统计日期                               |
| todo      | WorkbenchTodoVO         | 待办事项（不限定日期）                     |
| kpi       | WorkbenchKpiVO          | 当日经营指标                               |
| timeSlots | List&lt;ScheduleSlotVO&gt;  | 当日预约时段分布，按时段字符串升序         |

`WorkbenchTodoVO`

| 字段          | 类型 | 说明                                     |
|---------------|------|------------------------------------------|
| pendingAccept | Long | 待接单（status=2，含已指派待家政员确认）  |
| pendingAssign | Long | 待指派（status=2 且 housekeeper_id 为空） |
| refunding     | Long | 待退款（status=6）                        |

`WorkbenchKpiVO`

| 字段             | 类型       | 说明                                          |
|------------------|------------|-----------------------------------------------|
| orderCount       | Long       | 当日下单数（按 order_time）                   |
| appointmentCount | Long       | 当日预约数（按 schedule_date，状态 2~5）      |
| newUser          | Long       | 当日新增用户数                                |
| turnover         | BigDecimal | 当日营业额（已完成且已支付，按 pay_time；仅 admin/manager）|
| validOrderCount  | Long       | 当日有效订单数（仅 admin/manager）            |
| avgOrderAmount   | BigDecimal | 平均客单价（仅 admin/manager）                |

`ScheduleSlotVO`：`time`（时段，如 `8:00~9:00`）、`count`（该时段单数）。

---

## 接口汇总统计

| 序号   | 模块                              | Base URL                                                      | 接口数量 |
|--------|-----------------------------------|---------------------------------------------------------------|----------|
| 一     | 管理端 - 管理员认证               | `/admin/auth`                                                 | 7        |
| 二     | 管理端 - 员工管理                 | `/admin/employee`                                             | 6        |
| 三     | 管理端 - 员工详细信息             | `/admin/employee/detail`                                      | 4        |
| 四     | 管理端 - 分类管理                 | `/admin/category`                                             | 7        |
| 五     | 管理端 - 文件上传                 | `/admin/common`                                               | 1        |
| 六     | 管理端 - AI对话会话               | `/admin/chat`                                                 | 5        |
| 七     | 管理端 - 店铺营业状态管理         | `/admin/shop`                                                 | 2        |
| 八     | 员工端 - 员工认证                 | `/employee/auth`                                              | 3        |
| 九     | 员工端 - 服务项目管理             | `/employee/service-item`                                      | 7        |
| 十     | 员工端 - 服务套餐管理             | `/employee/service-package`                                   | 7        |
| 十一   | 员工端 - 店铺营业状态             | `/employee/shop`                                              | 1        |
| 十二   | C端 - 用户登录                    | `/user/user`                                                  | 1        |
| 十三   | C端 - 地址簿管理                  | `/user/address-book`                                          | 7        |
| 十四   | C端 - 购物车管理                  | `/user/shopping-cart`                                         | 5        |
| 十五   | 管理端 - 动态消息管理             | `/admin/updates`                                              | 5        |
| 十六   | 管理端 - 关于我们管理             | `/admin/about`                                                | 5        |
| 十七   | 管理端 - 服务介绍管理             | `/admin/service-intro`                                        | 5        |
| 十八   | C端 - 搜索记录管理                | `/user/search-history`                                        | 3        |
| 十九   | 管理端 - 服务推荐管理             | `/admin/hot-picks`                                            | 6        |
| 二十   | C端 - 最新动态搜索记录管理        | `/user/updates-search-history`                                | 3        |
| 二十一 | C端 - 服务介绍搜索记录管理        | `/user/service-intro-search-history`                          | 3        |
| 二十二 | 管理端 - 家政员管理               | `/admin/housekeeper`、`/user/housekeeper`                     | 7        |
| 二十三 | C端 - 家政员搜索记录管理          | `/user/housekeeper-search-history`                            | 3        |
| 二十四 | C端 - 资讯中心                    | `/user/feed`                                                  | 1        |
| 二十五 | 员工端 - 家政员预约时间管理       | `/employee/housekeeper-schedule`                              | 7        |
| 二十六 | C端 - 家政员可预约时段查询        | `/user/housekeeper-schedule`                                  | 1        |
| 二十七 | C端 - 订单管理                    | `/user/order`                                                 | 7        |
| 二十八 | 员工端 - 订单管理                 | `/employee/order`                                             | 8        |
| 二十九 | 员工端 - 优惠券管理               | `/employee/voucher`                                           | 6        |
| 三十   | C端 - 优惠券（领取/秒杀）         | `/user/voucher`                                               | 3        |
| 三十一 | 管理端 - 工作台统计               | `/admin/statistics`                                           | 4        |
| 三十二 | 员工端 - 家政员查询               | `/employee/housekeeper`                                       | 1        |
| 三十三 | 员工端 - 订单提醒消息中心         | `/employee/order-notify`                                      | 4        |
| 三十四 | C端 - 用户消息通知                | `/user/notify`                                                | 4        |
| 三十五 | C端 - 内容查询                    | `/user/about`、`/user/hot-picks`、`/user/service-intro`、`/user/updates` | 6 |
| 三十六 | 家政员端（小程序）                | `/worker/auth`、`/worker/order`、`/worker/profile`            | 10       |
| 三十七 | 员工端 - 工作台                   | `/employee/workbench`                                         | 1        |
|        | **合计**                          |                                                               | **166**  |

---

## 三十八、异步事件与通知（RabbitMQ，补充说明）

> 本节不是新增 REST 接口，而是说明订单状态变更后的异步通知机制。原有 HTTP 接口的路径、参数和返回结构均未变化。

### 1. 订单事件

订单状态变更后，后端写入本地消息表 `mq_outbox`，由 Spring Task 定时发送到 RabbitMQ：

| 事件 | 触发时机 |
|---|---|
| `order.created` | 用户下单（购物车 / 单条预约） |
| `order.paid` | 用户支付成功 |
| `order.assigned` | 员工端指派家政员 |
| `order.accepted` | 家政员接单 |
| `order.arrived` | 家政员到达 / 开始服务 |
| `order.completed` | 服务完成 |
| `order.cancelled` | 用户申请退款、员工取消、家政员无法服务 |
| `order.refunded` | 员工端确认退款完成 |

通知由消费者统一写入 `order_notify` / `user_notify`，通常在事件产生后 3 秒内完成。

### 2. 延迟任务

| 场景 | 规则 |
|---|---|
| 未支付自动取消 | 下单后 15 分钟仍未支付 → 已取消，释放时段 |
| 指派未确认提醒 | 指派后 30 分钟仍未接单 → 员工端消息提醒 |
| 无人接单转退款 | 距服务开始不足 2 小时仍未接单 → 待退款 |

延迟消息基于 TTL + 死信队列实现；RabbitMQ 不可用时由 `OrderTask` 每分钟巡检兜底。

### 3. WebSocket

- 地址：`ws://{host}:8080/ws/order-notify`
- 可选参数：`?token={JWT}`
  - 员工令牌：按 `empId` 归组
  - 用户令牌：按 `userId` 归组，只接收自己的通知
  - 不带 token：按旧版员工端处理，接收员工端广播
- 消息体：

```json
{
  "type": "newOrder / assign / accept / acceptRemind / reject / refund ...",
  "title": "来单提醒",
  "content": "您有新的订单，订单号 HS...",
  "orderId": 123,
  "orderNumber": "HS...",
  "amount": 99.00
}
```
