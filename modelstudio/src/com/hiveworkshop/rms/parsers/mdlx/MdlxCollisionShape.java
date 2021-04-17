package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxCollisionShape extends MdlxGenericObject {
	public enum Type {
		BOX,
		PLANE,
		SPHERE,
		CYLINDER;

		private static final Type[] VALUES = values();

		private final boolean boundsRadius;

		Type() {
			boundsRadius = false;
		}

		Type(final boolean boundsRadius) {
			this.boundsRadius = boundsRadius;
		}

		public boolean isBoundsRadius() {
			return boundsRadius;
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
		reader.readFloat32Array(vertices[0]);

		if (type != Type.SPHERE) {
			reader.readFloat32Array(vertices[1]);
		}

		if ((type == Type.SPHERE) || (type == Type.CYLINDER)) {
			boundsRadius = reader.readFloat32();
		}
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		super.writeMdx(writer, version);

		writer.writeInt32(type.ordinal());
		writer.writeFloat32Array(vertices[0]);

		if (type != MdlxCollisionShape.Type.SPHERE) {
			writer.writeFloat32Array(vertices[1]);
		}

		if ((type == Type.SPHERE) || (type == Type.CYLINDER)) {
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
					stream.readFloatArray(vertices[0]);
					if (count == 2) {
						stream.readFloatArray(vertices[1]);
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
		final String type;
		int vertices = 2;
		switch (this.type) {
			case BOX -> type = MdlUtils.TOKEN_BOX;
			case PLANE -> type = MdlUtils.TOKEN_PLANE;
			case SPHERE -> {
				type = MdlUtils.TOKEN_SPHERE;
				vertices = 1;
			}
			case CYLINDER -> type = MdlUtils.TOKEN_CYLINDER;
			default -> throw new IllegalStateException("Invalid type in CollisionShape " + name + ": " + this.type);
		}

		stream.writeFlag(type);
		stream.startBlock(MdlUtils.TOKEN_VERTICES, vertices);
		stream.writeFloatArray(this.vertices[0]);
		if (vertices == 2) {
			stream.writeFloatArray(this.vertices[1]);
		}
		stream.endBlock();

		if (this.type.boundsRadius) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_BOUNDSRADIUS, boundsRadius);
		}

		writeGenericTimelines(stream);
		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		long size = 16 + super.getByteLength(version);

		if (type != Type.SPHERE) {
			size += 12;
		}

		if ((type == Type.SPHERE) || (type == Type.CYLINDER)) {
			size += 4;
		}

		return size;
	}
}
