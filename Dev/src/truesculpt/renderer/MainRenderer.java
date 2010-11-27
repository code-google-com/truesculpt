/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package truesculpt.renderer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import truesculpt.managers.MeshManager;

import android.opengl.GLSurfaceView;
import android.os.SystemClock;

/**
 * Render a pair of tumbling cubes.
 */

public class MainRenderer implements GLSurfaceView.Renderer {
	private float mRot;
	private float mDistance;
	private float mElevation;

	private MeshManager mMeshManager=null;
	private ReferenceAxis mAxis= new ReferenceAxis();


	public MainRenderer(MeshManager mMeshManager) {
		super();
		this.mMeshManager = mMeshManager;		
	}

	private long mLastFrameDurationMs=0;

	public long getLastFrameDurationMs() {
		return mLastFrameDurationMs;
	}
	
	public void onPointOfViewChange(float fRot, float fDistance, float fElevation) {
		mRot = fRot;
		mDistance = fDistance;
		mElevation = fElevation;
	}
		
	@Override
	public void onDrawFrame(GL10 gl) {
		
		long tStart = SystemClock.uptimeMillis();
		/*
		 * Usually, the first thing one might want to do is to clear the screen.
		 * The most efficient way of doing this is to use glClear().
		 */

		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		/*
		 * Now we're ready to draw some 3D objects
		 */

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
				
		gl.glTranslatef(0, 0, -mDistance);
		
		gl.glRotatef(mElevation, 1, 0, 0);
		gl.glRotatef(mRot, 0, 1, 0);		
	
		//common part (normals optionnal)
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);			
		
		//only if point of view changed		
		mMeshManager.getCurrentModelView(gl);
		
		mAxis.draw(gl);
		
		//main draw call
		mMeshManager.draw(gl);
		
		long tStop = SystemClock.uptimeMillis();
		mLastFrameDurationMs=tStop-tStart;		
	}
	


	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		gl.glViewport(0, 0, width, height);

		/*
		 * Set our projection matrix. This doesn't have to be done each time we
		 * draw, but usually a new projection needs to be set when the viewport
		 * is resized.
		 */

		float ratio = (float) width / height;
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glFrustumf(-ratio, ratio, -1, 1, 1.0f, 10);
		
		mMeshManager.getCurrentProjection(gl);
		mMeshManager.getViewport(gl);
	}

	float lightAmbient[] = new float[] {0.05f, 0.05f, 0.05f, 1.0f};
	float lightDiffuse[] = new float[]	{0.5f, 0.5f, 0.5f, 1.0f};
	float lightSpecular[] = new float[] {0.7f, 0.7f, 0.7f, 1.0f};

	float[] lightPos = new float[] {5,5,10,1};
	
	float matAmbient[] = new float[] { 1,1,1,1};
	float matDiffuse[] = new float[] { 1,1,1,1};
	float matSpecular[] = new float[] {0.3f, 0.3f, 0.3f, 1.0f};
	float fShininess=25.0f;
	
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
		
		//TODO back screen color configuration in options
		gl.glClearColor(0, 0, 0, 0);	
		
	
		gl.glEnable(GL10.GL_LIGHTING);
		gl.glEnable(GL10.GL_LIGHT0);
		
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, matAmbient, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, matDiffuse, 0);		 
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, matSpecular, 0);
		gl.glMaterialf(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, fShininess );
		    
		//gl.glEnable(GL10.GL_COLOR_MATERIAL);
		
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0);	
		
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient,	0);
		gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse,	0);		
		gl.glLightfv( GL10.GL_LIGHT0,  GL10.GL_SPECULAR, lightSpecular, 0);			
		
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		
		gl.glEnable(GL10.GL_CULL_FACE);
		gl.glShadeModel(GL10.GL_SMOOTH);

	}	
}
