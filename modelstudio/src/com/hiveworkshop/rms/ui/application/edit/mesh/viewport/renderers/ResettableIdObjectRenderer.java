package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderNode;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportRenderableCamera;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.List;

public final class ResettableIdObjectRenderer {
    private int vertexSize;
    private final ViewportRenderableCamera renderableCameraProp = new ViewportRenderableCamera();
    private CoordinateSystem coordinateSystem;
    private Graphics2D graphics;
	private NodeIconPalette nodeIconPalette;
    private RenderModel renderModel;
    private boolean crosshairIsBox;
    boolean isAnimated;

	Vec2 minCoord = new Vec2();
	Vec2 maxCoord = new Vec2();
	Vec3 vertexHeap1 = new Vec3();
	Vec3 vertexHeap2 = new Vec3();
	Vec2 coordTemp = new Vec2();

	Vec2 vertSize = new Vec2();
	Vec2 ovalStart = new Vec2();
	Vec2 outerMin = new Vec2();
	Vec2 outerMax = new Vec2();

    public ResettableIdObjectRenderer(int vertexSize) {
        this.vertexSize = vertexSize;
    }

    public ResettableIdObjectRenderer reset(CoordinateSystem coordinateSystem, Graphics2D graphics,
                                            RenderModel renderModel, boolean isAnimated) {
        this.isAnimated = isAnimated;
        this.coordinateSystem = coordinateSystem;
        this.graphics = graphics;
        this.renderModel = renderModel;
        this.crosshairIsBox = ProgramGlobals.getPrefs().isUseBoxesForPivotPoints();
        this.vertexSize = ProgramGlobals.getPrefs().getVertexSize();
        return this;
    }

    public ResettableIdObjectRenderer reset(CoordinateSystem coordinateSystem, Graphics2D graphics,
                                            RenderModel renderModel, boolean isAnimated, boolean isHighLighted) {
        this.isAnimated = isAnimated;
        this.coordinateSystem = coordinateSystem;
        this.graphics = graphics;
        this.nodeIconPalette = isHighLighted ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED;
        this.renderModel = renderModel;
        this.crosshairIsBox = ProgramGlobals.getPrefs().isUseBoxesForPivotPoints();
        return this;
    }

    public ResettableIdObjectRenderer renderObject(boolean isHighLighted, boolean isSelected, IdObject object) {
	    Color color = getColor(isHighLighted, isSelected, object);

	    this.nodeIconPalette = isHighLighted ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED;
	    this.nodeIconPalette = isSelected ? NodeIconPalette.SELECTED : this.nodeIconPalette;

        this.crosshairIsBox = ProgramGlobals.getPrefs().isUseBoxesForPivotPoints();
        renderIdObject(object, color);
        return this;
    }

	private Color getColor(boolean isHighLighted, boolean isSelected, IdObject object) {
		Color color;
		if (object instanceof Light) {
		    color = ProgramGlobals.getPrefs().getLightsColor();
		} else {
			color = ProgramGlobals.getPrefs().getPivotPointsColor();
		    color = isAnimated ? ProgramGlobals.getPrefs().getAnimatedBoneUnselectedColor() : color;
		}
		color = isSelected ? ProgramGlobals.getPrefs().getSelectColor() : color;
		color = isHighLighted ? ProgramGlobals.getPrefs().getHighlighVertexColor() : color;
		return color;
	}


	public void renderIdObject(IdObject object, Color daColor) {
        graphics.setColor(daColor);
        if (object instanceof Helper || object instanceof Bone) {
	        drawCrossHair(graphics, getTransformedCoord(object, object.getPivotPoint()), vertexSize);
        } else if (object instanceof CollisionShape) {
            drawCollisionShape(graphics, vertexSize, (CollisionShape) object);
            drawNodeImage(graphics, object, nodeIconPalette.getObjectImage(object));
        } else if (object instanceof Light) {
            drawNodeImage(graphics, object, nodeIconPalette.getObjectImage(object));
            drawLight((Light) object);
        } else {
            drawNodeImage(graphics, object, nodeIconPalette.getObjectImage(object));
        }
    }

    public void drawNodeImage(Graphics2D graphics,
                              IdObject attachment,
                              Image nodeImage) {
		coordTemp.set(getTransformedCoord(attachment, attachment.getPivotPoint()));

        Vec2 imageSize = new Vec2(nodeImage.getWidth(null), nodeImage.getHeight(null));
	    coordTemp.addScaled(imageSize, -.5f);

        graphics.drawImage(nodeImage, (int) coordTemp.x, (int) coordTemp.y, (int) imageSize.x, (int) imageSize.y, null);
    }

