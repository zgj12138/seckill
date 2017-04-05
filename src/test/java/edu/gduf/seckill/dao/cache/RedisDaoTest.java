package edu.gduf.seckill.dao.cache;

import edu.gduf.seckill.dao.SeckillDao;
import edu.gduf.seckill.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

/**
 * Created by zhang on 2017/4/5.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-dao.xml")
public class RedisDaoTest {
    @Autowired
    private RedisDao redisDao;
    @Autowired
    private SeckillDao seckillDao;
    private long seckillId = 1000L;
    @Test
    public void testSeckill() throws Exception {
        //尝试从redis中获取seckill对象
        Seckill seckill = redisDao.getSeckill(seckillId);
        //如果缓存中不存在，从数据库中获取
        if(seckill == null) {
            seckill = seckillDao.queryById(seckillId);
            if(seckill != null) {
                //将seckill对象放入redis中
                String result = redisDao.putSeckill(seckill);
                System.out.println(result);
                seckill = redisDao.getSeckill(seckillId);
                System.out.println(seckill);
            }
        }
    }


}