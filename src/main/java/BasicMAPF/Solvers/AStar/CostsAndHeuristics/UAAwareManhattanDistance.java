package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

/**
 * Manhattan-distance heuristic for assigned agents and a zero heuristic for
 * unassigned agents. A zero heuristic makes a UA low-level search BFS/Dijkstra
 * like: it has no destination preference and is guided only by costs,
 * constraints, and (when enabled) conflict avoidance.
 */
public class UAAwareManhattanDistance implements SingleAgentGAndH {

    @Override
    public float getH(@NotNull SingleAgentAStar_Solver.AStarState state) {
        if (state.getMove().agent.isUA) {
            return 0;
        }
        return getHToTargetFromLocation(state.getMove().agent.target, state.getMove().currLocation);
    }

    @Override
    public int getHToTargetFromLocation(@NotNull I_Coordinate target, @NotNull I_Location currLocation) {
        return (int) currLocation.getCoordinate().distance(target);
    }

    @Override
    public boolean isConsistent() {
        return true;
    }
}
