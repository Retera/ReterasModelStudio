package com.hiveworkshop.scripts;

import java.io.File;

import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.MDL;

public class CorruptedGeosetIdGenerator {

	public static void main(final String[] args) {
		MDL.DISABLE_BONE_GEO_ID_VALIDATOR = true;
		final MDL mdl = MDL.read(new File("input/FootmanWithInvisibleDragon.mdl"));
		for (final Bone bone : mdl.sortedIdObjects(Bone.class)) {
			// bone.setGeoset(mdl.getGeoset(mdl.getGeosets().size() - 1));
			// link it to final the invisible dragon
			bone.setGeosetAnim(mdl.getGeosetAnim(mdl.getGeosetAnims().size() - 1));
			// bone.setMultiGeoId(false);
			// link it to the invisible dragon
		}
		mdl.printTo(new File("output/badFootmanGoodGeoIds.mdl"));
	}

}
