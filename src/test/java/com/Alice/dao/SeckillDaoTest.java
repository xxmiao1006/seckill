package com.Alice.dao;

import com.Alice.entity.Seckill;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest{


    @Autowired
    private SeckillDao secKillDao;

    @Test
    public void reduceNumber() {
        long seckillId=1000;
        Date date = new Date();
        int updateCount = secKillDao.reduceNumber(seckillId, date);
        System.out.println(updateCount);

    }

    @Test
    public void queryById() {
        long seckillId=1000;
        Seckill seckill = secKillDao.queryById(seckillId);
        System.out.println(seckill.getName());
        System.out.println(seckill);

    }

    @Test
    public void queryAll() {
        List<Seckill> seckills = secKillDao.queryAll(0, 100);
        for (Seckill seckill : seckills) {
            System.out.println(seckill);
        }

    }
}