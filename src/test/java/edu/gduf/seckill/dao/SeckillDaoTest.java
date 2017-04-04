package edu.gduf.seckill.dao;

import edu.gduf.seckill.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

/**配置spring和junit整合，junit启动时加载Spriing IOC容器
 * Created by zhang on 2017/4/3.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-dao.xml")
public class  SeckillDaoTest {

    @Resource
    private SeckillDao seckillDao;


    @Test
    public void reduceNumber() throws Exception {
        Date date = new Date();
        int updateCount = seckillDao.reduceNumber(1000,date);
        System.out.println(updateCount);
    }

    @Test
    public void queryById() throws Exception {
        long id = 1000L;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill);
    }

    @Test
    public void queryAll() throws Exception {
        List<Seckill> seckills = seckillDao.queryAll(0,100);
        for(Seckill seckill : seckills) {
            System.out.println(seckill);
        }
    }

}