package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

import java.util.Iterator;

public class MdlxLayer extends MdlxAnimatedObject {
	public enum FilterMode {
		NONE("None"),
		TRANSPARENT("Transparent"),
		BLEND("Blend"),
		ADDITIVE("Additive"),
		ADDALPHA("AddAlpha"),
		MODULATE("Modulate"),
		MODULATE2X("Modulate2x");

		String token;

		FilterMode(final String token) {
			this.token = token;
		}

		public static FilterMode fromId(final int id) {
			return values()[id];
		}

		public static int nameToId(final String name) {
			for (final FilterMode mode : values()) {
				if (mode.token.equals(name)) {
					return mode.ordinal();
				}
			}
			return -1;
		}

		public static FilterMode nameToFilter(final String name) {
			for (final FilterMode mode : values()) {
				if (mode.token.equals(name)) {
					return mode;
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return token;
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
	public void readMdx(final BinaryReader reader, final int version) {
		final int position = reader.position();
		final long size = reader.readUInt32();

		filterMode = FilterMode.fromId(reader.readInt32());
		flags = reader.readInt32(); // UInt32 in JS
		textureId = reader.readInt32();
		textureAnimationId = reader.readInt32();
		coordId = reader.readInt32();
		alpha = reader.readFloat32();

		if (version > 800) {
			emissiveGain = reader.readFloat32();

			if (version > 900) {
				reader.readFloat32Array(fresnelColor);
				fresnelOpacity = reader.readFloat32();
				fresnelTeamColor = reader.readFloat32();
			}
		}

		readTimelines(reader, size - (reader.position() - position));
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));
		writer.writeUInt32(filterMode.ordinal());
		writer.writeUInt32(flags);
		writer.writeInt32(textureId);
		writer.writeInt32(textureAnimationId);
		writer.writeUInt32(coordId);
		writer.writeFloat32(alpha);

		if (version > 800) {
			writer.writeFloat32(emissiveGain);

			if (version > 900) {
				writer.writeFloat32Array(fresnelColor);
				writer.writeFloat32(fresnelOpacity);
				writer.writeFloat32(fresnelTeamColor);
			}
		}

		writeTimelines(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		final Iterator<String> iterator = readAnimatedBlock(stream);
		while (iterator.hasNext()) {
			final String token = iterator.next();
			switch (token) {
			case MdlUtils.TOKEN_FILTER_MODE:
				filterMode = FilterMode.fromId(FilterMode.nameToId(stream.read()));
				break;
			case MdlUtils.TOKEN_UNSHADED:
				flags |= 0x1;
				break;
			case MdlUtils.TOKEN_SPHERE_ENV_MAP:
				flags |= 0x2;
				break;
			case MdlUtils.TOKEN_TWO_SIDED:
				flags |= 0x10;
				break;
			case MdlUtils.TOKEN_UNFOGGED:
				flags |= 0x20;
				break;
			case MdlUtils.TOKEN_NO_DEPTH_TEST:
				flags |= 0x40;
				break;
			case MdlUtils.TOKEN_NO_DEPTH_SET:
				flags |= 0x80;
				break;
			case "Unlit":
				flags |= 0x100;
			case MdlUtils.TOKEN_STATIC_TEXTURE_ID:
				textureId = stream.readInt();
				break;
			case MdlUtils.TOKEN_TEXTURE_ID:
				readTimeline(stream, AnimationMap.KMTF);
				break;
			case MdlUtils.TOKEN_TVERTEX_ANIM_ID:
				textureAnimationId = stream.readInt();
				break;
			case MdlUtils.TOKEN_COORD_ID:
				coordId = stream.readInt();
				break;
				case MdlUtils.TOKEN_STATIC_ALPHA:
					alpha = stream.readFloat();
					break;
				case MdlUtils.TOKEN_ALPHA:
					readTimeline(stream, AnimationMap.KMTA);
					break;
				case "static EmissiveGain":
					emissiveGain = stream.readFloat();
					break;
				case "static Emissive":
					emissiveGain = stream.readFloat();
					break;
				case "EmissiveGain":
					readTimeline(stream, AnimationMap.KMTE);
					break;
				case "Emissive":
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
					ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Layer: " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startBlock(MdlUtils.TOKEN_LAYER);

		stream.writeAttrib(MdlUtils.TOKEN_FILTER_MODE, filterMode.toString());

		if ((flags & 0x1) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_UNSHADED);
		}

		if ((flags & 0x2) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_SPHERE_ENV_MAP);
		}

		if ((flags & 0x10) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_TWO_SIDED);
		}

		if ((flags & 0x20) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_UNFOGGED);
		}

		if ((flags & 0x40) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_NO_DEPTH_TEST);
		}

		if ((flags & 0x100) != 0) {
			stream.writeFlag(MdlUtils.TOKEN_NO_DEPTH_SET);
		}

		if (version > 800 && (flags & 0x100) != 0) {
			stream.writeFlag("Unlit");
		}

		if (!writeTimeline(stream, AnimationMap.KMTF)) {
			stream.writeAttrib(MdlUtils.TOKEN_STATIC_TEXTURE_ID, textureId);
		}

		if (textureAnimationId != -1) {
			stream.writeAttrib(MdlUtils.TOKEN_TVERTEX_ANIM_ID, textureAnimationId);
		}

		if (coordId != 0) {
			stream.writeAttribUInt32(MdlUtils.TOKEN_COORD_ID, coordId);
		}

		if (!writeTimeline(stream, AnimationMap.KMTA) && (alpha != 1)) {
			stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_ALPHA, alpha);
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
