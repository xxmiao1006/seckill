package com.Alice.service.Impl;

import com.Alice.dao.SeckillDao;
import com.Alice.dao.SuccessKilledDao;
import com.Alice.dto.Exposer;
import com.Alice.dto.SeckillExecution;
import com.Alice.entity.Seckill;
import com.Alice.entity.SuccessKilled;
import com.Alice.enums.SeckillStatEnum;
import com.Alice.exception.RepeatKillException;
import com.Alice.exception.SeckillCloseException;
import com.Alice.exception.SeckillException;
import com.Alice.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;

@Service
public class SeckillServiceImpl implements SeckillService {

    //日记对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    //盐值，调味料  避免用户猜出md5的值 随便给
    private final String salt="shsdssljdd'l.";

    //注入dao层
    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;




    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0,4);
    }

    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }


    /**
     * 输出秒杀接口的地址
     * @param seckillId
     * @return
     */
    public Exposer exportSeckillUrl(long seckillId) {
        Seckill seckill = seckillDao.queryById(seckillId);
        if (seckill == null) {
            //找不到这个秒杀产品的记录
            return new Exposer(false,seckillId);
        }

        //若是秒杀未开启
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();

        //系统当前时间
        Date nowTime = new Date();
        if(startTime.getTime()>nowTime.getTime() || endTime.getTime()<nowTime.getTime()){
            //不再秒杀时间内
            return new Exposer(false,seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
        }

        //秒杀开启
        String md5 = getMD5(seckillId);

        return new Exposer(true,md5,seckillId);
    }

    private String getMD5(long seckillId){
        String base = seckillId+"/"+salt;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }

    /**
     * 判断秒杀是否成功
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    @Transactional
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
            throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null && !md5.equals(getMD5(seckillId))) {
            //秒杀数据被重写了
            throw new SeckillException("seckill data rewrite");
        }

        //执行秒杀逻辑：减库存+增加购买明细
        Date nowTime = new Date();

        //减库存
        try {
            int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
            if(updateCount<=0){
                //没有库存更新记录，说明秒杀结束
                throw new SeckillCloseException("seckill is closed");
            }else {
                //更新了库存，秒杀成功，增加明细
                int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
                if(insertCount<=0){
                    //看是否该明细被重复插入，即有用户重复秒杀
                    throw new SeckillCloseException("seckill repeated");
                }else {
                    //秒杀成功，得到成功插入的明细记录，并返回秒杀成功的细腻
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId,SeckillStatEnum.SUCCESS,successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2){
            throw e2;
        }catch (Exception e){
            logger.error(e.getMessage(),e);
            throw new SeckillException("seckill inner error:"+e.getMessage());
        }
    }
}
