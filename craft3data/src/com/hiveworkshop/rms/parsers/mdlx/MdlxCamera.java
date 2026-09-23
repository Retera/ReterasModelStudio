package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParseLog;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxCamera extends MdlxAnimatedObject {
	public String name = "";
	public float[] position = new float[3];
	public float fieldOfView = 0;
	public float farClippingPlane = 0;
	public float nearClippingPlane = 0;
	public float[] targetPosition = new float[3];
	/**
	 * MDX 1300+ (Forsaken Kingdom) keeps a header byte in the top 8 bits of the camera's inclusive size. Every
	 * stock 3.0 camera has 3 there; it is preserved when loaded and written as 3 for new cameras.
	 */
	public static final int DEFAULT_HEADER_BYTE = 3;
	public int headerByte = -1;

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final int startPosition = reader.position();
		long size = reader.readUInt32();
		if (MdlxVersion.hasCameraDepthOfField(version)) {
			headerByte = (int) (size >>> 24);
			size &= 0xFFFFFF;
		}

		name = reader.read(80);
		reader.readFloat32Array(position);
		fieldOfView = reader.readFloat32();
		farClippingPlane = reader.readFloat32();
		nearClippingPlane = reader.readFloat32();
		reader.readFloat32Array(targetPosition);

		readTimelines(reader, size - 120);
		// unknown trailing data (e.g. a track from a newer game version): skip it instead of desynchronising
		reader.position((int) (startPosition + size));
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		long size = getByteLength(version);
		if (MdlxVersion.hasCameraDepthOfField(version)) {
			size |= (long) ((headerByte < 0 ? DEFAULT_HEADER_BYTE : headerByte) & 0xFF) << 24;
		}
		writer.writeUInt32(size);
		writer.writeWithNulls(name, 80);
		writer.writeFloat32Array(position);
		writer.writeFloat32(fieldOfView);
		writer.writeFloat32(farClippingPlane);
		writer.writeFloat32(nearClippingPlane);
		writer.writeFloat32Array(targetPosition);

		writeTimelines(writer);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) {
		name = stream.read();

		for (final String token : stream.readBlock()) {
			switch (token) {
				case MdlUtils.TOKEN_POSITION -> stream.readFloatArray(position);
				case MdlUtils.TOKEN_TRANSLATION -> readTimeline(stream, AnimationMap.KCTR);
				case MdlUtils.TOKEN_ROTATION -> readTimeline(stream, AnimationMap.KCRL);
				case MdlUtils.TOKEN_FIELDOFVIEW -> fieldOfView = stream.readFloat();
				case MdlUtils.TOKEN_FARCLIP -> farClippingPlane = stream.readFloat();
				case MdlUtils.TOKEN_NEARCLIP -> nearClippingPlane = stream.readFloat();
				case MdlUtils.TOKEN_DOF_DISTANCE -> readTimeline(stream, AnimationMap.IDUF);
				case MdlUtils.TOKEN_FOCAL_LENGTH -> readTimeline(stream, AnimationMap.ELAF);
				case MdlUtils.TOKEN_FSTOP -> readTimeline(stream, AnimationMap.PTSF);
				case MdlUtils.TOKEN_TARGET -> readTargetChunk(stream);
				default -> MdlxParseLog.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Camera " + name + ": " + token);
			}
		}
	}

	private void readTargetChunk(MdlTokenInputStream stream) {
		for (final String subToken : stream.readBlock()) {
			switch (subToken) {
				case MdlUtils.TOKEN_POSITION -> stream.readFloatArray(targetPosition);
				case MdlUtils.TOKEN_TRANSLATION -> readTimeline(stream, AnimationMap.KTTR);
				default -> MdlxParseLog.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Camera " + name + "'s Target: " + subToken);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_CAMERA, name);

		stream.writeFloatArrayAttrib(MdlUtils.TOKEN_POSITION, position);
		if (MdlxVersion.hasCameraDepthOfField(version)) {
			// before the classic tracks, which is the order the game stores them in (IDUF ELAF PTSF KCTR KCRL KTTR)
			writeTimeline(stream, AnimationMap.IDUF);
			writeTimeline(stream, AnimationMap.ELAF);
			writeTimeline(stream, AnimationMap.PTSF);
		}
		writeTimeline(stream, AnimationMap.KCTR);
		writeTimeline(stream, AnimationMap.KCRL);
		stream.writeFloatAttrib(MdlUtils.TOKEN_FIELDOFVIEW, fieldOfView);
		stream.writeFloatAttrib(MdlUtils.TOKEN_FARCLIP, farClippingPlane);
		stream.writeFloatAttrib(MdlUtils.TOKEN_NEARCLIP, nearClippingPlane);

		stream.startBlock(MdlUtils.TOKEN_TARGET);
		stream.writeFloatArrayAttrib(MdlUtils.TOKEN_POSITION, targetPosition);
		writeTimeline(stream, AnimationMap.KTTR);
		stream.endBlock();

		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 120 + super.getByteLength(version);
	}
}
