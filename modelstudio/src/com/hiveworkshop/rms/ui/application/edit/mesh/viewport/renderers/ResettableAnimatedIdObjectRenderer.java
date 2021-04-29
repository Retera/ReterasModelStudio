package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportRenderableCamera;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.List;

public final class ResettableAnimatedIdObjectRenderer implements IdObjectVisitor {
    private final int vertexSize;
    private final ViewportRenderableCamera renderableCameraProp = new ViewportRenderableCamera();
    private CoordinateSystem coordinateSystem;
    private Graphics2D graphics;
    private Color lightColor;
    private Color pivotPointColor;
    private NodeIconPalette nodeIconPalette;
    private RenderModel renderModel;
    private boolean crosshairIsBox;

    public ResettableAnimatedIdObjectRenderer(int vertexSize) {
        this.vertexSize = vertexSize;
    }

    public static void drawCollisionShape(Graphics2D graphics, Color color,
                                          CoordinateSystem coordinateSystem,
                                          byte xDimension, byte yDimension,
                                          int vertexSize,
                                          CollisionShape collisionShape, Image collisionImage,
                                          Mat4 worldMatrix, boolean crosshairIsBox) {
        Vec3 pivotPoint = collisionShape.getPivotPoint();
        List<Vec3> vertices = collisionShape.getVertices();
        graphics.setColor(color);
        Vec3 vertexHeap = Vec3.getTransformed(pivotPoint, worldMatrix);
        int xCoord = (int) coordinateSystem.viewX(vertexHeap.getCoord(xDimension));
        int yCoord = (int) coordinateSystem.viewY(vertexHeap.getCoord(yDimension));
        if (collisionShape.getType() == MdlxCollisionShape.Type.BOX) {
            if (vertices.size() > 1) {
                Vec3 vertex = vertices.get(0);
                Vec3 vertex2 = vertices.get(1);
                Vec3 vertexHeap2 = Vec3.getTransformed(vertex2, worldMatrix);

                int firstXCoord = (int) coordinateSystem.viewX(vertexHeap2.getCoord(xDimension));
                int firstYCoord = (int) coordinateSystem.viewY(vertexHeap2.getCoord(yDimension));
                int secondXCoord = (int) coordinateSystem.viewX(vertexHeap2.getCoord(xDimension));
                int secondYCoord = (int) coordinateSystem.viewY(vertexHeap2.getCoord(yDimension));

                int minXCoord = Math.min(firstXCoord, secondXCoord);
                int minYCoord = Math.min(firstYCoord, secondYCoord);
                int maxXCoord = Math.max(firstXCoord, secondXCoord);
                int maxYCoord = Math.max(firstYCoord, secondYCoord);

                graphics.drawRoundRect(minXCoord, minYCoord, maxXCoord - minXCoord, maxYCoord - minYCoord, vertexSize, vertexSize);
            }
        } else {
            if (collisionShape.getExtents() != null) {
                double zoom = CoordinateSystem.getZoom(coordinateSystem);
                double boundsRadius = collisionShape.getExtents().getBoundsRadius() * zoom;
                graphics.drawOval((int) (xCoord - boundsRadius), (int) (yCoord - boundsRadius), (int) (boundsRadius * 2), (int) (boundsRadius * 2));
            }
        }
        drawNodeImage(graphics, xDimension, yDimension, coordinateSystem, collisionShape, collisionImage, worldMatrix);

        for (Vec3 vertex : vertices) {
            drawCrosshair(graphics, coordinateSystem, vertexSize, vertex, worldMatrix, crosshairIsBox);
        }
    }

    public static void drawNodeImage(Graphics2D graphics, byte xDimension, byte yDimension,
                                     CoordinateSystem coordinateSystem, IdObject attachment, Image nodeImage,
                                     Mat4 worldMatrix) {
        Vec3 vertexHeap = Vec3.getTransformed(attachment.getPivotPoint(), worldMatrix);
        int xCoord = (int) coordinateSystem.viewX(vertexHeap.getCoord(xDimension));
        int yCoord = (int) coordinateSystem.viewY(vertexHeap.getCoord(yDimension));
        graphics.drawImage(nodeImage, xCoord - (nodeImage.getWidth(null) / 2), yCoord - (nodeImage.getHeight(null) / 2), nodeImage.getWidth(null), nodeImage.getHeight(null), null);
    }

