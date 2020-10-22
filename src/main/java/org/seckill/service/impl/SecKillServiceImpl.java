package org.seckill.service.impl;

import org.seckill.dao.SecKillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SecKillExecution;
import org.seckill.entity.SecKill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SecKillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SecKillCloseException;
import org.seckill.exception.SecKillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

@Service
public class SecKillServiceImpl implements SeckillService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SecKillDao secKillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    //MD5盐值字符串，用于混淆MD5
    private final String salt = "!@(*^#(*@!DGHASIUDGIasjdhash}{P:|HJIGVIY";

    @Override
    public List<SecKill> getSecKillList() {
        return secKillDao.queryAll(0,4);
    }

    @Override
    public SecKill getById(long secKillId) {
        return secKillDao.queryById(secKillId);
    }

    @Override
    public Exposer exportSecKillUrl(long secKillId) {
        // 优化点：缓存优化
//        SecKill secKill = secKillDao.queryById(secKillId);
        SecKill secKill = redisDao.getSeckill(secKillId);
        // 如果缓存为空
        if(secKill == null){
            // 访问数据库
            secKill = secKillDao.queryById(secKillId);
            if(secKill == null){
                // 数据库中也没有
                return new Exposer(false, secKillId);
            } else{
                // 数据库中有，加入缓存
                redisDao.putSeckill(secKill);
            }
        }
        Date start_time = secKill.getStartTime();
        Date end_time = secKill.getEndTime();
        Date now_time = new Date();
        if(now_time.getTime() < start_time.getTime() || now_time.getTime() > end_time.getTime()){
            return new Exposer(false, secKillId, now_time.getTime(), start_time.getTime(), end_time.getTime());
        }
        //转化特定字符串
        String md5 = getMD5(secKillId);
        return new Exposer(true, md5, secKillId);
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点:
     * - 开发团队达到一致约定，明确标注事务方法的编程风格
     * - 保证事务方法的执行时间尽可能短，不要穿插其他网络操作RPC/HTTP请求，或者剥离到事务方法外部
     * - 不是所有的方法都需要事务，如只有一条修改操作，只读操作就不需要事务控制
     * */
    public SecKillExecution executeSecKill(long secKillId, long userPhone, String md5) throws SecKillException, RepeatKillException, SecKillCloseException {
        if(md5 == null || !md5.equals(getMD5(secKillId))){
            throw new SecKillException("seckill data rewrite");
        }
        //执行秒杀逻辑：减库存 + 记录购买行为
        Date now_time = new Date();
        try {
            //记录购买行为
            int i = successKilledDao.inertSuccessKilled(secKillId, userPhone);
            if(i <= 0){
                throw new RepeatKillException("seckill repeated");
            }else{
                //减库存
                int updateCount = secKillDao.reduceNumber(secKillId, now_time);
                if(updateCount <= 0){
                    throw new SecKillCloseException("seckill closed");
                }else{
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSecKill(secKillId, userPhone);
                    return new SecKillExecution(secKillId, SecKillStatEnum.SUCCESS, successKilled);
                }
            }

        }catch(SecKillCloseException e1){
            throw e1;
        }catch(RepeatKillException e2){
            throw e2;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            //所有编译期异常，转化为运行期异常
            throw new SecKillException("seckill inner error:" + e.getMessage());
        }
    }

    private String getMD5(long secKillId){
        String base = secKillId + "/" + salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
