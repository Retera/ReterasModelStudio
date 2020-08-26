package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.List;

import com.hiveworkshop.rms.editor.model.Attachment;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Light;
import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.ParticleEmitterPopcorn;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportRenderableCamera;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vector3;
import com.hiveworkshop.rms.util.Vector4;

public final class ResettableAnimatedIdObjectRenderer implements IdObjectVisitor {
    private CoordinateSystem coordinateSystem;
    private Graphics2D graphics;
    private final int vertexSize;
    private Color lightColor;
    private Color pivotPointColor;
    private NodeIconPalette nodeIconPalette;
    private RenderModel renderModel;
    private final ViewportRenderableCamera renderableCameraProp = new ViewportRenderableCamera();
    private boolean crosshairIsBox;

    public ResettableAnimatedIdObjectRenderer(final int vertexSize) {
        this.vertexSize = vertexSize;
    }

    public ResettableAnimatedIdObjectRenderer reset(final CoordinateSystem coordinateSystem, final Graphics2D graphics,
                                                    final Color lightColor, final Color pivotPointColor, final NodeIconPalette nodeIconPalette,
                                                    final RenderModel renderModel, final boolean crosshairIsBox) {
        this.coordinateSystem = coordinateSystem;
        this.graphics = graphics;
        this.lightColor = lightColor;
        this.pivotPointColor = pivotPointColor;
        this.nodeIconPalette = nodeIconPalette;
        this.renderModel = renderModel;
        this.crosshairIsBox = crosshairIsBox;
        return this;
    }

    @Override
    public void bone(final Bone object) {
        graphics.setColor(pivotPointColor);
        drawCrosshair(object);
    }

    @Override
    public void light(final Light light) {
        final Image lightImage = nodeIconPalette.getLightImage();
        graphics.setColor(lightColor);
        loadPivotInVertexHeap(light.getPivotPoint(), renderModel.getRenderNode(light).getWorldMatrix(), vertexHeap);
        final int xCoord = (int) coordinateSystem
                .convertX(Vector3.getCoord(vertexHeap, coordinateSystem.getPortFirstXYZ()));
        final int yCoord = (int) coordinateSystem
                .convertY(Vector3.getCoord(vertexHeap, coordinateSystem.getPortSecondXYZ()));
        final double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
        // graphics.drawOval(xCoord - vertexSize * 2, yCoord - vertexSize * 2,
        // vertexSize * 4, vertexSize * 4);
        // graphics.setColor(programPreferences.getAmbientLightColor());
        // graphics.drawLine(xCoord - vertexSize * 3, yCoord, xCoord +
        // vertexSize * 3, yCoord);
        // graphics.drawLine(xCoord, yCoord - vertexSize * 3, xCoord, yCoord +
        // vertexSize * 3);
        graphics.drawImage(lightImage, xCoord - (lightImage.getWidth(null) / 2),
                yCoord - (lightImage.getHeight(null) / 2), lightImage.getWidth(null), lightImage.getHeight(null), null);

        final int attenuationStart = (int) (light.getAttenuationStart() * zoom);
        if (attenuationStart > 0) {
            graphics.drawOval(xCoord - attenuationStart, yCoord - attenuationStart, attenuationStart * 2,
                    attenuationStart * 2);
        }
        final int attenuationEnd = (int) (light.getAttenuationEnd() * zoom);
        if (attenuationEnd > 0) {
            graphics.drawOval(xCoord - attenuationEnd, yCoord - attenuationEnd, attenuationEnd * 2, attenuationEnd * 2);
        }
    }

    private void drawCrosshair(final Bone object) {
        drawCrosshair(object.getPivotPoint(), renderModel.getRenderNode(object).getWorldMatrix());
    }

    private void drawCrosshair(final Vector3 pivotPoint, final Mat4 worldMatrix) {
        drawCrosshair(graphics, coordinateSystem, vertexSize, pivotPoint, worldMatrix, crosshairIsBox);
    }

    @Override
    public void helper(final Helper object) {
        graphics.setColor(pivotPointColor.darker());
        drawCrosshair(object);
    }

    @Override
    public void attachment(final Attachment attachment) {
        drawNodeImage(attachment, nodeIconPalette.getAttachmentImage(),
                renderModel.getRenderNode(attachment).getWorldMatrix());
    }

    @Override
    public void particleEmitter(final ParticleEmitter particleEmitter) {
        drawNodeImage(particleEmitter, nodeIconPalette.getParticleImage(),
                renderModel.getRenderNode(particleEmitter).getWorldMatrix());
    }

