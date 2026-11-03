package com.ollie.mc3d;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GameView extends GLSurfaceView {
    private MyGLRenderer renderer;
    private float lastX, lastY;

    public GameView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        renderer = new MyGLRenderer(context);
        setRenderer(renderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastX;
                float dy = event.getY() - lastY;
                
                renderer.yaw -= dx * 0.005f;
                renderer.pitch -= dy * 0.005f;
                
                if (renderer.pitch > 1.5f) renderer.pitch = 1.5f;
                if (renderer.pitch < -1.5f) renderer.pitch = -1.5f;
                
                lastX = event.getX();
                lastY = event.getY();
                break;
        }
        return true;
    }
}
