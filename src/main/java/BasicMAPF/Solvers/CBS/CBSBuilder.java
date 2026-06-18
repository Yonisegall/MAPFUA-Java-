package BasicMAPF.Solvers.CBS;

import java.util.Comparator;
import java.util.Set;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.DataTypesAndStructures.I_OpenList;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.I_Solver;
import TransientMAPF.TransientMAPFSettings;

public class CBSBuilder {
    private I_Solver lowLevelSolver = null;
    private I_OpenList<CBS_Solver.CBS_Node> openList = null;
    private CBS_Solver.OpenListManagementMode openListManagementMode = null;
    private I_SolutionCostFunction costFunction = null;
    private Comparator<? super CBS_Solver.CBS_Node> cbsNodeComparator = null;
    private Boolean useCorridorReasoning = null;
    private Boolean sharedGoals = null;
    private Boolean sharedSources = null;
    private TransientMAPFSettings transientMAPFSettings = null;
    private boolean staticObstaclesForUnassignedAgents = false;
    private boolean staticUAForUnassignedAgents = false;
    private boolean localRepairForUaConflicts = false;
    private boolean uaFutureConflictHeuristic = false;
    private boolean uaAwareLowLevel = false;
    private Set<Agent> staticUA = null;

    public CBSBuilder setLowLevelSolver(I_Solver lowLevelSolver) {
        this.lowLevelSolver = lowLevelSolver;
        return this;
    }

    public CBSBuilder setOpenList(I_OpenList<CBS_Solver.CBS_Node> openList) {
        this.openList = openList;
        return this;
    }

    public CBSBuilder setOpenListManagementMode(CBS_Solver.OpenListManagementMode openListManagementMode) {
        this.openListManagementMode = openListManagementMode;
        return this;
    }

    public CBSBuilder setCostFunction(I_SolutionCostFunction costFunction) {
        this.costFunction = costFunction;
        return this;
    }

    public CBSBuilder setCbsNodeComparator(Comparator<? super CBS_Solver.CBS_Node> cbsNodeComparator) {
        this.cbsNodeComparator = cbsNodeComparator;
        return this;
    }

    public CBSBuilder setUseCorridorReasoning(Boolean useCorridorReasoning) {
        this.useCorridorReasoning = useCorridorReasoning;
        return this;
    }

    public CBSBuilder setSharedGoals(Boolean sharedGoals) {
        this.sharedGoals = sharedGoals;
        return this;
    }

    public CBSBuilder setSharedSources(Boolean sharedSources) {
        this.sharedSources = sharedSources;
        return this;
    }

    public CBSBuilder setTransientMAPFSettings(TransientMAPFSettings transientMAPFSettings) {
        this.transientMAPFSettings = transientMAPFSettings;
        return this;
    }

    public CBSBuilder setStaticObstaclesForUnassignedAgents(boolean staticObstaclesForUnassignedAgents) {
        this.staticObstaclesForUnassignedAgents = staticObstaclesForUnassignedAgents;
        return this;
    }
    public CBSBuilder setStaticUA(Set<Agent> staticUA) {
        this.staticUA = staticUA;
        return this;
    }
    public CBSBuilder setStaticUAForUnassignedAgents(boolean staticUAForUnassignedAgents) {
        this.staticUAForUnassignedAgents = staticUAForUnassignedAgents;
        return this;
    }

    public CBSBuilder setLocalRepairForUaConflicts(boolean localRepairForUaConflicts) {
        this.localRepairForUaConflicts = localRepairForUaConflicts;
        return this;
    }

    public CBSBuilder setUaBypass(boolean enabled) {
        this.localRepairForUaConflicts = enabled;
        return this;
    }

    public CBSBuilder setUaFutureConflictHeuristic(boolean enabled) {
        this.uaFutureConflictHeuristic = enabled;
        return this;
    }

    public CBSBuilder setUaAwareLowLevel(boolean enabled) {
        this.uaAwareLowLevel = enabled;
        return this;
    }

    // public CBS_Solver createCBS_Solver() {
    //     return new CBS_Solver(lowLevelSolver, openList, openListManagementMode, costFunction, cbsNodeComparator, useCorridorReasoning, sharedGoals, sharedSources, transientMAPFSettings, staticObstaclesForUnassignedAgents);
    // }

    public CBS_Solver createCBS_Solver() {
        CBS_Solver solver = new CBS_Solver(lowLevelSolver, openList, openListManagementMode, costFunction, cbsNodeComparator, useCorridorReasoning, sharedGoals, sharedSources, transientMAPFSettings, staticObstaclesForUnassignedAgents, localRepairForUaConflicts, uaFutureConflictHeuristic, uaAwareLowLevel);
        if (this.staticUAForUnassignedAgents) {
            solver.setStaticUA(staticUA);
        }
        return solver;
    }
}
