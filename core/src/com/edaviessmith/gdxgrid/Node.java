package com.edaviessmith.gdxgrid;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public List<Node> neighbours;
    public int x;
    public int y;

    public Node() {
        neighbours = new ArrayList<Node>();
    }

    public Node(int x, int y) {
        neighbours = new ArrayList<Node>();
        this.x = x;
        this.y = y;
    }

    public float distanceTo(Node n) {
        return Vector2.dst(x, y, n.x, n.y);
    }

}
