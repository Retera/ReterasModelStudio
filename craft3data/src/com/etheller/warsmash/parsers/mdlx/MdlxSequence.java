package com.etheller.warsmash.parsers.mdlx;

import java.util.EnumSet;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.etheller.warsmash.viewer5.handlers.w3x.AnimationTokens;
import com.etheller.warsmash.viewer5.handlers.w3x.AnimationTokens.PrimaryTag;
import com.etheller.warsmash.viewer5.handlers.w3x.AnimationTokens.SecondaryTag;
import com.hiveworkshop.util.BinaryReader;
import com.hiveworkshop.util.BinaryWriter;

public class MdlxSequence implements MdlxBlock {
	public String name = "";
	public long[] interval = new long[2];
	public float moveSpeed = 0;
	public int flags = 0;
	public float rarity = 0;
	public long syncPoint = 0;
	public MdlxExtent extent = new MdlxExtent();
	
	public final EnumSet<AnimationTokens.PrimaryTag> primaryTags = EnumSet.noneOf(AnimationTokens.PrimaryTag.class);
	public final EnumSet<AnimationTokens.SecondaryTag> secondaryTags = EnumSet
			.noneOf(AnimationTokens.SecondaryTag.class);
	
	public void readMdx(final BinaryReader reader, final int version) {
		this.name = reader.read(80);
		reader.readUInt32Array(this.interval);
		this.moveSpeed = reader.readFloat32();
		this.flags = reader.readInt32();
		this.rarity = reader.readFloat32();
		this.syncPoint = reader.readUInt32();
		this.extent.readMdx(reader);
		populateTags();
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeWithNulls(name, 80);
		writer.writeUInt32Array(this.interval);
		writer.writeFloat32(this.moveSpeed);
		writer.writeUInt32(this.flags);
		writer.writeFloat32(this.rarity);
		writer.writeUInt32(this.syncPoint);
		this.extent.writeMdx(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		this.name = stream.read();

		for (final String token : stream.readBlock()) {
			switch (token) {
			case MdlUtils.TOKEN_INTERVAL:
				stream.readIntArray(this.interval);
				break;
			case MdlUtils.TOKEN_NONLOOPING:
				this.flags = 1;
				break;
			case MdlUtils.TOKEN_MOVESPEED:
				this.moveSpeed = stream.readFloat();
				break;
			case MdlUtils.TOKEN_RARITY:
				this.rarity = stream.readFloat();
				break;
			case MdlUtils.TOKEN_MINIMUM_EXTENT:
				stream.readFloatArray(this.extent.min);
				break;
			case MdlUtils.TOKEN_MAXIMUM_EXTENT:
				stream.readFloatArray(this.extent.max);
				break;
			case MdlUtils.TOKEN_BOUNDSRADIUS:
				this.extent.boundsRadius = stream.readFloat();
				break;
			default:
				throw new IllegalStateException("Unknown token in Sequence \"" + this.name + "\": " + token);
			}
		}
		populateTags();
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_ANIM, this.name);
		stream.writeArrayAttrib(MdlUtils.TOKEN_INTERVAL, this.interval);

		if (this.flags == 1) {
			stream.writeFlag(MdlUtils.TOKEN_NONLOOPING);
		}

		if (this.moveSpeed != 0) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_MOVESPEED, this.moveSpeed);
		}

		if (this.rarity != 0) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_RARITY, this.rarity);
		}

		this.extent.writeMdl(stream);
		stream.endBlock();
	}

	private void populateTags() {
		this.primaryTags.clear();
		this.secondaryTags.clear();
		TokenLoop: for (final String token : this.name.split("\\s+")) {
			final String upperCaseToken = token.toUpperCase();
			for (final PrimaryTag primaryTag : PrimaryTag.values()) {
				if (upperCaseToken.equals(primaryTag.name())) {
					this.primaryTags.add(primaryTag);
					continue TokenLoop;
				}
			}
			for (final SecondaryTag secondaryTag : SecondaryTag.values()) {
				if (upperCaseToken.equals(secondaryTag.name())) {
					this.secondaryTags.add(secondaryTag);
					continue TokenLoop;
				}
			}
			break;
		}
	}

	public EnumSet<AnimationTokens.PrimaryTag> getPrimaryTags() {
		return this.primaryTags;
	}

	public EnumSet<AnimationTokens.SecondaryTag> getSecondaryTags() {
		return this.secondaryTags;
	}
}
