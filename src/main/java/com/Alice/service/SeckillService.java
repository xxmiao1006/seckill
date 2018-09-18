package com.Alice.service;

import com.Alice.dto.Exposer;
import com.Alice.dto.SeckillExecution;
import com.Alice.entity.Seckill;
import com.Alice.exception.RepeatKillException;
import com.Alice.exception.SeckillCloseException;
import com.Alice.exception.SeckillException;

import java.util.List;

public interface SeckillService {

    /**
     * 查询全部的秒杀商品
     * @return
     */
    List<Seckill> getSeckillList();

    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
    Seckill getById(long seckillId);

    /**
     * 在秒杀开始时输出秒杀接口的地址，否则输出系统时间和秒杀时间
     * @param seckillId
     * @return
     */
    Exposer exportSeckillUrl(long seckillId);

    /**
     * 执行秒杀操作，有可能失败，也有可能成功，所以要抛出需要的异常
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    SeckillExecution executeSeckill(long seckillId,long userPhone,String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException;

}
