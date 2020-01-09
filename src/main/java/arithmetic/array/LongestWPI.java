package arithmetic.array;

/**
 * @Description:  表现良好的最大时间段 //todo 前缀和+单调栈解法
 * 9960669-->996  输出长度：3
 * @Author: nianjie.chen
 * @Date: 1/6/2020
 */
public class LongestWPI {

    public int longestWPI(int[] hours) {
        //遍历一遍数组将数据变成1或-1
        for (int i = 0; i < hours.length; i++) {
            if (hours[i] > 8){
                hours[i] = 1;
            }else {
                hours[i] = -1;
            }
        }

        int sum, dis, res = 0;
        for (int i = 0; i < hours.length; i++) {
            sum = 0;
            dis = 0;
            for (int j = i; j < hours.length; j++) {
                sum+=hours[j];
                if (sum > 0){
                    dis = j - i + 1;
                    res = Math.max(dis, res);
                }
            }
        }
        return res;
    }

    public static void main(String[] args) {
        int i = new LongestWPI().longestWPI(new int[]{9, 9, 6, 0, 6, 6, 9});
        System.out.println(i);
    }
}
