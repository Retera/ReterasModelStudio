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
	private final Vec2 tempV2 = new Vec2();
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
		boolean gridOnTop = true;
		if (ProgramGlobals.getPrefs().show2dGrid()) {
			if(gridOnTop){
				PaintBackgroundImage(g);
				drawGrid(g);
			} else {
				drawGrid(g);
				PaintBackgroundImage(g);
			}
		} else {
			PaintBackgroundImage(g);
		}

		Graphics2D graphics2d = (Graphics2D) g;
		if(modelHandler != null){
			viewportModelRenderer.drawGeosetUVs(graphics2d, coordinateSystem, modelHandler);

			viewportActivityManager.render(graphics2d, coordinateSystem, modelHandler.getRenderModel(), false);
		}
	}

	private void PaintBackgroundImage(Graphics g) {
		for (Image background : backgrounds) {
			if (isWrapImage) {
				tempV2.set(coordinateSystem.geomVN(-1,-1));
				startDrawPoint.set(tempV2);
				endDrawPoint.set(tempV2);

				tempV2.set(coordinateSystem.geomVN(1, 1));
				startDrawPoint.minimize(tempV2);
				endDrawPoint.maximize(tempV2);
				float exp = 1f;

				for (int y = (int) (startDrawPoint.y-exp); y < (int) (endDrawPoint.y+exp); y++) {
					for (int x = (int) (startDrawPoint.x-exp); x < (int) (endDrawPoint.x+exp); x++) {
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
	public void setUvLayer(int uvLayer) {
		viewportModelRenderer.setUvLayer(uvLayer);
	}

	Vec2 start = new Vec2();
	Vec2 end = new Vec2();
	private void drawImage(Graphics g, Image background, double x, double y) {
		start.set(coordinateSystem.viewV(x, y));
		end.set(coordinateSystem.viewV(x+1, y+1));

		int width  = Math.round(end.x - start.x);
		int height = Math.round(end.y - start.y);

		g.drawImage(background, (int) start.x, (int) start.y, width, height, null);
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