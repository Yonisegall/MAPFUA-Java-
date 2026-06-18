package BasicMAPF.Solvers.CBS;

import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import Environment.Metrics.InstanceReport;
import org.junit.jupiter.api.Test;

import java.util.List;

import static BasicMAPF.TestConstants.Coordinates.coor00;
import static BasicMAPF.TestConstants.Coordinates.coor10;
import static BasicMAPF.TestConstants.Coordinates.coor20;
import static BasicMAPF.TestConstants.Coordinates.coor55;
import static BasicMAPF.TestConstants.Maps.mapEmpty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CBSUAVariantsTest {

    @Test
    void allUaCbsVariantsAreIndependentlyNamedAndSolve() {
        List<CBS_Solver> solvers = List.of(
                CanonicalSolversFactory.createCBS_UA_NoHeuristic_SST_Solver(),
                CanonicalSolversFactory.createCBS_UA_NoHeuristic_Fuel_Solver(),
                CanonicalSolversFactory.createCBS_UA_NoHeuristic_NUA_Solver(),
                CanonicalSolversFactory.createCBS_UA_Bypass_SST_Solver(),
                CanonicalSolversFactory.createCBS_UA_Bypass_Fuel_Solver(),
                CanonicalSolversFactory.createCBS_UA_Bypass_NUA_Solver(),
                CanonicalSolversFactory.createCBS_UA_Heuristic_SST_Solver(),
                CanonicalSolversFactory.createCBS_UA_Heuristic_Fuel_Solver(),
                CanonicalSolversFactory.createCBS_UA_Heuristic_NUA_Solver(),
                CanonicalSolversFactory.createCBS_UA_BypassHeuristic_SST_Solver(),
                CanonicalSolversFactory.createCBS_UA_BypassHeuristic_Fuel_Solver(),
                CanonicalSolversFactory.createCBS_UA_BypassHeuristic_NUA_Solver()
        );

        assertEquals(12, solvers.stream().map(CBS_Solver::getName).distinct().count());

        Agent assigned = new Agent(100, coor00, coor10, false);
        Agent unassigned = new Agent(101, coor55, coor55, true);
        MAPF_Instance instance = new MAPF_Instance(
                "cbs-ua-variants-smoke", mapEmpty, new Agent[]{assigned, unassigned});

        for (CBS_Solver solver : solvers) {
            Solution solution = solver.solve(
                    instance, new RunParametersBuilder().setTimeout(5_000).createRP());
            assertNotNull(solution, solver.getName());
            assertTrue(solution.isValidSolution(), solver.getName());
            assertTrue(solution.solves(instance), solver.getName());
        }
    }

    @Test
    void bypassRepairsUaConflictInsideTheSameHighLevelNode() {
        Agent assigned = new Agent(200, coor00, coor20, false);
        Agent unassigned = new Agent(201, coor10, coor10, true);
        MAPF_Instance instance = new MAPF_Instance(
                "cbs-ua-bypass", mapEmpty, new Agent[]{assigned, unassigned});
        CBS_Solver solver = CanonicalSolversFactory.createCBS_UA_Bypass_SST_Solver();
        InstanceReport report = new InstanceReport();

        Solution solution = solver.solve(instance, new RunParametersBuilder()
                .setTimeout(5_000)
                .setInstanceReport(report)
                .createRP());

        assertNotNull(solution);
        assertTrue(solution.isValidSolution());
        assertTrue(report.getIntegerValue("UA Local Repair Attempts") > 0);
        assertTrue(report.getIntegerValue("UA Local Repair Successes") > 0);
    }

    @Test
    void uaFutureConflictHeuristicAvoidsKnownAssignedRouteAtRoot() {
        Agent assigned = new Agent(300, coor00, coor20, false);
        Agent unassigned = new Agent(301, coor10, coor10, true);
        MAPF_Instance instance = new MAPF_Instance(
                "cbs-ua-lookahead", mapEmpty, new Agent[]{assigned, unassigned});
        InstanceReport report = new InstanceReport();

        Solution solution = CanonicalSolversFactory.createCBS_UA_Heuristic_SST_Solver().solve(
                instance, new RunParametersBuilder()
                        .setTimeout(5_000)
                        .setInstanceReport(report)
                        .createRP());

        assertNotNull(solution);
        assertTrue(solution.isValidSolution());
        assertEquals(0, report.getIntegerValue(InstanceReport.StandardFields.expandedNodes));
    }
}
