package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.BinaryReader;
import com.hiveworkshop.rms.util.BinaryWriter;

public class MdlxCamera extends MdlxAnimatedObject {
	public String name = "";
	public float[] position = new float[3];
	public float fieldOfView = 0;
	public float farClippingPlane = 0;
	public float nearClippingPlane = 0;
	public float[] targetPosition = new float[3];

	@Override
	public void readMdx(final BinaryReader reader, final int version) {
		final long size = reader.readUInt32();

		name = reader.read(80);
		reader.readFloat32Array(position);
		fieldOfView = reader.readFloat32();
		farClippingPlane = reader.readFloat32();
		nearClippingPlane = reader.readFloat32();
		reader.readFloat32Array(targetPosition);

		readTimelines(reader, size - 120);
	}

	@Override
	public void writeMdx(final BinaryWriter writer, final int version) {
		writer.writeUInt32(getByteLength(version));
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
				case MdlUtils.TOKEN_POSITION:
					stream.readFloatArray(position);
					break;
				case MdlUtils.TOKEN_TRANSLATION:
					readTimeline(stream, AnimationMap.KCTR);
					break;
				case MdlUtils.TOKEN_ROTATION:
					readTimeline(stream, AnimationMap.KCRL);
					break;
				case MdlUtils.TOKEN_FIELDOFVIEW:
					fieldOfView = stream.readFloat();
					break;
				case MdlUtils.TOKEN_FARCLIP:
					farClippingPlane = stream.readFloat();
					break;
				case MdlUtils.TOKEN_NEARCLIP:
					nearClippingPlane = stream.readFloat();
					break;
				case MdlUtils.TOKEN_TARGET:
					for (final String subToken : stream.readBlock()) {
						switch (subToken) {
							case MdlUtils.TOKEN_POSITION -> stream.readFloatArray(targetPosition);
							case MdlUtils.TOKEN_TRANSLATION -> readTimeline(stream, AnimationMap.KTTR);
							default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Camera " + name + "'s Target: " + subToken);
						}
					}
					break;
				default:
					ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Camera " + name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) {
		stream.startObjectBlock(MdlUtils.TOKEN_CAMERA, name);

		stream.writeFloatArrayAttrib(MdlUtils.TOKEN_POSITION, position);
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
