package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

import java.util.Arrays;

public class MdlxSequence implements MdlxBlock {
	public String name = "";
	public long[] interval = new long[2];
	public float moveSpeed = 0;
	public int flags = 0;
	public float rarity = 0;
	public long syncPoint = 0;
	public MdlxExtent extent = new MdlxExtent();
	
	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		name = reader.read(80);
		System.out.println("MdlxSequence name: " + name);
		reader.readUInt32Array(interval);
		System.out.println("MdlxSequence interval: " + Arrays.toString(interval));
		moveSpeed = reader.readFloat32();
		System.out.println("MdlxSequence moveSpeed: " + moveSpeed);
		flags = reader.readInt32();
		System.out.println("MdlxSequence flags: " + flags);
		if(version != 1300) {
			rarity = reader.readFloat32();
			System.out.println("MdlxSequence rarity: " + rarity);
			syncPoint = reader.readUInt32();
			System.out.println("MdlxSequence syncPoint: " + syncPoint);
		}
		extent.readMdx(reader);
		System.out.println("MdlxSequence extent: " + extent);
		if(version == 1300) {
			float frequency = reader.readFloat32();
			System.out.println("MdlxSequence frequency: " + frequency);
			long[] replay = new long[2];
			reader.readUInt32Array(replay);
			System.out.println("MdlxSequence replay: " + Arrays.toString(replay));
			long blendTime = reader.readUInt32();
			System.out.println("MdlxSequence blendTime: " + blendTime);
		}
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeWithNulls(name, 80);
		writer.writeUInt32Array(interval);
		writer.writeFloat32(moveSpeed);
		writer.writeUInt32(flags);
		writer.writeFloat32(rarity);
		writer.writeUInt32(syncPoint);
		extent.writeMdx(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		name = stream.read();

		for (final String token : stream.readBlock()) {
			switch (token) {
			case MdlUtils.TOKEN_INTERVAL:
				stream.readIntArray(interval);
				break;
			case MdlUtils.TOKEN_NONLOOPING:
				flags = 1;
				break;
			case MdlUtils.TOKEN_MOVESPEED:
				moveSpeed = stream.readFloat();
				break;
			case MdlUtils.TOKEN_RARITY:
				rarity = stream.readFloat();
				break;
			case MdlUtils.TOKEN_MINIMUM_EXTENT:
				stream.readFloatArray(extent.min);
				break;
			case MdlUtils.TOKEN_MAXIMUM_EXTENT:
				stream.readFloatArray(extent.max);
				break;
			case MdlUtils.TOKEN_BOUNDSRADIUS:
				extent.boundsRadius = stream.readFloat();
				break;
			default:
				throw new IllegalStateException("Unknown token in Sequence \"" + name + "\": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_ANIM, name);
		stream.writeArrayAttrib(MdlUtils.TOKEN_INTERVAL, interval);

		if (flags == 1) {
			stream.writeFlag(MdlUtils.TOKEN_NONLOOPING);
		}

		if (moveSpeed != 0) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_MOVESPEED, moveSpeed);
		}

		if (rarity != 0) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_RARITY, rarity);
		}

		extent.writeMdl(stream);
		stream.endBlock();
	}
}
