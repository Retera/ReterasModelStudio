package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.timeline.MdlxTimeline;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;
import com.hiveworkshop.rms.util.War3ID;

import java.util.*;

public class MdlxLayer extends MdlxAnimatedObject {
	public FilterMode filterMode = FilterMode.NONE;
	public int flags = 0;
	public int textureId = -1;
	public int textureAnimationId = -1;
	public long coordId = 0;
	public float alpha = 1;
	public int hdFlag = 0;

	// since 900
	public float emissiveGain = 1;
	// since 1000
	public float[] fresnelColor = new float[] { 1, 1, 1 };
	public float fresnelOpacity = 0;
	public float fresnelTeamColor = 0;
	// since 1100
	public List<Integer> hdTextureIds = new ArrayList<>();
	public List<Integer> hdTextureSlots = new ArrayList<>();
	public Map<Integer, MdlxTimeline<?>> textureIdTimelineMap = new HashMap<>();


	String[] textureSlots = {
			"Diffuse",
			"Normal",
			"ORM",
			"Emissive",
			"TeamColor",
			"Env",
			"unknown",
	};

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final int position = reader.position();
		final long size = reader.readUInt32();
//		System.out.println("\nLAYER size: " + size);

		int sizeTracker = 4;
		filterMode = FilterMode.fromId(reader.readInt32());
//		System.out.println(sizeTracker/4 + " (" + sizeTracker + "): fMode " + filterMode);
		sizeTracker+=4;

		flags = reader.readInt32(); // UInt32 in JS
////		System.out.println(sizeTracker/4 + " (" + sizeTracker + "): flag  " + flags);
//		System.out.println(sizeTracker/4 + " (" + sizeTracker + "): flag  " + Integer.toBinaryString(flags) + " ("  + flags + ")");
		sizeTracker+=4;

		textureId = reader.readInt32();
//		System.out.println(sizeTracker/4 + " (" + sizeTracker + "): old-texId " + textureId);
		sizeTracker+=4;

		textureAnimationId = reader.readInt32();
//		System.out.println(sizeTracker/4 + " (" + sizeTracker + "): tAnim " + textureAnimationId);
		sizeTracker+=4;

		coordId = reader.readInt32();
//		System.out.println(sizeTracker/4 + " (" + sizeTracker + "): coord " + coordId);
		sizeTracker+=4;

		alpha = reader.readFloat32();
//		System.out.println(sizeTracker/4 + " (" + sizeTracker + "): alpha " + alpha);
		sizeTracker+=4;

		if (800 < version) {

			emissiveGain = reader.readFloat32();
//			System.out.println(sizeTracker/4 + " (" + sizeTracker + "): emGain " + emissiveGain);
			sizeTracker+=4;

		}
		if (900 < version) {
			reader.readFloat32Array(fresnelColor);
			sizeTracker+=4;
			sizeTracker+=4;
			sizeTracker+=4;

			fresnelOpacity = reader.readFloat32();
//			System.out.println(sizeTracker/4 + " (" + sizeTracker + "): frsOp " + fresnelOpacity);
			sizeTracker+=4;

			fresnelTeamColor = reader.readFloat32();
//			System.out.println(sizeTracker/4 + " (" + sizeTracker + "): fresTC " + fresnelTeamColor);
			sizeTracker+=4;
		}
		if(1000 < version){

			hdFlag = reader.readInt32();
//			System.out.println(sizeTracker/4 + " (" + sizeTracker + "): hdFlag(?) " + hdFlag);
			sizeTracker+=4;

			int numTextures = reader.readInt32();
//			System.out.println(sizeTracker/4 + " (" + sizeTracker + "): num-texs? " + numTextures);
			sizeTracker+=4;


			sizeTracker = readAndDoStuff(reader, sizeTracker, numTextures);

		} else {
			hdTextureIds.add(textureId);
			hdTextureSlots.add(0);
		}
//		System.out.println(sizeTracker + "/" + size + " layer bytes read");

