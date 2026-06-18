package BasicMAPF.Solvers.CBS;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumFuel;
import BasicMAPF.CostFunctions.SumNUA;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.CostFunctions.SumServiceTimes;
import BasicMAPF.DataTypesAndStructures.I_OpenList;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.OpenListHeap;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.CachingDistanceTableHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.FuelGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.NUAGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.ServiceTimeGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.UAAwareManhattanDistance;
import BasicMAPF.Solvers.AStar.RunParameters_SAAStar;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.RemovableConflictAvoidanceTableWithContestedGoals;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.SingleUseConflictAvoidanceTable;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictManager;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.CorridorConflictManager;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.I_ConflictManager;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.GoalConstraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.UnmodifiableConstraintSet;
import BasicMAPF.Solvers.I_Solver;
import Environment.Config;
import Environment.Metrics.InstanceReport;
import TransientMAPF.TransientMAPFSettings;
import TransientMAPF.TransientMAPFSolution;
import TransientMAPF.TransientMAPFUtils;

/**
 * The Conflict Based Search (CBS) Multi Agent Path Finding (MAPF) algorithm.
 */
public class CBS_Solver extends A_Solver {

    /*  = Fields =  */

    /*  = Fields related to the MAPF instance =  */

    private MAPF_Instance instance;

    /*  = Fields related to the run =  */

    /**
     * A {@link SingleAgentGAndH heuristic} for the low level solver.
     */
    private SingleAgentGAndH singleAgentGAndH;
    /**
     * Initial constraints given to the solver to work with.
     */
    private I_ConstraintSet initialConstraints;
    /**
     * Reused for each {@link CBS_Node}.
     */
    private I_ConstraintSet currentConstraints;

    /*  = Fields related to the class instance =  */

    /**
     * A queue of open {@link CBS_Node nodes/states}. Also referred to as OPEN.
     */
    public final I_OpenList<CBS_Node> openList;
    /**
     * @see OpenListManagementMode
     */
    private final OpenListManagementMode openListManagementMode;
    /**
     * A {@link I_Solver solver}, to be used for solving single-{@link Agent agent} sub-problems.
     */
    private final I_Solver lowLevelSolver;
    /**
     * Cost may be more complicated than a simple SOC (Sum of Individual Costs), so retrieve it through this method.
     */
    private final I_SolutionCostFunction costFunction;
    /**
     * Determines how to sort {@link #openList OPEN}.
     */
    private final Comparator<? super CBS_Node> CBSNodeComparator;
    /**
     * Whether to use corridor reasoning.
     * @see <a href="jiaoyangli.me/files/2020-ICAPS.pdf#page=1&zoom=180,-78,792">New Techniques for Pairwise Symmetry Breaking in Multi-Agent Path Finding</a>
     */
    private final boolean corridorReasoning;
    /**
     * if true, agents can have shared goals, so they can stay at their goal together (only last move onwards).
     */
    private final boolean sharedGoals;

    /**
     * If true, agents staying at their source (since the start) will not conflict 
     */
    private final boolean sharedSources;
    private Set<I_Coordinate> separatingVerticesSet;
    private final boolean staticObstaclesForUnassignedAgents;
    private final boolean localRepairForUaConflicts;
    private final boolean uaFutureConflictHeuristic;
    private final boolean uaAwareLowLevel;
    private List<Agent> unassignedAgents = null;
    private Set<Agent> staticUA = null;
    private Solution initialSolution = null;
    private int uaLocalRepairAttempts = 0;
    private int uaLocalRepairSuccesses = 0;

    /*  = Constructors =  */

    /**
     * Parameterised constructor.
     * @param lowLevelSolver this {@link I_Solver solver} will be used to solve single agent sub-problems.
     * @param openList this will be used as the {@link I_OpenList open list} in the solver. This instance will be reused by calling {@link I_OpenList#clear()} after every run.
     * @param openListManagementMode @see {@link OpenListManagementMode}.
     * @param costFunction a cost function for solutions.
     * @param cbsNodeComparator determines how to sort {@link #openList OPEN}.
     * @param useCorridorReasoning whether to use corridor reasoning.
     */

