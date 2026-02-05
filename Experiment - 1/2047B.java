import java.util.*;
public class 2047B {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int t = sc.nextInt();

        while (t-- > 0) {
            sc.nextInt();
            char[] s = sc.next().toCharArray();

            int[] f = new int[26];
            for (char c : s) f[c - 'a']++;

            int max = 0, min = Integer.MAX_VALUE;
            char mx = 'a', mn = 'a';

            for (int i = 0; i < 26; i++) {
                if (f[i] >= max) {
                    max = f[i];
                    mx = (char) (i + 'a');
                }
                if (f[i] > 0 && f[i] < min) {
                    min = f[i];
                    mn = (char) (i + 'a');
                }
            }

            for (int i = 0; i < s.length; i++) {
                if (s[i] == mn) {
                    s[i] = mx;
                    break;
                }
            }

            System.out.println(s);
        }
    }
}