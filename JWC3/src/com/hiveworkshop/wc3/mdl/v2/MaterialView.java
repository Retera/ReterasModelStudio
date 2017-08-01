package com.hiveworkshop.wc3.mdl.v2;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.v2.timelines.AnimatableVisitor;
import com.hiveworkshop.wc3.mdl.v2.timelines.TangentialKeyFrame;
import com.hiveworkshop.wc3.mdl.v2.timelines.Timeline;

public interface MaterialView {
	ListView<? extends LayerView> getLayers();

	int getPriorityPlane();

	boolean isConstantColor();

	boolean isSortPrimsFarZ();

	boolean isFullResolution();

	public static final class Util {
		private static final TextureNameGetter TEXTURE_NAME_GETTER = new TextureNameGetter();
		private static final IsAlphaAnimatedChecker IS_ALPHA_ANIMATED_CHECKER = new IsAlphaAnimatedChecker();

		public static String createDefaultName(final MaterialView materialView) {
			final StringBuilder name = new StringBuilder();
			final ListView<? extends LayerView> layers = materialView.getLayers();
			for (int i = layers.size() - 1; i >= 0; i--) {
				final LayerView layer = layers.get(i);
				if (name.length() > 0) {
					name.append(" over ");
				}
				name.append(layer.getTexture().visit(TEXTURE_NAME_GETTER));
				if (layer.getAlpha().visit(IS_ALPHA_ANIMATED_CHECKER)) {
					name.append(" (animated Alpha)");
				}
			}
			return name.toString();
		}

		private static final class TextureNameGetter implements AnimatableVisitor<Bitmap, String> {

			@Override
			public String staticValue(final Bitmap value) {
				return value.getName();
			}

			@Override
			public String animatedValues(final Timeline<Bitmap> timeline) {
				// SortedMapView<Integer, Bitmap> timeToKey =
				// timeline.getTimeToKey();
				// return timeToKey.get(timeToKey.firstKey()).getName();
				return "animated texture layers";
			}

			@Override
			public String animatedTangentialValues(final Timeline<TangentialKeyFrame<Bitmap>> timeline) {
				return "animated texture layers";
			}

		}

		private static final class IsAlphaAnimatedChecker implements AnimatableVisitor<Double, Boolean> {

			@Override
			public Boolean staticValue(final Double value) {
				return false;
			}

			@Override
			public Boolean animatedValues(final Timeline<Double> timeline) {
				return true;
			}

			@Override
			public Boolean animatedTangentialValues(final Timeline<TangentialKeyFrame<Double>> timeline) {
				return true;
			}

		}

		private Util() {
		}
	}
}