    CBS_Solver(@Nullable I_Solver lowLevelSolver, @Nullable I_OpenList<CBS_Node> openList, @Nullable OpenListManagementMode openListManagementMode,
               @Nullable I_SolutionCostFunction costFunction, @Nullable Comparator<? super CBS_Node> cbsNodeComparator, @Nullable Boolean useCorridorReasoning,
               @Nullable Boolean sharedGoals, @Nullable Boolean sharedSources, @Nullable TransientMAPFSettings transientMAPFSettings,
               @Nullable Boolean staticObstaclesForUnassignedAgents, @Nullable Boolean localRepairForUaConflicts,
               @Nullable Boolean uaFutureConflictHeuristic, @Nullable Boolean uaAwareLowLevel) {

        this.lowLevelSolver = Objects.requireNonNullElseGet(lowLevelSolver, SingleAgentAStar_Solver::new);
        this.openList = Objects.requireNonNullElseGet(openList, OpenListHeap::new);
        this.openListManagementMode = openListManagementMode != null ? openListManagementMode : OpenListManagementMode.AUTOMATIC;
        this.corridorReasoning = Objects.requireNonNullElse(useCorridorReasoning, false);
        clearOPEN();
        // if a specific cost function is not provided, use standard SOC (Sum of Individual Costs)
        this.costFunction = Objects.requireNonNullElseGet(costFunction, SumOfCosts::new);
        this.CBSNodeComparator = cbsNodeComparator != null ? cbsNodeComparator : new CBSNodeComparatorForcedTotalOrdering();
        this.sharedGoals = Objects.requireNonNullElse(sharedGoals, false);
        this.sharedSources = Objects.requireNonNullElse(sharedSources, false);
        this.transientMAPFSettings = Objects.requireNonNullElse(transientMAPFSettings, TransientMAPFSettings.defaultRegularMAPF);
        if (Config.WARNING >= 1 && this.sharedGoals && this.transientMAPFSettings.isTransientMAPF()){
            System.err.println("Warning: " + this.name + " has shared goals and is set to transient MAPF. Shared goals is unnecessary if transient.");
        }

        this.staticObstaclesForUnassignedAgents = Objects.requireNonNullElse(staticObstaclesForUnassignedAgents, false);
        this.localRepairForUaConflicts = Objects.requireNonNullElse(localRepairForUaConflicts, false);
        this.uaFutureConflictHeuristic = Objects.requireNonNullElse(uaFutureConflictHeuristic, false);
        this.uaAwareLowLevel = Objects.requireNonNullElse(uaAwareLowLevel, false);

        if (this.transientMAPFSettings.isTransientMAPF()) {
            super.name = "CBS_Dynamic_UA";
        } else if (this.staticObstaclesForUnassignedAgents) {
            super.name = "CBS_Static_UA";
        } else {
            super.name = "CBS";
        }
    }

