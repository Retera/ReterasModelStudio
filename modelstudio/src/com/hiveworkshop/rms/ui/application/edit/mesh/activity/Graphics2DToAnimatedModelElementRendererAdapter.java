package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelElementRenderer;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableAnimatedIdObjectRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public final class Graphics2DToAnimatedModelElementRendererAdapter implements ModelElementRenderer {
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private ProgramPreferences programPreferences;
	private int vertexSize;
	private ResettableAnimatedIdObjectRenderer idObjectRenderer;
	private RenderModel renderModel;

	public Graphics2DToAnimatedModelElementRendererAdapter(final int vertexSize) {
		this.vertexSize = vertexSize;
		idObjectRenderer = new ResettableAnimatedIdObjectRenderer(vertexSize);
	}

	public Graphics2DToAnimatedModelElementRendererAdapter reset(Graphics2D graphics, CoordinateSystem coordinateSystem, RenderModel renderModel, ProgramPreferences preferences) {
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		this.renderModel = renderModel;
		programPreferences = preferences;
		return this;
	}

	@Override
	public void renderFace(Color borderColor, Color color,
	                       GeosetVertex a, GeosetVertex b, GeosetVertex c) {
		graphics.setColor(color);

		Point pointA = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, a, renderModel);
		Point pointB = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, b, renderModel);
		Point pointC = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, c, renderModel);

		GU.fillPolygon(graphics, pointA, pointB, pointC);
		graphics.setColor(borderColor);
		GU.drawPolygon(graphics, pointA, pointB, pointC);
	}

	@Override
	public void renderVertex(Color color, Vec3 vertex) {
		Point point = CoordinateSystem.Util.convertToViewPoint(coordinateSystem, vertex);
		graphics.setColor(color);
		GU.fillCenteredSquare(graphics, point, vertexSize);
//		graphics.fillRect(point.x - (vertexSize / 2), (int) (point.y - (vertexSize / 2.0)), vertexSize, vertexSize);
	}

	@Override
	public void renderIdObject(IdObject object, NodeIconPalette nodeIconPalette, Color lightColor, Color pivotPointColor) {
		object.apply(idObjectRenderer.reset(coordinateSystem, graphics, lightColor, pivotPointColor, nodeIconPalette, renderModel, programPreferences.isUseBoxesForPivotPoints()));
	}

	@Override
	public void renderCamera(Camera camera, Color boxColor, Vec3 position, Color targetColor, Vec3 targetPosition) {
		// TODO ANIMATION
		if (true) {
			throw new WrongModeException("not animating cameras yet, code not finished");
		}

		Graphics2D g2 = ((Graphics2D) graphics.create());
		Vec3 ver = position;
		Vec3 targ = targetPosition;
		// boolean verSel = selection.contains(ver);
		// boolean tarSel = selection.contains(targ);
		Point start = new Point(
				(int) Math.round(coordinateSystem.viewX(ver.getCoord(coordinateSystem.getPortFirstXYZ()))),
				(int) Math.round(coordinateSystem.viewY(ver.getCoord(coordinateSystem.getPortSecondXYZ()))));
		Point end = new Point(
				(int) Math.round(coordinateSystem.viewX(targ.getCoord(coordinateSystem.getPortFirstXYZ()))),
				(int) Math.round(coordinateSystem.viewY(targ.getCoord(coordinateSystem.getPortSecondXYZ()))));
		// if (dispCameraNames) {
		// boolean changedCol = false;
		//
		// if (verSel) {
		// g2.setColor(Color.orange.darker());
		// changedCol = true;}
		// g2.drawString(cam.getName(), (int)
		// Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),
		// (int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
		// if (tarSel) {
		// g2.setColor(Color.orange.darker());
		// changedCol = true;} else if (verSel) {
		// g2.setColor(Color.green.darker());
		// changedCol = false; }
		// g2.drawString(cam.getName() + "_target",
		// (int) Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ()))),
		// (int) Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ()))));
		// if (changedCol) {g2.setColor(Color.green.darker());}}

		g2.translate(end.x, end.y);
		g2.rotate(-((Math.PI / 2) + Math.atan2(end.x - start.x, end.y - start.y)));
		double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
		int size = (int) (20 * zoom);
		double dist = start.distance(end);

		g2.setColor(boxColor);
		g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
		g2.drawRect((int) dist - size, -size, size * 2, size * 2);

		g2.setColor(targetColor);

		g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
		g2.drawLine(0, 0, size, size);
		g2.drawLine(0, 0, size, -size);

		g2.drawLine(0, 0, (int) dist, 0);
	}

}