    public static void drawCrosshair(Graphics2D graphics, CoordinateSystem coordinateSystem, int vertexSize, Vec3 pivotPoint, Mat4 worldMatrix, boolean crosshairIsBox) {
        Vec3 vertexHeap = Vec3.getTransformed(pivotPoint, worldMatrix);

        int xCoord = (int) coordinateSystem.viewX(vertexHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
        int yCoord = (int) coordinateSystem.viewY(vertexHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
        if (crosshairIsBox) {
            vertexSize *= 3;
            graphics.fillRect(xCoord - vertexSize, yCoord - vertexSize, vertexSize * 2, vertexSize * 2);
        } else {
            graphics.drawOval(xCoord - vertexSize, yCoord - vertexSize, vertexSize * 2, vertexSize * 2);
            graphics.drawLine(xCoord - (int) (vertexSize * 1.5f), yCoord, xCoord + (int) (vertexSize * 1.5f), yCoord);
            graphics.drawLine(xCoord, yCoord - (int) (vertexSize * 1.5f), xCoord, yCoord + (int) (vertexSize * 1.5f));
        }
    }

    public ResettableAnimatedIdObjectRenderer reset(CoordinateSystem coordinateSystem, Graphics2D graphics,
                                                    Color lightColor, Color pivotPointColor, NodeIconPalette nodeIconPalette,
                                                    RenderModel renderModel, boolean crosshairIsBox) {
        this.coordinateSystem = coordinateSystem;
        this.graphics = graphics;
        this.lightColor = lightColor;
        this.pivotPointColor = pivotPointColor;
        this.nodeIconPalette = nodeIconPalette;
        this.renderModel = renderModel;
        this.crosshairIsBox = crosshairIsBox;
        return this;
    }

    private void drawNodeImage(IdObject object, Image nodeImage, Mat4 worldMatrix) {
        drawNodeImage(graphics, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ(), coordinateSystem, object, nodeImage, worldMatrix);
    }

    private void drawCrosshair(Bone object) {
        drawCrosshair(object.getPivotPoint(), renderModel.getRenderNode(object).getWorldMatrix());
    }

    private void drawCrosshair(Vec3 pivotPoint, Mat4 worldMatrix) {
        drawCrosshair(graphics, coordinateSystem, vertexSize, pivotPoint, worldMatrix, crosshairIsBox);
    }

    @Override
    public void visitIdObject(IdObject object) {
        if (object instanceof Helper) {
            graphics.setColor(pivotPointColor.darker());
            drawCrosshair((Bone) object);
        } else if (object instanceof Bone) {
            graphics.setColor(pivotPointColor);
            drawCrosshair((Bone) object);
        } else if (object instanceof CollisionShape) {
            collisionShape((CollisionShape) object);
        } else if (object instanceof Light) {
            graphics.setColor(lightColor);
            drawNodeImage(object, nodeIconPalette.getObjectImage(object), renderModel.getRenderNode(object).getWorldMatrix());
            light((Light) object);
        } else {
            drawNodeImage(object, nodeIconPalette.getObjectImage(object), renderModel.getRenderNode(object).getWorldMatrix());
        }
    }

    public void collisionShape(CollisionShape object) {
        drawCollisionShape(graphics, pivotPointColor, coordinateSystem, coordinateSystem.getPortFirstXYZ(),
                coordinateSystem.getPortSecondXYZ(), vertexSize, object, nodeIconPalette.getCollisionImage(),
                renderModel.getRenderNode(object).getWorldMatrix(), crosshairIsBox);
    }

    public void light(Light object) {
        Vec3 vertexHeap = Vec3.getTransformed(object.getPivotPoint(), renderModel.getRenderNode(object).getWorldMatrix());
        int xCoord = (int) coordinateSystem.viewX(vertexHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
        int yCoord = (int) coordinateSystem.viewY(vertexHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
        double zoom = CoordinateSystem.getZoom(coordinateSystem);

        int attenuationStart = (int) (object.getAttenuationStart() * zoom);
        if (attenuationStart > 0) {
            graphics.drawOval(xCoord - attenuationStart, yCoord - attenuationStart, attenuationStart * 2, attenuationStart * 2);
        }
        int attenuationEnd = (int) (object.getAttenuationEnd() * zoom);
        if (attenuationEnd > 0) {
            graphics.drawOval(xCoord - attenuationEnd, yCoord - attenuationEnd, attenuationEnd * 2, attenuationEnd * 2);
        }
    }

    @Override
    public void camera(Camera camera) {
        graphics.setColor(Color.GREEN.darker());
        Graphics2D g2 = ((Graphics2D) graphics.create());

        Vec3 position = camera.getPosition();
        Vec3 vec3Start = Vec3.getTransformed(position, renderModel.getRenderNode(camera.getSourceNode()).getWorldMatrix());

        Vec3 targetPosition = camera.getTargetPosition();
        Vec3 vec3End = Vec3.getTransformed(targetPosition, renderModel.getRenderNode(camera.getTargetNode()).getWorldMatrix());

        float renderRotationScalar = renderModel.getAnimatedRenderEnvironment() == null ? 0 : camera.getSourceNode().getRenderRotationScalar(renderModel.getAnimatedRenderEnvironment());

        renderableCameraProp.render(g2, coordinateSystem, vec3Start, vec3End, renderRotationScalar);
    }
}