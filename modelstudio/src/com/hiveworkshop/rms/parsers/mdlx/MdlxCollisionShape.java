package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxCollisionShape extends MdlxGenericObject {
	public enum Type {
		BOX(false, 2, MdlUtils.TOKEN_BOX),
		PLANE(false, 2, MdlUtils.TOKEN_PLANE),
		SPHERE(true, 1, MdlUtils.TOKEN_SPHERE),
		CYLINDER(true, 2, MdlUtils.TOKEN_CYLINDER);

		private static final Type[] VALUES = values();

		private final boolean boundsRadius;
		private final int vertices;
		private final String mdlName;

		Type(final boolean boundsRadius, final int vertices, String mdlName) {
			this.boundsRadius = boundsRadius;
			this.vertices = vertices;
			this.mdlName = mdlName;
		}

		public boolean isBoundsRadius() {
			return boundsRadius;
		}

		public int getVertices() {
			return vertices;
		}

		public String getMdlName() {
			return mdlName;
		}

		public static Type from(final int index) {
			return VALUES[index];
		}
	}

	public MdlxCollisionShape.Type type = Type.SPHERE;
	public final float[][] vertices = {new float[3], new float[3]};
	public float boundsRadius = 0;

	public MdlxCollisionShape() {
		super(0x2000);
	}

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		super.readMdx(reader, version);

		type = MdlxCollisionShape.Type.from(reader.readInt32());

		for (int i = 0; i < type.getVertices(); i++) {
			reader.readFloat32Array(vertices[i]);
		}

		if (type.isBoundsRadius()) {
			boundsRadius = reader.readFloat32();
		}
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		super.writeMdx(writer, version);

		writer.writeInt32(type.ordinal());

		for (int i = 0; i < type.getVertices(); i++) {
			writer.writeFloat32Array(vertices[i]);
		}

		if (type.isBoundsRadius()) {
			writer.writeFloat32(boundsRadius);
		}
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		for (final String token : super.readMdlGeneric(stream)) {
			switch (token) {
				case MdlUtils.TOKEN_BOX -> type = Type.BOX;
				case MdlUtils.TOKEN_PLANE -> type = Type.PLANE;
				case MdlUtils.TOKEN_SPHERE -> type = Type.SPHERE;
				case MdlUtils.TOKEN_CYLINDER -> type = Type.CYLINDER;
				case MdlUtils.TOKEN_VERTICES -> {
					final int count = stream.readInt();
					stream.read(); // {
					for (int i = 0; i < count; i++) {
						stream.readFloatArray(vertices[i]);
					}
					stream.read(); // }
				}
				case MdlUtils.TOKEN_BOUNDSRADIUS -> boundsRadius = stream.readFloat();
				default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in CollisionShape " + name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_COLLISION_SHAPE, name);
		writeGenericHeader(stream);

		stream.writeFlag(this.type.getMdlName());
		stream.startBlock(MdlUtils.TOKEN_VERTICES, this.type.getVertices());
		for (int i = 0; i < this.type.getVertices(); i++) {
			stream.writeFloatArray(this.vertices[i]);
		}
		stream.endBlock();

		if (this.type.isBoundsRadius()) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_BOUNDSRADIUS, boundsRadius);
		}

		writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		long size = super.getByteLength(version) + 4 + (12L * type.getVertices());

		if (type.isBoundsRadius()) {
			size += 4;
		}

		return size;
	}
}
