-- 将所有非管理员用户的余额更新为0
UPDATE user SET balance = 0.00 WHERE id > 1;