		int pos = reader.position();
		readTimelines(reader, size - (reader.position() - position));
		if(hdTextureIds.size() == 1 && 0 < timelines.size()){
			MdlxTimeline<?> timeline = timelines.stream().filter(t -> t.name.equals(War3ID.fromString("KMTF"))).findFirst().orElse(null);
			if(timeline != null){
				textureIdTimelineMap.put(0, timeline);
			}
		}
		sizeTracker += (reader.position() - pos);
//		System.out.println(sizeTracker + "/" + size + " layer bytes read");
		for(; sizeTracker< size; sizeTracker+=4){
				System.out.println(sizeTracker/4 + " (" + sizeTracker + "): " + reader.readInt32());
//				System.out.println(sizeTracker/4 + " (" + sizeTracker + "): " + reader.read(4));
		}
	}

	private int readAndDoStuff(BinaryReader reader, int sizeTracker, int numTextures){

		for(int i = 0; i<numTextures; i++){
			int nameSlot = Math.min(textureSlots.length-1, i);
			int animOrTextureId = reader.readInt32();
			sizeTracker+=4;
			if(1024<animOrTextureId){
				int pos = reader.position();
				i--;
//				readTimeline(animOrTextureId, reader);
				MdlxTimeline<?> timeline = getTimeline(animOrTextureId, reader);
//				textureTimelines[i] = timeline;
				textureIdTimelineMap.put(i, timeline);
				sizeTracker += (reader.position() - pos);
			} else {
				int textureSlot = reader.readInt32();
//				hdTextureIds[i] = animOrTextureId;
//				hdTextureSlots[i] = textureSlot;

				hdTextureIds.add(animOrTextureId);
				hdTextureSlots.add(textureSlot);
//				System.out.println("~~~~ " + sizeTracker/4 + " (" + sizeTracker + "): " + i + " " + textureSlots[nameSlot] + " - id: " + hdTextureIds.get(i) + ", slot: " + hdTextureSlots.get(i));
////				System.out.println(sizeTracker/4 + " (" + sizeTracker + "): " + i*2 + " " + textureSlots[nameSlot] + "_id  : " + hdTextureIds[i]);
////				System.out.println(sizeTracker/4 + " (" + sizeTracker + "): " + (i*2+1) + " " + textureSlots[nameSlot] + "_slot: " + hdTextureSlots[i]);
				sizeTracker+=4;
			}
		}
//		textureId = numTextures == 0 ? 0 : hdTextureIds[0];
		textureId = numTextures == 0 ? 0 : hdTextureIds.get(0);
		return sizeTracker;
	}

	private int readAndPrintChunk(BinaryReader reader, int sizeTracker, long size) {
//		sizeTracker +=4;
		System.out.println("skipping \"texture\"(?): " + reader.readInt32());
		sizeTracker +=4;
		System.out.println("skipping \"slot\"(?): " + reader.readInt32());
		sizeTracker +=4;
//		System.out.println("layer " + sizeTracker/4 + " (" + sizeTracker + "): " + reader.read(4));
		int animOrSlot = reader.readInt32();
		System.out.println("layer " + sizeTracker/4 + " (" + sizeTracker + "): " + animOrSlot);
		sizeTracker +=4;
		if(1024<animOrSlot){
			int pos = reader.position();
			readTimeline(animOrSlot, reader);
			sizeTracker += (reader.position() - pos);
		}


		for (int i = sizeTracker; i < size; i+=4) {
				System.out.println(i/4 + " (" + i + "): " + reader.readInt32());
//			System.out.println("layer " + i/4 + " (" + i + "): " + reader.read(4));
		}
		return sizeTracker;
	}


	public void readTimeline(int revTag, final BinaryReader reader) {
		final War3ID name = new War3ID(Integer.reverseBytes(revTag));
		AnimationMap animationMap = AnimationMap.ID_TO_TAG.get(name);
//			if(animationMap != AnimationMap.KGTR && animationMap != AnimationMap.KGRT && animationMap != AnimationMap.KGSC)
//				System.out.println("reading timeline for: " + name);
		if(animationMap != null){
			final MdlxTimeline<?> timeline = animationMap.getNewTimeline();

			timeline.readMdx(reader);

			timelines.add(timeline);
//			System.out.println("~~~~ " + name + " (" + animationMap.getMdlToken() + ", " + timeline.frames.length + " frames)");
//			System.out.println("\t" + name + " (" + animationMap.getMdlToken() + ", " + timeline.frames.length + " frames)");
		} else {
			System.out.println("couldn't find tag for: " + name);
		}
	}
	public MdlxTimeline<?> getTimeline(int revTag, final BinaryReader reader) {
		final War3ID name = new War3ID(Integer.reverseBytes(revTag));
		AnimationMap animationMap = AnimationMap.ID_TO_TAG.get(name);
//			if(animationMap != AnimationMap.KGTR && animationMap != AnimationMap.KGRT && animationMap != AnimationMap.KGSC)
//				System.out.println("reading timeline for: " + name);
		if(animationMap != null){
			final MdlxTimeline<?> timeline = animationMap.getNewTimeline();

			timeline.readMdx(reader);
			return timeline;
		}
		return null;
	}


	public void readMdxORG(final BinaryReader reader, final int version) {
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
//		int sizeTracker = 0;
		long byteLength = getByteLength(version);
//		System.out.println("writing layer of size: " + byteLength);
//		int position = writer.position();
		writer.writeUInt32(byteLength);
		writer.writeUInt32(filterMode.ordinal());
		writer.writeUInt32(flags);
//		sizeTracker += 4;
//		sizeTracker += 4;
//		sizeTracker += 4;

		if(version < 1100){
			writer.writeInt32(textureId);
		} else {
			writer.writeInt32(0);
		}
//		sizeTracker += 4;

		writer.writeInt32(textureAnimationId);
		writer.writeUInt32(coordId);
		writer.writeFloat32(alpha);
//		sizeTracker += 4;
//		sizeTracker += 4;
//		sizeTracker += 4;

		if (800 < version) {
			writer.writeFloat32(emissiveGain);
//			sizeTracker += 4;

			if (900 < version) {
				writer.writeFloat32Array(fresnelColor);
				writer.writeFloat32(fresnelOpacity);
				writer.writeFloat32(fresnelTeamColor);
//				sizeTracker += 4 * fresnelColor.length;
//				sizeTracker += 4;
//				sizeTracker += 4;
			}
		}

		if(1000 < version){
			writer.writeInt32(hdFlag);
			writer.writeInt32(hdTextureIds.size());
			System.out.println("textures:" + hdTextureIds.size());
//			sizeTracker += 4;
//			sizeTracker += 4;
			for(int i = 0; i < hdTextureIds.size(); i++){
				Integer textureID = hdTextureIds.get(i);
				textureID = (textureID == null || textureID == -1) ? 0 : textureID;
				writer.writeInt32(textureID);
				writer.writeInt32(hdTextureSlots.get(i));
				System.out.println("texture " + i + "/" + hdTextureIds.size() + " - textureID:" + textureID);
				if(textureIdTimelineMap.get(i) != null){
					if(textureIdTimelineMap.get(i).values[0] instanceof long[]){
						long[] ugg = (long[]) textureIdTimelineMap.get(i).values[0];
						System.out.println("\ttextureIdTimeline: " + textureIdTimelineMap.get(i).name + " [0]: " + ugg[0] + " - " + textureIdTimelineMap.get(i));
					} else {
						System.out.println("\ttextureIdTimeline: " + textureIdTimelineMap.get(i).name + " [0]: " + textureIdTimelineMap.get(i).values[0] + " - " + textureIdTimelineMap.get(i));
					}
					textureIdTimelineMap.get(i).writeMdx(writer);
//					sizeTracker += integerMdlxTimelineMap.get(i).size();
				}
			}
		}

		writeTimelines(writer);
//		for (final MdlxTimeline<?> timeline : timelines) {
//			sizeTracker += timeline.getByteLength();
//		}
//		System.out.println("wrote " + sizeTracker + " bytes for LAYER (" + (writer.position()-position) + ")");
	}


	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
