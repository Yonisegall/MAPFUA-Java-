package BasicMAPF.Solvers.AStar.CostsAndHeuristics;

import org.jetbrains.annotations.NotNull;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;

/**
 * A {@link SingleAgentGAndH} that wraps another implementation for NUA (Non-Unit Acceleration) cost function.
 * 
 * Assigned Agent (before reaching target): cost = 1, heuristic = distance
 * Unassigned Agent (after reaching target):
 *   - First move after target = 100
 *   - Subsequent moves/waits = 0
 *   - Heuristic = 0
 */
public class NUAGAndH implements SingleAgentGAndH {

    private final SingleAgentGAndH gAndH;

    /**
     * Constructor.
     * @param gAndH delegate to this {@link SingleAgentGAndH}
     */
    public NUAGAndH(@NotNull SingleAgentGAndH gAndH) {
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
        // Unassigned (after target): heuristic = 0
        if (state.visitedTarget) {return 0;}
        
        // Assigned (before target): heuristic = distance
        return getHToTargetFromLocation(state.getMove().agent.target, state.getMove().currLocation);
    }

    /**
     * NUA cost:
     * - Assigned: cost = 1
     * - Unassigned (after target):
     *     - First move: cost = 100
     *     - Subsequent: cost = 0
     */
    @Override
    public int cost(Move move, boolean isAfterTargetExcludingFirstMoveToTarget) {
        // Assigned: Move or wait = 1
        if (!isAfterTargetExcludingFirstMoveToTarget) {return 1;}
        
        // Unassigned
        // Wait = 1, to encourage waiting after target rather than moving
        if (move.prevLocation.equals(move.currLocation)) {return 0;} // return 0;
        
        // Movement after target
        // Check if prevLocation is the target (first move away from target)
        I_Coordinate target = move.agent.target;

        // First move away from target = 1,000 to strongly discourage moving after reaching target (but still allow it if necessary to reach the goal in some cases)
        if (move.prevLocation.getCoordinate().equals(target)) {return 1;}
        
        // Already moved at least once - move or wait = 0
        return 0;
    }

    @Override
    // Default: Assigned agent cost = 1
    public int cost(Move move) {return 1;}

    /**
     * Override to check if this is the first move after target reached
     */
    @Override
    public int cost(Move move, SingleAgentAStar_Solver.AStarState prevState) {
        boolean afterTarget = prevState != null && prevState.visitedTarget;
        
        // Assigned: cost = 1
        if (!afterTarget) {return 1;}
        
        // Unassigned
        // Wait = 1, to encourage waiting after target rather than moving
        if (move.prevLocation.equals(move.currLocation)) {return 0;} // return 0;
        
        // Movement after target
        // Check if prevLocation is the target (first move away from target)
        I_Coordinate target = move.agent.target;

        // First move away from target = 1,000 to strongly discourage moving after reaching target (but still allow it if necessary to reach the goal in some cases)
        if (move.prevLocation.getCoordinate().equals(target)) {return 1;}
        
        // Already moved at least once - move or wait = 0
        return 0;
    }

    public SingleAgentGAndH getWrappedHeuristic() {return this.gAndH;}

    @Override
    public boolean isTransient() {return true;}
}