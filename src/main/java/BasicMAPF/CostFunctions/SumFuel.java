package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

/**
 * Cost function class
 * FUEL - Sum of all movements of the agent (not include the stay in place)
 */
// FUEL with no tiebreaker:
public class SumFuel implements I_SolutionCostFunction{
    
    public static final String NAME = "FUEL";
    public static final SumFuel instance = new SumFuel();

    @Override
    public int solutionCost(Solution solution) {
        return solution.FUEL();
    }

    @Override
    public String name() {
        return NAME;
    }
}

// FUEL with SST tiebreaker:
// public class SumFuel implements I_SolutionCostFunction{
    
//     public static final String NAME = "FUEL";
//     public static final SumFuel instance = new SumFuel();

//     // FUEL dominates; sst only breaks ties
//     private static final int FUEL_WEIGHT = 1000000; // 1,000,000 is large enough to ensure that differences in FUEL dominate differences in SST

//     @Override
//     public int solutionCost(Solution solution) {
//         int fuel = solution.FUEL();
//         int sst = solution.SST();
//         return fuel * FUEL_WEIGHT + sst;
//     }

//     @Override
//     public String name() {
//         return NAME;
//     }
// }