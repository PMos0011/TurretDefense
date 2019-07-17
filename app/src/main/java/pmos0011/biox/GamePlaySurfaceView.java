package pmos0011.biox;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class GamePlaySurfaceView extends GLSurfaceView {

    private final GamePlayRenderer renderer;

    public GamePlaySurfaceView(Context context) {
        super(context);

        setEGLContextClientVersion(3);

        renderer = new GamePlayRenderer(context);

        setRenderer(renderer);
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}