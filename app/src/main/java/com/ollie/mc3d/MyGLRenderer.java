package com.ollie.mc3d;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.opengl.GLUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer implements GLSurfaceView.Renderer {
    private Context context;
    private World world;
    private int program;
    private int aPosition, aNormal, aColor;
    private int uMVPMatrix, uMVMatrix, uLightPos;

    private float[] mvpMatrix = new float[16];
    private float[] viewMatrix = new float[16];
    private float[] projectionMatrix = new float[16];

    // Камера
    public float camX = 8f, camY = 18f, camZ = 8f;
    public float yaw = 0f, pitch = -0.5f;
    
    private FloatBuffer vertexBuffer;
    private int vertexCount = 0;

    public MyGLRenderer(Context context) {
        this.context = context;
        this.world = new World();
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.7f, 1.0f, 1.0f); // Небо
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Загрузка шейдеров
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, ShaderUtils.readTextFileFromRawResource(context, R.raw.vertex_shader));
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, ShaderUtils.readTextFileFromRawResource(context, R.raw.fragment_shader));
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        aPosition = GLES20.glGetAttribLocation(program, "a_Position");
        aNormal = GLES20.glGetAttribLocation(program, "a_Normal");
        aColor = GLES20.glGetAttribLocation(program, "a_Color");
        uMVPMatrix = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        uMVMatrix = GLES20.glGetUniformLocation(program, "u_MVMatrix");
        uLightPos = GLES20.glGetUniformLocation(program, "u_LightPos");

        buildMesh();
    }

    private void buildMesh() {
        ArrayList<Float> vertices = new ArrayList<>();
        for (int x = 0; x < World.SIZE; x++) {
            for (int y = 0; y < World.HEIGHT; y++) {
                for (int z = 0; z < World.SIZE; z++) {
                    if (world.blocks[x][y][z] != 0) {
                        addCube(vertices, x, y, z, world.blocks[x][y][z]);
                    }
                }
            }
        }
        vertexCount = vertices.size() / 9; // 3 pos + 3 norm + 3 col

        float[] vArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) vArray[i] = vertices.get(i);

        vertexBuffer = ByteBuffer.allocateDirect(vArray.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vArray).position(0);
    }

    private void addCube(ArrayList<Float> v, int x, int y, int z, int type) {
        float r = 0, g = 0, b = 0;
        if (type == 1) { r = 0.2f; g = 0.7f; b = 0.2f; } // Трава
        else if (type == 2) { r = 0.5f; g = 0.3f; b = 0.1f; } // Земля
        else if (type == 3) { r = 0.5f; g = 0.5f; b = 0.5f; } // Камень

        // Простое добавление куба (6 граней)
        // Передняя, задняя, левая, правая, верхняя, нижняя
        // Для экономии кода добавляем только видимые грани (упрощенно)
        addFace(v, x, y, z, 1, 0, 0, r, g, b); // Право
        addFace(v, x, y, z, -1, 0, 0, r, g, b); // Лево
        addFace(v, x, y, z, 0, 1, 0, r, g, b); // Верх
        addFace(v, x, y, z, 0, -1, 0, r, g, b); // Низ
        addFace(v, x, y, z, 0, 0, 1, r, g, b); // Перед
        addFace(v, x, y, z, 0, 0, -1, r, g, b); // Зад
    }

    private void addFace(ArrayList<Float> v, float x, float y, float z, float nx, float ny, float nz, float r, float g, float b) {
        // Упрощенная генерация граней для скорости (квадраты)
        float size = 0.5f;
        float[] verts = new float[12];
        
        if (nx != 0) { // Право/Лево
            float dx = nx * size;
            verts = new float[]{x+dx,y-size,z-size, x+dx,y-size,z+size, x+dx,y+size,z+size, x+dx,y-size,z-size, x+dx,y+size,z+size, x+dx,y+size,z-size};
        } else if (ny != 0) { // Верх/Низ
            float dy = ny * size;
            verts = new float[]{x-size,y+dy,z-size, x-size,y+dy,z+size, x+size,y+dy,z+size, x-size,y+dy,z-size, x+size,y+dy,z+size, x+size,y+dy,z-size};
        } else { // Перед/Зад
            float dz = nz * size;
            verts = new float[]{x-size,y-size,z+dz, x+size,y-size,z+dz, x+size,y+size,z+dz, x-size,y-size,z+dz, x+size,y+size,z+dz, x-size,y+size,z+dz};
        }

        for (int i = 0; i < 6; i++) {
            v.add(verts[i*3]); v.add(verts[i*3+1]); v.add(verts[i*3+2]);
            v.add(nx); v.add(ny); v.add(nz);
            v.add(r); v.add(g); v.add(b);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1, 1, 0.1f, 100f);
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Позиция камеры
        float lookX = (float) (Math.sin(yaw) * Math.cos(pitch));
        float lookY = (float) Math.sin(pitch);
        float lookZ = (float) (-Math.cos(yaw) * Math.cos(pitch));

        Matrix.setLookAtM(viewMatrix, 0, camX, camY, camZ, camX + lookX, camY + lookY, camZ + lookZ, 0f, 1f, 0f);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        GLES20.glUseProgram(program);
        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(uMVMatrix, 1, false, viewMatrix, 0);
        GLES20.glUniform3f(uLightPos, 10f, 20f, 10f);

        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(aPosition, 3, GLES20.GL_FLOAT, false, 9*4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aPosition);
        vertexBuffer.position(3);
        GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 9*4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aNormal);
        vertexBuffer.position(6);
        GLES20.glVertexAttribPointer(aColor, 3, GLES20.GL_FLOAT, false, 9*4, vertexBuffer);
        GLES20.glEnableVertexAttribArray(aColor);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
