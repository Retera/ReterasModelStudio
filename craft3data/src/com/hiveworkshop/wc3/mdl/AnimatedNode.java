package com.hiveworkshop.wc3.mdl;

import java.util.List;

import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;

public interface AnimatedNode extends TimelineContainer {

	public AddKeyframeAction createTranslationKeyframe(final RenderModel renderModel, final AnimFlag translationFlag,
                                                       final ModelStructureChangeListener structureChangeListener);

	public AddKeyframeAction createRotationKeyframe(final RenderModel renderModel, final AnimFlag rotationTimeline,
			final ModelStructureChangeListener structureChangeListener);

	public AddKeyframeAction createScalingKeyframe(final RenderModel renderModel, final AnimFlag scalingTimeline,
			final ModelStructureChangeListener structureChangeListener);

	public void updateTranslationKeyframe(final RenderModel renderModel, final double newDeltaX, final double newDeltaY,
			final double newDeltaZ, final Vector3f savedLocalTranslation);

	public void updateRotationKeyframe(final RenderModel renderModel, final double centerX, final double centerY,
			final double centerZ, final double radians, final byte firstXYZ, final byte secondXYZ,
			final Quaternion savedLocalRotation);

	public void updateScalingKeyframe(final RenderModel renderModel, final double scaleX, final double scaleY,
			final double scaleZ, final Vector3f savedLocalScaling);

	public void updateLocalRotationKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final Quaternion localRotation);

	public void updateLocalRotationKeyframeInverse(final int trackTime, final Integer trackGlobalSeq,
			final Quaternion localRotation);

	public void updateLocalTranslationKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final double newDeltaX, final double newDeltaY, final double newDeltaZ);

	public void updateLocalScalingKeyframe(final int trackTime, final Integer trackGlobalSeq,
			final Vector3f localScaling);

	boolean hasFlag(IdObject.NodeFlags flag);

	AnimatedNode getParent();

	Vertex getPivotPoint();

	List<? extends AnimatedNode> getChildrenNodes();

	String getName();

	public float getRenderVisibility(AnimatedRenderEnvironment animatedRenderEnvironment);

	public Vertex getRenderTranslation(AnimatedRenderEnvironment animatedRenderEnvironment);

	public QuaternionRotation getRenderRotation(AnimatedRenderEnvironment animatedRenderEnvironment);

	public Vertex getRenderScale(AnimatedRenderEnvironment animatedRenderEnvironment);
}
