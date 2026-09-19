package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.util.ModelUtils;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

public class CameraChunk {
	public Camera[] camera = new Camera[0];

	public static final String key = "CAMS";

		public void load(final BlizzardDataInputStream in, final int version) throws IOException {
		MdxUtils.checkId(in, "CAMS");
		final int chunkSize = in.readInt();
		final List<Camera> cameraList = new ArrayList();
		int cameraCounter = chunkSize;
		while (cameraCounter > 0) {
			final Camera tempcamera = new Camera();
			cameraList.add(tempcamera);
			tempcamera.load(in, version);
			cameraCounter -= tempcamera.loadedInclusiveSize;
		}
		camera = cameraList.toArray(new Camera[cameraList.size()]);
	}

	public void save(final BlizzardDataOutputStream out, final int version) throws IOException {
		final int nrOfCameras = camera.length;
		out.writeNByteString("CAMS", 4);
		out.writeInt(getSize(version) - 8);// ChunkSize
		for (int i = 0; i < camera.length; i++) {
			camera[i].save(out, version);
		}

	}

	public int getSize(final int version) {
		int a = 0;
		a += 4;
		a += 4;
		for (int i = 0; i < camera.length; i++) {
			a += camera[i].getSize(version);
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
		/**
		 * Camera visibility track ("KCVS").
		 */
		public CameraVisibility cameraVisibility;
		/**
		 * MDX 1800+ depth of field focus distance ("IDUF").
		 */
		public CameraFocusDistance cameraFocusDistance;
		/**
		 * MDX 1800+ depth of field focal length ("ELAF") and f-stop ("PTSF").
		 */
		public CameraFocalLength cameraFocalLength;
		public CameraFStop cameraFStop;
		/**
		 * Inclusive size read from the file (without the header byte), used to skip unknown trailing data.
		 */
		public int loadedInclusiveSize;
		/**
		 * MDX 1800+ stores an extra byte in the top 8 bits of the camera inclusive size. Every stock 3.0 camera
		 * has the value 3 there. We preserve whatever was loaded, and write 3 for cameras created from scratch.
		 */
		public static final int DEFAULT_HEADER_BYTE_V1800 = 3;
		public int headerByte = -1;

		public void load(final BlizzardDataInputStream in, final int version) throws IOException {
			final int inclusiveSizeRaw = in.readInt();
			if (ModelUtils.isCameraDepthOfFieldSupported(version)) {
				headerByte = inclusiveSizeRaw >>> 24;
			}
			final int inclusiveSize = inclusiveSizeRaw & 0xFFFFFF;
			loadedInclusiveSize = inclusiveSize;
			name = in.readCharsAsString(80);
			position = MdxUtils.loadFloatArray(in, 3);
			fieldOfView = in.readFloat();
			farClippingPlane = in.readFloat();
			nearClippingPlane = in.readFloat();
			targetPosition = MdxUtils.loadFloatArray(in, 3);
			for (int i = 0; i < 7; i++) {
				if (MdxUtils.checkOptionalId(in, CameraFocusDistance.key)) {
					cameraFocusDistance = new CameraFocusDistance();
					cameraFocusDistance.load(in);
				} else if (MdxUtils.checkOptionalId(in, CameraFocalLength.key)) {
					cameraFocalLength = new CameraFocalLength();
					cameraFocalLength.load(in);
				} else if (MdxUtils.checkOptionalId(in, CameraFStop.key)) {
					cameraFStop = new CameraFStop();
					cameraFStop.load(in);
				} else if (MdxUtils.checkOptionalId(in, CameraPositionTranslation.key)) {
					cameraPositionTranslation = new CameraPositionTranslation();
					cameraPositionTranslation.load(in);
				} else if (MdxUtils.checkOptionalId(in, CameraTargetTranslation.key)) {
					cameraTargetTranslation = new CameraTargetTranslation();
					cameraTargetTranslation.load(in);
				} else if (MdxUtils.checkOptionalId(in, CameraRotation.key)) {
					cameraRotation = new CameraRotation();
					cameraRotation.load(in);
				} else if (MdxUtils.checkOptionalId(in, CameraVisibility.key)) {
					cameraVisibility = new CameraVisibility();
					cameraVisibility.load(in);
				}

			}
			final int consumed = getSize(version);
			if (inclusiveSize > consumed) {
				// unknown trailing data (for example a camera track from a newer game version): skip it
				System.err.println("Camera '" + name + "' has " + (inclusiveSize - consumed)
						+ " bytes of unknown trailing data (MDX version " + version + "); skipping");
				in.skip(inclusiveSize - consumed);
			}
		}

				public void save(final BlizzardDataOutputStream out, final int version) throws IOException {
			int inclusiveSize = getSize(version);
			if (ModelUtils.isCameraDepthOfFieldSupported(version)) {
				final int usedHeaderByte = headerByte == -1 ? DEFAULT_HEADER_BYTE_V1800 : headerByte;
				inclusiveSize |= (usedHeaderByte & 0xFF) << 24;
			}
			out.writeInt(inclusiveSize);// InclusiveSize
			out.writeNByteString(name, 80);
			if ((position.length % 3) != 0) {
				throw new IllegalArgumentException(
						"The array position needs either the length 3 or a multiple of this number. (got "
								+ position.length + ")");
			}
			MdxUtils.saveFloatArray(out, position);
			out.writeFloat(fieldOfView);
			out.writeFloat(farClippingPlane);
			out.writeFloat(nearClippingPlane);
			if ((targetPosition.length % 3) != 0) {
				throw new IllegalArgumentException(
						"The array targetPosition needs either the length 3 or a multiple of this number. (got "
								+ targetPosition.length + ")");
			}
			MdxUtils.saveFloatArray(out, targetPosition);
						// track order as written by Warcraft III 3.0: DoF tracks first, then position, rotation, target
			if (ModelUtils.isCameraDepthOfFieldSupported(version)) {
				if (cameraFocusDistance != null) {
					cameraFocusDistance.save(out);
				}
				if (cameraFocalLength != null) {
					cameraFocalLength.save(out);
				}
				if (cameraFStop != null) {
					cameraFStop.save(out);
				}
			}
			if (cameraPositionTranslation != null) {
				cameraPositionTranslation.save(out);
			}
			if (cameraRotation != null) {
				cameraRotation.save(out);
			}
			if (cameraTargetTranslation != null) {
				cameraTargetTranslation.save(out);
			}
			if (cameraVisibility != null) {
				cameraVisibility.save(out);
			}
		}

		public int getSize(final int version) {
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
			if (cameraVisibility != null) {
				a += cameraVisibility.getSize();
			}
						if (ModelUtils.isCameraDepthOfFieldSupported(version)) {
				if (cameraFocusDistance != null) {
					a += cameraFocusDistance.getSize();
				}
				if (cameraFocalLength != null) {
					a += cameraFocalLength.getSize();
				}
				if (cameraFStop != null) {
					a += cameraFStop.getSize();
				}
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
			headerByte = mdlCam.getHeaderByte();
			for (final AnimFlag af : mdlCam.getAnimFlags()) {
				if (af.getName().equals("Translation") && (af.size() > 0)) {
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
								} else if (af.getName().equals("DOFDistance") && (af.size() > 0)) {
					cameraFocusDistance = new CameraFocusDistance();
					cameraFocusDistance.globalSequenceId = af.getGlobalSeqId();
					cameraFocusDistance.interpolationType = af.getInterpType();
					cameraFocusDistance.translationTrack = new CameraFocusDistance.TranslationTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final CameraFocusDistance.TranslationTrack mdxEntry = cameraFocusDistance.new TranslationTrack();
						cameraFocusDistance.translationTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.focusDistance = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
								} else if (af.getName().equals("FocalLength") && (af.size() > 0)) {
					cameraFocalLength = new CameraFocalLength();
					cameraFocalLength.globalSequenceId = af.getGlobalSeqId();
					cameraFocalLength.interpolationType = af.getInterpType();
					cameraFocalLength.translationTrack = new CameraFocalLength.TranslationTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final CameraFocalLength.TranslationTrack mdxEntry = cameraFocalLength.new TranslationTrack();
						cameraFocalLength.translationTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.focalLength = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("FStop") && (af.size() > 0)) {
					cameraFStop = new CameraFStop();
					cameraFStop.globalSequenceId = af.getGlobalSeqId();
					cameraFStop.interpolationType = af.getInterpType();
					cameraFStop.translationTrack = new CameraFStop.TranslationTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final CameraFStop.TranslationTrack mdxEntry = cameraFStop.new TranslationTrack();
						cameraFStop.translationTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.fStop = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Visibility") && (af.size() > 0)) {
					cameraVisibility = new CameraVisibility();
					cameraVisibility.globalSequenceId = af.getGlobalSeqId();
					cameraVisibility.interpolationType = af.getInterpType();
					cameraVisibility.translationTrack = new CameraVisibility.TranslationTrack[af.size()];
					final boolean hasTans = af.tans();
					for (int i = 0; i < af.size(); i++) {
						final CameraVisibility.TranslationTrack mdxEntry = cameraVisibility.new TranslationTrack();
						cameraVisibility.translationTrack[i] = mdxEntry;
						final AnimFlag.Entry mdlEntry = af.getEntry(i);
						mdxEntry.visibility = ((Number) mdlEntry.value).floatValue();
						mdxEntry.time = mdlEntry.time.intValue();
						if (hasTans) {
							mdxEntry.inTan = ((Number) mdlEntry.inTan).floatValue();
							mdxEntry.outTan = ((Number) mdlEntry.outTan).floatValue();
						}
					}
				} else if (af.getName().equals("Rotation") && (af.size() > 0)) {
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
			for (final AnimFlag af : mdlCam.getTargetAnimFlags()) {
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
