package edu.gduf.seckill.exception;

/**秒杀关闭异常
 * Created by zhang on 2017/4/3.
 */
public class SeckillCloseException extends SeckillException {

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
