package com.hiveworkshop.wc3.mdx;

import java.io.IOException;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.Vertex;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class Node {
	public static final boolean LOG_DISCARDED_FLAGS = false;

	public String name = "";
	public int objectId;
	public int parentId;
	public int flags;
	public GeosetTranslation geosetTranslation;
	public GeosetRotation geosetRotation;
	public GeosetScaling geosetScaling;

	public void load(final BlizzardDataInputStream in) throws IOException {
		final int inclusiveSize = in.readInt();
		name = in.readCharsAsString(80);
		objectId = in.readInt();
		parentId = in.readInt();
		flags = in.readInt();
		for (int i = 0; i < 3; i++) {
			if (MdxUtils.checkOptionalId(in, GeosetTranslation.key)) {
				geosetTranslation = new GeosetTranslation();
				geosetTranslation.load(in);
			} else if (MdxUtils.checkOptionalId(in, GeosetRotation.key)) {
				geosetRotation = new GeosetRotation();
				geosetRotation.load(in);
			} else if (MdxUtils.checkOptionalId(in, GeosetScaling.key)) {
				geosetScaling = new GeosetScaling();
				geosetScaling.load(in);
			}

		}
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		out.writeInt(getSize());// InclusiveSize
		out.writeNByteString(name, 80);
		out.writeInt(objectId);
		out.writeInt(parentId);
		out.writeInt(flags);
		if (geosetTranslation != null) {
			geosetTranslation.save(out);
		}
		if (geosetRotation != null) {
			geosetRotation.save(out);
		}
		if (geosetScaling != null) {
			geosetScaling.save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 80;
		a += 4;
		a += 4;
		a += 4;
		if (geosetTranslation != null) {
			a += geosetTranslation.getSize();
		}
		if (geosetRotation != null) {
			a += geosetRotation.getSize();
		}
		if (geosetScaling != null) {
			a += geosetScaling.getSize();
		}

		return a;
	}

	public Node() {

	}

	public static enum NodeFlag {
		DONTINHERIT_TRANSLATION("DontInherit { Translation }", 0x1),
		DONTINHERIT_SCALING("DontInherit { Scaling }", 0x2), DONTINHERIT_ROTATION("DontInherit { Rotation }", 0x4),
		BILLBOARDED("Billboarded", 0x8), BILLBOARD_LOCK_X("BillboardLockX", 0x10),
		BILLBOARD_LOCK_Y("BillboardLockY", 0x20), BILLBOARD_LOCK_Z("BillboardLockZ", 0x40),
		CAMERA_ANCHORED("CameraAnchored", 0x80), TEAM_COLORED_CORN("TeamColor", 0x1000),
		EMITTER_USES_MDL("EmitterUsesMDL", 0x8000), EMITTER_USES_TGA("EmitterUsesTGA", 0x10000),
		UNSHADED("Unshaded", 0x8000), // deliberate repeat of index
		SORT_PRIMS_FAR_Z("SortPrimsFarZ", 0x10000), LINE_EMITTER("LineEmitter", 0x20000), UNFOGGED("Unfogged", 0x40000),
		MODEL_SPACE("ModelSpace", 0x80000), XY_QUAD("XYQuad", 0x100000);

		String mdlText;
		int value;

		NodeFlag(final String str, final int value) {
			this.mdlText = str;
			this.value = value;
		}

		public String getMdlText() {
			return mdlText;
		}

		public int getValue() {
			return value;
		}

		public static NodeFlag fromId(final int id) {
			return values()[id];
		}
	}

	public Node(final IdObject mdlNode) {
		name = mdlNode.getName();
		objectId = mdlNode.getObjectId();
		parentId = mdlNode.getParentId();
		for (final NodeFlag nodeFlag : NodeFlag.values()) {
			if (mdlNode.getFlags().contains(nodeFlag.getMdlText())) {
				flags |= nodeFlag.getValue();
			}
		}
		for (final AnimFlag af : mdlNode.getAnimFlags()) {
			if (af.getName().equals("Translation")) {
				geosetTranslation = new GeosetTranslation();
				geosetTranslation.globalSequenceId = af.getGlobalSeqId();
				geosetTranslation.interpolationType = af.getInterpType();
				geosetTranslation.translationTrack = new GeosetTranslation.TranslationTrack[af.size()];
				final boolean hasTans = af.tans();
				for (int i = 0; i < af.size(); i++) {
					final GeosetTranslation.TranslationTrack mdxEntry = geosetTranslation.new TranslationTrack();
					geosetTranslation.translationTrack[i] = mdxEntry;
					final AnimFlag.Entry mdlEntry = af.getEntry(i);
					mdxEntry.translation = ((Vertex) mdlEntry.value).toFloatArray();
					mdxEntry.time = mdlEntry.time.intValue();
					if (hasTans) {
						mdxEntry.inTan = ((Vertex) mdlEntry.inTan).toFloatArray();
						mdxEntry.outTan = ((Vertex) mdlEntry.outTan).toFloatArray();
					}
				}
			} else if (af.getName().equals("Scaling")) {
				geosetScaling = new GeosetScaling();
				geosetScaling.globalSequenceId = af.getGlobalSeqId();
				geosetScaling.interpolationType = af.getInterpType();
				geosetScaling.scalingTrack = new GeosetScaling.ScalingTrack[af.size()];
				final boolean hasTans = af.tans();
				for (int i = 0; i < af.size(); i++) {
					final GeosetScaling.ScalingTrack mdxEntry = geosetScaling.new ScalingTrack();
					geosetScaling.scalingTrack[i] = mdxEntry;
					final AnimFlag.Entry mdlEntry = af.getEntry(i);
					mdxEntry.scaling = ((Vertex) mdlEntry.value).toFloatArray();
					mdxEntry.time = mdlEntry.time.intValue();
					if (hasTans) {
						mdxEntry.inTan = ((Vertex) mdlEntry.inTan).toFloatArray();
						mdxEntry.outTan = ((Vertex) mdlEntry.outTan).toFloatArray();
					}
				}
			} else if (af.getName().equals("Rotation")) {
				geosetRotation = new GeosetRotation();
				geosetRotation.globalSequenceId = af.getGlobalSeqId();
				geosetRotation.interpolationType = af.getInterpType();
				geosetRotation.rotationTrack = new GeosetRotation.RotationTrack[af.size()];
				final boolean hasTans = af.tans();
				for (int i = 0; i < af.size(); i++) {
					final GeosetRotation.RotationTrack mdxEntry = geosetRotation.new RotationTrack();
					geosetRotation.rotationTrack[i] = mdxEntry;
					final AnimFlag.Entry mdlEntry = af.getEntry(i);
					mdxEntry.rotation = ((QuaternionRotation) mdlEntry.value).toFloatArray();
					mdxEntry.time = mdlEntry.time.intValue();
					if (hasTans) {
						mdxEntry.inTan = ((QuaternionRotation) mdlEntry.inTan).toFloatArray();
						mdxEntry.outTan = ((QuaternionRotation) mdlEntry.outTan).toFloatArray();
					}
				}
			} else {
				if (Node.LOG_DISCARDED_FLAGS) {
					System.err.println("node discarded flag " + af.getName());
				}
			}
		}
	}
}
