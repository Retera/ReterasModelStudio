package com.matrixeater.hacks;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdx.MdxUtils;

public class GenModels {

	public static void main(final String[] args) throws IOException {
		final File dest = new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\WhiteUIElementColors");
		dest.mkdir();
		for (int i = 0; i < 36; i++) {
			final EditableModel whiteUIModel = MdxUtils.loadEditable(new File("C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\WhiteUIElement.mdl"));
			final GeosetAnim geosetAnim = whiteUIModel.getGeoset(0).forceGetGeosetAnim();
			final Color color = colorByAngle(i * 10);
			geosetAnim
					.setStaticColor(new Vertex(color.getBlue() / 255., color.getGreen() / 255., color.getRed() / 255.));
			MdxUtils.saveMdx(whiteUIModel, new File(dest.getPath() + "\\ColoredElement" + i + ".mdx"));
		}
	}

	private static Color colorByAngle(final double angle) {
		final int red = Math.min(255, Math.max(0, (int) (Math.abs(((180 - angle) * 510) / 120.)) - 255));
		final int green = Math.min(255, Math.max(0, (int) (510 - Math.abs(((angle - 120) * 510) / 120.))));
		final int blue = Math.min(255, Math.max(0, (int) (510 - Math.abs(((angle - 240) * 510) / 120.))));
		return new Color(red, green, blue);
	}
}
