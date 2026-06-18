package BasicMAPF.Solvers.LaCAM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

public class HighLevelNodeStar extends HighLevelNode {

    public Set<HighLevelNodeStar> neighbors;
    public HighLevelNodeStar parent;
    public float g;
    public float h;
    public float f;

    public HighLevelNodeStar(HashMap<Agent, I_Location> configuration, LowLevelNode root,ArrayList<Agent> order, 
                             HashMap<Agent, Float> priorities, HighLevelNodeStar parent, float g, float h) {
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // super(configuration, root, order, priorities, parent);
        super(configuration, root, order, priorities, parent, (parent != null ? parent.movedUA : new HashSet<>()));
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        this.parent = parent;
        if (parent != null) {
            parent.neighbors.add(this);
        }
        this.neighbors = new HashSet<>();
        this.g = g;
        this.h = h;
        this.f = this.g + this.h;
    }

    public float getG() {
        return g;
    }

    public float getH() {
        return h;
    }

    public float getF() {
        return f;
    }

    public void setG(float g) {
        this.g = g;
    }

    public void setH(float h) {
        this.h = h;
    }

    public void setF(float f) {
        this.f = f;
    }
}
