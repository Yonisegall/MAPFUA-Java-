// package BasicMAPF.CostFunctions;

// import BasicMAPF.DataTypesAndStructures.Solution;

// // NUA with FUEL tiebreaker 1 and SST tiebreaker 2 (NUA dominates, then FUEL, then SST):
// public class SumNUA implements I_SolutionCostFunction {

//     public static final String NAME = "NUA";
//     public static final SumNUA instance = new SumNUA();

//     // NUA dominates, then FUEL, then SST
//     private static final int SST_WEIGHT = 1;
//     private static final int FUEL_WEIGHT = 1000;   // 1,000 is large enough to ensure that differences in FUEL dominate differences in SST  
//     private static final int NUA_WEIGHT = 1000000;   // 1,000,000 is large enough to ensure that differences in NUA dominate differences in FUEL and SST
    

//     @Override
//     public int solutionCost(Solution solution) {
//         int nua = solution.NUA();
//         int fuel = solution.FUEL();
//         int sst = solution.SST(); 

//         // NUA first, then FUEL, then SST
//         System.out.println("NUA: " + nua + ", FUEL: " + fuel + ", SST: " + sst);
//         return (nua * NUA_WEIGHT) + (fuel * FUEL_WEIGHT) + (sst * SST_WEIGHT);
//     }

//     @Override
//     public String name() {
//         return NAME;
//     }
// }

package BasicMAPF.CostFunctions;

import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_GridMap;

// NUA with FUEL tiebreaker 1 and SST tiebreaker 2 (NUA dominates, then FUEL, then SST):
public class SumNUA implements I_SolutionCostFunction {

    public static final String NAME = "NUA";
    public static final SumNUA instance = new SumNUA();

    // NUA dominates, then FUEL, then SST
    private int freeCells;
    private int nuaAgents;
    private static final int SST_WEIGHT = 1;
    private static final int FUEL_WEIGHT = 1000;   // 1,000 is large enough to ensure that differences in FUEL dominate differences in SST

    public void configureFromInstance(MAPF_Instance instance) {
        if (instance == null || instance.map == null) return;
        this.freeCells = (instance.map instanceof I_ExplicitMap) ? ((I_ExplicitMap) instance.map).getNumMapLocations()
                : (instance.map instanceof I_GridMap ? ((I_GridMap) instance.map).getWidth() * ((I_GridMap) instance.map).getHeight() : 0);

        int numUA = 0;
        for (Agent a : instance.agents) if (a.source.equals(a.target)) numUA++;
        this.nuaAgents = numUA;
    }

    @Override
    public int solutionCost(Solution solution) {
        int nua = solution.NUA();
        int fuel = solution.Fuel_Assigned();
        int sst = solution.SST();

        // NUA first, then FUEL, then SST
        System.out.println("NUA: " + nua + ", FUEL: " + fuel + ", SST: " + sst + ", freeCells: " + freeCells + ", nuaAgents: " + nuaAgents);
        return (nua * ((freeCells - nuaAgents) * FUEL_WEIGHT)) + (fuel * FUEL_WEIGHT) + (sst * SST_WEIGHT);
    }

    @Override
    public String name() {
        return NAME;
    }

}
