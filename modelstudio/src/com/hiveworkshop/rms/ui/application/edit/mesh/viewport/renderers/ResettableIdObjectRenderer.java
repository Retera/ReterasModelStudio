package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportRenderableCamera;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.List;

public final class ResettableIdObjectRenderer {
    private int vertexSize;
    private final ViewportRenderableCamera renderableCameraProp = new ViewportRenderableCamera();
    private CoordinateSystem coordinateSystem;
    private Graphics2D graphics;
    private Color lightColor;
    private Color daColor;
    private Color pivotPointColor;
    private NodeIconPalette nodeIconPalette;
    private RenderModel renderModel;
    private boolean crosshairIsBox;
    boolean isAnimated;

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
        this.lightColor = isHighLighted ? ProgramGlobals.getPrefs().getHighlighVertexColor() : ProgramGlobals.getPrefs().getLightsColor();
        Color pivotPointColor1 = isAnimated ? ProgramGlobals.getPrefs().getAnimatedBoneUnselectedColor() : ProgramGlobals.getPrefs().getPivotPointsColor();
        this.pivotPointColor = isHighLighted ? ProgramGlobals.getPrefs().getHighlighVertexColor() : pivotPointColor1;
        this.nodeIconPalette = isHighLighted ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED;
        this.renderModel = renderModel;
        this.crosshairIsBox = ProgramGlobals.getPrefs().isUseBoxesForPivotPoints();
        return this;
    }

    public ResettableIdObjectRenderer renderObject(CoordinateSystem coordinateSystem, Graphics2D graphics,
                                                   RenderModel renderModel, boolean isAnimated, boolean isHighLighted, IdObject object) {
        this.isAnimated = isAnimated;
        this.coordinateSystem = coordinateSystem;
        this.graphics = graphics;
        this.lightColor = isHighLighted ? ProgramGlobals.getPrefs().getHighlighVertexColor() : ProgramGlobals.getPrefs().getLightsColor();
        Color pivotPointColor1 = isAnimated ? ProgramGlobals.getPrefs().getAnimatedBoneUnselectedColor() : ProgramGlobals.getPrefs().getPivotPointsColor();
        this.pivotPointColor = isHighLighted ? ProgramGlobals.getPrefs().getHighlighVertexColor() : pivotPointColor1;
        this.nodeIconPalette = isHighLighted ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED;
        this.renderModel = renderModel;
        this.crosshairIsBox = ProgramGlobals.getPrefs().isUseBoxesForPivotPoints();
        renderIdObject(object);
        return this;
    }

    public ResettableIdObjectRenderer renderObject(boolean isHighLighted, IdObject object) {
        this.lightColor = isHighLighted ? ProgramGlobals.getPrefs().getHighlighVertexColor() : ProgramGlobals.getPrefs().getLightsColor();
        Color pivotPointColor1 = isAnimated ? ProgramGlobals.getPrefs().getAnimatedBoneUnselectedColor() : ProgramGlobals.getPrefs().getPivotPointsColor();
        this.pivotPointColor = isHighLighted ? ProgramGlobals.getPrefs().getHighlighVertexColor() : pivotPointColor1;
        this.nodeIconPalette = isHighLighted ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED;
        this.crosshairIsBox = ProgramGlobals.getPrefs().isUseBoxesForPivotPoints();
        renderIdObject(object);
        return this;
    }

    public ResettableIdObjectRenderer renderObject(boolean isHighLighted, boolean isSelected, IdObject object) {
        daColor = Color.CYAN;
        if (object instanceof Light) {
            daColor = isSelected ? ProgramGlobals.getPrefs().getSelectColor() : ProgramGlobals.getPrefs().getLightsColor();
            daColor = isHighLighted ? ProgramGlobals.getPrefs().getHighlighVertexColor() : daColor;

        } else {
            daColor = isAnimated ? ProgramGlobals.getPrefs().getAnimatedBoneUnselectedColor() : ProgramGlobals.getPrefs().getPivotPointsColor();
            daColor = isSelected ? ProgramGlobals.getPrefs().getSelectColor() : daColor;
            daColor = isHighLighted ? ProgramGlobals.getPrefs().getHighlighVertexColor() : daColor;
        }
//        Color pivotPointColor1 = isAnimated ? ProgramGlobals.getPrefs().getAnimatedBoneUnselectedColor() : ProgramGlobals.getPrefs().getPivotPointsColor();
//        this.pivotPointColor = isHighLighted ? ProgramGlobals.getPrefs().getHighlighVertexColor() : pivotPointColor1;

        this.nodeIconPalette = isHighLighted ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED;
        this.nodeIconPalette = isSelected ? this.nodeIconPalette : NodeIconPalette.SELECTED;

        this.crosshairIsBox = ProgramGlobals.getPrefs().isUseBoxesForPivotPoints();
        renderIdObject1(object);
        return this;
    }

    public void renderIdObject(IdObject object) {
        if (object instanceof Helper) {
            graphics.setColor(pivotPointColor.darker());
            drawCrosshair(graphics, coordinateSystem, vertexSize, object.getPivotPoint(), getWorldMatrix(object), crosshairIsBox);
        } else if (object instanceof Bone) {
            graphics.setColor(pivotPointColor);
            drawCrosshair(graphics, coordinateSystem, vertexSize, object.getPivotPoint(), getWorldMatrix(object), crosshairIsBox);
        } else if (object instanceof CollisionShape) {
            graphics.setColor(pivotPointColor);
            drawCollisionShape(graphics, coordinateSystem, vertexSize, (CollisionShape) object, getWorldMatrix(object), crosshairIsBox);
            drawNodeImage(graphics, coordinateSystem, object, nodeIconPalette.getObjectImage(object), getWorldMatrix(object));
        } else if (object instanceof Light) {
            graphics.setColor(lightColor);
            drawNodeImage(graphics, coordinateSystem, object, nodeIconPalette.getObjectImage(object), getWorldMatrix(object));
            light((Light) object);
        } else {
            drawNodeImage(graphics, coordinateSystem, object, nodeIconPalette.getObjectImage(object), getWorldMatrix(object));
        }
    }

    public void renderIdObject1(IdObject object) {
        graphics.setColor(daColor);
        if (object instanceof Helper) {
//            graphics.setColor(pivotPointColor.darker());
            drawCrosshair(graphics, coordinateSystem, vertexSize, object.getPivotPoint(), getWorldMatrix(object), crosshairIsBox);
        } else if (object instanceof Bone) {
//            graphics.setColor(pivotPointColor);
            drawCrosshair(graphics, coordinateSystem, vertexSize, object.getPivotPoint(), getWorldMatrix(object), crosshairIsBox);
        } else if (object instanceof CollisionShape) {
//            graphics.setColor(pivotPointColor);
            drawCollisionShape(graphics, coordinateSystem, vertexSize, (CollisionShape) object, getWorldMatrix(object), crosshairIsBox);
            drawNodeImage(graphics, coordinateSystem, object, nodeIconPalette.getObjectImage(object), getWorldMatrix(object));
        } else if (object instanceof Light) {
//            graphics.setColor(lightColor);
            drawNodeImage(graphics, coordinateSystem, object, nodeIconPalette.getObjectImage(object), getWorldMatrix(object));
            light((Light) object);
        } else {
            drawNodeImage(graphics, coordinateSystem, object, nodeIconPalette.getObjectImage(object), getWorldMatrix(object));
        }
    }

    public void drawNodeImage(Graphics2D graphics,
                              CoordinateSystem coordinateSystem,
                              IdObject attachment,
                              Image nodeImage,
                              Mat4 worldMatrix) {

        Vec3 vertexHeap = new Vec3(attachment.getPivotPoint());
        if (worldMatrix != null) {
            vertexHeap.transform(worldMatrix);
        }
        Vec2 coord = CoordSysUtils.convertToViewVec2(coordinateSystem, vertexHeap);

        Vec2 imageSize = new Vec2(nodeImage.getWidth(null), nodeImage.getHeight(null));
        coord.sub(imageSize.getScaled(.5f));

        graphics.drawImage(nodeImage, (int) coord.x, (int) coord.y, (int) imageSize.x, (int) imageSize.y, null);
    }

    public void drawCollisionShape(Graphics2D graphics,
                                   CoordinateSystem coordinateSystem,
                                   int vertexSize,
                                   CollisionShape collisionShape,
                                   Mat4 worldMatrix, boolean crosshairIsBox) {
        List<Vec3> vertices = collisionShape.getVertices();

        if (collisionShape.getType() == MdlxCollisionShape.Type.BOX) {
            if (vertices.size() > 1) {
                Vec3 vertexHeap1 = new Vec3(vertices.get(0));
                Vec3 vertexHeap2 = new Vec3(vertices.get(1));
                if (worldMatrix != null) {
                    vertexHeap1.transform(worldMatrix);
                    vertexHeap2.transform(worldMatrix);
                }

                Vec2 firstCoord = CoordSysUtils.convertToViewVec2(coordinateSystem, vertexHeap1);
                Vec2 secondCoord = CoordSysUtils.convertToViewVec2(coordinateSystem, vertexHeap2);

                Vec2 minCoord = new Vec2(firstCoord).minimize(secondCoord);
                Vec2 maxCoord = new Vec2(firstCoord).maximize(secondCoord);

                Vec2 diff = Vec2.getDif(maxCoord, minCoord);

                graphics.drawRoundRect((int) minCoord.x, (int) minCoord.y, (int) diff.x, (int) diff.y, vertexSize, vertexSize);
            }
        } else if (collisionShape.getExtents() != null) {
            Vec3 vertexHeap = new Vec3(collisionShape.getPivotPoint());
            if (worldMatrix != null) {
                vertexHeap.transform(worldMatrix);
            }
            Vec2 coord = CoordSysUtils.convertToViewVec2(coordinateSystem, vertexHeap);

            double boundsRadius = collisionShape.getExtents().getBoundsRadius() * coordinateSystem.getZoom();
            graphics.drawOval((int) (coord.x - boundsRadius), (int) (coord.y - boundsRadius), (int) (boundsRadius * 2), (int) (boundsRadius * 2));
        }

        for (Vec3 vertex : vertices) {
            drawCrosshair(graphics, coordinateSystem, vertexSize, vertex, worldMatrix, crosshairIsBox);
        }
    }


    public void drawCrosshair(Graphics2D graphics, CoordinateSystem coordinateSystem, int vertexSize, Vec3 pivotPoint, Mat4 worldMatrix, boolean crosshairIsBox) {
        Vec3 vertexHeap = new Vec3(pivotPoint);
        if (worldMatrix != null) {
            vertexHeap.transform(worldMatrix);
        }
        Vec2 coord = CoordSysUtils.convertToViewVec2(coordinateSystem, vertexHeap);

        Vec2 vertSize = new Vec2(vertexSize, vertexSize);

        Vec2 ovalStart = Vec2.getDif(coord, vertSize);

        Vec2 outerMin = Vec2.getDif(coord, vertSize.getScaled(1.5f));
        Vec2 outerMax = Vec2.getSum(coord, vertSize.getScaled(1.5f));

        if (crosshairIsBox) {
            vertexSize *= 3;
            graphics.fillRect((int) ovalStart.x, (int) ovalStart.y, vertexSize * 2, vertexSize * 2);
        } else {
            graphics.drawOval((int) ovalStart.x, (int) ovalStart.y, vertexSize * 2, vertexSize * 2);
            graphics.drawLine((int) outerMin.x, (int) coord.y, (int) outerMax.x, (int) coord.y);
            graphics.drawLine((int) coord.x, (int) outerMin.y, (int) coord.x, (int) outerMax.y);
        }
    }

    public Mat4 getWorldMatrix(AnimatedNode object) {
        if (!isAnimated || renderModel == null || renderModel.getRenderNode(object) == null) {
            return null;
        }
        return renderModel.getRenderNode(object).getWorldMatrix();
    }

    public void light(Light object) {
        Vec3 vertexHeap = new Vec3(object.getPivotPoint());
        Mat4 worldMatrix = getWorldMatrix(object);
        if (worldMatrix != null) {
            vertexHeap.transform(worldMatrix);
        }
        int xCoord = (int) coordinateSystem.viewX(vertexHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
        int yCoord = (int) coordinateSystem.viewY(vertexHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
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

        Vec3 vec3Start = new Vec3(camera.getPosition());
        Mat4 worldMatrix = getWorldMatrix(camera.getSourceNode());
        if (worldMatrix != null) {
            vec3Start.transform(worldMatrix);
        }

        Vec3 vec3End = new Vec3(camera.getTargetPosition());
        worldMatrix = getWorldMatrix(camera.getTargetNode());
        if (worldMatrix != null) {
            vec3Start.transform(worldMatrix);
        }

        float renderRotationScalar = 0;
        if (renderModel != null && renderModel.getAnimatedRenderEnvironment() != null) {
            renderRotationScalar = camera.getSourceNode().getRenderRotationScalar(renderModel.getAnimatedRenderEnvironment());
        }

        renderableCameraProp.render(g2, coordinateSystem, vec3Start, vec3End, renderRotationScalar);

//		Point start = CoordSysUtils.convertToViewPoint(coordinateSystem, position);
//		Point end = CoordSysUtils.convertToViewPoint(coordinateSystem, targetPosition);
//
//		g2.translate(end.x, end.y);
//		g2.rotate(-((Math.PI / 2) + Math.atan2(end.x - start.x, end.y - start.y)));
//		double zoom = CoordSysUtils.getZoom(coordinateSystem);
//		int size = (int) (20 * zoom);
//		double dist = start.distance(end);
//
//		g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
//		g2.drawRect((int) dist - size, -size, size * 2, size * 2);
//
//		g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
//		g2.drawLine(0, 0, size, size);
//		g2.drawLine(0, 0, size, -size);
//
//		g2.drawLine(0, 0, (int) dist, 0);
    }

    public void renderCamera(Camera camera, Graphics2D graphics, RenderModel renderModel, CoordinateSystem coordinateSystem) {
        graphics.setColor(Color.GREEN.darker());
        Graphics2D g2 = ((Graphics2D) graphics.create());

        Vec3 vec3Start = new Vec3(camera.getPosition());
        Mat4 worldMatrix = getWorldMatrix(camera.getSourceNode());
        if (worldMatrix != null) {
            vec3Start.transform(worldMatrix);
        }

        Vec3 vec3End = new Vec3(camera.getTargetPosition());
        worldMatrix = getWorldMatrix(camera.getTargetNode());
        if (worldMatrix != null) {
            vec3Start.transform(worldMatrix);
        }

        float renderRotationScalar = 0;
        if (renderModel != null && renderModel.getAnimatedRenderEnvironment() != null) {
            renderRotationScalar = camera.getSourceNode().getRenderRotationScalar(renderModel.getAnimatedRenderEnvironment());
        }

        renderableCameraProp.render(g2, coordinateSystem, vec3Start, vec3End, renderRotationScalar);

//		Point start = CoordSysUtils.convertToViewPoint(coordinateSystem, position);
//		Point end = CoordSysUtils.convertToViewPoint(coordinateSystem, targetPosition);
//
//		g2.translate(end.x, end.y);
//		g2.rotate(-((Math.PI / 2) + Math.atan2(end.x - start.x, end.y - start.y)));
//		double zoom = CoordSysUtils.getZoom(coordinateSystem);
//		int size = (int) (20 * zoom);
//		double dist = start.distance(end);
//
//		g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
//		g2.drawRect((int) dist - size, -size, size * 2, size * 2);
//
//		g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
//		g2.drawLine(0, 0, size, size);
//		g2.drawLine(0, 0, size, -size);
//
//		g2.drawLine(0, 0, (int) dist, 0);
    }

    public void renderCamera(Camera camera, Color boxColor, Vec3 position, Color targetColor, Vec3 targetPosition) {
        if (isAnimated) {
            throw new WrongModeException("not animating cameras yet, code not finished");
        }
        Graphics2D g2 = ((Graphics2D) graphics.create());

        byte dim1 = coordinateSystem.getPortFirstXYZ();
        byte dim2 = coordinateSystem.getPortSecondXYZ();
        Point start = new Point(
                (int) Math.round(coordinateSystem.viewX(position.getCoord(dim1))),
                (int) Math.round(coordinateSystem.viewY(position.getCoord(dim2))));
        Point end = new Point(
                (int) Math.round(coordinateSystem.viewX(targetPosition.getCoord(dim1))),
                (int) Math.round(coordinateSystem.viewY(targetPosition.getCoord(dim2))));

        g2.translate(end.x, end.y);
        g2.rotate(-(Math.PI / 2 + Math.atan2(end.x - start.x, end.y - start.y)));
        double zoom = coordinateSystem.getZoom();
        int size = (int) (20 * zoom);
        double dist = start.distance(end);

        g2.setColor(boxColor);
        g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 + vertexSize * 2);
        g2.drawRect((int) dist - size, -size, size * 2, size * 2);

        g2.setColor(targetColor);

        g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 + vertexSize * 2);
        g2.drawLine(0, 0, size, size);
        g2.drawLine(0, 0, size, -size);

        g2.drawLine(0, 0, (int) dist, 0);
    }
}