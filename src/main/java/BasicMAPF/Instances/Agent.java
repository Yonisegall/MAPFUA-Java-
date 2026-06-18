package BasicMAPF.Instances;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;

public class Agent {

    public final int iD;
    public final I_Coordinate source;
    public final I_Coordinate target;
    public final int priorityClass;
    public final boolean isUA;

    public Agent(int iD, I_Coordinate source, I_Coordinate target) {
        this(iD, source, target, 1, source.equals(target));
    }

    public Agent(int iD, I_Coordinate source, I_Coordinate target, boolean isUA) {
        this(iD, source, target, 1, isUA);
    }

    public Agent(int iD, I_Coordinate source, I_Coordinate target, int priorityClass) {
        this(iD, source, target, priorityClass, source.equals(target));
    }

    public Agent(int iD, I_Coordinate source, I_Coordinate target, int priorityClass, boolean isUA) {
        this.iD = iD;
        this.source = source;
        this.target = target;
        this.priorityClass = priorityClass;
        this.isUA = isUA;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agent)) return false;

        Agent agent = (Agent) o;

        if (iD != agent.iD) return false;
        if (isUA != agent.isUA) return false;
        if (!source.equals(agent.source)) return false;
        return target.equals(agent.target);
    }

    @Override
    public int hashCode() {
        int result = iD;
        result = 31 * result + source.hashCode();
        result = 31 * result + target.hashCode();
        result = 31 * result + (isUA ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Agent{" + iD + " (" + (isUA ? "UA" : "A") + "): " + source + "->" + target +'}';
    }
}
