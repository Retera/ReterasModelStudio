package com.hiveworkshop.wc3.mdl.v2.timelines.concept;

import static org.lwjgl.opengl.GL11.glMultMatrix;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Vector3f;

import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.timelines.TangentialKeyFrame;
import com.hiveworkshop.wc3.mdl.v2.timelines.Timeline;
import com.hiveworkshop.wc3.mdl.v2.timelines.VoidAnimatableVisitor;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

public class RenderingThing {
	public static void main(final String[] args) {
		final MDL model = MDL.read(MpqCodebase.get().getFile("units\\human\\footman\\footman.mdx"));
		model.getMaterials().get(0).getLayers().get(0).getAlpha().visit(new VoidAnimatableVisitor<Double>() {

			@Override
			public void staticValue(final Double value) {

			}

			@Override
			public void animatedValues(final Timeline<Double> timeline) {
				timeline.interpolate(new InterpolationCallback<Double, Void>() {
					@Override
					public Void frame(final int keyframeIndex, final Double interpolatedValue) {
						return null;
					}
				});
			}

			@Override
			public void animatedTangentialValues(final Timeline<TangentialKeyFrame<Double>> timeline) {

			}
		});
	}

	Timeline<Float> alphaTimeline;
	Timeline<Vector3f> myTranslationTimeline;
	FloatBuffer buffer;

	public void renderFrame() {
		final boolean visible = alphaTimeline.interpolate(new InterpolationCallback<Float, Boolean>() {
			@Override
			public Boolean frame(final int keyframeIndex, final Float interpolatedValue) {
				return interpolatedValue > 0.5;
			}
		});
		if (visible) {
			myTranslationTimeline.interpolate(new InterpolationCallback<Vector3f, Void>() {
				@Override
				public Void frame(final int keyframeIndex, final Vector3f interpolatedKey) {
					interpolatedKey.load(buffer);
					glMultMatrix(buffer);
					return null;
				}
			});
		}
	}
}
