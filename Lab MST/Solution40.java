import java.util.*;
class Solution40 {
    List<List<Integer>> res = new ArrayList<>();
    void solve(int[] nums, int target, int start, List<Integer> list) {

        if (target == 0) {
            res.add(new ArrayList<>(list));
            return;
        }

        for (int i = start; i < nums.length; i++) {
            if (i > start && nums[i] == nums[i - 1]) continue;
            if (nums[i] > target) break;

            list.add(nums[i]);
            solve(nums, target - nums[i], i + 1, list);
            list.remove(list.size() - 1);
        }
    }

    public List<List<Integer>> combinationSum2(int[] candidates, int target) {
        Arrays.sort(candidates);
        solve(candidates, target, 0, new ArrayList<>());
        return res;
    }
}