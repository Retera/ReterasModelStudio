package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import com.hiveworkshop.wc3.mdl.TextureAnim;

public class ComponentTextureAnimCard extends ComponentCard<TextureAnim> {
	private final TrackSummaryPanel tracks = new TrackSummaryPanel();

	public ComponentTextureAnimCard() {
		addInfo("Texture Animation", anim -> "TextureAnim " + model().getTextureAnimId(anim));
		addInfo("Used by", anim -> {
			int count = 0;
			for (final com.hiveworkshop.wc3.mdl.Material material : model().getMaterials()) {
				for (final com.hiveworkshop.wc3.mdl.Layer layer : material.getLayers()) {
					if (layer.getTextureAnim() == anim) {
						count++;
					}
				}
			}
			return count + (count == 1 ? " layer" : " layers");
		});
		addWide(tracks);
	}

	@Override
	protected void onReload() {
		tracks.setTracks(item.getAnimFlags(), navigationListener);
	}
}
