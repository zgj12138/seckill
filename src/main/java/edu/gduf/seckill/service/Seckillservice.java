package edu.gduf.seckill.service;

import edu.gduf.seckill.dto.Exposer;
import edu.gduf.seckill.dto.SeckillExecution;
import edu.gduf.seckill.entity.Seckill;
import edu.gduf.seckill.exception.RepeatKillException;
import edu.gduf.seckill.exception.SeckillCloseException;
import edu.gduf.seckill.exception.SeckillException;

import java.util.List;

/** 秒杀业务服务层接口，站在“使用者”的角度去设计接口
 * 三个方面：方法定义的粒度，参数，返回类型（return 类型/异常）
 * Created by zhang on 2017/4/3.
 */
public interface Seckillservice {
    /**
     * 查询所有秒杀记录
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 秒杀开启时输出秒杀接口地址
     * 否则输出系统时间和秒杀时间
     * @param seckillId
     * @return 秒杀地址
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
     SeckillExecution executeSkill(long seckillId, long userPhone, String md5)
     throws SeckillException, SeckillCloseException, RepeatKillException;
}
