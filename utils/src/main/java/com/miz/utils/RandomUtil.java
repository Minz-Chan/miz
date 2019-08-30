package com.miz.utils;

import java.util.ArrayList;
import java.util.List;


public class RandomUtil {

    /**
     * 1.总金额不能超过XXX*100 单位是分
     * 2.每个红包都要有钱，最低不能低于1分，最大金额不能超过XXX*100
     */
    private static final int MIN_MONEY = 200;
    private static final int MAX_MONEY = 100000 * 100;
    /**
     * 这里为了避免某一个红包占用大量资金，我们需要设定非最后一个红包的最大金额，我们把他设置为红包金额平均值的N倍；
     */
    private static final double TIMES = 2.1;

    /**
     * 拆分整数（算法与随机红包算法类似）
     * @param value 待分解整数值
     * @param count 分解的整数个数
     * @return 生成的整数列表
     */
    public static List<Integer> splitIntegerByRandom(int value, int count) {
        return splitRedPacketsByRandom(value, count);
    }


    /**
     * 拆分红包(单位为分，按随机方式拆分)
     * @param money 红包总金额
     * @param count 个数
     * @return 生成的小红包金额列表
     */
    private static List<Integer> splitRedPacketsByRandom(int money, int count) {
        // 红包 合法性校验
        if (!isRight(money, count)) {
            return null;
        }
        // 红包列表
        List<Integer> list = new ArrayList<Integer>();
        // 每个红包最大的金额为平均金额的Times 倍
        int max = (int) (money * TIMES / count);

        max = max > MAX_MONEY ? MAX_MONEY : max;
        // 分配红包
        for (int i = 0; i < count; i++) {
            int one = randomRedPacket(money, MIN_MONEY, max, count - i);
            list.add(one);
            money -= one;
        }
        return list;
    }

    /**
     * 随机分配一个红包
     * @param money
     * @param minS  最小金额
     * @param maxS  最大金额(每个红包的默认Times倍最大值)
     * @param count
     * @return
     */
    private static int randomRedPacket(int money, int minS, int maxS, int count) {
        // 若是只有一个，直接返回红包
        if (count == 1) {
            return money;
        }
        // 若是最小金额红包 == 最大金额红包， 直接返回最小金额红包
        if (minS == maxS) {
            return minS;
        }
        // 校验 最大值 max 要是比money 金额高的话？ 去 money 金额
        int max = maxS > money ? money : maxS;
        // 随机一个红包 = 随机一个数* (金额-最小)+最小
        int one = ((int) Math.rint(Math.random() * (max - minS) + minS));
        // 剩下的金额
        int moneyOther = money - one;
        // 校验这种随机方案是否可行，不合法的话，就要重新分配方案
        if (isRight(moneyOther, count - 1)) {
            return one;
        } else {
            // 重新分配
            double avg = moneyOther / (count - 1);
            // 本次红包过大，导致下次的红包过小；如果红包过大，下次就随机一个小值到本次红包金额的一个红包
            if (avg < MIN_MONEY) {
                // 递归调用，修改红包最大金额
                return randomRedPacket(money, minS, one, count);

            } else if (avg > MAX_MONEY) {
                // 递归调用，修改红包最小金额
                return randomRedPacket(money, one, maxS, count);
            }
        }
        return one;
    }

    /**
     * 红包 合法性校验
     * @param money
     * @param count
     * @return
     */
    private static boolean isRight(int money, int count) {
        double avg = money / count;
        // 小于最小金额
        if (avg < MIN_MONEY) {
            return false;
            // 大于最大金额
        } else if (avg > MAX_MONEY) {
            return false;
        }
        return true;
    }

}

