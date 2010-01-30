package eti.domino;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;

public class DominoRenderer implements GLSurfaceView.Renderer {
	private Context context;
	private Object3D mTriangle;
	private int mTextureID;

	public DominoRenderer(Context context) {
		this.context = context;
		mTriangle = new Rectangle();
	}

	public void touch(float x, float y) {
		// TODO
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glDisable(GL10.GL_DITHER);
		gl.glEnable(GL10.GL_CULL_FACE);

		gl.glClearColor(.5f, .5f, .5f, 1);

		mTextureID = loadTexture(gl, R.drawable.icon);
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		gl.glViewport(0, 0, w, h);
	}

	public void onDrawFrame(GL10 gl) {
		clearScreen(gl);
		setupCamera(gl);
		mTriangle.draw(gl, mTextureID);
	}

	private void clearScreen(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	}

	private void setupCamera(GL10 gl) {
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, 0, 0, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
	}

	private int loadTexture(GL10 gl, int resource) {
		int[] textures = new int[1];
		gl.glGenTextures(1, textures, 0);

		int textureId = textures[0];
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);

		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

		gl.glTexEnvf(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_REPLACE);

		InputStream is = context.getResources().openRawResource(resource);
		Bitmap bitmap;
		try {
			bitmap = BitmapFactory.decodeStream(is);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				// Ignore.
			}
		}
		GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
		bitmap.recycle();

		return textureId;
	}
}

class Object3D {
	private int drawMode = GL10.GL_TRIANGLE_STRIP;
	private int vertexCount;
	private FloatBuffer vertexBuffer;
	private FloatBuffer textureBuffer;
	private ShortBuffer indexBuffer;

	public Object3D(float[] coords, int vertexCount) {
		this.vertexCount = vertexCount;
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCount * 3 * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();

		ByteBuffer tbb = ByteBuffer.allocateDirect(vertexCount * 2 * 4);
		tbb.order(ByteOrder.nativeOrder());
		textureBuffer = tbb.asFloatBuffer();

		ByteBuffer ibb = ByteBuffer.allocateDirect(vertexCount * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();

		for (int i = 0; i < vertexCount; i++) {
			for (int j = 0; j < 3; j++) {
				vertexBuffer.put(coords[i * 3 + j]);
			}
		}

		for (int i = 0; i < vertexCount; i++) {
			for (int j = 0; j < 2; j++) {
				textureBuffer.put(coords[i * 3 + j] + 0.25f);
			}
		}

		for (int i = 0; i < vertexCount; i++) {
			indexBuffer.put((short) i);
		}

		vertexBuffer.position(0);
		textureBuffer.position(0);
		indexBuffer.position(0);
	}

	public void draw(GL10 gl, int textureId) {
		useTexture(gl, textureId);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		gl.glDrawElements(drawMode, vertexCount, GL10.GL_UNSIGNED_SHORT, indexBuffer);
	}

	private void useTexture(GL10 gl, int textureId) {
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		gl.glActiveTexture(GL10.GL_TEXTURE0);
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
		gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);

		gl.glFrontFace(GL10.GL_CCW);
	}
}

class Triangle extends Object3D {
	private static float[] coords = {
			// X, Y, Z
			-0.5f, -0.25f, 0,
			0.5f, -0.25f, 0,
			0.0f, 0.5f, 0 };
	public Triangle() {
		super(coords, 3);
	}
}

class Rectangle extends Object3D {
	private static float[] coords = {
			// X, Y, Z
			-0.5f, -0.25f, 0,
			0.5f, -0.25f, 0,
			-0.5f, 0.25f, 0,
			0.5f, 0.25f, 0};
	public Rectangle() {
		super(coords, 4);
	}
}
