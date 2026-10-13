# 数据库建表脚本

这个目录用来**从零建出后端需要的库和表**。每个 `.sql` 文件都是统一形式：开头 `USE \`smart_housekeeping_system\`;`，正文只有一条 `CREATE TABLE`（一个文件一张表）。

按编号从小到大依次执行即可，`00` 先跑（建库），然后 `01` → `31`。

## 执行方式

**方式一：命令行批量执行（推荐）**

在 `db` 目录下，用 Git Bash：

```bash
cd db
for f in [0-9][0-9]_*.sql; do echo ">> $f"; mysql -uroot -pchange-me-password --default-character-set=utf8mb4 < "$f" || break; done
```

用 PowerShell：

```powershell
cd db
Get-ChildItem [0-9][0-9]_*.sql | Sort-Object Name | ForEach-Object { Write-Host ">> $_"; cmd /c "mysql -uroot -pchange-me-password --default-character-set=utf8mb4 < `"$($_.Name)`"" }
```

把 `-pchange-me-password` 换成你自己的 MySQL 密码。任何一步报错都会停下来并打印是哪个文件，照着改就行。

**方式二：用 Navicat / DataGrip 等工具**

先执行 `00_create_database.sql` 建库，再打开 `01` 到 `31` 逐个执行。注意要**按编号顺序**，因为 `07_employee_detail.sql` 里有指向 `06_employee.sql` 的外键。

## 目录说明

| 位置 | 用途 |
| --- | --- |
| `00_create_database.sql` | 建库，第一步 |
| `01_*.sql` ~ `31_*.sql` | 建表，按编号顺序执行 |
| `migrate/` | **只给「已经有数据的老库」做增量升级用**，新库不要跑 |
| `backup/` | 某次改数据前的快照备份，仅供参考，不要执行 |

### 关于编号

`01~27` 和后追加的 `28~31` 是同一批建表脚本，只是 `28~31` 是开发后期才加的表（订单提醒、用户优惠券、用户消息中心），**编号大不代表要先跑**，直接接在 `27` 后面执行即可，不用插回中间。文件名前缀的数字只表示执行顺序。

### migrate/ 里的脚本（新库一律不要执行）

这几个是从「老库」升级到当前结构用的增量脚本，内容已经全部并进上面的建表文件了，只有在**手上已有一份带数据的老库、想原地升级**时才需要：

| 文件 | 作用 | 注意 |
| --- | --- | --- |
| `29_employee_detail_schedule.sql` | 给 `employee_detail` 加 `schedule` 排班列 | 现在 `07_employee_detail.sql` 已含该列，新库跑会因为列重复报错 |
| `30_orders_assign_refund.sql` | 给 `orders` 加接单/指派/退款相关列与索引，回填历史订单 | 现在 `14_orders.sql` 已含这些列 |
| `31_user_voucher.sql` | 建 `tb_user_voucher` 表 + 给 `orders` 加用券快照列 | 现在 `30_tb_user_voucher.sql` / `14_orders.sql` 已含 |
| `32_user_notify.sql` | 建 `user_notify` 表 | 现在 `31_user_notify.sql` 已含 |
| `33_user_voucher_index.sql` | 给 `tb_user_voucher` 加 `idx_voucher` 索引、改 `stock` 列注释 | 现在 `30_tb_user_voucher.sql` / `23_tb_seckill_voucher.sql` 已含 |
| `34_migrate_seckill_stock_to_quota.sql` | 把存量秒杀券的 `stock` 从「剩余库存」换算成「总发放额度」 | ⚠️ **只能执行一次**，重复执行会把总额度算翻倍 |

现在这些文件要执行时**请手动逐个跑，不要用上面的循环命令整个目录扫**，否则会把新库也一起改一遍。
