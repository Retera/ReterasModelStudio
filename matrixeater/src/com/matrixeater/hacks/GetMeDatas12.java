package com.matrixeater.hacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.wc3data.stream.BlizzardDataInputStream;

public class GetMeDatas12 {

	public static void main(final String[] args) {

		try (final InputStream thrallStream = new FileInputStream(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Test\\Thrall\\Thrall_Mounted_Original.mdx");
				final InputStream spiritwolfStream = new FileInputStream(
						"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Test\\Thrall\\SpiritWolf_Original.mdx")) {
			try {

				final EditableModel thrall = new EditableModel(MdxUtils.loadModel(new BlizzardDataInputStream(thrallStream)));
				final EditableModel spiritwolf = new EditableModel(MdxUtils.loadModel(new BlizzardDataInputStream(spiritwolfStream)));

				final int[] spiritwolfGeosets = { 0, 1, 4, 5, 6, 7, 8, 9 };

				final List<Geoset> necessarySpiritwolfGeos = new ArrayList<>();

				for (final int index : spiritwolfGeosets) {
					necessarySpiritwolfGeos.add(spiritwolf.getGeoset(index));
				}

				final Map<String, Bone> nameToNode = new HashMap<>();
				for (final Bone bone : thrall.sortedIdObjects(Bone.class)) {
					nameToNode.put(bone.getName(), bone);
				}

				for (final Geoset geo : necessarySpiritwolfGeos) {
					for (final GeosetVertex gv : geo.getVertices()) {
						for (int i = 0; i < gv.getSkinBones().length; i++) {
							IdObject bone = gv.getSkinBones()[i];
							if (bone != null) {
								final String boneName = bone.getName();
								Bone replacement = nameToNode.get(boneName);
								int upwardDepth = 0;
								while ((replacement == null) && (bone != null)) {
									bone = bone.getParent();
									upwardDepth++;
									if (bone != null) {
										replacement = nameToNode.get(bone.getName());
									} else {
										replacement = null;
									}
								}
								if (replacement == null) {
									throw new IllegalStateException("failed to replace: " + boneName);
								} else {
									while ((upwardDepth > 0) && (replacement.getChildrenNodes().size() == 1)
											&& (replacement.getChildrenNodes().get(0) instanceof Bone)) {
										replacement = (Bone) replacement.getChildrenNodes().get(0);
										upwardDepth--;
									}
								}
								gv.getSkinBones()[i] = replacement;

							}
						}
					}
					thrall.add(geo);
					final GeosetAnim geosetAnim = geo.forceGetGeosetAnim();
					geosetAnim.copyVisibilityFrom(thrall.getGeoset(3).getGeosetAnim(), thrall);
				}

				thrall.printTo(new File(
						"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Test\\Thrall\\Thrall_Output.mdx"));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

}
