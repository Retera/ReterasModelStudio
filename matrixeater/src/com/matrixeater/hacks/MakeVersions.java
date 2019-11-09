package com.matrixeater.hacks;

import java.io.File;
import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;

public class MakeVersions {

	public static void main(final String[] args) {
		makeReplicas(InterpolationType.BEZIER);
		makeReplicas(InterpolationType.HERMITE);
	}

	public static void makeReplicas(final InterpolationType interpType) {
		final File sourceDir = new File(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\ReteraCubes\\Work\\Animation\\Linear");
		for (final File linearModel : sourceDir.listFiles()) {
			final MDL modelData = MDL.read(linearModel);
			String flagName = "None";
			for (final Bone bone : modelData.sortedIdObjects(Bone.class)) {
				final ArrayList<AnimFlag> flags = bone.getAnimFlags();
				for (final AnimFlag flag : flags) {
					flag.setInterpType(interpType);
					if (flag.tans()) {
						for (int i = 0; i < flag.size(); i++) {
							final Object value = flag.getValues().get(i);
							flag.getInTans().add(value);
							flag.getOutTans().add(value);
						}
					}
					flagName = flag.getName();
				}
			}
			final File outputFile = new File(
					"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\ReteraCubes\\Work\\Animation\\"
							+ interpType.name().charAt(0) + interpType.name().toLowerCase().substring(1)
							+ "\\ReteraCube_" + interpType.name().charAt(0)
							+ interpType.name().toLowerCase().substring(1) + flagName + ".mdx");
			outputFile.getParentFile().mkdirs();
			modelData.printTo(outputFile);
		}
	}

}
