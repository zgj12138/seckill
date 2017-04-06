package edu.gduf.seckill.service.impl;

import edu.gduf.seckill.dto.Exposer;
import edu.gduf.seckill.dto.SeckillExecution;
import edu.gduf.seckill.entity.Seckill;
import edu.gduf.seckill.entity.SuccessKilled;
import edu.gduf.seckill.exception.RepeatKillException;
import edu.gduf.seckill.exception.SeckillCloseException;
import edu.gduf.seckill.service.Seckillservice;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by zhang on 2017/4/3.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-service.xml","classpath:spring/spring-dao.xml"})
public class SeckillServiceImplTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private Seckillservice seckillservice;
    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> list = seckillservice.getSeckillList();
        logger.info("list={}", list);
    }

    @Test
    public void getById() throws Exception {
        long id = 1000L;
        Seckill seckill = seckillservice.getById(id);
        logger.info("seckill={}", seckill);
    }

    /**
     * 执行秒杀逻辑
     * @throws Exception
     */
    @Test
    public void testSeckillLogic() throws Exception {
        long id = 1000L;
        Exposer exposer = seckillservice.exportSeckillUrl(id);
        if(exposer.isExposed()) {
            logger.info("exposer={}", exposer);
            long userPhone = 18826089013L;
            try {
                SeckillExecution seckillExecution = seckillservice.executeSkill(id, userPhone,exposer.getMd5());
                logger.info("seckillExecution={}", seckillExecution);
            } catch (SeckillCloseException e1) {
                logger.error(e1.getMessage());
            } catch (RepeatKillException e2) {
                logger.error(e2.getMessage());
            }
        }
        //秒杀未开启
        logger.warn("exposer={}", exposer);

    }
    /*
    exposer=Exposer{exposed=true, md5='77431bc1e10a7b1d3467f0d5e2d3e78a', seckillId=1001, now=0, strat=0, end=0}
     */

    @Test
    public void executeSkill() throws Exception {
        long id = 1001L;
        long userPhone = 18826089011L;
        String md5 = "77431bc1e10a7b1d3467f0d5e2d3e78a";
        try {
            SeckillExecution seckillExecution = seckillservice.executeSkill(id, userPhone,md5);
            logger.info("seckillExecution={}", seckillExecution);
        } catch (SeckillCloseException e1) {
            logger.error(e1.getMessage());
        } catch (RepeatKillException e2) {
            logger.error(e2.getMessage());
        }
    }

    @Test
    public void seckillProcedure() {
        long id = 1002L;
        long userPhone = 18826089011L;
        Exposer exposer = seckillservice.exportSeckillUrl(id);
        if(exposer.isExposed()) {
            String md5 = exposer.getMd5();
            SeckillExecution seckillExecution = seckillservice.executeSekillProcedure(id, userPhone, md5);
            logger.info(seckillExecution.getStateInfo());
        }
    }
}