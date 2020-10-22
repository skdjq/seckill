package org.seckill.dao;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.SecKill;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

public interface SecKillDao {
    /**
     * 减库存
     * @param secKillId
     * @param killTime
     * @return如果影响行数大于1，表示更新的记录行数
     */
    int reduceNumber(@Param("secKillId") long secKillId, @Param("killTime") Date killTime);

    /**
     * 根据id查询秒杀对象
     * @param secKillId
     * @return
     */
    SecKill queryById(long secKillId);

    /**
     * 根据偏移量查询秒杀商品列表
     * @param offset
     * @param limit
     * @return
     */
    List<SecKill> queryAll(@Param("offset") int offset, @Param("limit") int limit);
}
