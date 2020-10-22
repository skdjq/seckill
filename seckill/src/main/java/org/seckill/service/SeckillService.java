package org.seckill.service;

import org.seckill.dto.Exposer;
import org.seckill.dto.SecKillExecution;
import org.seckill.entity.SecKill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SecKillCloseException;
import org.seckill.exception.SecKillException;

import java.util.List;

public interface SeckillService {

    /**
     * 查询所有的秒杀记录
     * @return
     */
    List<SecKill> getSecKillList();

    /**
     * 查询单个秒杀记录
     * @param secKillId
     * @return
     */
    SecKill getById(long secKillId);

    /**
     * 秒杀开启时输出秒杀接口地址
     * 否则输出系统时间和秒杀时间
     * 防止用户猜测出秒杀地址的规律
     * @param secKillId
     */
    Exposer exportSecKillUrl(long secKillId);

    /**
     *执行秒杀操作
     * @param secKillId
     * @param userPhone
     * @param md5
     */
    SecKillExecution executeSecKill(long secKillId, long userPhone, String md5) throws SecKillException, RepeatKillException, SecKillCloseException;

}