//		System.out.println("mdl layer, version: " + version);
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
				case MdlUtils.TOKEN_UNLIT:
					flags |= 0x100;
					break;
				case MdlUtils.TOKEN_STATIC_TEXTURE_ID:
					textureId = stream.readInt();
					System.out.println(MdlUtils.TOKEN_STATIC_TEXTURE_ID + ": " + hdTextureIds.size());
					hdTextureIds.add(textureId);
					hdTextureSlots.add(hdTextureSlots.size());
					break;
				case MdlUtils.TOKEN_TEXTURE_ID:
					System.out.println(MdlUtils.TOKEN_TEXTURE_ID + ": " + hdTextureIds.size());
					if(1000 < version){
						final MdlxTimeline<?> timeline = AnimationMap.KMTF.getNewTimeline();
						timeline.readMdl(stream);
						textureIdTimelineMap.put(hdTextureIds.size(), timeline);
					} else {
						readTimeline(stream, AnimationMap.KMTF);
					}
					hdTextureIds.add(0);
					hdTextureSlots.add(0);
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
				case MdlUtils.TOKEN_STATIC_EMISSIVE_GAIN:
					emissiveGain = stream.readFloat();
					break;
				case MdlUtils.TOKEN_STATIC_EMISSIVE:
					emissiveGain = stream.readFloat();
					break;
				case MdlUtils.TOKEN_EMISSIVE_GAIN:
					readTimeline(stream, AnimationMap.KMTE);
					break;
				case MdlUtils.TOKEN_EMISSIVE:
					readTimeline(stream, AnimationMap.KMTE);
					break;
				case MdlUtils.TOKEN_STATIC_FRESNEL_COLOR:
					stream.readColor(fresnelColor);
					break;
				case MdlUtils.TOKEN_FRESNEL_COLOR:
					readTimeline(stream, AnimationMap.KFC3);
					break;
				case MdlUtils.TOKEN_STATIC_FRESNEL_OPACITY:
					fresnelOpacity = stream.readFloat();
					break;
				case MdlUtils.TOKEN_FRESNEL_OPACITY:
					readTimeline(stream, AnimationMap.KFCA);
					break;
				case MdlUtils.TOKEN_STATIC_FRESNEL_TEAM_COLOR:
					fresnelTeamColor = stream.readFloat();
					break;
				case MdlUtils.TOKEN_FRESNEL_TEAM_COLOR:
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
			stream.writeFlag(MdlUtils.TOKEN_UNLIT);
		}

		if(1000 < version) {
			for(int i = 0; i < hdTextureIds.size(); i++){
				if(textureIdTimelineMap.get(i) != null){
					textureIdTimelineMap.get(i).writeMdl(stream);
				} else {
					stream.writeAttrib(MdlUtils.TOKEN_STATIC_TEXTURE_ID, hdTextureIds.get(i));
				}
			}
		} else if (!writeTimeline(stream, AnimationMap.KMTF)) {
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
				stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_EMISSIVE_GAIN, emissiveGain);
			}
		
			  if (!writeTimeline(stream, AnimationMap.KFC3) && (fresnelColor[0] != 1 || fresnelColor[1] != 1 || fresnelColor[2] != 1)) {
				  stream.writeFloatArrayAttrib(MdlUtils.TOKEN_STATIC_FRESNEL_COLOR, fresnelColor);
			  }
		
			  if (!writeTimeline(stream, AnimationMap.KFCA) && fresnelOpacity != 0) {
				  stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_FRESNEL_OPACITY, fresnelOpacity);
			  }
		
			  if (!writeTimeline(stream, AnimationMap.KFTC) && fresnelTeamColor != 0) {
				  stream.writeFloatAttrib(MdlUtils.TOKEN_STATIC_FRESNEL_TEAM_COLOR, fresnelTeamColor);
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
				if(1000 < version){
					byteLength += 8;
					if(!hdTextureSlots.isEmpty()){
						byteLength += 8L * hdTextureSlots.size();
					}

					for(MdlxTimeline<?> timeline : textureIdTimelineMap.values()){
						if(timeline != null){
							byteLength += timeline.getByteLength();
						}
					}
				}
			}
		}

		return byteLength;
	}
}
