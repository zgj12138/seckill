-- 执行秒杀存储过程
DELIMITER $$ -- console ;转成 $$
-- 定义存储过程
-- 参数：in 输入参数；out: 输出参数
-- row_count():返回上一条修改类型sql影响的行数
-- row_count():0未修改数据 >0 表示修改的行数 <0 sql错误
CREATE PROCEDURE `seckill`.`exeute_seckill`
  (IN v_seckillId INT, IN v_phone BIGINT, IN v_kill_time TIMESTAMP,
    OUT  r_result INT)
  BEGIN
    DECLARE insert_count INT DEFAULT 0;
    START TRANSACTION;
    INSERT IGNORE INTO success_killed
    (seckill_id,user_phone,create_time) VALUES
      (v_seckillId,v_phone,v_kill_time);
    SELECT row_count() INTO insert_count;
    IF (insert_count = 0) THEN
      ROLLBACK ;
      set r_result = -1;
      ELSEIF (insert_count < 0) THEN
      ROLLBACK ;
      SET r_result = -2;
      ELSE
        UPDATE seckill
          SET number = number - 1
        WHERE seckill_id = v_seckillId
        AND end_time > v_kill_time
        and start_time < v_kill_time
        and number > 0;
      SELECT row_count() INTO insert_count;
      IF (insert_count = 0) THEN
        ROLLBACK ;
        SET r_result = 0;
      ELSEIF (insert_count < 0) THEN
        ROLLBACK ;
        SET r_result = -2;
      ELSE
        COMMIT ;
        SET r_result = 1;
      END IF;
    END IF;
  END;
$$
-- 存储过程结束
DELIMITER ;

SET @r_result = -3;
-- 执行存储过程
CALL execute_seckill(1000,18826079018,now(),@r_result)