    /**
     * Default constructor.
     */
    CBS_Solver() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null);
    }

    /*  = initialization =  */

    @Override
    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        super.init(instance, runParameters);

        this.initialConstraints = Objects.requireNonNullElseGet(runParameters.constraints, ConstraintSet::new);
        this.initialSolution = runParameters.existingSolution;

        // All UA is static obstacles
        if (this.staticObstaclesForUnassignedAgents) {
            this.unassignedAgents = new ArrayList<>();
            List<Agent> newAgentsList = new ArrayList<>();
            for (Agent agent : instance.agents) {
                if (agent.isUA) {
                    Constraint infiniteConstraint = new GoalConstraint(null, 1, null, instance.map.getMapLocation(agent.source), agent);
                    this.initialConstraints.add(infiniteConstraint);
                    this.unassignedAgents.add(agent);
                }
                else {
                    newAgentsList.add(agent);
                }
            }
            instance = new MAPF_Instance(instance.name, instance.map, newAgentsList.toArray(new Agent[0]));
        }

        // Some of UA is static obstacles
        if (this.staticUA != null && !this.staticUA.isEmpty()) {
            this.unassignedAgents = new ArrayList<>();
            List<Agent> newAgentsList = new ArrayList<>();
            for (Agent agent : instance.agents) {
                if (staticUA.contains(agent)) {
                    // add infinite constraint to make it static
                    Constraint infiniteConstraint = new GoalConstraint(null, 1, null, instance.map.getMapLocation(agent.source), agent);
                    this.initialConstraints.add(infiniteConstraint);
                    this.unassignedAgents.add(agent);
                } else {
                    newAgentsList.add(agent); // transient agents
                }
            }
            instance = new MAPF_Instance(instance.name, instance.map, newAgentsList.toArray(new Agent[0]));
        }

        this.currentConstraints = new ConstraintSet();
        this.generatedNodes = 0;
        this.expandedNodes = 0;
        this.uaLocalRepairAttempts = 0;
        this.uaLocalRepairSuccesses = 0;
        this.instance = instance;

        if (this.costFunction instanceof BasicMAPF.CostFunctions.SumNUA) {
            ((BasicMAPF.CostFunctions.SumNUA)this.costFunction).configureFromInstance(instance);
        }

        // heuristic
        if (runParameters.singleAgentGAndH != null){
            this.singleAgentGAndH = runParameters.singleAgentGAndH;
        }
        else {
            if (this.lowLevelSolver instanceof SingleAgentAStar_Solver){
                this.singleAgentGAndH = this.uaAwareLowLevel
                        ? new UAAwareManhattanDistance()
                        : new DistanceTableSingleAgentHeuristic(new ArrayList<>(instance.agents), instance.map);
            }

            if (this.singleAgentGAndH instanceof CachingDistanceTableHeuristic){
                ((CachingDistanceTableHeuristic)this.singleAgentGAndH).setCurrentMap(instance.map);
            }

            if (this.singleAgentGAndH != null) {
                if (this.costFunction instanceof SumServiceTimes) {
                    this.singleAgentGAndH = new ServiceTimeGAndH(this.singleAgentGAndH);
                } else if (this.costFunction instanceof SumFuel) {
                    this.singleAgentGAndH = new FuelGAndH(this.singleAgentGAndH);
                } else if (this.costFunction instanceof SumNUA) {
                    this.singleAgentGAndH = new NUAGAndH(this.singleAgentGAndH);
                }
            }
        }

        if (this.transientMAPFSettings.avoidSeparatingVertices()) {
            this.separatingVerticesSet = TransientMAPFUtils.createSeparatingVerticesSetOfCoordinates(instance, runParameters);
        }
    }

    /*  = algorithm =  */

    /**
     * Implements the CBS algorithm, as described in the original CBS article from Proceedings of the Twenty-Sixth AAAI
     * Conference on Artificial Intelligence.
     * @param instance {@inheritDoc}
     * @param parameters {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        initOpen(Objects.requireNonNullElseGet(this.initialConstraints, ConstraintSet::new));
        CBS_Node goal = mainLoop();
        return solutionFromGoal(goal);
    }

    /**
     * Initialises the {@link #openList OPEN} and inserts the root.
     * @param initialConstraints a set of initial constraints on the agents.
     */
    private void initOpen(I_ConstraintSet initialConstraints) {
        if(this.openListManagementMode == OpenListManagementMode.AUTOMATIC ||
                this.openListManagementMode == OpenListManagementMode.AUTO_INIT_MANUAL_CLEAR){
            addToOpen(generateRoot(initialConstraints));
        }
    }

    /**
     * Creates a root node.
     */

    private CBS_Node generateRoot(I_ConstraintSet initialConstraints) {
        // if an initial solution was provided, use it (may be partial)
        Solution solution;
        if (this.initialSolution != null) {
            solution = this.transientMAPFSettings.isTransientMAPF() ? new TransientMAPFSolution(this.initialSolution) : new Solution(this.initialSolution);
            // ensure all agents have a plan; replan missing ones
            for (Agent agent : this.instance.agents) {
                if (solution.getPlanFor(agent) == null) {
                    solution = solveSubproblem(agent, solution, initialConstraints);
                    if (solution == null) {
                        return null;
                    }
                }
            }
        } else {
            solution = this.transientMAPFSettings.isTransientMAPF() ? new TransientMAPFSolution() : new Solution();
            List<Agent> planningOrder = new ArrayList<>(this.instance.agents);
            if (this.uaFutureConflictHeuristic) {
                // Plan all assigned routes first so every UA can see and avoid them.
                planningOrder.sort(Comparator.comparing(agent -> agent.isUA));
            }
            for (Agent agent : planningOrder) {
                solution = solveSubproblem(agent, solution, initialConstraints);
                if (solution == null){
                    // failed to solve for some agent
                    return null;
                }
            }
        }

        return new CBS_Node(solution, costFunction.solutionCost(solution));
    }

    /**
     * The main loop of the CBS algorithm. Expands and generates nodes.
     * @return the goal node, or null if a timeout occurs before it is found.
     */
    private CBS_Node mainLoop() {
        while(!openList.isEmpty() && !checkTimeout()){
            CBS_Node node = openList.poll();

            // verify solution (find conflicts)
            I_ConflictManager cat = getConflictManagerFor(node);
            node.setSelectedConflict(cat.selectConflict());

            if(isGoal(node)){ // todo early goal test
                return node;
            }
            else {
                if (this.localRepairForUaConflicts && tryBypassUaOnlyConflicts(node)) {
                    openList.add(node);
                    continue;
                }
                if (this.transientMAPFSettings.resolveAfterGoalConflictsLocally()) {
                    if (tryResolveConflictsLocally(node)) {
                        openList.add(node);
                        continue;
                    };
                }
                expandNode(node);
            }
        }

        return null; //probably a timeout
    }

    public void setStaticUA(Set<Agent> staticUA) {
        this.staticUA = staticUA;
    }

    /**
     * When a node is first generated, it is given the same {@link ConflictManager} as its parent. Only when that
     * node is later dequeued from {@link #openList}, will we update the table.
     * @param node a {@link CBS_Node node} that contains an out of date {@link ConflictManager}.
     * @return a {@link I_ConflictManager} for the solution in this node.
     */
    private I_ConflictManager getConflictManagerFor(CBS_Node node) {
        I_ConflictManager cat = this.corridorReasoning ?
                new CorridorConflictManager(buildConstraintSet(node,null), this.instance) :
                new ConflictManager(null, this.sharedGoals, this.sharedSources);
        for (SingleAgentPlan plan :
                node.getSolution()) {
            cat.addPlan(plan);
        }
        return cat;
    }

    /**
     * Bypasses CT branching when all remaining conflicts involve at least one
     * UA. Only a conflicting UA is replanned and the repaired solution is put
     * back into the same high-level node.
     */
    private boolean tryBypassUaOnlyConflicts(CBS_Node node) {
        if (node.selectedConflict == null || hasAssignedAssignedConflict(node.solution)) {
            return false;
        }
        this.uaLocalRepairAttempts++;

        Solution originalSolution = node.getSolution();
        int originalConflictCount = originalSolution.countConflicts(this.sharedGoals, this.sharedSources);
        Constraint[] preventingConstraints = node.selectedConflict.getPreventingConstraints();

        for (Constraint preventingConstraint : preventingConstraints) {
            Agent ua = preventingConstraint.agent;
            if (!ua.isUA) {
                continue;
            }

            Solution repairedSolution = transientMAPFSettings.isTransientMAPF() ?
                    new TransientMAPFSolution(originalSolution) : new Solution(originalSolution);
            repairedSolution.putPlan(new SingleAgentPlan(ua));

            Solution repairedUaSolution = solveSubproblem(
                    ua, repairedSolution, buildConstraintSet(node, preventingConstraint));
            if (repairedUaSolution == null || repairedUaSolution.getPlanFor(ua) == null) {
                continue;
            }

            repairedSolution.putPlan(repairedUaSolution.getPlanFor(ua));
            int repairedConflictCount = repairedSolution.countConflicts(this.sharedGoals, this.sharedSources);
            if (repairedConflictCount >= originalConflictCount) {
                continue;
            }

            node.replaceSolution(repairedSolution, costFunction.solutionCost(repairedSolution));
            node.setSelectedConflict(null);
            this.uaLocalRepairSuccesses++;
            return true;
        }
        return false;
    }

    private boolean hasAssignedAssignedConflict(Solution solution) {
        List<SingleAgentPlan> assignedPlans = new ArrayList<>();
        for (SingleAgentPlan plan : solution) {
            if (!plan.agent.isUA) {
                assignedPlans.add(plan);
            }
        }
        for (int i = 0; i < assignedPlans.size(); i++) {
            for (int j = i + 1; j < assignedPlans.size(); j++) {
                if (assignedPlans.get(i).conflictsWith(
                        assignedPlans.get(j), this.sharedGoals, this.sharedSources)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isGoal(CBS_Node node) {
        // no conflicts -> found goal
        return node.selectedConflict == null;
    }

    /**
     * Expands a {@link CBS_Node}.
     * @param node a node to expand.
     */

    private void expandNode(CBS_Node node) {
        this.expandedNodes++;

        // Special handling for FUEL: if there is a timestep where all agents stay,
        // branch into one child per agent, forbidding that agent to stay at that timestep.
        if (this.costFunction instanceof SumFuel || this.costFunction instanceof SumNUA) {
            Solution sol = node.getSolution();
            int last = sol.endTime();
            for (int t = 1; t <= last; t++) {
                boolean allStayed = true;
                for (SingleAgentPlan plan : sol) {
                    if (!plan.StayInIndex(t)) {
                        allStayed = false;
                        break;
                    }
                }
                if (allStayed) {
                    // create a child per agent that forbids staying at time t at its location
                    List<CBS_Node> children = new ArrayList<>();
                    for (SingleAgentPlan plan : sol) {
                        Agent a = plan.agent;
                        // get agent location at time t (handles before/after plan bounds)
                        BasicMAPF.Instances.Maps.I_Location loc = sol.getAgentLocation(a, t);
                        Constraint cons = new Constraint(a, t, loc, loc); // forbids stay (prevLocation == location)
                        CBS_Node child = generateNode(node, cons, true); // make copy for safety
                        if (child != null) {
                            children.add(child);
                            addToOpen(child);
                        }
                    }
                    // attach first two children to left/right if present (keeps existing structure)
                    if (children.size() > 0) node.leftChild = children.get(0);
                    if (children.size() > 1) node.rightChild = children.get(1);
                    return;
                }
            }
        }

        // default (pairwise conflict) expansion
        Constraint[] constraints = node.selectedConflict.getPreventingConstraints();
        node.leftChild = generateNode(node, constraints[0], true);
        node.rightChild = generateNode(node, constraints[1], false);

        addToOpen(node.leftChild);
        addToOpen(node.rightChild);
    }
    
    /**
     * Adds a node to {@link #openList OPEN}. If a duplicate node exists, keeps the one with less cost.
     * @param node a node to insert into {@link #openList OPEN}
     * @return true if {@link #openList OPEN} changed as a result of the call.
     */
    private boolean addToOpen(CBS_Node node) {
        if(node == null){
            // either the low level encountered a timeout (in which case we will also timeout very soon), or the low
            // level was unsolvable (in which case we prune this node and continue).
            return false;
        }
        return openList.add(node);
    }

    /**
     * Since the creation of a new {@link CBS_Node node} is somewhat complicated, it is handled in its own method.
     * @param parent the new node's parent
     * @param constraint the constraint that we want to add in this node, before re-solving the agent that is constrained.
     * @param copyDatastructures for one child, we may be able to reuse the parent's data structures, instead of copying them.
     * @return a new {@link CBS_Node}.
     */
    private CBS_Node generateNode(CBS_Node parent, Constraint constraint, boolean copyDatastructures) {
        Agent agent = constraint.agent;

        Solution solution = parent.solution;

        // replace with copies if required
        if(copyDatastructures) {
            solution =  transientMAPFSettings.isTransientMAPF() ? new TransientMAPFSolution(solution) : new Solution(solution);
        }

        // modify for this node
        /*  replace the current plan for the agent with an empty plan, so that the low level won't try to continue the
            existing plan.
            Also we don't want to reuse (modify) SingleAgentPlan objects, as they are pointed to by other Solution objects, which
            we don't want to modify.
         */
        solution.putPlan(new SingleAgentPlan(agent));

        //the low-level should update the solution, so this is a reference to the same object as solution. We do this to
        //reuse Solution objects instead of creating extra ones.
        Solution agentSolution = solveSubproblem(agent, solution, buildConstraintSet(parent, constraint));
        if(agentSolution == null) {
            return null; //probably a timeout
        }
        // in case the low-level didn't update the Solution object it was given, this makes sure we preserve other agents'
        // plans, and add the re-planned agent's new plan.
        solution.putPlan(agentSolution.getPlanFor(agent));

        return new CBS_Node(solution, costFunction.solutionCost(solution), constraint, parent);
    }

    /**
     * When solving a new node, you want a set of constraints that apply to it. To save on memory, this set is created
     * on the spot, by climbing up the CT and collecting all the constraints that were added
     * @param parentNode the new node's parent.
     * @param newConstraint the constraint that this new node adds.
     * @return a {@link ConstraintSet} of all the constraints from parentNode to the root, plus newConstraint.
     */
    private I_ConstraintSet buildConstraintSet(CBS_Node parentNode, Constraint newConstraint) {
        // clear currentConstraints. we reuse this object every time.
        this.currentConstraints.clear();
        I_ConstraintSet constraintSet = this.currentConstraints;
        // start by adding all the constraints that we were asked to start the solver with (and are therefore not in the CT)
        constraintSet.addAll(initialConstraints);

        CBS_Node currentNode = parentNode;
        while (currentNode.addedConstraint != null){ // will skip the root (it has no constraints)
            constraintSet.add(currentNode.addedConstraint);
            currentNode = currentNode.parent;
        }
        if(newConstraint != null){
            constraintSet.add(newConstraint);
        }
        return constraintSet;
    }

    /**
     * Solves a single agent sub-problem.
     * @param agent
     * @param currentSolution
     * @param constraints
     * @return a solution to a single agent sub-problem. Typically the same object as currentSolution, after being modified.
     */
    private Solution solveSubproblem(Agent agent, Solution currentSolution, I_ConstraintSet constraints) {
        InstanceReport instanceReport = new InstanceReport();
        instanceReport.keepSolutionString = false;
        RunParameters subproblemParameters = getSubproblemParameters(currentSolution, constraints, instanceReport, agent);
        Solution subproblemSolution = this.lowLevelSolver.solve(this.instance.getSubproblemFor(agent), subproblemParameters);
        digestSubproblemReport(instanceReport);
        return subproblemSolution;
    }

    private RunParameters getSubproblemParameters(Solution currentSolution, I_ConstraintSet constraints, InstanceReport instanceReport, Agent agent) {
        // if there was already a timeout while solving a node, we will get a negative time left, which would be
        // interpreted as "use default timeout". In such a case we should instead give the solver 0 time to solve.
        long timeLeftToTimeout = Math.max(super.maximumRuntime - (System.nanoTime()/1000000 - super.startTime), 0);
        RunParameters subproblemParametes = new RunParametersBuilder().setTimeout(timeLeftToTimeout).setConstraints(new UnmodifiableConstraintSet(constraints)).
                setInstanceReport(instanceReport).setExistingSolution(currentSolution).setAStarGAndH(this.singleAgentGAndH).createRP();
        if(this.lowLevelSolver instanceof SingleAgentAStar_Solver){ // upgrades to a better heuristic
            RunParameters_SAAStar astarSubproblemParameters = new RunParameters_SAAStar(subproblemParametes);

            // TMAPF goal condition
            if (transientMAPFSettings.isTransientMAPF()) {
                astarSubproblemParameters.goalCondition = TransientMAPFUtils.createLowLevelGoalConditionForTransientMAPF(transientMAPFSettings, separatingVerticesSet, instance.agents, agent, null);
            }

            I_ConflictAvoidanceTable cat = null;
            boolean useConflictAvoidance = !agent.isUA || this.uaFutureConflictHeuristic;
            if (useConflictAvoidance) {
                if (this.transientMAPFSettings.isTransientMAPF()){
                    // This CAT also evaluates conflicts caused by staying forever at a candidate location.
                    if (this.sharedGoals || this.sharedSources){
                        throw new UnsupportedOperationException("Shared goals and shared sources are not supported in TMAPF");
                    }
                    cat = new RemovableConflictAvoidanceTableWithContestedGoals(currentSolution, agent);
                }
                else{
                    cat = new SingleUseConflictAvoidanceTable(currentSolution, agent);
                    ((SingleUseConflictAvoidanceTable)cat).sharedGoals = this.sharedGoals;
                    ((SingleUseConflictAvoidanceTable)cat).sharedSources = this.sharedSources;
                }
            }
            astarSubproblemParameters.conflictAvoidanceTable = cat;
            subproblemParametes = astarSubproblemParameters;
        }
        return subproblemParametes;
    }

    /**
     * Extracts a solution from a goal {@link CBS_Node node}.
     * @param goal a {@link CBS_Node} that we consider to be a goal node.
     * @return a solution from a goal {@link CBS_Node node}.
     */
    private Solution solutionFromGoal(CBS_Node goal) {
        if(goal == null){
            return null;
        }
        else{
            if (this.staticObstaclesForUnassignedAgents) {
                for (Agent agent : this.unassignedAgents) {
                    SingleAgentPlan plan = new SingleAgentPlan(agent);

                    Move move = new Move(agent, 1, this.instance.map.getMapLocation(agent.source), this.instance.map.getMapLocation(agent.target));
                    plan.addMove(move);
                    goal.solution.putPlan(plan);
                }
            }
            if (this.staticUA != null && !this.staticUA.isEmpty()) {
                for (Agent agent : this.unassignedAgents) {
                    SingleAgentPlan plan = new SingleAgentPlan(agent);
                    Move move = new Move(agent, 1, this.instance.map.getMapLocation(agent.source), this.instance.map.getMapLocation(agent.target));
                    plan.addMove(move);
                    goal.solution.putPlan(plan);
                }
            }
            // return trimTransientMAPFSolution(goal.solution);
            return goal.solution;
        }
    }

    /**
     * Clears OPEN
     */
    private void clearOPEN() {
        if(this.openListManagementMode == OpenListManagementMode.AUTOMATIC ||
                this.openListManagementMode == OpenListManagementMode.MANUAL_INIT_AUTO_CLEAR){
            openList.clear();
        }
    }

    /**
     * The function checks if the conflict in the CBS node happens after one of the agents reached its target.
     * If so, try to resolve it using the function {@link #resolveConflict(Agent, Agent, CBS_Node)}
     * @param node - CBS node containing a conflict to resolve.
     * @return true if the conflict was resolved, false otherwise.
     */
    private boolean tryResolveConflictsLocally(CBS_Node node) {
        // check if the conflict occurs after one agent reached its target
        Agent agent1 = node.selectedConflict.agent1;
        Agent agent2 = node.selectedConflict.agent2;
        int conflictTime = node.selectedConflict.time;
        int agent1VisitedTargetTime = node.solution.getPlanFor(agent1).firstVisitToTargetTime();
        int agent2VisitedTargetTime = node.solution.getPlanFor(agent2).firstVisitToTargetTime();

        // If the conflict occurs after one of the agents reached its target, try to resolve the conflict locally
        boolean conflictResolved = false;
        if (agent1VisitedTargetTime < conflictTime) {
            conflictResolved = resolveConflict(agent1, agent2, node);
        }
        if (agent2VisitedTargetTime < conflictTime) {
            conflictResolved = resolveConflict(agent2, agent1, node);
        }
        return conflictResolved;
    }


    /**
     * Try to resolve a conflict locally. If succeeded, the solution in the CBS node is replaced with the new solution.
     * @param resolvingAgent is the agent that reached its target, and needs to move to allow the other agent to move.
     * @param otherAgent is the agent that does not change its plan.
     * @param node - CBS node contains the current solution.
     * @return True if the conflict is resolved and successfully replaced in node, false otherwise.
     */
    private boolean resolveConflict(Agent resolvingAgent, Agent otherAgent, CBS_Node node) {
        ConstraintSet allConstraints = new ConstraintSet(this.currentConstraints);
        allConstraints.addAll(allConstraints.allConstraintsForPlan(node.solution.getPlanFor(otherAgent)));
        Solution newSolution = solveSubproblem(resolvingAgent, node.solution, allConstraints);
        if (newSolution == null) {
            return false;
        }
        else {
            node.solution = newSolution;
            return true;
        }
    }


    /*  = wind down =  */

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        if(solution != null){
            super.instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, costFunction.name());
            super.instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, costFunction.solutionCost(solution));
        }
        super.instanceReport.putIntegerValue("UA Local Repair Attempts", this.uaLocalRepairAttempts);
        super.instanceReport.putIntegerValue("UA Local Repair Successes", this.uaLocalRepairSuccesses);
    }

    @Override
    protected void releaseMemory() {
        clearOPEN();
        this.initialConstraints = null;
        this.currentConstraints = null;
        this.instance = null;
        this.singleAgentGAndH = null;
    }

    /*  = internal classes and interfaces =  */

    /**
     * A data type for representing a single node in the CBS search tree.
     * Try to keep most logic in {@link CBS_Solver}, avoiding methods in this class.
     */
    public class CBS_Node implements Comparable<CBS_Node>{

        /*  =  = fields =  */

        /**
         * The solution in this node. For every non-root node, this solution is after rerouting (solving low level) an
         * agent to overcome a conflict.
         * Holds references to the same {@link SingleAgentPlan plans} as in {@link #parent}, apart from the plan
         * of the re-routed agent.
         */
        private Solution solution;
        /**
         * The cost of the solution.
         */
        private float solutionCost;
        /**
         * The constraint that was added in this node (missing from {@link #parent}).
         */
        private Constraint addedConstraint;
        /**
         * A {@link A_Conflict conflict}, selected to be solved by new constraints in child nodes.
         */
        private A_Conflict selectedConflict;
        /**
         * Needed to enforce total ordering on nodes, which is needed to make node expansions fully deterministic. That
         * is to say, if all tie breaking methods still result in equality, tie break for using serialID.
         */
        private final int serialID = CBS_Solver.this.generatedNodes++; // take and increment

        /*  =  =  = CBS tree branches =  =  */

        /**
         * This node's parent node. This node's {@link #addedConstraint} solves parent's {@link #selectedConflict}.
         */
        private CBS_Node parent;
        /**
         * One of this node's child nodes. Solves this node's {@link #selectedConflict} in one way.
         */
        private CBS_Node leftChild;
        /**
         * One of this node's child nodes. Solves this node's {@link #selectedConflict} in one way.
         */
        private CBS_Node rightChild;

        /*  =  = constructors =  */

        /**
         * Root constructor.
         * @param solution an initial solution for all agents.
         * @param solutionCost the cost of the solution.
         */
        public CBS_Node(Solution solution, float solutionCost) {
            this.solution = solution;
            this.solutionCost = solutionCost;
            this.parent = null;
        }

        /**
         * Non-root constructor.
         */
        public CBS_Node(Solution solution, float solutionCost, Constraint addedConstraint, CBS_Node parent) {
            this.solution = solution;
            this.solutionCost = solutionCost;
            this.addedConstraint = addedConstraint;
            this.parent = parent;
        }

        /*  =  = when expanding a node =  */

        /**
         * Set the selected conflict. Typically done through delegation to {@link I_ConflictManager#selectConflict()}.
         * @param selectedConflict
         */
        public void setSelectedConflict(A_Conflict selectedConflict) {
            this.selectedConflict = selectedConflict;
        }

        private void replaceSolution(Solution solution, float solutionCost) {
            this.solution = solution;
            this.solutionCost = solutionCost;
        }

        /**
         * Set a reference to one of the generated child nodes when expanding this node.
         * @param leftChild One of this node's child nodes. Solves this node's {@link #selectedConflict} in one way.
         */
        public void setLeftChild(CBS_Node leftChild) {
            this.leftChild = leftChild;
        }

        /**
         * Set a reference to one of the generated child nodes when expanding this node.
         * @param rightChild One of this node's child nodes. Solves this node's {@link #selectedConflict} in one way.
         */
        public void setRightChild(CBS_Node rightChild) {
            this.rightChild = rightChild;
        }

        /*  =  = getters =  */

        public Solution getSolution() {
            return solution;
        }

        public float getSolutionCost() {
            return solutionCost;
        }

        public Constraint getAddedConstraint() {
            return addedConstraint;
        }

        public A_Conflict getSelectedConflict() {
            return selectedConflict;
        }

        public CBS_Node getParent() {
            return parent;
        }

        public CBS_Node getLeftChild() {
            return leftChild;
        }

        public CBS_Node getRightChild() {
            return rightChild;
        }

        @Override
        public int compareTo(CBS_Node o) {
            return Objects.compare(this, o, CBS_Solver.this.CBSNodeComparator);
        }

    }

    public static class CBSNodeComparatorForcedTotalOrdering implements Comparator<CBS_Node>{

        private static final Comparator<CBS_Node> costComparator = Comparator.comparing(CBS_Node::getSolutionCost);

        @Override
        public int compare(CBS_Node o1, CBS_Node o2) {
            if(Math.abs(o1.getSolutionCost() - o2.getSolutionCost()) < 0.1){ // floats are equal
                // If still equal, we tie break for smaller ID (older nodes) (arbitrary) to force a total ordering and remain deterministic
                return o2.serialID- o1.serialID;
            }
            else {
                return costComparator.compare(o1, o2);
            }
        }
    }


    /**
     * Modes for handling the initialization and clearing of {@link #openList OPEN}. The default mode of operation is
     * {@link #AUTOMATIC}.
     */
    public enum OpenListManagementMode{
        /**
         * Will handle OPEN automatically. This is the standard mode of operation. The solver will clear OPEN before and
         * after every run, and initialize OPEN at the start of every run with a single root {@link CBS_Node node}.
         */
        AUTOMATIC,
        /**
         * Will initialize OPEN automatically, but clearing it before or after a run will be controlled manually.
         * Note that this means the solver keeps part of its state after running. If you want to reuse the solver, you
         * have to manually handle the clearing of OPEN. If you keep references to many such solvers, this may adversely
         * affect available memory.
         */
        AUTO_INIT_MANUAL_CLEAR,
        /**
         * Will not initialize OPEN (assumes that it was already initialized), but will clear it after running.
         * It is not cleared before running. If it were to be cleared before running, manual initialization would be
         * impossible.
         */
        MANUAL_INIT_AUTO_CLEAR,
        /**
         * Will not initialize OPEN (assumes that it was already initialized).
         * Will not clear OPEN automatically.
         * Note that this means the solver keeps part of its state after running. If you want to reuse the solver, you
         * have to manually handle the clearing of OPEN. If you keep references to many such solvers, this may adversely
         * affect available memory.
         */
        MANUAL
    }


    /**
     * Trims unnecessary moves from plans after agents reach their goals in Transient MAPF.
     */
    public Solution trimTransientMAPFSolution(Solution solution) {
        if (!this.transientMAPFSettings.isTransientMAPF()) {
            return solution;
        }
        
        Solution trimmedSolution = new TransientMAPFSolution();
        
        for (SingleAgentPlan plan : solution) {
            Agent agent = plan.agent;
            int goalTime = plan.firstVisitToTargetTime();
            
            // If agent never reaches goal, keep full plan
            if (goalTime == -1) {
                trimmedSolution.putPlan(plan);
                continue;
            }
            
            // If plan already ends at or before goal, keep it as-is
            if (plan.getEndTime() <= goalTime) {
                trimmedSolution.putPlan(plan);
                continue;
            }
            
            // Collect moves up to goalTime
            List<Move> trimmedMoves = new ArrayList<>();
            for (int t = plan.getFirstMoveTime(); t <= goalTime; t++) {
                Move move = plan.moveAt(t);
                if (move != null) {
                    trimmedMoves.add(move);
                }
            }
            
            // Create new plan with trimmed moves using constructor
            SingleAgentPlan trimmedPlan = new SingleAgentPlan(agent, trimmedMoves);
            trimmedSolution.putPlan(trimmedPlan);
        }
        
        return trimmedSolution;
    }
}
