package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.uv.UVViewportModelRenderer;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;
import java.util.ArrayList;

public class UVViewport extends ViewportView {
	private final ArrayList<Image> backgrounds = new ArrayList<>();

	private boolean isWrapImage = false;
	private final Vec2 startDrawPoint = new Vec2();
	private final Vec2 endDrawPoint = new Vec2();
	private final UVViewportModelRenderer viewportModelRenderer;

	public UVViewport(CoordDisplayListener coordDisplayListener) {
		super((byte) 0, (byte) 1, new Dimension(400, 400), coordDisplayListener);

		coordinateSystem.setYFlip(1);

		viewportModelRenderer = new UVViewportModelRenderer();
	}

	public void init() {
		coordinateSystem.setZoom(Math.min(getWidth(), getHeight()));
		coordinateSystem.setPosition(0,0);
//		coordinateSystem.translate(-.25,-.5);
		coordinateSystem.translate(-.5,-.5);
	}

	public void paintComponent(Graphics g, int vertexSize) {
		if (ProgramGlobals.getPrefs().show2dGrid()) {
			drawGrid(g);
		}

		PaintBackgroundImage(g);

		Graphics2D graphics2d = (Graphics2D) g;
		if(modelHandler != null){
			viewportModelRenderer.drawGeosetUVs(graphics2d, coordinateSystem, modelHandler);

			viewportActivity.render(graphics2d, coordinateSystem, modelHandler.getRenderModel(), false);
		}
	}

	private void PaintBackgroundImage(Graphics g) {
		for (Image background : backgrounds) {
			if (isWrapImage) {
				calcStartDrawPoint(startDrawPoint);
				calcEndDrawPoint(endDrawPoint);
				for (int y = (int) startDrawPoint.y; y < (int) endDrawPoint.y; y++) {
					for (int x = (int) startDrawPoint.x; x < (int) endDrawPoint.x; x++) {
						drawImage(g, background, x, y);
					}
				}
			} else {
				drawImage(g, background, 0, 0);
			}
		}
	}

	public UVViewport setWrapImage(boolean wrapImage) {
		isWrapImage = wrapImage;
		return this;
	}

	private Vec2 calcStartDrawPoint(Vec2 minHeap){
		int minX = (int) Math.floor(coordinateSystem.geomX(0));
		int minY = (int) Math.floor(coordinateSystem.geomY(0));

		return minHeap.set(minX, minY);
	}
	private Vec2 calcEndDrawPoint(Vec2 maxHeap){
		int maxX = (int) Math.ceil(coordinateSystem.geomX(getWidth()));
		int maxY = (int) Math.ceil(coordinateSystem.geomY(getHeight()));

		return maxHeap.set(maxX, maxY);
	}

	private void drawImage(Graphics g, Image background, double x, double y) {
		double startX = coordinateSystem.viewX(x);
		double endX = coordinateSystem.viewX(x + 1);
		double startY = coordinateSystem.viewY(y);
		double endY = coordinateSystem.viewY(y + 1);
		g.drawImage(background, (int) startX, (int) startY, (int) (endX - startX), (int) (endY - startY), null);
	}

	public void setMinimumSize(int w, int h){
		Dimension minimumSize = new Dimension(w, h);
		setMinimumSize(minimumSize);
	}

	public void setAspectRatio(double ratio) {
		coordinateSystem.setAspectRatio(ratio);
	}

	public void addBackgroundImage(final Image i) {
		backgrounds.add(i);
		double ratio = i.getWidth(null) / (double) i.getHeight(null);
		setAspectRatio(ratio);
	}

	public void clearBackgroundImage() {
		backgrounds.clear();
	}

//	public double[] getImageAdj(double ratio) {
//		double[] adj = {0, 0};
//		if (ratio < 1) {
//			adj[0] = (1 - ratio) / 2;
//			System.out.println("xAdj: " + adj[0]);
//		} else if (ratio > 1) {
//			adj[1] = (ratio - 1) / 2;
//			System.out.println("yAdj: " + adj[1]);
//		}
//		return adj;
//	}
}