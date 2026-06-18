package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import org.jetbrains.annotations.NotNull;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;

/**
 * A {@link SingleAgentGAndH} that wraps another implementation for FUEL cost function.
 * FUEL: move = 1, wait = 0 (before and after target)
 * Heuristic: distance to target before target, 0 after target
 */
public class FuelGAndH implements SingleAgentGAndH {

    private final SingleAgentGAndH gAndH;

    /**
     * Constructor.
     * @param gAndH delegate to this {@link SingleAgentGAndH}
     */
    public FuelGAndH(@NotNull SingleAgentGAndH gAndH) {
        this.gAndH = gAndH;
    }

    @Override
    public int getHToTargetFromLocation(@NotNull I_Coordinate target, @NotNull I_Location currLocation) {
        return gAndH.getHToTargetFromLocation(target, currLocation);
    }

    @Override
    public boolean isConsistent() {
        return gAndH.isConsistent();
    }

    @Override
    public float getH(@NotNull SingleAgentAStar_Solver.AStarState state) {
        if (state.visitedTarget) {
            return 0;
        }
        return getHToTargetFromLocation(state.getMove().agent.target, state.getMove().currLocation);
    }

    /**
     * FUEL cost: move = 1, wait = 0 (before and after target)
     */
    @Override
    public int cost(Move move, boolean isAfterTargetExcludingFirstMoveToTarget) {
        // Fuel counts movements (not waits)
        if (move.prevLocation.equals(move.currLocation)) {
            return 0; // wait = 0
        }
        return 1; // move = 1 (regardless of whether before or after target)
    }

    @Override
    public int cost(Move move) {
        // Fuel counts movements (not waits)
        if (move.prevLocation.equals(move.currLocation)) {
            return 0; // wait = 0
        }
        return 1; // move = 1
    }

    public SingleAgentGAndH getWrappedHeuristic() {
        return this.gAndH;
    }

    @Override
    public boolean isTransient() {
        return true;
    }
}