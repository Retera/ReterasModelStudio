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


	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final int position = reader.position();
		final long size = reader.readUInt32();

		int sizeTracker = 4;
		filterMode = FilterMode.fromId(reader.readInt32());
		sizeTracker+=4;

		flags = reader.readInt32(); // UInt32 in JS
		sizeTracker+=4;

		textureId = reader.readInt32();
		sizeTracker+=4;

		textureAnimationId = reader.readInt32();
		sizeTracker+=4;

		coordId = reader.readInt32();
		sizeTracker+=4;

		alpha = reader.readFloat32();
		sizeTracker+=4;

		if (800 < version) {

			emissiveGain = reader.readFloat32();
			sizeTracker+=4;

		}
		if (900 < version) {
			reader.readFloat32Array(fresnelColor);
			sizeTracker+=4;
			sizeTracker+=4;
			sizeTracker+=4;

			fresnelOpacity = reader.readFloat32();
			sizeTracker+=4;

			fresnelTeamColor = reader.readFloat32();
			sizeTracker+=4;
		}
		if(1000 < version){

			hdFlag = reader.readInt32();
			sizeTracker+=4;

			int numTextures = reader.readInt32();
			sizeTracker+=4;

			sizeTracker = readAndDoStuff(reader, sizeTracker, numTextures);

		} else {
			hdTextureIds.add(textureId);
			hdTextureSlots.add(0);
		}

		int pos = reader.position();
		long tempSize = size - (reader.position() - position);

		while (0 < tempSize) {
			final War3ID name = new War3ID(reader.readTag());
			MdlxTimeline<?> timeline = getTimeline(reader, name);
			if(timeline != null){
				if(War3ID.fromString("KMTF").equals(name) && hdTextureIds.size() == 1){
					textureIdTimelineMap.put(0, timeline);
				} else {
					timelines.add(timeline);
				}
				tempSize -=  timeline.getByteLength();
			} else {
				System.out.println("couldn't find tag for: " + name);
				break;
			}
		}

		sizeTracker += (reader.position() - pos);
		for(; sizeTracker< size; sizeTracker+=4){
				System.out.println(sizeTracker/4 + " (" + sizeTracker + "): " + reader.readInt32());
		}
	}

	private int readAndDoStuff(BinaryReader reader, int sizeTracker, int numTextures){

		for(int i = 0; i<numTextures; i++){
			int animOrTextureId = reader.readInt32();
			sizeTracker+=4;
			if(1024<animOrTextureId){
				int pos = reader.position();
				i--;
//				MdlxTimeline<?> timeline = getTimeline(animOrTextureId, reader);
				MdlxTimeline<?> timeline = getTimeline(reader, new War3ID(Integer.reverseBytes(animOrTextureId)));
				textureIdTimelineMap.put(i, timeline);
				sizeTracker += (reader.position() - pos);
			} else {
				int textureSlot = reader.readInt32();

				hdTextureIds.add(animOrTextureId);
				hdTextureSlots.add(textureSlot);
				sizeTracker+=4;
			}
		}
		textureId = numTextures == 0 ? 0 : hdTextureIds.get(0);
		return sizeTracker;
	}

	private MdlxTimeline<?> getTimeline(BinaryReader reader, War3ID name) {
		AnimationMap animationMap = AnimationMap.ID_TO_TAG.get(name);
		if(animationMap != null){
			final MdlxTimeline<?> timeline = animationMap.getNewTimeline();

			timeline.readMdx(reader);
			return timeline;
		}
		return null;
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		long byteLength = getByteLength(version);
		writer.writeUInt32(byteLength);
		writer.writeUInt32(filterMode.ordinal());
		writer.writeUInt32(flags);

		if(version < 1100){
			writer.writeInt32(textureId);
		} else {
			writer.writeInt32(0);
		}

		writer.writeInt32(textureAnimationId);
		writer.writeUInt32(coordId);
		writer.writeFloat32(alpha);

		if (800 < version) {
			writer.writeFloat32(emissiveGain);

			if (900 < version) {
				writer.writeFloat32Array(fresnelColor);
				writer.writeFloat32(fresnelOpacity);
				writer.writeFloat32(fresnelTeamColor);
			}
		}

		if(1000 < version){
			writer.writeInt32(hdFlag);
			writer.writeInt32(hdTextureIds.size());
			for(int i = 0; i < hdTextureIds.size(); i++){
				Integer textureID = hdTextureIds.get(i);
				textureID = (textureID == null || textureID == -1) ? 0 : textureID;
				writer.writeInt32(textureID);
				writer.writeInt32(hdTextureSlots.get(i));
				if(textureIdTimelineMap.get(i) != null){
					textureIdTimelineMap.get(i).writeMdx(writer);
				}
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
				case MdlUtils.TOKEN_FILTER_MODE -> filterMode = FilterMode.fromId(FilterMode.nameToId(stream.read()));
				case MdlUtils.TOKEN_UNSHADED -> flags |= 0x1;
				case MdlUtils.TOKEN_SPHERE_ENV_MAP -> flags |= 0x2;
				case MdlUtils.TOKEN_TWO_SIDED -> flags |= 0x10;
				case MdlUtils.TOKEN_UNFOGGED -> flags |= 0x20;
				case MdlUtils.TOKEN_NO_DEPTH_TEST -> flags |= 0x40;
				case MdlUtils.TOKEN_NO_DEPTH_SET -> flags |= 0x80;
				case MdlUtils.TOKEN_UNLIT -> flags |= 0x100;
				case MdlUtils.TOKEN_STATIC_TEXTURE_ID -> {
					textureId = stream.readInt();
					hdTextureIds.add(textureId);
					hdTextureSlots.add(hdTextureSlots.size());
				}
				case MdlUtils.TOKEN_TEXTURE_ID -> {
					final MdlxTimeline<?> timeline = AnimationMap.KMTF.getNewTimeline();
					timeline.readMdl(stream);
					textureIdTimelineMap.put(hdTextureIds.size(), timeline);
//					if (1000 < version) {
//						final MdlxTimeline<?> timeline = AnimationMap.KMTF.getNewTimeline();
//						timeline.readMdl(stream);
//						textureIdTimelineMap.put(hdTextureIds.size(), timeline);
//					} else {
//						readTimeline(stream, AnimationMap.KMTF);
//					}
					hdTextureIds.add(0);
					hdTextureSlots.add(0);
				}
				case MdlUtils.TOKEN_TVERTEX_ANIM_ID -> textureAnimationId = stream.readInt();
				case MdlUtils.TOKEN_COORD_ID -> coordId = stream.readInt();
				case MdlUtils.TOKEN_STATIC_ALPHA -> alpha = stream.readFloat();
				case MdlUtils.TOKEN_ALPHA -> readTimeline(stream, AnimationMap.KMTA);
				case MdlUtils.TOKEN_STATIC_EMISSIVE_GAIN, MdlUtils.TOKEN_STATIC_EMISSIVE -> emissiveGain = stream.readFloat();
				case MdlUtils.TOKEN_EMISSIVE_GAIN, MdlUtils.TOKEN_EMISSIVE -> readTimeline(stream, AnimationMap.KMTE);
				case MdlUtils.TOKEN_STATIC_FRESNEL_COLOR -> stream.readColor(fresnelColor);
				case MdlUtils.TOKEN_FRESNEL_COLOR -> readTimeline(stream, AnimationMap.KFC3);
				case MdlUtils.TOKEN_STATIC_FRESNEL_OPACITY -> fresnelOpacity = stream.readFloat();
				case MdlUtils.TOKEN_FRESNEL_OPACITY -> readTimeline(stream, AnimationMap.KFCA);
				case MdlUtils.TOKEN_STATIC_FRESNEL_TEAM_COLOR -> fresnelTeamColor = stream.readFloat();
				case MdlUtils.TOKEN_FRESNEL_TEAM_COLOR -> readTimeline(stream, AnimationMap.KFTC);
				default ->
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
				  stream.writeColor(MdlUtils.TOKEN_STATIC_FRESNEL_COLOR, fresnelColor);
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
