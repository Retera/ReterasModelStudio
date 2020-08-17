package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;
import java.util.Iterator;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataInputStream;
import com.google.common.io.LittleEndianDataOutputStream;

public class MdlxLayer extends AnimatedObject {
	// 0: none
	// 1: transparent
	// 2: blend
	// 3: additive
	// 4: add alpha
	// 5: modulate
	// 6: modulate 2x
	public static enum FilterMode {
		NONE("None"),
		TRANSPARENT("Transparent"),
		BLEND("Blend"),
		ADDITIVE("Additive"),
		ADDALPHA("AddAlpha"),
		MODULATE("Modulate"),
		MODULATE2X("Modulate2x");

		String mdlText;

		FilterMode(final String str) {
			this.mdlText = str;
		}

		public String getMdlText() {
			return this.mdlText;
		}

		public static FilterMode fromId(final int id) {
			return values()[id];
		}

		public static int nameToId(final String name) {
			for (final FilterMode mode : values()) {
				if (mode.getMdlText().equals(name)) {
					return mode.ordinal();
				}
			}
			return -1;
		}

		@Override
		public String toString() {
			return getMdlText();
		}
	}

	public FilterMode filterMode = FilterMode.NONE;
	public int flags = 0;
	public int textureId = -1;
	public int textureAnimationId = -1;
	public long coordId = 0;
	public float alpha = 1;
	/** 
	 * @since 900
	 */
	public float emissiveGain = 1;
	/** 
	 * @since 1000
	 */
	public float[] fresnelColor = new float[] { 1, 1, 1 };
	/** 
	 * @since 1000
	 */
	public float fresnelOpacity = 0;
	/** 
	 * @since 1000
	 */
	public float fresnelTeamColor = 0;

	@Override
	public void readMdx(final LittleEndianDataInputStream stream, final int version) throws IOException {
		long size = ParseUtils.readUInt32(stream);

		this.filterMode = FilterMode.fromId((int) ParseUtils.readUInt32(stream));
		this.flags = stream.readInt(); // UInt32 in JS
		this.textureId = stream.readInt();
		this.textureAnimationId = stream.readInt();
		this.coordId = ParseUtils.readUInt32(stream);
		this.alpha = stream.readFloat();

		if (version > 800) {
			emissiveGain = stream.readFloat();

			if (version > 900) {
				fresnelColor = ParseUtils.readFloatArray(stream, 3);
				fresnelOpacity = stream.readFloat();
				fresnelTeamColor = stream.readFloat();

				size -= 20;
			}

			size -= 4;
		}

		readTimelines(stream, size - 28);
	}

	@Override
	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		ParseUtils.writeUInt32(stream, getByteLength(version));
		ParseUtils.writeUInt32(stream, this.filterMode.ordinal());
		ParseUtils.writeUInt32(stream, this.flags);
		stream.writeInt(this.textureId);
		stream.writeInt(this.textureAnimationId);
		ParseUtils.writeUInt32(stream, this.coordId);
		stream.writeFloat(this.alpha);

		if (version > 800) {
			stream.writeFloat(emissiveGain);

			if (version > 900) {
				ParseUtils.writeFloatArray(stream, fresnelColor);
				stream.writeFloat(fresnelOpacity);
				stream.writeFloat(fresnelTeamColor);
			}
		}

