package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Animation;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;

public class ViewportHelpers {



	public static double getBoundsRadius(TimeEnvironmentImpl renderEnv, ExtLog modelExtent) {
		ExtLog defaultAnimationExtent = new ExtLog(Vec3.ZERO, Vec3.ZERO, 0);
		if (renderEnv != null) {
			Animation currentAnimation = renderEnv.getCurrentAnimation();
			if ((currentAnimation != null) && currentAnimation.getExtents() != null) {
				defaultAnimationExtent.setMinMax(currentAnimation.getExtents());
			}
		}

		ExtLog someExtent = new ExtLog(Vec3.ZERO, Vec3.ZERO, 0);
		someExtent.setMinMax(defaultAnimationExtent);
		someExtent.setMinMax(modelExtent);

		double boundsRadius = 0;
		if (someExtent.hasBoundsRadius() && 0.01 < someExtent.getBoundsRadius()) {
			final double extBoundRadius = someExtent.getBoundsRadius();
			if (boundsRadius < extBoundRadius) {
				boundsRadius = extBoundRadius;
			}
		}
		if (someExtent.getMaximumExtent() != null) {
			final double minMaxBoundRadius = someExtent.getMaximumExtent().distance(someExtent.getMinimumExtent()) / 2;
			if (minMaxBoundRadius > boundsRadius) {
				boundsRadius = minMaxBoundRadius;
			}
		}
		if (boundsRadius <= 0 || boundsRadius > 100000) {
			boundsRadius = 64;
		}

		return boundsRadius;
	}

	public static Animation findDefaultAnimation(EditableModel model) {
		Animation defaultAnimation = null;
		for (Animation animation : model.getAnims()) {
			if (defaultAnimation == null
					|| !defaultAnimation.getName().toLowerCase().contains("stand")
					|| (animation.getName().toLowerCase().contains("stand")
					&& (animation.getName().length() < defaultAnimation.getName().length()))) {
				defaultAnimation = animation;
			}
		}
		return defaultAnimation;
	}

//	public static ExtLog getAnimExt(Sequence sequence){
//		if(sequence instanceof Animation){
//			return ((Animation) sequence).getExtents();
//		}
//	}

	public static Vec3 getGoodCameraPos(ExtLog extLog, double ry, double rz){
		Vec3 look = new Vec3(extLog.getMaximumExtent()).add(extLog.getMinimumExtent()).scale(.5f);
		float length = look.length()*1.3f;
		System.out.println("look: " + look + ", length: " + length);
//		look.scale(.5f);

		Vec3 pos = new Vec3(Vec3.X_AXIS).scale(length);
		float x = pos.x;
		pos.x = (float) (x*Math.cos(ry));
		pos.z = (float) (x*Math.sin(ry));

		x = pos.x;
		pos.x = (float) (x*Math.cos(-rz));
		pos.y = (float) (x*Math.sin(-rz));

		System.out.println("pos:  " + pos);
//		pos.scale(100);
//		pos.add(look);
//		pos.scale(length).add(look);
////		pos.multiply(look);
//
//
//
//		pos.x = (float) (pos.x*Math.cos(-rz));
//		pos.y = (float) (pos.y*Math.sin(-rz));
//
//		pos.x = (float) (pos.x/Math.cos(-ry));
//		pos.z = (float) (pos.z/Math.sin(-ry));

		System.out.println("pos: " + pos);

		return pos;
	}
}