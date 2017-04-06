package edu.gduf.seckill.service.impl;

import edu.gduf.seckill.dao.SeckillDao;
import edu.gduf.seckill.dao.SuccessKilledDao;
import edu.gduf.seckill.dao.cache.RedisDao;
import edu.gduf.seckill.dto.Exposer;
import edu.gduf.seckill.dto.SeckillExecution;
import edu.gduf.seckill.entity.Seckill;
import edu.gduf.seckill.entity.SuccessKilled;
import edu.gduf.seckill.enums.SeckillStateEnum;
import edu.gduf.seckill.exception.RepeatKillException;
import edu.gduf.seckill.exception.SeckillCloseException;
import edu.gduf.seckill.exception.SeckillException;
import edu.gduf.seckill.service.Seckillservice;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhang on 2017/4/3.
 */
@Service
public class SeckillServiceImpl implements Seckillservice {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillDao seckillDao;
    @Autowired
    private SuccessKilledDao successKilledDao;
    @Autowired
    private RedisDao redisDao;

    private final String salt = "dmdf;as^&R@DASdasD@)Dkl";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 10);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //优化点：缓存优化

        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                //把seckill放入redis
                redisDao.putSeckill(seckill);
            }
        }
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();
        if (nowTime.getTime() < startTime.getTime()
                || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId,
                    nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        String md5 = getMD5(seckillId);
        return new Exposer(true, md5, seckillId);

    }

    private String getMD5(long seckillId) {
        String base = seckillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;

    }

    /**
     * 优化执行过程，原来的过程是先执行减库存，再插入购买明细
     * 现在先增加购买明细，插入成功再减少库存
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws SeckillCloseException
     * @throws RepeatKillException
     */
    @Transactional
    @Override
    public SeckillExecution executeSkill(long seckillId, long userPhone, String md5)
            throws SeckillException, SeckillCloseException, RepeatKillException {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new RepeatKillException("seckill data rewrite");
        }
        //执行秒杀逻辑:减库存+记录购买行为，优化，两个行为
        Date nowTime = new Date();
        try {
            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insertCount <= 0) {
                //重复秒杀，唯一主键：seckillId,userPhone
                throw new RepeatKillException("seckill repeated");
            } else {
                //秒杀成功
                //减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //没有更新到记录，秒杀结束,rollback
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    //成功，commit
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        } catch (RepeatKillException e1) {
            throw e1;
        } catch (SeckillCloseException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //所有编译器异常全部转换成运行期异常
            throw new SeckillException("seckill inner error:" + e.getMessage());
        }
    }

    @Override
    public SeckillExecution executeSekillProcedure(long seckillId, long userPhone, String md5) throws SeckillException, SeckillCloseException, RepeatKillException {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            throw new RepeatKillException("seckill data rewrite");
        }
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        try{
            seckillDao.killByProcedure(map);
            int result = MapUtils.getInteger(map, "result");
            if(result == 1) {
                SuccessKilled successKilled = successKilledDao
                        .queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
            } else {
                return new SeckillExecution(seckillId, SeckillStateEnum.StateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId,SeckillStateEnum.INNER_ERROR);
        }
    }
}
