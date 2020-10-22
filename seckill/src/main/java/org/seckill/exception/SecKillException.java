package org.seckill.exception;

/**
 * 所有秒杀业务相关异常（其余异常应继承此异常）
 * */
public class SecKillException extends RuntimeException {
    public SecKillException(String message) {
        super(message);
    }

    public SecKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
