package BasicMAPF.Solvers.LaCAM;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

public class NodeKey {
    private final Map<Agent, I_Location> configuration;
    private final Set<Agent> movedUA;

    public NodeKey(Map<Agent, I_Location> configuration, Set<Agent> movedUA) {
        this.configuration = new HashMap<>(configuration);
        this.movedUA = new HashSet<>(movedUA);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeKey nodeKey = (NodeKey) o;
        return Objects.equals(configuration, nodeKey.configuration) &&
               Objects.equals(movedUA, nodeKey.movedUA);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configuration, movedUA);
    }
}