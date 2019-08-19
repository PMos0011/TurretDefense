package pmos0011.biox;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES31;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePlayRenderer implements GLSurfaceView.Renderer {

    public static final float Z_DIMENSION = -1.0000001f;
    public static final float GAME_CONTROL_OBJECT_SIZE = 0.18f;

    private Context mContext;
    private Texture backgroundTextures;
    private Texture towerTextures;
    private Texture radarTexture;
    private Texture buttonsTextures;
    private Square laserSight;
    private Square leftCannonReload;
    private Square rightCannonReload;

    GameControlObjects rightArrow;
    GameControlObjects leftArrow;
    GameControlObjects leftCannonButton;
    GameControlObjects rightCannonButton;

    public static float ratio;

    private int[] staticBitmapID;
    public static float[] mProjectionMatrix = new float[16];
    private float[] mModelMatrix = new float[16];

    private float turretAngle = 0;
    private float radarAngle = 0;
    public volatile boolean rotateRight = false;
    public volatile boolean rotateLeft = false;

    public volatile boolean isLeftCannonReloading = false;
    public volatile boolean isRightCannonReloading = false;
    private volatile float leftCannonReloadStatus = 100.0f;
    private volatile float rightCannonReloadStatus = 100.0f;
    private long leftCannonLastSecond = 0;
    private long rightCannonLastSecond = 0;
    private float leftCannonPosition = 0;
    private float rightCannonPosition = 0;

    public List<SmokeEffect> smokeEffects = new ArrayList<>();

    public GamePlayRenderer(Context context) {
        mContext = context;
    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        GLES31.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES31.glEnable(GLES31.GL_BLEND);
        GLES31.glBlendFunc(GLES31.GL_SRC_ALPHA, GLES31.GL_ONE_MINUS_SRC_ALPHA);

        staticBitmapID = BitmapID.getStaticBitmapID();
        ShadersManager.loadShaders(mContext);

        backgroundTextures = new Texture(2, false);
        backgroundTextures.loadTexture(mContext, staticBitmapID[BitmapID.textureNames.BACKGROUND.getValue()]);

        towerTextures = new Texture(0.4f, false);
        for (int i = BitmapID.textureNames.TURRET_BASE.getValue(); i <= BitmapID.textureNames.TURRET_TOWER.getValue(); i++)
            towerTextures.loadTexture(mContext, staticBitmapID[i]);

        radarTexture = new Texture(0.065f, false);
        radarTexture.loadTexture(mContext, staticBitmapID[BitmapID.textureNames.RADAR.getValue()]);

        buttonsTextures = new Texture(GAME_CONTROL_OBJECT_SIZE, false);
        for (int i = BitmapID.textureNames.LEFT_ARROW.getValue(); i <= BitmapID.textureNames.RIGHT_CANNON_BUTTON.getValue(); i++)
            buttonsTextures.loadTexture(mContext, staticBitmapID[i]);

        leftArrow = new GameControlObjects();
        rightArrow = new GameControlObjects();
        leftCannonButton = new GameControlObjects();
        rightCannonButton = new GameControlObjects();

        laserSight = new Square();
        leftCannonReload = new Square();
        rightCannonReload = new Square();
    }

    public void onDrawFrame(GL10 unused) {

        GLES31.glClear(GLES31.GL_COLOR_BUFFER_BIT);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0, 0, Z_DIMENSION);
        backgroundTextures.draw(mModelMatrix, staticBitmapID[BitmapID.textureNames.BACKGROUND.getValue()], 1);
        towerTextures.draw(mModelMatrix, staticBitmapID[BitmapID.textureNames.TURRET_BASE.getValue()], 1);
        laserSight.draw(mModelMatrix, turretAngle, true);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, turretAngle, 0, 0, 1.0f);
        Matrix.translateM(mModelMatrix, 0, 0, leftCannonPosition, Z_DIMENSION);
        towerTextures.draw(mModelMatrix, staticBitmapID[BitmapID.textureNames.TURRET_L_CANNON.getValue()], 1);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, turretAngle, 0, 0, 1.0f);
        Matrix.translateM(mModelMatrix, 0, 0, rightCannonPosition, Z_DIMENSION);
        towerTextures.draw(mModelMatrix, staticBitmapID[BitmapID.textureNames.TURRET_R_CANNON.getValue()], 1);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, 0, 0, Z_DIMENSION);
        Matrix.rotateM(mModelMatrix, 0, turretAngle, 0, 0, 1.0f);
        towerTextures.draw(mModelMatrix, staticBitmapID[BitmapID.textureNames.TURRET_TOWER.getValue()], 1);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, turretAngle, 0, 0, 1.0f);
        Matrix.translateM(mModelMatrix, 0, 0.065f, -0.1f, Z_DIMENSION);
        Matrix.rotateM(mModelMatrix, 0, radarAngle, 0, 0, 1.0f);
        radarTexture.draw(mModelMatrix, staticBitmapID[BitmapID.textureNames.RADAR.getValue()], 1);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, leftArrow.xOpenGLPosition, rightArrow.yOpenGLPosition, Z_DIMENSION);
        Matrix.rotateM(mModelMatrix, 0, turretAngle, 0, 0, 1.0f);
        buttonsTextures.draw(mModelMatrix, staticBitmapID[BitmapID.textureNames.LEFT_ARROW.getValue()], 1);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, rightArrow.xOpenGLPosition, rightArrow.yOpenGLPosition, Z_DIMENSION);
        Matrix.rotateM(mModelMatrix, 0, turretAngle, 0, 0, 1.0f);
        buttonsTextures.draw(mModelMatrix, staticBitmapID[BitmapID.textureNames.RIGHT_ARROW.getValue()], 1);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, leftCannonButton.xOpenGLPosition, leftCannonButton.yOpenGLPosition, Z_DIMENSION);
        buttonsTextures.draw(mModelMatrix, staticBitmapID[BitmapID.textureNames.LEFT_CANNON_BUTTON.getValue()], 1);

        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.translateM(mModelMatrix, 0, rightCannonButton.xOpenGLPosition, rightCannonButton.yOpenGLPosition, Z_DIMENSION);
        buttonsTextures.draw(mModelMatrix, staticBitmapID[BitmapID.textureNames.RIGHT_CANNON_BUTTON.getValue()], 1);

        Iterator<SmokeEffect> smokeEffectIterator = smokeEffects.iterator();
        while (smokeEffectIterator.hasNext()) {
            SmokeEffect smoke = smokeEffectIterator.next();
            smoke.draw(staticBitmapID[BitmapID.textureNames.LEFT_ARROW.getValue()], turretAngle);
            if (smoke.visibility <= 0)
                smokeEffectIterator.remove();
        }
        gameActions();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES31.glViewport(0, 0, width, height);
        ratio = (float) width / height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1.0f, 1.0f, 1.0f, 2.0f);

        leftArrow.setObject(width, height, GAME_CONTROL_OBJECT_SIZE, -ratio + 1.2f * GAME_CONTROL_OBJECT_SIZE, -1 + GAME_CONTROL_OBJECT_SIZE);
        rightArrow.setObject(width, height, GAME_CONTROL_OBJECT_SIZE, -ratio + 3.4f * GAME_CONTROL_OBJECT_SIZE, -1 + GAME_CONTROL_OBJECT_SIZE);
        leftCannonButton.setObject(width, height, GAME_CONTROL_OBJECT_SIZE, ratio - 3.2f * GAME_CONTROL_OBJECT_SIZE, -1 + GAME_CONTROL_OBJECT_SIZE);
        rightCannonButton.setObject(width, height, GAME_CONTROL_OBJECT_SIZE, ratio - 1.1f * GAME_CONTROL_OBJECT_SIZE, -1 + GAME_CONTROL_OBJECT_SIZE);

        laserSight.setSquare();
        leftCannonReload.setSquare();
        rightCannonReload.setSquare();
    }

    private void gameActions() {

        radarAngle -= 2;
        if (radarAngle < -358)
            radarAngle = 0;

        if (rotateLeft) {
            turretAngle += 0.2f;
            if (turretAngle > 360)
                turretAngle = 0.2f;
        }
        if (rotateRight) {
            turretAngle -= 0.2f;
            if (turretAngle < 0)
                turretAngle = 359.8f;
        }

        if (isLeftCannonReloading) {
            if (leftCannonReloadStatus == 100) {
                leftCannonPosition = -0.035f;
                smokeEffects.add(new SmokeEffect(0.2f, true, SmokeEffect.effectsNames.LEFT_CANNON_SMOKE));
                smokeEffects.add(new SmokeEffect(0.2f, true, SmokeEffect.effectsNames.LEFT_CANNON_FIRE));
            }
            if (leftCannonPosition < 0.0)
                leftCannonPosition += 0.001;

            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, leftCannonButton.xOpenGLPosition, leftCannonButton.yOpenGLPosition, Z_DIMENSION);
            leftCannonReload.draw(mModelMatrix, leftCannonReloadStatus, false);

            long time = SystemClock.uptimeMillis() / 30;
            if (leftCannonLastSecond != time) {
                leftCannonReloadStatus -= 0.4f;
                leftCannonLastSecond = time;
            }

            if (leftCannonReloadStatus <= 0) {
                isLeftCannonReloading = false;
                leftCannonReloadStatus = 100;
            }
        }

        if (isRightCannonReloading) {
            if (rightCannonReloadStatus == 100) {
                rightCannonPosition = -0.035f;
                smokeEffects.add(new SmokeEffect(0.2f, true, SmokeEffect.effectsNames.RIGHT_CANNON_SMOKE));
                smokeEffects.add(new SmokeEffect(0.2f, true, SmokeEffect.effectsNames.RIGHT_CANNON_FIRE));
            }
            if (rightCannonPosition < 0.0)
                rightCannonPosition += 0.001;

            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, rightCannonButton.xOpenGLPosition, rightCannonButton.yOpenGLPosition, Z_DIMENSION);
            Matrix.rotateM(mModelMatrix, 0, 180, 0, 0, 1.0f);
            rightCannonReload.draw(mModelMatrix, rightCannonReloadStatus, false);

            long time = SystemClock.uptimeMillis() / 30;
            if (rightCannonLastSecond != time) {
                rightCannonReloadStatus -= 0.4f;
                rightCannonLastSecond = time;
            }

            if (rightCannonReloadStatus <= 0) {
                isRightCannonReloading = false;
                rightCannonReloadStatus = 100;
            }
        }


    }

}