		writeTimelines(stream);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) throws IOException {
		final Iterator<String> iterator = readAnimatedBlock(stream);
		while (iterator.hasNext()) {
			final String token = iterator.next();
			switch (token) {
			case MdlUtils.TOKEN_FILTER_MODE:
				this.filterMode = FilterMode.fromId(FilterMode.nameToId(stream.read()));
				break;
			case MdlUtils.TOKEN_UNSHADED:
				this.flags |= 0x1;
				break;
			case MdlUtils.TOKEN_SPHERE_ENV_MAP:
				this.flags |= 0x2;
				break;
			case MdlUtils.TOKEN_TWO_SIDED:
				this.flags |= 0x10;
				break;
			case MdlUtils.TOKEN_UNFOGGED:
				this.flags |= 0x20;
				break;
			case MdlUtils.TOKEN_NO_DEPTH_TEST:
				this.flags |= 0x40;
				break;
			case MdlUtils.TOKEN_NO_DEPTH_SET:
				this.flags |= 0x80;
				break;
			case "Unlit":
				this.flags |= 0x100;
			case MdlUtils.TOKEN_STATIC_TEXTURE_ID:
				this.textureId = stream.readInt();
				break;
			case MdlUtils.TOKEN_TEXTURE_ID:
				readTimeline(stream, AnimationMap.KMTF);
				break;
			case MdlUtils.TOKEN_TVERTEX_ANIM_ID:
				this.textureAnimationId = stream.readInt();
				break;
			case MdlUtils.TOKEN_COORD_ID:
				this.coordId = stream.readInt();
				break;
			case MdlUtils.TOKEN_STATIC_ALPHA:
				this.alpha = stream.readFloat();
				break;
			case MdlUtils.TOKEN_ALPHA:
				readTimeline(stream, AnimationMap.KMTA);
				break;
			case "static EmissiveGain":
				emissiveGain = stream.readFloat();
				break;
			case "EmissiveGain":
				readTimeline(stream, AnimationMap.KMTE);
				break;
			case "static FresnelColor":
				stream.readColor(fresnelColor);
				break;
			case "FresnelColor":
				readTimeline(stream, AnimationMap.KFC3);
				break;
			case "static FresnelOpacity":
				fresnelOpacity = stream.readFloat();
				break;
			case "FresnelOpacity":
				readTimeline(stream, AnimationMap.KFCA);
				break;
			case "static FresnelTeamColor":
				fresnelTeamColor = stream.readFloat();
				break;
			case "FresnelTeamColor":
				readTimeline(stream, AnimationMap.KFTC);
				break;
			default:
				throw new IllegalStateException("Unknown token in Layer: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) throws IOException {
		stream.startBlock(MdlUtils.TOKEN_LAYER);

		stream.writeAttrib(MdlUtils.TOKEN_FILTER_MODE, this.filterMode.getMdlText());

		if ((this.flags & 0x1) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_UNSHADED);
		}

		if ((this.flags & 0x2) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_SPHERE_ENV_MAP);
		}

		if ((this.flags & 0x10) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_TWO_SIDED);
		}

		if ((this.flags & 0x20) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_UNFOGGED);
		}

		if ((this.flags & 0x40) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_NO_DEPTH_TEST);
		}

		if ((this.flags & 0x100) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_NO_DEPTH_SET);
		}

		if (version > 800 && (flags & 0x100) != 0) {
			stream.writeFlag("Unlit");
		}

		if (!writeTimeline(stream, AnimationMap.KMTF)) {
			stream.writeAttrib(MdlUtils.TOKEN_STATIC_TEXTURE_ID, this.textureId);
		}

		if (this.textureAnimationId != -1) {
			stream.writeAttrib(MdlUtils.TOKEN_TVERTEX_ANIM_ID, this.textureAnimationId);
		}

		if (this.coordId != 0) {
			stream.writeAttribUInt32(MdlUtils.TOKEN_COORD_ID, this.coordId);
		}

		if (!writeTimeline(stream, AnimationMap.KMTA) && (this.alpha != 1)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_ALPHA, this.alpha);
		}

		if (version > 800) {
			if (!writeTimeline(stream, AnimationMap.KMTE) && emissiveGain != 1) {
				stream.writeFloatAttrib("static EmissiveGain", emissiveGain);
			  }
		
			  if (!writeTimeline(stream, AnimationMap.KFC3) && (fresnelColor[0] != 1 || fresnelColor[1] != 1 || fresnelColor[2] != 1)) {
				stream.writeFloatArrayAttrib("static FresnelColor", fresnelColor);
			  }
		
			  if (!writeTimeline(stream, AnimationMap.KFCA) && fresnelOpacity != 0) {
				stream.writeFloatAttrib("static FresnelOpacity", fresnelOpacity);
			  }
		
			  if (!writeTimeline(stream, AnimationMap.KFTC) && fresnelTeamColor != 0) {
				stream.writeFloatAttrib("static FresnelTeamColor", fresnelTeamColor);
			  }
		}

		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		long byteLength = 28 + super.getByteLength(version);

		if (version > 800) {
			byteLength += 4;

			if (version > 900) {
				byteLength += 20;
			}
		}
		
		return byteLength;
	}
}
