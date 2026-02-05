import java.util.*;
public class 2037C {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int t = sc.nextInt();
        while (t-- > 0) {
            int n = sc.nextInt();
            String s = sc.next();

            int[] freq = new int[26];
            int len = s.length();

            for (int i = 0; i < len; i++) {
                freq[s.charAt(i) - 'a']++;
            }

            int mx = 0, mn = Integer.MAX_VALUE;
            int p = 0, q = 0;

            for (int i = 0; i < 26; i++) {
                if (freq[i] >= mx) {
                    mx = freq[i];
                    p = i;
                }
                if (freq[i] < mn && freq[i] != 0) {
                    mn = freq[i];
                    q = i;
                }
            }

            char[] arr = s.toCharArray();
            for (int i = 0; i < len; i++) {
                if (arr[i] == (char) (q + 'a')) {
                    arr[i] = (char) (p + 'a');
                    break;
                }
            }

            System.out.println(new String(arr));
        }
    }
}