    public void drawCollisionShape(Graphics2D graphics,
                                   int vertexSize,
                                   CollisionShape collisionShape) {
	    List<Vec3> vertices = collisionShape.getVertices();

	    vertexHeap1.set(getTransformedCoord2(collisionShape, vertices.get(0)));
	    vertexHeap2.set(getTransformedCoord2(collisionShape, 1 < vertices.size() ? vertices.get(1) : Vec3.ZERO));

        if (collisionShape.getType() == MdlxCollisionShape.Type.BOX) {
	        Vec2 tempCoord1 = coordinateSystem.viewV(vertexHeap1);
	        minCoord.set(tempCoord1);
	        maxCoord.set(tempCoord1);

	        Vec2 tempCoord2 = coordinateSystem.viewV(vertexHeap2);
	        minCoord.minimize(tempCoord2);
	        maxCoord.maximize(tempCoord2);

	        maxCoord.sub(minCoord);
	        graphics.drawRoundRect((int) minCoord.x, (int) minCoord.y, (int) maxCoord.x, (int) maxCoord.y, vertexSize, vertexSize);

	        maxCoord.add(minCoord);
	        drawCrossHair(graphics, minCoord, vertexSize);
	        drawCrossHair(graphics, maxCoord, vertexSize);
        } else if (collisionShape.getType() == MdlxCollisionShape.Type.CYLINDER || collisionShape.getType() == MdlxCollisionShape.Type.SPHERE) {
	        Vec2 coord = coordinateSystem.viewV(vertexHeap1);

            double boundsRadius = collisionShape.getBoundsRadius() * coordinateSystem.getZoom();
            graphics.drawOval((int) (coord.x - boundsRadius), (int) (coord.y - boundsRadius), (int) (boundsRadius * 2), (int) (boundsRadius * 2));
	        drawCrossHair(graphics, coord, vertexSize);
        } else {
	        for (Vec3 vertex : vertices) {
		        drawCrossHair(graphics, getTransformedCoord(collisionShape, vertex), vertexSize);
	        }
        }
    }
	private void drawCrossHair(Graphics2D graphics, Vec2 coord, int vertexSize) {
		vertSize.set(vertexSize, vertexSize);

		ovalStart.set(coord).sub(vertSize);
		outerMin.set(coord).addScaled(vertSize, -1.5f);
		outerMax.set(coord).addScaled(vertSize,1.5f);

		if (crosshairIsBox) {
			vertexSize *= 3;
			graphics.fillRect((int) ovalStart.x, (int) ovalStart.y, vertexSize * 2, vertexSize * 2);
		} else {
			graphics.drawOval((int) ovalStart.x, (int) ovalStart.y, vertexSize * 2, vertexSize * 2);
			graphics.drawLine((int) outerMin.x, (int) coord.y, (int) outerMax.x, (int) coord.y);
			graphics.drawLine((int) coord.x, (int) outerMin.y, (int) coord.x, (int) outerMax.y);
		}
	}

	Vec3 vertexHeap = new Vec3();
	public Vec3 getTransformedCoord2(AnimatedNode object, Vec3 point) {
		vertexHeap.set(point);
		if(isAnimated && renderModel != null){
			RenderNode<AnimatedNode> renderNode = renderModel.getRenderNode(object);
			if(renderNode != null){
				vertexHeap.transform(renderNode.getWorldMatrix());

			}
		}
		return vertexHeap;
	}

	public Vec2 getTransformedCoord(AnimatedNode object, Vec3 point){
		vertexHeap.set(point);

		if(isAnimated && renderModel != null){
			RenderNode<AnimatedNode> renderNode = renderModel.getRenderNode(object);
			if(renderNode != null){
				vertexHeap.transform(renderNode.getWorldMatrix());

			}
		}
		return coordinateSystem.viewV(vertexHeap);
	}

	public void drawLight(Light object) {
		Vec2 vec2 = getTransformedCoord(object, object.getPivotPoint());
		int xCoord = (int) vec2.x;
        int yCoord = (int) vec2.y;
        double zoom = coordinateSystem.getZoom();

        int attenuationStart = (int) (object.getAttenuationStart() * zoom);
        if (attenuationStart > 0) {
            graphics.drawOval(xCoord - attenuationStart, yCoord - attenuationStart, attenuationStart * 2, attenuationStart * 2);
        }
        int attenuationEnd = (int) (object.getAttenuationEnd() * zoom);
        if (attenuationEnd > 0) {
            graphics.drawOval(xCoord - attenuationEnd, yCoord - attenuationEnd, attenuationEnd * 2, attenuationEnd * 2);
        }
    }

    public void camera(Camera camera) {
        graphics.setColor(Color.GREEN.darker());
        Graphics2D g2 = ((Graphics2D) graphics.create());

	    vertexHeap1.set(getTransformedCoord2(camera.getSourceNode(), camera.getPosition()));
	    vertexHeap2.set(getTransformedCoord2(camera.getTargetNode(), camera.getTargetPosition()));


	    float renderRotationScalar = 0;
	    if (renderModel != null && renderModel.getTimeEnvironment() != null) {
		    renderRotationScalar = camera.getSourceNode().getRenderRotationScalar(renderModel.getTimeEnvironment());
	    }

	    renderableCameraProp.render(g2, coordinateSystem, vertexHeap1, vertexHeap2, renderRotationScalar);
    }
}