package cn.running.demo.test;

/**
 * @Author DevRunning
 * @Date 2023/12/16 9:01
 */
public class Solution {

     public String intToRoman(int num) {

        Integer[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] reps = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) {
                num -= values[i];
                str.append(reps[i]);
            }
        }
        return str.toString();
    }

    public int beautySum(String s) {
        int value = 0;
        int value2 = 0;

        // 初始化美丽字符串
        String[] str = new String[]{"aab", "aabc", "aabcb", "abcb", "bcb"};

        String[] strSplit = s.split("");

        int j = strSplit.length - 1;

        for (int i = 0; i < str.length; i++) {
            // 判断参数字符串是否包含美丽字符串
            if (s.contains(str[i]) || str[i].contains(s)) {
                value += str[i].length();
                // 判断美丽
                if (!str[i].endsWith(strSplit[j])) {
                    value2 += 1;
                    --j;
                }
            }
        }
        return value - value2;
    }

    public int divide() {
        // 被除数
        Number dividend = 10;

        // 除数
        Number divisor = 3;

        // 得到商
        Number result = dividend.doubleValue() / divisor.doubleValue();

        // 截取
        String[] split = String.valueOf(result).split(".");
        return Integer.valueOf(split[0]);
    }

    public static void main(String[] args) {
        Solution s = new Solution();
        System.out.print(s.intToRoman(1994));
    }


}
