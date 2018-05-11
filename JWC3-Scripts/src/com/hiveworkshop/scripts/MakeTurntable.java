package com.hiveworkshop.scripts;

import java.io.File;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;

public class MakeTurntable {
	public static void main(final String[] args) {
		final File myFolder = new File("input");
		final File output = new File("WithTurntable");
		output.mkdir();
		for (final File modelFile : myFolder.listFiles()) {
			if (modelFile.getName().toLowerCase().endsWith("mdx")
					|| modelFile.getName().toLowerCase().endsWith("mdl")) {
				final MDL footman = MDL.read(modelFile);
				final Helper turntable = new Helper("Turntable");
				final AnimFlag rotationData = new AnimFlag("Rotation");
				rotationData.setGlobSeq(4000);
				rotationData.setInterpType(InterpolationType.LINEAR);
				rotationData.addEntry(0, new QuaternionRotation(new Vertex(0, 0, 1), 0));
				rotationData.addEntry(1000, new QuaternionRotation(new Vertex(0, 0, 1), Math.PI / 2));
				rotationData.addEntry(2000, new QuaternionRotation(new Vertex(0, 0, 1), Math.PI));
				rotationData.addEntry(3000, new QuaternionRotation(new Vertex(0, 0, 1), 3 * Math.PI / 2));
				rotationData.addEntry(4000, new QuaternionRotation(new Vertex(0, 0, 1), 0));
				turntable.add(rotationData);
				for (final IdObject node : footman.getIdObjects()) {
					if (node.getParent() == null) {
						node.setParent(turntable);
					}
				}
				footman.add(turntable);
				footman.printTo(new File(output.getPath() + "/" + modelFile.getName()));
			}
		}
	}
}
