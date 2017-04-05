package edu.gduf.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import edu.gduf.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * redis dao层
 * Created by zhang on 2017/4/5.
 */
public class RedisDao {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    //jedis连接池
    private JedisPool jedisPool = null;
    //自定义序列化schema
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }

    /**
     * 从redis中获取seckill对象
     * @param seckillId
     * @return
     */
    public Seckill getSeckill(long seckillId) {
        try {
            //获取jedis连接对象
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckillId;
                //redis并没有实现内部序列化操作,采用自定义序列化
                byte[] bytes = jedis.get(key.getBytes());
                if(bytes != null) {
                    //创建一个空对象
                    Seckill seckill = schema.newMessage();
                    //seckill被反序列化
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    return seckill;
                }
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    public String putSeckill(Seckill seckill) {
        try {
            //获取jedis连接对象
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckill.getSeckillId();
                //创建seckill的序列化字节数组
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //设置缓存超时1小时
                int timeout = 60 * 60;
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            } finally {
                jedis.close();
            }
        }  catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return  null;
    }
}
