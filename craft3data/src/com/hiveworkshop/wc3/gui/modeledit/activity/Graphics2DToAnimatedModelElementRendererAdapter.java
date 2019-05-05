package com.hiveworkshop.wc3.gui.modeledit.activity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.animedit.WrongModeException;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelElementRenderer;
import com.hiveworkshop.wc3.gui.modeledit.viewport.NodeIconPalette;
import com.hiveworkshop.wc3.gui.modeledit.viewport.ResettableAnimatedIdObjectRenderer;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class Graphics2DToAnimatedModelElementRendererAdapter implements ModelElementRenderer {
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private final Point recyclePointA = new Point(), recyclePointB = new Point(), recyclePointC = new Point();
	private final int[] recycleXCoords = new int[3];
	private final int[] recycleYCoords = new int[3];
	private ProgramPreferences programPreferences;
	private final int vertexSize;
	private final ResettableAnimatedIdObjectRenderer idObjectRenderer;
	private RenderModel renderModel;

	public Graphics2DToAnimatedModelElementRendererAdapter(final int vertexSize) {
		this.vertexSize = vertexSize;
		this.idObjectRenderer = new ResettableAnimatedIdObjectRenderer(vertexSize);
	}

	public Graphics2DToAnimatedModelElementRendererAdapter reset(final Graphics2D graphics,
			final CoordinateSystem coordinateSystem, final RenderModel renderModel) {
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		this.renderModel = renderModel;
		return this;
	}

	@Override
	public void renderFace(final Color borderColor, final Color color, final GeosetVertex a, final GeosetVertex b,
			final GeosetVertex c) {
		graphics.setColor(color);
		CoordinateSystem.Util.convertToPoint(coordinateSystem, a, recyclePointA, renderModel);
		CoordinateSystem.Util.convertToPoint(coordinateSystem, b, recyclePointB, renderModel);
		CoordinateSystem.Util.convertToPoint(coordinateSystem, c, recyclePointC, renderModel);
		recycleXCoords[0] = recyclePointA.x;
		recycleXCoords[1] = recyclePointB.x;
		recycleXCoords[2] = recyclePointC.x;
		recycleYCoords[0] = recyclePointA.y;
		recycleYCoords[1] = recyclePointB.y;
		recycleYCoords[2] = recyclePointC.y;
		graphics.fillPolygon(recycleXCoords, recycleYCoords, 3);
		graphics.setColor(borderColor);
		graphics.drawPolygon(recycleXCoords, recycleYCoords, 3);
	}

	@Override
	public void renderVertex(final Color color, final Vertex vertex) {
		CoordinateSystem.Util.convertToPoint(coordinateSystem, vertex, recyclePointA);
		graphics.setColor(color);
		graphics.fillRect(recyclePointA.x - vertexSize / 2, (int) (recyclePointA.y - (vertexSize / 2.0)), vertexSize,
				vertexSize);
	}

	@Override
	public void renderIdObject(final IdObject object, final NodeIconPalette nodeIconPalette, final Color lightColor,
			final Color pivotPointColor) {
		object.apply(idObjectRenderer.reset(coordinateSystem, graphics, lightColor, pivotPointColor, nodeIconPalette,
				renderModel));
	}

	@Override
	public void renderCamera(final Camera camera, final Color boxColor, final Vertex position, final Color targetColor,
			final Vertex targetPosition) {
		// TODO ANIMATION
		if (true) {
			throw new WrongModeException("not animating cameras yet, code not finished");
		}

		final Graphics2D g2 = ((Graphics2D) graphics.create());
		final Vertex ver = position;
		final Vertex targ = targetPosition;
		// final boolean verSel = selection.contains(ver);
		// final boolean tarSel = selection.contains(targ);
		final Point start = new Point(
				(int) Math.round(coordinateSystem.convertX(ver.getCoord(coordinateSystem.getPortFirstXYZ()))),
				(int) Math.round(coordinateSystem.convertY(ver.getCoord(coordinateSystem.getPortSecondXYZ()))));
		final Point end = new Point(
				(int) Math.round(coordinateSystem.convertX(targ.getCoord(coordinateSystem.getPortFirstXYZ()))),
				(int) Math.round(coordinateSystem.convertY(targ.getCoord(coordinateSystem.getPortSecondXYZ()))));
		// if (dispCameraNames) {
		// boolean changedCol = false;
		//
		// if (verSel) {
		// g2.setColor(Color.orange.darker());
		// changedCol = true;
		// }
		// g2.drawString(cam.getName(), (int)
		// Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),
		// (int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
		// if (tarSel) {
		// g2.setColor(Color.orange.darker());
		// changedCol = true;
		// } else if (verSel) {
		// g2.setColor(Color.green.darker());
		// changedCol = false;
		// }
		// g2.drawString(cam.getName() + "_target",
		// (int) Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ()))),
		// (int) Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ()))));
		// if (changedCol) {
		// g2.setColor(Color.green.darker());
		// }
		// }

		g2.translate(end.x, end.y);
		g2.rotate(-(Math.PI / 2 + Math.atan2(end.x - start.x, end.y - start.y)));
		final double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
		final int size = (int) (20 * zoom);
		final double dist = start.distance(end);

		g2.setColor(boxColor);
		g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 + vertexSize * 2);
		g2.drawRect((int) dist - size, -size, size * 2, size * 2);

		// if (tarSel) {
		// g2.setColor(Color.orange.darker());
		// } else if (verSel) {
		g2.setColor(targetColor);
		// }
		// Target
		g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 + vertexSize * 2);
		g2.drawLine(0, 0, size, size);// (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())+5)),
										// (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())+5)));
		g2.drawLine(0, 0, size, -size);// (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())-5)),
										// (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())-5)));

		// if (!verSel && tarSel) {
		// g2.setColor(Color.green.darker());
		// }
		g2.drawLine(0, 0, (int) dist, 0);
	}

}
