package arithmetic.dynamic_plan.fbonacci;

/**
 * 用2*1的小矩形横着或者竖着去覆盖更大的矩形。请问用n个2*1的小矩形无重叠地覆盖一个2*n的大矩形，总共有多少种方法
 *
 * 思路：
 * 链接：https://www.nowcoder.com/questionTerminal/72a5a919508a4251859fb2cfb987a0e6
 * 来源：牛客网
 *
 * 2*n的大矩形，和n个2*1的小矩形
 * 其中target*2为大矩阵的大小
 * 有以下几种情形：
 * 1⃣️target <= 0 大矩形为<= 2*0,直接return 1；
 * 2⃣️target = 1大矩形为2*1，只有一种摆放方法，return1；
 * 3⃣️target = 2 大矩形为2*2，有两种摆放方法，return2；
 * 4⃣️target = n 分为两步考虑：
 *         第一次摆放一块 2*1 的小矩阵，则摆放方法总共为f(target - 1)
 *         第一次摆放2块 1*2 的小矩阵，则摆放方法总共为f(target - 2)
 */
public class RectCover {

    public int receCover(int target) {
        if (target < 0) {
            return 0;
        }else if (target <= 2) {
            return target;
        } else {
            return receCover(target - 1) + receCover(target - 2);
        }
    }
}
