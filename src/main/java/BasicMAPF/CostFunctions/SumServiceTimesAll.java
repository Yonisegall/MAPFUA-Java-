package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

/**
 * SST - Sum of times for UA agents until they have no conflicts.
 */
// SST_All with no tiebreaker:
public class SumServiceTimesAll implements I_SolutionCostFunction{
    
    public static final String NAME = "SST_All";
    public static final SumServiceTimesAll instance = new SumServiceTimesAll();

    @Override
    public int solutionCost(Solution solution) {
        return solution.SST_All();
    }

    @Override
    public String name() {
        return NAME;
    }
}
// // SST_All with Fuel tiebreaker:
// public class SumServiceTimesAll implements I_SolutionCostFunction{
    
//     public static final String NAME = "SST_All";
//     public static final SumServiceTimesAll instance = new SumServiceTimesAll();

//     private static final int FUEL_TIEBREAK_WEIGHT = 1000000; // SST dominates; fuel only breaks ties

//     @Override
//     public int solutionCost(Solution solution) {
//         int service = solution.SST_All();
//         int fuel = solution.FUEL();
//         return service * FUEL_TIEBREAK_WEIGHT + fuel;
//     }

//     @Override
//     public String name() {
//         return NAME;
//     }
// }