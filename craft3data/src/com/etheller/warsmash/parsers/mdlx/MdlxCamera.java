package com.etheller.warsmash.parsers.mdlx;

import java.io.IOException;

import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenInputStream;
import com.etheller.warsmash.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.etheller.warsmash.util.MdlUtils;
import com.etheller.warsmash.util.ParseUtils;
import com.google.common.io.LittleEndianDataOutputStream;
import com.hiveworkshop.util.BinaryReader;

public class MdlxCamera extends MdlxAnimatedObject {
	public String name = "";
	public float[] position = new float[3];
	public float fieldOfView = 0;
	public float farClippingPlane = 0;
	public float nearClippingPlane = 0;
	public float[] targetPosition = new float[3];

	public void readMdx(final BinaryReader reader, final int version) throws IOException {
		final long size = reader.readUInt32();

		this.name = reader.read(80);
		reader.readFloat32Array(this.position);
		this.fieldOfView = reader.readFloat32();
		this.farClippingPlane = reader.readFloat32();
		this.nearClippingPlane = reader.readFloat32();
		reader.readFloat32Array(this.targetPosition);

		readTimelines(reader, size - 120);
	}

	@Override
	public void writeMdx(final LittleEndianDataOutputStream stream, final int version) throws IOException {
		ParseUtils.writeUInt32(stream, getByteLength(version));
		final byte[] bytes = this.name.getBytes(ParseUtils.UTF8);
		stream.write(bytes);
		for (int i = 0; i < (80 - bytes.length); i++) {
			stream.write((byte) 0);
		}
		ParseUtils.writeFloatArray(stream, this.position);
		stream.writeFloat(this.fieldOfView);
		stream.writeFloat(this.farClippingPlane);
		stream.writeFloat(this.nearClippingPlane);
		ParseUtils.writeFloatArray(stream, this.targetPosition);

		writeTimelines(stream);
	}

	@Override
	public void readMdl(final MdlTokenInputStream stream, final int version) throws IOException {
		this.name = stream.read();

		for (final String token : stream.readBlock()) {
			switch (token) {
			case MdlUtils.TOKEN_POSITION:
				stream.readFloatArray(this.position);
				break;
			case MdlUtils.TOKEN_TRANSLATION:
				readTimeline(stream, AnimationMap.KCTR);
				break;
			case MdlUtils.TOKEN_ROTATION:
				readTimeline(stream, AnimationMap.KCRL);
				break;
			case MdlUtils.TOKEN_FIELDOFVIEW:
				this.fieldOfView = stream.readFloat();
				break;
			case MdlUtils.TOKEN_FARCLIP:
				this.farClippingPlane = stream.readFloat();
				break;
			case MdlUtils.TOKEN_NEARCLIP:
				this.nearClippingPlane = stream.readFloat();
				break;
			case MdlUtils.TOKEN_TARGET:
				for (final String subToken : stream.readBlock()) {
					switch (subToken) {
					case MdlUtils.TOKEN_POSITION:
						stream.readFloatArray(this.targetPosition);
						break;
					case MdlUtils.TOKEN_TRANSLATION:
						readTimeline(stream, AnimationMap.KTTR);
						break;
					default:
						throw new IllegalStateException(
								"Unknown token in Camera " + this.name + "'s Target: " + subToken);
					}
				}
				break;
			default:
				throw new IllegalStateException("Unknown token in Camera " + this.name + ": " + token);
			}
		}
	}

	@Override
	public void writeMdl(final MdlTokenOutputStream stream, final int version) throws IOException {
		stream.startObjectBlock(MdlUtils.TOKEN_CAMERA, this.name);

		stream.writeFloatArrayAttrib(MdlUtils.TOKEN_POSITION, this.position);
		writeTimeline(stream, AnimationMap.KCTR);
		writeTimeline(stream, AnimationMap.KCRL);
		stream.writeFloatAttrib(MdlUtils.TOKEN_FIELDOFVIEW, this.fieldOfView);
		stream.writeFloatAttrib(MdlUtils.TOKEN_FARCLIP, this.farClippingPlane);
		stream.writeFloatAttrib(MdlUtils.TOKEN_NEARCLIP, this.nearClippingPlane);

		stream.startBlock(MdlUtils.TOKEN_TARGET);
		stream.writeFloatArrayAttrib(MdlUtils.TOKEN_POSITION, this.targetPosition);
		writeTimeline(stream, AnimationMap.KTTR);
		stream.endBlock();

		stream.endBlock();
	}

	@Override
	public long getByteLength(final int version) {
		return 120 + super.getByteLength(version);
	}
}
