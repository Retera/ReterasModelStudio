package com.matrixeater.hacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.wc3data.stream.BlizzardDataInputStream;

public class GetMeDatas9 {

	public static void main(final String[] args) {

		try (final InputStream footman = new FileInputStream(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Requests\\Wazzz\\Generated10.mdx")) {
			try {

				final MDL model = new MDL(MdxUtils.loadModel(new BlizzardDataInputStream(footman)));
				Animation deathSequence = null;
				for (final Animation sequence : model.getAnims()) {
					if (sequence.getName().startsWith("Death")) {
						deathSequence = sequence;
					}
				}

				final List<AnimFlag> allAnimFlags = model.getAllAnimFlags();
				for (final AnimFlag af : allAnimFlags) {
					fix(af, deathSequence);
				}

				model.printTo(new File(
						"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\Models\\Requests\\Wazzz\\WispGrove50.mdx"));
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} catch (final FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
	}

	private static void fix(final AnimFlag visibilityAnimation, final Animation deathSequence) {
		final int startOfDeath = deathSequence.getStart();
		final int endOfDeath = deathSequence.getEnd();
		if (visibilityAnimation != null) {
			final int floorIndexOfStart = visibilityAnimation.floorIndex(startOfDeath);
			if (floorIndexOfStart < visibilityAnimation.floorIndex(endOfDeath)) {
				if (visibilityAnimation.getTimes().get(floorIndexOfStart) < startOfDeath) {
					visibilityAnimation.addEntry(startOfDeath, visibilityAnimation.getEntry(floorIndexOfStart).value);
				}
			}
		}
	}

}
