package com.edaviessmith.gdxgrid;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;


public class GameObject {
    public final Vector3 center = new Vector3();
    public final Vector3 dimensions = new Vector3();
    public ModelInstance modelInstance;
    public final float radius;
    public int type;

    private final static BoundingBox bounds = new BoundingBox();

    public GameObject(Model m) {
        this.modelInstance = new ModelInstance(m);
        this.modelInstance.calculateBoundingBox(bounds);
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        radius = dimensions.len() / 2.2f;
        type = GdxGrid.TYPE_WALL;
    }

   /* public GameObject(Model model, String rootNode, boolean mergeTransform) {
        super(model, rootNode, mergeTransform);
        calculateBoundingBox(bounds);
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        radius = dimensions.len() / 2f;
    }*/
}