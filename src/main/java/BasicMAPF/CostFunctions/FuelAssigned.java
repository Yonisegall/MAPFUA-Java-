package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;

/**
 * Cost
 * Fuel_Assigned - Sum of all movements of the assigned agents (not include the stay in place)
 */ 

// Fuel_Assigned without tiebreaker:
public class FuelAssigned implements I_SolutionCostFunction{
    
    public static final String NAME = "Fuel_Assigned";
    public static final FuelAssigned instance = new FuelAssigned();

    @Override
    public int solutionCost(Solution solution) {
        return solution.Fuel_Assigned();
    }

    @Override
    public String name() {
        return NAME;
    }
}

// Fuel_Assigned with SST tiebreaker:
// public class FuelAssigned implements I_SolutionCostFunction{
    
//     public static final String NAME = "Fuel_Assigned";
//     public static final FuelAssigned instance = new FuelAssigned();

//     private static final int SST_TIEBREAK_WEIGHT = 1000000; // FUEL dominates; sst only breaks ties

//     @Override
//     public int solutionCost(Solution solution) {
//         int service = solution.Fuel_Assigned();
//         int sst = solution.SST();
//         return service * SST_TIEBREAK_WEIGHT + sst;
//     }

//     @Override
//     public String name() {
//         return NAME;
//     }
// }