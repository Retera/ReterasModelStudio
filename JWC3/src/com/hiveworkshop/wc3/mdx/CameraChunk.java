package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Vertex;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class CameraChunk {
	public Camera[] camera = new Camera[0];

	public static final String key = "CAMS";

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "CAMS");
		final int chunkSize = in.readInt();
		final List<Camera> cameraList = new ArrayList();
		int cameraCounter = chunkSize;
		while (cameraCounter > 0) {
			final Camera tempcamera = new Camera();
			cameraList.add(tempcamera);
			tempcamera.load(in);
			cameraCounter -= tempcamera.getSize();
		}
		camera = cameraList.toArray(new Camera[cameraList.size()]);
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
		final int nrOfCameras = camera.length;
		out.writeNByteString("CAMS", 4);
		out.writeInt(getSize() - 8);// ChunkSize
		for (int i = 0; i < camera.length; i++) {
			camera[i].save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < camera.length; i++) {
			a += camera[i].getSize();
		}

		return a;
	}

	public class Camera {
		public String name = "";
		public float[] position = new float[3];
		public float fieldOfView;
		public float farClippingPlane;
		public float nearClippingPlane;
		public float[] targetPosition = new float[3];
		public CameraPositionTranslation cameraPositionTranslation;
		public CameraTargetTranslation cameraTargetTranslation;
		// TODO Needs rotation!
		public CameraRotation cameraRotation;

		public void load(final BlizzardDataInputStream in) throws IOException {
			final int inclusiveSize = in.readInt();
			name = in.readCharsAsString(80);
			position = MdxUtils.loadFloatArray(in, 3);
			fieldOfView = in.readFloat();
			farClippingPlane = in.readFloat();
			nearClippingPlane = in.readFloat();
			targetPosition = MdxUtils.loadFloatArray(in, 3);
			for (int i = 0; i < 3; i++) {
				if (MdxUtils.checkOptionalId(in, CameraPositionTranslation.key)) {
					cameraPositionTranslation = new CameraPositionTranslation();
					cameraPositionTranslation.load(in);
				} else if (MdxUtils.checkOptionalId(in, CameraTargetTranslation.key)) {
					cameraTargetTranslation = new CameraTargetTranslation();
					cameraTargetTranslation.load(in);
				} else if (MdxUtils.checkOptionalId(in, CameraRotation.key)) {
					cameraRotation = new CameraRotation();
					cameraRotation.load(in);
				}

			}
		}

		public void save(final BlizzardDataOutputStream out) throws IOException {
			out.writeInt(getSize());// InclusiveSize
			out.writeNByteString(name, 80);
			if (position.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array position needs either the length 3 or a multiple of this number. (got "
								+ position.length + ")");
			}
			MdxUtils.saveFloatArray(out, position);
			out.writeFloat(fieldOfView);
			out.writeFloat(farClippingPlane);
			out.writeFloat(nearClippingPlane);
			if (targetPosition.length % 3 != 0) {
				throw new IllegalArgumentException(
						"The array targetPosition needs either the length 3 or a multiple of this number. (got "
								+ targetPosition.length + ")");
			}
			MdxUtils.saveFloatArray(out, targetPosition);
			if (cameraPositionTranslation != null) {
				cameraPositionTranslation.save(out);
			}
			if (cameraTargetTranslation != null) {
				cameraTargetTranslation.save(out);
			}
			if (cameraRotation != null) {
				cameraRotation.save(out);
			}

		}

		public int getSize() {
			int a = 0;
			a += 4;
			a += 80;
			a += 12;
			a += 4;
			a += 4;
			a += 4;
			a += 12;
			if (cameraPositionTranslation != null) {
				a += cameraPositionTranslation.getSize();
			}
			if (cameraTargetTranslation != null) {
				a += cameraTargetTranslation.getSize();
			}
			if (cameraRotation != null) {
				a += cameraRotation.getSize();
			}

			return a;
		}

		public Camera() {

		}

		public Camera(final com.hiveworkshop.wc3.mdl.Camera mdlCam) {
			name = mdlCam.getName();
			position = mdlCam.getPosition().toFloatArray();
			fieldOfView = (float) mdlCam.getFieldOfView();
			farClippingPlane = (float) mdlCam.getFarClip();
			nearClippingPlane = (float) mdlCam.getNearClip();
			targetPosition = mdlCam.getTargetPosition().toFloatArray();
			for (final AnimFlag af : mdlCam.getAnimFlags()) {
				if (af.getName().equals("Translation")) {
					cameraPositionTranslation = new CameraPositionTranslation();
					cameraPositionTranslation.globalSequenceId = af.getGlobalSeqId();
					cameraPositionTranslation.interpolationType = af.getInterpType();
					cameraPositionTranslation.translationTrack = new CameraPositionTranslation.TranslationTrack[af
							.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final CameraPositionTranslation.TranslationTrack mdxEntry = cameraPositionTranslation.new TranslationTrack();
						cameraPositionTranslation.translationTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.translation = ((Vertex) mdlEntry.value).toFloatArray();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Vertex) mdlEntry.inTan).toFloatArray();
							mdxEntry.outTan = ((Vertex) mdlEntry.outTan).toFloatArray();
						}
					}
				} else if (af.getName().equals("Rotation")) {
					cameraRotation = new CameraRotation();
					cameraRotation.globalSequenceId = af.getGlobalSeqId();
					cameraRotation.interpolationType = af.getInterpType();
					cameraRotation.translationTrack = new CameraRotation.TranslationTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final CameraRotation.TranslationTrack mdxEntry = cameraRotation.new TranslationTrack();
						cameraRotation.translationTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.rotation = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else {
					if (Node.LOG_DISCARDED_FLAGS) {
						System.err.println("discarded flag " + af.getName());
					}
				}
			}
			for (final AnimFlag af : mdlCam.getAnimFlags()) {
				if (af.getName().equals("Translation")) {
					cameraTargetTranslation = new CameraTargetTranslation();
					cameraTargetTranslation.globalSequenceId = af.getGlobalSeqId();
					cameraTargetTranslation.interpolationType = af.getInterpType();
					cameraTargetTranslation.translationTrack = new CameraTargetTranslation.TranslationTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final CameraTargetTranslation.TranslationTrack mdxEntry = cameraTargetTranslation.new TranslationTrack();
						cameraTargetTranslation.translationTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.translation = ((Vertex) mdlEntry.value).toFloatArray();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Vertex) mdlEntry.inTan).toFloatArray();
							mdxEntry.outTan = ((Vertex) mdlEntry.outTan).toFloatArray();
						}
					}
				} else {
					if (Node.LOG_DISCARDED_FLAGS) {
						System.err.println("discarded flag " + af.getName());
					}
				}
			}
		}
	}
}
