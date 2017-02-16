package org.seckill.watchway;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

/**
 * 使用redis的watch实现秒杀功能
 *
 * Created by lfwang on 2017/2/16.
 */
public class SecKillRunnable implements Runnable {

    Jedis jedis = new Jedis();

    @Override
    public void run() {
        try {
            String userId = UUID.randomUUID().toString();

            jedis.watch("stock");
            int stock  = Integer.valueOf(jedis.get("stock"));

            if (stock  > 0) {
                Transaction tx = jedis.multi(); // 开启事物

                int nowStock = stock - 1;

                tx.set("stock", String.valueOf(nowStock));

                List list = tx.exec(); // 提交事务，如果此时watchKeys被改动了，则返回null

                if (null != list && !list.isEmpty()) {
                    System.out.println("用户: " + userId + " 抢购成功，当前库存: " + nowStock);
                    jedis.sadd("successUsers", userId);
                } else {
                    System.out.println("用户: " + userId + " 抢购失败");
                    jedis.sadd("failureUsers", userId);
                }
            } else {
                System.out.println("用户: " + userId + " 抢购失败，没库存了!");
                jedis.sadd("failureUsers", userId);
            }
        } finally {
            jedis.close();
        }
    }
}
