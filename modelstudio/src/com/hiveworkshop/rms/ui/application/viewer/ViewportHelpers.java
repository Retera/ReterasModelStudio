package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;

public class ViewportHelpers {



	public static double getBoundsRadius(TimeEnvironmentImpl renderEnv, ExtLog modelExtent) {
		ExtLog defaultAnimationExtent = new ExtLog(new Vec3(0, 0, 0), new Vec3(0, 0, 0), 0);
		if (renderEnv != null) {
			Animation currentAnimation = renderEnv.getCurrentAnimation();
			if ((currentAnimation != null) && currentAnimation.getExtents() != null) {
				defaultAnimationExtent.setMinMax(currentAnimation.getExtents());
			}
		}
		ExtLog someExtent = new ExtLog(new Vec3(0, 0, 0), new Vec3(0, 0, 0), 0);
		someExtent.setMinMax(defaultAnimationExtent);
		someExtent.setMinMax(modelExtent);

		double boundsRadius = 64;
		if (someExtent.hasBoundsRadius() && (someExtent.getBoundsRadius() > 1)) {
			final double extBoundRadius = someExtent.getBoundsRadius();
			if (extBoundRadius > boundsRadius) {
				boundsRadius = extBoundRadius;
			}
		}
		if ((someExtent.getMaximumExtent() != null) && (someExtent.getMaximumExtent() != null)) {
			final double minMaxBoundRadius = someExtent.getMaximumExtent().distance(someExtent.getMinimumExtent()) / 2;
			if (minMaxBoundRadius > boundsRadius) {
				boundsRadius = minMaxBoundRadius;
			}
		}
		if ((boundsRadius > 10000) || (boundsRadius < 0.1)) {
			boundsRadius = 64;
		}

		return boundsRadius;
	}

	public static Animation findDefaultAnimation(ModelView modelView, TimeEnvironmentImpl renderEnv) {
		Animation defaultAnimation = null;
		for (final Animation animation : modelView.getModel().getAnims()) {
			if ((defaultAnimation == null)
					|| !defaultAnimation.getName().toLowerCase().contains("stand")
					|| (animation.getName().toLowerCase().contains("stand")
					&& (animation.getName().length() < defaultAnimation.getName().length()))) {
				defaultAnimation = animation;
			}
		}
		renderEnv.setAnimation(defaultAnimation);
		return defaultAnimation;
	}
}
