package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import javax.swing.JButton;

import com.hiveworkshop.wc3.mdl.GeosetAnim;

public class ComponentGeosetAnimCard extends ComponentCard<GeosetAnim> {
	private final TrackSummaryPanel tracks = new TrackSummaryPanel();
	private final JButton openGeoset;

	public ComponentGeosetAnimCard() {
		addInfo("Geoset", anim -> anim.getGeoset() == null ? "(none)" : anim.getGeoset().getUIName(model()));
		openGeoset = addButton("Open Geoset", () -> {
			if (item.getGeoset() != null) {
				navigationListener.openInModelTab(item.getGeoset());
			}
		});
		addRow(null, openGeoset, "align right");
		addCheckBox("Drop Shadow", GeosetAnim::isDropShadow, GeosetAnim::setDropShadow);
		addFloatValue("Alpha", anim -> anim.getStaticAlpha() < 0 ? 1.0 : anim.getStaticAlpha(), GeosetAnim::setStaticAlpha, anim -> anim,
				GeosetAnim::getAnimFlags, "Alpha");
		addColorValue("Color", GeosetAnim::getStaticColor, GeosetAnim::setStaticColor, anim -> anim,
				GeosetAnim::getAnimFlags, "Color");
		addWide(tracks);
	}

	@Override
	protected void onReload() {
		openGeoset.setEnabled(item.getGeoset() != null);
		tracks.setTracks(item.getAnimFlags(), navigationListener);
	}
}
