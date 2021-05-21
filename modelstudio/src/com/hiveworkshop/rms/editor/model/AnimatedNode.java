package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.List;

public abstract class AnimatedNode extends TimelineContainer {

//	abstract public AnimatedNode getParent();

	abstract public Vec3 getPivotPoint();

	abstract public List<? extends AnimatedNode> getChildrenNodes();

	abstract public String getName();

	public Vec3 getRenderTranslation(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Translation", null);
	}

	public Quat getRenderRotation(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedQuat(animatedRenderEnvironment, "Rotation", null);
	}

	public Vec3 getRenderScale(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, "Scaling", null);
	}
}