    @Override
    public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
        drawNodeImage(particleEmitter, nodeIconPalette.getParticle2Image(),
                renderModel.getRenderNode(particleEmitter).getWorldMatrix());
    }

    @Override
    public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
        drawNodeImage(popcornFxEmitter, nodeIconPalette.getParticleImage(),
                renderModel.getRenderNode(popcornFxEmitter).getWorldMatrix());
    }

    @Override
    public void ribbonEmitter(final RibbonEmitter ribbonEmitter) {
        drawNodeImage(ribbonEmitter, nodeIconPalette.getRibbonImage(),
                renderModel.getRenderNode(ribbonEmitter).getWorldMatrix());
    }

    @Override
    public void eventObject(final EventObject eventObject) {
        drawNodeImage(eventObject, nodeIconPalette.getEventImage(),
                renderModel.getRenderNode(eventObject).getWorldMatrix());
    }

    @Override
    public void collisionShape(final CollisionShape collisionShape) {
        drawCollisionShape(graphics, pivotPointColor, coordinateSystem, coordinateSystem.getPortFirstXYZ(),
                coordinateSystem.getPortSecondXYZ(), vertexSize, collisionShape, nodeIconPalette.getCollisionImage(),
                renderModel.getRenderNode(collisionShape).getWorldMatrix(), crosshairIsBox);
    }

    @Override
    public void camera(final Camera camera) {
        graphics.setColor(Color.GREEN.darker());
        final Graphics2D g2 = ((Graphics2D) graphics.create());
        final Vector3 ver = camera.getPosition();
        final Vector3 targ = camera.getTargetPosition();
        loadPivotInVertexHeap(ver, renderModel.getRenderNode(camera.getSourceNode()).getWorldMatrix(), vertexHeap);
        final float startX = Vector3.getCoord(vertexHeap, (byte) 0);
        final float startY = Vector3.getCoord(vertexHeap, (byte) 1);
        final float startZ = Vector3.getCoord(vertexHeap, (byte) 2);
        final Point start = new Point((int) Math.round(coordinateSystem.convertX(startX)),
                (int) Math.round(coordinateSystem.convertY(startY)));
        loadPivotInVertexHeap(targ, renderModel.getRenderNode(camera.getTargetNode()).getWorldMatrix(), vertexHeap);
        final float endX = Vector3.getCoord(vertexHeap, (byte) 0);
        final float endY = Vector3.getCoord(vertexHeap, (byte) 1);
        final float endZ = Vector3.getCoord(vertexHeap, (byte) 2);
        final Point end = new Point((int) Math.round(coordinateSystem.convertX(endX)),
                (int) Math.round(coordinateSystem.convertY(endY)));

        Double renderRotationScalar = renderModel.getAnimatedRenderEnvironment() == null ? Double.valueOf(0)
                : camera.getSourceNode().getRenderRotationScalar(renderModel.getAnimatedRenderEnvironment());
        if (renderRotationScalar == null) {
            renderRotationScalar = 0.;
        }
        renderableCameraProp.render(g2, coordinateSystem, startX, startY, startZ, endX, endY, endZ,
                renderRotationScalar);

        // g2.translate(end.x, end.y);
        // g2.rotate(-(Math.PI / 2 + Math.atan2(end.x - start.x, end.y - start.y)));
        // final double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
        // final int size = (int) (20 * zoom);
        // final double dist = start.distance(end);
        //
        // // if (verSel) {
        // // g2.setColor(Color.orange.darker());
        // // }
        // // Cam
        // g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 +
        // vertexSize * 2);
        // g2.drawRect((int) dist - size, -size, size * 2, size * 2);
        //
        // // if (tarSel) {
        // // g2.setColor(Color.orange.darker());
        // // } else if (verSel) {
        // // g2.setColor(Color.green.darker());
        // // }
        // // Target
        // g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 +
        // vertexSize * 2);
        // g2.drawLine(0, 0, size, size);//
        // (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())+5)),
        // // (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())+5)));
        // g2.drawLine(0, 0, size, -size);//
        // (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())-5)),
        // // (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())-5)));
        //
        // // if (!verSel && tarSel) {
        // // g2.setColor(Color.green.darker());
        // // }
        // g2.drawLine(0, 0, (int) dist, 0);
    }

    private void drawNodeImage(final IdObject attachment, final Image nodeImage, final Mat4 worldMatrix) {
        drawNodeImage(graphics, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ(),
                coordinateSystem, attachment, nodeImage, worldMatrix);
    }

    public static void drawNodeImage(final Graphics2D graphics, final byte xDimension, final byte yDimension,
                                     final CoordinateSystem coordinateSystem, final IdObject attachment, final Image nodeImage,
                                     final Mat4 worldMatrix) {
        loadPivotInVertexHeap(attachment.getPivotPoint(), worldMatrix, vertexHeap);
        final int xCoord = (int) coordinateSystem.convertX(Vector3.getCoord(vertexHeap, xDimension));
        final int yCoord = (int) coordinateSystem.convertY(Vector3.getCoord(vertexHeap, yDimension));
        graphics.drawImage(nodeImage, xCoord - (nodeImage.getWidth(null) / 2), yCoord - (nodeImage.getHeight(null) / 2),
                nodeImage.getWidth(null), nodeImage.getHeight(null), null);
    }

    private static final Vector4 vertexHeap = new Vector4();
    private static final Vector4 vertexHeap2 = new Vector4();

    public static void drawCrosshair(final Graphics2D graphics, final CoordinateSystem coordinateSystem, int vertexSize,
                                     final Vector3 pivotPoint, final Mat4 worldMatrix, final boolean crosshairIsBox) {
        loadPivotInVertexHeap(pivotPoint, worldMatrix, vertexHeap);

        final int xCoord = (int) coordinateSystem
                .convertX(Vector3.getCoord(vertexHeap, coordinateSystem.getPortFirstXYZ()));
        final int yCoord = (int) coordinateSystem
                .convertY(Vector3.getCoord(vertexHeap, coordinateSystem.getPortSecondXYZ()));
        if (crosshairIsBox) {
            vertexSize *= 3;
            graphics.fillRect(xCoord - vertexSize, yCoord - vertexSize, vertexSize * 2, vertexSize * 2);
        } else {
            graphics.drawOval(xCoord - vertexSize, yCoord - vertexSize, vertexSize * 2, vertexSize * 2);
            graphics.drawLine(xCoord - (int) (vertexSize * 1.5f), yCoord, xCoord + (int) (vertexSize * 1.5f), yCoord);
            graphics.drawLine(xCoord, yCoord - (int) (vertexSize * 1.5f), xCoord, yCoord + (int) (vertexSize * 1.5f));
        }
    }

    public static void loadPivotInVertexHeap(final Vector3 pivotPoint, final Mat4 worldMatrix,
                                             final Vector4 vertexHeap) {
        vertexHeap.x = (float) pivotPoint.x;
        vertexHeap.y = (float) pivotPoint.y;
        vertexHeap.z = (float) pivotPoint.z;
        vertexHeap.w = 1;
        worldMatrix.transform(vertexHeap);
    }

    public static void drawCollisionShape(final Graphics2D graphics, final Color color,
                                          final CoordinateSystem coordinateSystem, final byte xDimension, final byte yDimension, final int vertexSize,
                                          final CollisionShape collisionShape, final Image collisionImage, final Mat4 worldMatrix,
                                          final boolean crosshairIsBox) {
        final Vector3 pivotPoint = collisionShape.getPivotPoint();
        final List<Vector3> vertices = collisionShape.getVertices();
        graphics.setColor(color);
        loadPivotInVertexHeap(pivotPoint, worldMatrix, vertexHeap);
        final int xCoord = (int) coordinateSystem.convertX(Vector3.getCoord(vertexHeap, xDimension));
        final int yCoord = (int) coordinateSystem.convertY(Vector3.getCoord(vertexHeap, yDimension));
        if (collisionShape.getType() == MdlxCollisionShape.Type.BOX) {
            if (vertices.size() > 1) {
                final Vector3 vertex = vertices.get(0);
                final Vector3 vertex2 = vertices.get(1);
                loadPivotInVertexHeap(vertex, worldMatrix, vertexHeap);
                loadPivotInVertexHeap(vertex2, worldMatrix, vertexHeap2);
                final int firstXCoord = (int) coordinateSystem.convertX(Vector3.getCoord(vertexHeap2, xDimension));
                final int firstYCoord = (int) coordinateSystem.convertY(Vector3.getCoord(vertexHeap2, yDimension));
                final int secondXCoord = (int) coordinateSystem.convertX(Vector3.getCoord(vertexHeap, xDimension));
                final int secondYCoord = (int) coordinateSystem.convertY(Vector3.getCoord(vertexHeap, yDimension));
                final int minXCoord = Math.min(firstXCoord, secondXCoord);
                final int minYCoord = Math.min(firstYCoord, secondYCoord);
                final int maxXCoord = Math.max(firstXCoord, secondXCoord);
                final int maxYCoord = Math.max(firstYCoord, secondYCoord);

                graphics.drawRoundRect(minXCoord, minYCoord, maxXCoord - minXCoord, maxYCoord - minYCoord, vertexSize,
                        vertexSize);
                drawNodeImage(graphics, xDimension, yDimension, coordinateSystem, collisionShape, collisionImage,
                        worldMatrix);
            } else {
                drawNodeImage(graphics, xDimension, yDimension, coordinateSystem, collisionShape, collisionImage,
                        worldMatrix);
            }
        } else {
            if (collisionShape.getExtents() != null) {
                final double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
                final double boundsRadius = collisionShape.getExtents().getBoundsRadius() * zoom;
                graphics.drawOval((int) (xCoord - boundsRadius), (int) (yCoord - boundsRadius),
                        (int) (boundsRadius * 2), (int) (boundsRadius * 2));
                drawNodeImage(graphics, xDimension, yDimension, coordinateSystem, collisionShape, collisionImage,
                        worldMatrix);
            } else {
                drawNodeImage(graphics, xDimension, yDimension, coordinateSystem, collisionShape, collisionImage,
                        worldMatrix);
            }
        }
        for (final Vector3 vertex : vertices) {
            drawCrosshair(graphics, coordinateSystem, vertexSize, vertex, worldMatrix, crosshairIsBox);
        }
    }
}