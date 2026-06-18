package BasicMAPF.Solvers.AStart_OD;


import java.util.Set;

import org.jetbrains.annotations.Nullable;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import TransientMAPF.TransientMAPFSettings;

public class AStartOD_Builder {

    private SingleAgentGAndH gAndH = null;
    private I_SolutionCostFunction costFunction = null;
    private TransientMAPFSettings transientMAPFSettings = null;
    private boolean staticObstaclesForUnassignedAgents = false;
    private boolean staticUAForUnassignedAgents = false;
    private Set<Agent> staticUA = null;


    public AStartOD_Builder setAStarGAndH(@Nullable SingleAgentGAndH gh) {
        this.gAndH = gh;
        return this;
    }

    public AStartOD_Builder setCostFunction(@Nullable I_SolutionCostFunction cf) {
        this.costFunction = cf;
        return this;
    }
    public AStartOD_Builder setTransientMAPFSettings(TransientMAPFSettings transientMAPFSettings) {
        this.transientMAPFSettings = transientMAPFSettings;
        return this;
    }
    public AStartOD_Builder setStaticObstaclesForUnassignedAgents(boolean staticObstaclesForUnassignedAgents) {
        this.staticObstaclesForUnassignedAgents = staticObstaclesForUnassignedAgents;
        return this;
    }
    public AStartOD_Builder setStaticUA(Set<Agent> staticUA) {
        this.staticUA = staticUA;
        return this;
    }
    public AStartOD_Builder setStaticUAForUnassignedAgents(boolean staticUAForUnassignedAgents) {
        this.staticUAForUnassignedAgents = staticUAForUnassignedAgents;
        return this;
    }

    public AStarOD_Solver createAStarOD() {
        AStarOD_Solver solver =  new AStarOD_Solver(gAndH, costFunction, transientMAPFSettings, staticObstaclesForUnassignedAgents);
        if (this.staticUAForUnassignedAgents && this.staticUA != null) 
            solver.setStaticUA(staticUA);
        return solver;
    }


}

