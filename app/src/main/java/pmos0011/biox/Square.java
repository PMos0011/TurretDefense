package pmos0011.biox;

import android.content.Context;
import android.opengl.GLES31;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Square {

    Context context;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mModelMatrixHandle;
    private int mProjectionMatrixHandle;

    public void setSquare() {
        this.radius = GamePlayRenderer.ratio * 0.85f;
    }

    float radius;

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer colorBuffer;

    private final int COORDS_PER_VERTEX = 2;
    private final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4;
    float squareCoords[] = new float[8];

    short drawOrder[] = {
            0, 1, 2,
            0, 2, 3
    };

    private final int COORDS_PER_COLOR = 4;
    private final int COLOR_STRIDE = COORDS_PER_COLOR * 4;
    float squareColors[] = {
            1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f, 0.5f,
            1.0f, 0.0f, 0.0f, 0.5f
    };

    public Square(Context context) {

        this.context = context;
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        ByteBuffer cb = ByteBuffer.allocateDirect(squareColors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(squareColors);
        colorBuffer.position(0);

        int vertexShader = FileReader.reader(context, GLES31.GL_VERTEX_SHADER, R.raw.square_vertex_shader);
        int fragmentShader = FileReader.reader(context, GLES31.GL_FRAGMENT_SHADER, R.raw.square_fragment_shader);

        mProgram = GLES31.glCreateProgram();

        GLES31.glAttachShader(mProgram, vertexShader);
        GLES31.glAttachShader(mProgram, fragmentShader);
        GLES31.glLinkProgram(mProgram);
    }

    public void draw(float[] mModelMatrix, float param, boolean isLaser) {

        if (isLaser) {
            int quarter = 0;

            while (param >= 90) {
                param -= 90;
                quarter++;
            }
            double radians = Math.toRadians(param);
            float delta;

            float centerXPos;
            float centerYPos;

            if (param <= 45)
                delta = (float) (0.015f * Math.cos(radians));
            else
                delta = (float) (0.015f * Math.sin(radians));

            switch (quarter) {

                case 0:
                    centerXPos = (float) (radius * Math.sin(radians)) * -1.0f;
                    centerYPos = (float) (radius * Math.cos(radians));
                    squareCoords[0] = centerXPos + delta;
                    squareCoords[2] = centerXPos - delta;
                    squareCoords[1] = centerYPos + delta;
                    squareCoords[3] = centerYPos - delta;
                    break;

                case 1:
                    centerXPos = (float) (radius * Math.cos(radians)) * -1.0f;
                    centerYPos = (float) (radius * Math.sin(radians)) * -1.0f;
                    squareCoords[0] = centerXPos - delta;
                    squareCoords[2] = centerXPos + delta;
                    squareCoords[1] = centerYPos + delta;
                    squareCoords[3] = centerYPos - delta;
                    break;

                case 2:
                    centerXPos = (float) (radius * Math.sin(radians));
                    centerYPos = (float) (radius * Math.cos(radians)) * -1.0f;
                    squareCoords[0] = centerXPos + delta;
                    squareCoords[2] = centerXPos - delta;
                    squareCoords[1] = centerYPos + delta;
                    squareCoords[3] = centerYPos - delta;
                    break;

                case 3:
                    centerXPos = (float) (radius * Math.cos(radians));
                    centerYPos = (float) (radius * Math.sin(radians));
                    squareCoords[0] = centerXPos - delta;
                    squareCoords[2] = centerXPos + delta;
                    squareCoords[1] = centerYPos + delta;
                    squareCoords[3] = centerYPos - delta;
                    break;
            }
        } else {
            float coords = GamePlayRenderer.GAME_CONTROL_OBJECT_SIZE;

            float statusModifer = param / 100.0f;
            float coordMod = coords - 2 * coords * statusModifer;

            float tmpCoords[] = {
                    coordMod, coords,
                    coordMod, -coords,
                    coords, -coords,
                    coords, coords,
            };
            squareCoords = tmpCoords;
        }
        openGLProgram(mModelMatrix);
    }

    private void openGLProgram(float[] mModelMatrix) {

        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        GLES31.glUseProgram(mProgram);
        mPositionHandle = GLES31.glGetAttribLocation(mProgram, "a_Position");
        mColorHandle = GLES31.glGetAttribLocation(mProgram, "a_Color");
        mModelMatrixHandle = GLES31.glGetUniformLocation(mProgram, "u_mModelMatrix");
        mProjectionMatrixHandle = GLES31.glGetUniformLocation(mProgram, "u_mProjectionMatrix");

        GLES31.glUniformMatrix4fv(mModelMatrixHandle, 1, false, mModelMatrix, 0);
        GLES31.glUniformMatrix4fv(mProjectionMatrixHandle, 1, false, GamePlayRenderer.mProjectionMatrix, 0);

        GLES31.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES31.GL_FLOAT, false, VERTEX_STRIDE, vertexBuffer);
        GLES31.glEnableVertexAttribArray(mPositionHandle);

        GLES31.glVertexAttribPointer(mColorHandle, COORDS_PER_COLOR, GLES31.GL_FLOAT, false, COLOR_STRIDE, colorBuffer);
        GLES31.glEnableVertexAttribArray(mColorHandle);

        GLES31.glDrawElements(GLES31.GL_TRIANGLES, drawOrder.length, GLES31.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES31.glDisableVertexAttribArray(mPositionHandle);
        GLES31.glDisableVertexAttribArray(mColorHandle);
    }
}
