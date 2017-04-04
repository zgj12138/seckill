package edu.gduf.seckill.exception;

/** 秒杀业务异常
 * Created by zhang on 2017/4/3.
 */
public class SeckillException extends RuntimeException {
    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
