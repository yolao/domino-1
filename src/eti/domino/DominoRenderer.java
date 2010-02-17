package eti.domino;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

public class DominoRenderer implements GLSurfaceView.Renderer {
	private Context context;
	private Table table;
	private DominoPiece currentPiece;

	public DominoRenderer(Context context) {
		this.context = context;
		table = new Table();
		table.startGame();
	}

	private float xOnScreenToCoord(float x) {
		final float leftEdgeCoord = -1.35f;
		final float rightEdgeCoord = 1.35f;
		final float screenWidth = 320;
		return (x / screenWidth) * (rightEdgeCoord - leftEdgeCoord) + leftEdgeCoord;
	}

	private float yOnScreenToCoord(float y) {
		final float topEdgeCoord = 1.85f;
		final float bottomEdgeCoord = -1.85f;
		final float screenHeight = 430;
		return -1 * ((y / screenHeight) - 1) * (topEdgeCoord - bottomEdgeCoord) + bottomEdgeCoord;
	}

	public void touch(float xOnScreen, float yOnScreen) {
		float x = xOnScreenToCoord(xOnScreen);
		float y = yOnScreenToCoord(yOnScreen);
		for (DominoPiece piece : table.getHumanPlayerPieces()) {
			try {
				if (piece.containsPoint(x, y)) {
					activatePiece(piece);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void release(float xOnScreen, float yOnScreen) {
		float x = xOnScreenToCoord(xOnScreen);
		float y = yOnScreenToCoord(yOnScreen);
		try {
			if (currentPiece != null && !currentPiece.containsPoint(x, y)) {
				deactivateCurrentPiece();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glDisable(GL10.GL_DITHER);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);

		gl.glClearColor(.5f, .5f, .5f, 1);

		float x=-1.04f;
		for (DominoPiece piece : table.getHumanPlayerPieces()) {
			piece.show(new Position(x, -1.4f, 0));
			x += 0.3f;
			// TODO: make sure x < 1.2f
		}
	}

	public void onSurfaceChanged(GL10 gl, int w, int h) {
		gl.glViewport(0, 0, w, h);
		
        float ratio = (float) w / h;
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glFrustumf(-ratio, ratio, -1, 1, 3, 7);
	}

	public void onDrawFrame(GL10 gl) {
		clearScreen(gl);
		setupCamera(gl);
		for (DominoPiece piece : table.getHumanPlayerPieces()) {
			try {
				piece.draw(gl);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void clearScreen(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
	}

	private void setupCamera(GL10 gl) {
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
		GLU.gluLookAt(gl, 0, 0, 5, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
	}

	private void deactivateCurrentPiece() throws Exception {
		if (currentPiece != null) {
			currentPiece.deactivate();
			currentPiece = null;
		}
	}

	private void activatePiece(DominoPiece piece) throws Exception {
		deactivateCurrentPiece();
		currentPiece = piece;
		currentPiece.activate();
	}
}
