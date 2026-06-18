package TransientMAPF;

import TransientMAPF.dummyGoals.I_DummyGoalsHeuristics;

public record TransientMAPFSettings(boolean isTransientMAPF, boolean avoidOtherAgentsTargets, boolean avoidSeparatingVertices, boolean resolveAfterGoalConflictsLocally, I_DummyGoalsHeuristics dummyGoalsHeuristic) {
    public TransientMAPFSettings {
        if (! isTransientMAPF && (avoidOtherAgentsTargets || avoidSeparatingVertices || resolveAfterGoalConflictsLocally)) {
            throw new IllegalArgumentException("useBlacklist, avoidSeparatingVertices, and resolveAfterGoalConflictsLocally can only be true if isTransientMAPF");
        }
    }
    public static TransientMAPFSettings defaultRegularMAPF = new TransientMAPFSettings(false, false, false, false, null);
    public static TransientMAPFSettings defaultTransientMAPF = new TransientMAPFSettings(true, true, false, false, null);
}
