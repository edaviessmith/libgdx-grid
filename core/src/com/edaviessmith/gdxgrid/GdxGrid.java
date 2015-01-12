package com.edaviessmith.gdxgrid;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;


public class GdxGrid extends InputAdapter implements ApplicationListener {

    public Environment environment;
    public CameraInputController camController;
    public PerspectiveCamera cam;

    public ModelInstance modelWall, modelFloor, modelRobot, modelSelector;
    public Array<GameObject> instances = new Array<GameObject>();

    public ModelBatch modelBatch;
    SpriteBatch batch;

    protected AssetManager assets;

    protected Stage stage;
    protected Label label;
    protected BitmapFont font;
    protected StringBuilder stringBuilder;



    Texture exploreImage;
    Texture buildImage;
    Texture dragImage;
    TextureRegion exploreTex;
    TextureRegion buildTex;
    TextureRegion dragTex;


    private Vector3 position = new Vector3();
    private boolean loading;

    public float padding = 0f;//0.2f;
    public float width = 1f;
    public float depth = 1f;

    int cubeY = 12, cubeX = 12;

    int editMode, selectedType;

    private static final int MODE_EXPLORE = 0;
    private static final int MODE_BUILD = 1;
    private static final int MODE_DRAG = 2;

    public static final int TYPE_WALL = 0;
    public static final int TYPE_FLOOR = 1;


	@Override
	public void create () {
        stage = new Stage();
        font = new BitmapFont();
        label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
        stage.addActor(label);
        stringBuilder = new StringBuilder();

        batch = new SpriteBatch();

        exploreImage = new Texture(Gdx.files.internal("explore.png"));
        buildImage = new Texture(Gdx.files.internal("build.png"));
        dragImage = new Texture(Gdx.files.internal("pick_robot.png"));

        exploreTex = new TextureRegion(exploreImage);
        buildTex = new TextureRegion(buildImage);
        dragTex = new TextureRegion(dragImage);

        modelBatch = new ModelBatch();

        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(10f, 0f, 10f);
        cam.lookAt(((cubeX / 2) * width + padding),0,((cubeY / 2) * depth + padding));
        cam.near = 1f;
        cam.far = 300f;
        cam.update();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));

        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(new InputMultiplexer(this, camController));

        selectedType = -1;

        assets = new AssetManager();
        assets.load("floor.obj", Model.class);
        assets.load("wall.obj", Model.class);
        assets.load("robot.obj", Model.class);
        assets.load("selector.obj", Model.class);
        loading = true;
	}

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    private void doneLoading() {
        modelFloor = new ModelInstance(assets.get("floor.obj", Model.class));
        modelWall = new ModelInstance(assets.get("wall.obj", Model.class));
        modelRobot = new ModelInstance(assets.get("robot.obj", Model.class));
        modelSelector = new ModelInstance(assets.get("selector.obj", Model.class));

        System.out.println("modelRobot: "+modelRobot.toString());

        for(int z=0; z < cubeY; z++) {
            for (int x = 0; x < cubeX; x++) {
                GameObject go = new GameObject(modelWall.model);
                go.modelInstance.transform.set(new Vector3(-((cubeX / 2) * width + padding) + x * (width + padding), 0f, -((cubeY / 2) * depth + padding) + z * (depth + padding)), new Quaternion());
                instances.add(go);
            }
        }

        loading = false;
    }

    @Override
	public void render () {
        if (loading && assets.update())
            doneLoading();

        camController.update();

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(cam);

        int visibleCount = 0;
        for (final GameObject go : instances) {
            if (isVisible(cam, go)) {
                modelBatch.render(go.modelInstance, environment);
                visibleCount ++;
            }
        }
        if(modelRobot != null) {
            modelBatch.render(modelRobot, environment);
        }
        if(editMode == MODE_DRAG) {
            modelBatch.render(modelSelector, environment);
        }
        modelBatch.end();

        stringBuilder.setLength(0);
        stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
        stringBuilder.append(" Visible: ").append(visibleCount);
        label.setText(stringBuilder);
        stage.draw();

        batch.begin();
        batch.draw(editMode == MODE_BUILD ? exploreTex : buildTex, 20f, 60f);
        batch.draw(dragTex, 20f, 130f);
        batch.end();

	}


    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        modelBatch.dispose();

        modelWall.model.dispose();
        modelFloor.model.dispose();
        modelRobot.model.dispose();

        exploreImage.dispose();
        buildImage.dispose();
        dragImage.dispose();

        instances.clear();
    }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        if(screenX < 80 && screenY > stage.getHeight() - 120) {         //Edit Button
            editMode = (editMode == MODE_BUILD? MODE_EXPLORE: MODE_BUILD);
        } else if(screenX < 80 && screenY > stage.getHeight() - 180) {  //Drag Button
            editMode = MODE_DRAG;
        }

        if(editMode == MODE_BUILD) {
            setSelected(getObject(screenX, screenY));
        }

        return canCameraMove();
    }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) {
        if(editMode == MODE_DRAG) {
            int block = getObject(screenX, screenY - 160);
            if(block >= 0 && instances.get(block).type == TYPE_FLOOR) {
                modelSelector.transform = instances.get(block).modelInstance.transform;
            }
        }

        if(editMode == MODE_BUILD) {
            setSelected(getObject(screenX, screenY));
        }

        return canCameraMove();
    }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) {

        if(editMode == MODE_DRAG) {
            modelRobot.transform = modelSelector.transform;
            editMode = MODE_EXPLORE;
        }

        selectedType = -1;
        return canCameraMove();
    }

    public void setSelected (int selected) {
        if (selected < 0) return;

        if(selectedType == -1) {
            if(instances.get(selected).type == TYPE_WALL)
                selectedType = TYPE_FLOOR;
            else if(instances.get(selected).type == TYPE_FLOOR)
                selectedType = TYPE_WALL;
        }


        if (selected >= 0) {
            Matrix4 transform = instances.get(selected).modelInstance.transform;
            if(instances.get(selected).type != selectedType) {
                if (instances.get(selected).type == TYPE_WALL) {
                    instances.get(selected).modelInstance = new ModelInstance(modelFloor);
                    instances.get(selected).type = TYPE_FLOOR;
                } else if (instances.get(selected).type == TYPE_FLOOR) {
                    instances.get(selected).modelInstance = new ModelInstance(modelWall);
                    instances.get(selected).type = TYPE_WALL;
                }
            }
            instances.get(selected).modelInstance.transform = transform;
        }
    }

    public int getObject (int screenX, int screenY) {
        Ray ray = cam.getPickRay(screenX, screenY);
        int result = -1;
        float distance = -1;
        for (int i = 0; i < instances.size; ++i) {
            final GameObject instance = instances.get(i);
            instance.modelInstance.transform.getTranslation(position);
            position.add(instance.center);
            float dist2 = ray.origin.dst2(position);
            if (distance >= 0f && dist2 > distance) continue;
            if (Intersector.intersectRaySphere(ray, position, instance.radius, null)) {
                result = i;
                distance = dist2;
            }
        }
        return result;
    }

    //Return false if camera can move
    boolean canCameraMove() {
        return editMode != MODE_EXPLORE;
    }

    protected boolean isVisible(final Camera cam, final GameObject instance) {
        instance.modelInstance.transform.getTranslation(position);
        position.add(instance.center);
        return cam.frustum.sphereInFrustum(position, instance.radius);
    }


}
