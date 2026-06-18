package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

/**
 * Cost function class
 * SumServiceTimes - The sum of all the agents from there start point to the goal point (if the agent keep moving after he reaches his goal, it does not count)
 */

// SST with no tiebreaker:
public class SumServiceTimes implements I_SolutionCostFunction{
    public static final String NAME = "SST";
    public static final SumServiceTimes instance = new SumServiceTimes();

    @Override
    public int solutionCost(Solution solution) {
        return solution.SST();
    }

    @Override
    public String name() {
        return NAME;
    }
}

// // SST with FUEL tiebreaker:
// public class SumServiceTimes implements I_SolutionCostFunction{
//     public static final String NAME = "SST";
//     public static final SumServiceTimes instance = new SumServiceTimes();

//     // SST dominates; fuel only breaks ties
//     private static final int SST_WEIGHT = 1000000; // 1,000,000 is large enough to ensure that differences in SST dominate differences in FUEL

//     @Override
//     public int solutionCost(Solution solution) {
//         int sst = solution.SST();
//         int fuel = solution.FUEL();
//         return sst * SST_WEIGHT + fuel;
//     }

//     @Override
//     public String name() {
//         return NAME;
//     }
// }
