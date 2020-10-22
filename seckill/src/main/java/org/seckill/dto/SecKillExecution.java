package org.seckill.dto;

import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SecKillStatEnum;

/**
 * 秒杀执行后结果dto
 * */
public class SecKillExecution {

    private long secKillId;

    /**
     * 秒杀执行结果状态
     */
    private int state;

    /**
     * 状态表示
     */
    private String stateInfo;

    private SuccessKilled successKilled;

    public SecKillExecution(long secKillId, SecKillStatEnum secKillStatEnum, SuccessKilled successKilled) {
        this.secKillId = secKillId;
        this.state = secKillStatEnum.getState();
        this.stateInfo = secKillStatEnum.getStateInfo();
        this.successKilled = successKilled;
    }

    public SecKillExecution(long secKillId, SecKillStatEnum secKillStatEnum) {
        this.secKillId = secKillId;
        this.state = secKillStatEnum.getState();
        this.stateInfo = secKillStatEnum.getStateInfo();
    }

    public long getSecKillId() {
        return secKillId;
    }

    public void setSecKillId(long secKillId) {
        this.secKillId = secKillId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public SuccessKilled getSuccessKilled() {
        return successKilled;
    }

    public void setSuccessKilled(SuccessKilled successKilled) {
        this.successKilled = successKilled;
    }
}
