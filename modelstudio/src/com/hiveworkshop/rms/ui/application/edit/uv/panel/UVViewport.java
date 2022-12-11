package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportView;
import com.hiveworkshop.rms.ui.application.edit.uv.UVViewportModelRenderer;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.BiConsumer;

public class UVViewport extends ViewportView {
	private final ArrayList<Image> backgrounds = new ArrayList<>();

	private boolean isWrapImage = false;
	private final Vec2 startDrawPoint = new Vec2();
	private final Vec2 endDrawPoint = new Vec2();
	private final UVViewportModelRenderer viewportModelRenderer;

	public UVViewport(BiConsumer<Double, Double> coordDisplayListener2) {
		super(Vec3.X_AXIS, Vec3.NEGATIVE_Y_AXIS, coordDisplayListener2);

		viewportModelRenderer = new UVViewportModelRenderer();
	}

	public void init() {
		coordinateSystem.setZoom(Math.min(getWidth(), getHeight()));
		coordinateSystem.setPosition(.5,-.5);
	}

	public void paintComponent(Graphics g, int vertexSize) {
		if (ProgramGlobals.getPrefs().show2dGrid()) {
			drawGrid(g);
		}

		PaintBackgroundImage(g);

		Graphics2D graphics2d = (Graphics2D) g;
		if(modelHandler != null){
			viewportModelRenderer.drawGeosetUVs(graphics2d, coordinateSystem, modelHandler);

			viewportActivityManager.render(graphics2d, coordinateSystem, modelHandler.getRenderModel(), false);
		}
	}

	private void PaintBackgroundImage(Graphics g) {
		for (Image background : backgrounds) {
			if (isWrapImage) {
				startDrawPoint.set(coordinateSystem.geomVN(-1,-1));
				endDrawPoint.set(coordinateSystem.geomVN(1, 1));

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

	private void drawImage(Graphics g, Image background, double x, double y) {
		Vec2 start = coordinateSystem.viewVN(x, y);
		Vec2 end = coordinateSystem.viewVN(x+1, y+1);

		double startX = (1 + start.x)/2f * getWidth();
		double startY = (1 - start.y)/2f * getHeight();

		double endX   = (1 + end.x  )/2f * getWidth();
		double endY   = (1 - end.y  )/2f * getHeight();


		int width = (int) (endX - startX);
		int height = (int) (endY - startY);

		g.drawImage(background, (int) startX, (int) startY, width, height, null);
	}

	public void setMinimumSize(int w, int h){
		Dimension minimumSize = new Dimension(w, h);
		setMinimumSize(minimumSize);
	}

	public void setAspectRatio(double ratio) {
		coordinateSystem.setImageAspectRatio(ratio);
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