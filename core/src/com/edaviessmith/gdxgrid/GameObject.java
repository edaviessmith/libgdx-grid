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
    private final static BoundingBox bounds = new BoundingBox();


    public int type, x, y;



    public GameObject(Model m, int x, int y) {
        this.modelInstance = new ModelInstance(m);
        this.modelInstance.calculateBoundingBox(bounds);
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        radius = dimensions.len() / 2.2f;

        this.type = GdxGrid.TYPE_FLOOR;
        this.x = x;
        this.y = y;
    }

   /* public GameObject(Model model, String rootNode, boolean mergeTransform) {
        super(model, rootNode, mergeTransform);
        calculateBoundingBox(bounds);
        bounds.getCenter(center);
        bounds.getDimensions(dimensions);
        radius = dimensions.len() / 2f;
    }*/
}