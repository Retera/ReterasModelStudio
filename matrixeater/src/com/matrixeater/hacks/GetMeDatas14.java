package com.matrixeater.hacks;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.Warcraft3MapObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData;
import com.hiveworkshop.wc3.units.objectdata.MutableObjectData.MutableGameObject;
import com.hiveworkshop.wc3.units.objectdata.War3ID;

import de.wc3data.stream.BlizzardDataInputStream;

public class GetMeDatas14 {
	private static final War3ID UNIT_MODEL = War3ID.fromString("umdl");
	private static int static_counter = 0;

	public static void main(final String[] args) {
		final MpqCodebase mpqCodebase = MpqCodebase.get();
		final SetView<String> mergedListfile = mpqCodebase.getMergedListfile();
//		for (final String item : mergedListfile) {
//			checkPath(mpqCodebase, item);
//		}
		try {
			int n = 0;
			final Warcraft3MapObjectData data = Warcraft3MapObjectData.load(true);
			final MutableObjectData units = data.getUnits();
			for (final War3ID unitId : units.keySet()) {
				final MutableGameObject unitObject = units.get(unitId);
//				if (unitObject.getAlias().asStringValue().contains("0")) {
				final String unitModel = unitObject.getFieldAsString(UNIT_MODEL, 0);
				checkPath(mpqCodebase, unitModel);
//				}
				n++;
			}
			System.out.println("Checked " + static_counter + " of " + n + " units.");
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private static void checkPath(final MpqCodebase mpqCodebase, String item) {
		EditableModel model = null;
		if (!item.contains(".")) {
			item += ".mdx";
		}
		if (item.toLowerCase().endsWith(".mdl")) {
			try (InputStream inputStream = mpqCodebase.getResourceAsStream(item)) {
				if (inputStream == null) {
					item = item.substring(0, item.lastIndexOf('.')) + ".mdx";
				} else {
					model = EditableModel.read(inputStream);
				}
			} catch (final Exception e) {
				System.err.println("FAIL: " + item);
				e.printStackTrace();
				return;
			}
		}
		if (item.toLowerCase().endsWith(".mdx")) {
			try (InputStream inputStream = mpqCodebase.getResourceAsStream(item)) {
				if (inputStream == null) {
					System.err.println("skip: " + item);
					return;
				}
				try (BlizzardDataInputStream blzStream = new BlizzardDataInputStream(inputStream)) {
					model = new EditableModel(MdxUtils.loadModel(blzStream));
				}
			} catch (final Exception e) {
				System.err.println("FAIL: " + item);
				e.printStackTrace();
				return;
			}
		}
		checkModel(item, model);
	}

	private static void checkModel(final String item, final EditableModel model) {
		if (model != null) {
			final ArrayList<Light> lights = model.sortedIdObjects(Light.class);
//			System.out.println("checking: " + item);
			static_counter++;
			for (final Light light : lights) {
				final double intensity = light.getIntensity();
				final double ambIntensity = light.getAmbIntensity();
				if (!light.getFlags().contains("Omnidirectional")) {
					System.out.println("BAD: " + item);
					return;
				}
				if (intensity > 10000 || ambIntensity > 10000) {
					System.out.println("BAD: " + item);
					return;
				} else {
					{
						final AnimFlag animIntensity = AnimFlag.find(light.getAnimFlags(), "Intensity");
						if (animIntensity != null) {
							for (int i = 0; i < animIntensity.size(); i++) {
								final Double value = (Double) animIntensity.getValues().get(i);
								if (value > 10000) {
									System.out.println("BAD: " + item);
									return;
								}
							}
						}
					}
					{
						final AnimFlag animIntensity = AnimFlag.find(light.getAnimFlags(), "AmbIntensity");
						if (animIntensity != null) {
							for (int i = 0; i < animIntensity.size(); i++) {
								final Double value = (Double) animIntensity.getValues().get(i);
								if (value > 10000) {
									System.out.println("BAD: " + item);
									return;
								}
							}
						}
					}
				}
			}
		}
	}
}
