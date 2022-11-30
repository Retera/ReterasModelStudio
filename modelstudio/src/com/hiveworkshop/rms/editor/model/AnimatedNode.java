package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.QuatAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public abstract class AnimatedNode extends TimelineContainer {

	abstract public Vec3 getPivotPoint();

	abstract public void setPivotPoint(final Vec3 p);

	public Vec3 getRenderTranslation(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, MdlUtils.TOKEN_TRANSLATION, null);
	}

	public Quat getRenderRotation(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedQuat(animatedRenderEnvironment, MdlUtils.TOKEN_ROTATION, null);
	}

	public Vec3 getRenderScale(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getInterpolatedVector(animatedRenderEnvironment, MdlUtils.TOKEN_SCALING, null);
	}


	public AnimFlag<Vec3> getTranslationFlag(GlobalSeq globalSeq) {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_TRANSLATION, globalSeq);

		if (timeline instanceof Vec3AnimFlag) {
			return (Vec3AnimFlag) timeline;
		}
		return null;
	}

	public AnimFlag<Vec3> getTranslationFlag() {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_TRANSLATION);

		if (timeline instanceof Vec3AnimFlag) {
			return (Vec3AnimFlag) timeline;
		}
		return null;
	}

	public AnimFlag<Vec3> getScalingFlag(GlobalSeq globalSeq) {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_SCALING, globalSeq);

		if (timeline instanceof Vec3AnimFlag) {
			return (Vec3AnimFlag) timeline;
		}
		return null;
	}

	public AnimFlag<Vec3> getScalingFlag() {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_SCALING);

		if (timeline instanceof Vec3AnimFlag) {
			return (Vec3AnimFlag) timeline;
		}
		return null;
	}

	public AnimFlag<Quat> getRotationFlag(GlobalSeq globalSeq) {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_ROTATION, globalSeq);

		if (timeline instanceof QuatAnimFlag) {
			return (QuatAnimFlag) timeline;
		}
		return null;
	}

	public AnimFlag<Quat> getRotationFlag() {
		AnimFlag<?> timeline = find(MdlUtils.TOKEN_ROTATION);

		if (timeline instanceof QuatAnimFlag) {
			return (QuatAnimFlag) timeline;
		}
		return null;
	}
}
