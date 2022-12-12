package com.hiveworkshop.rms.parsers.mdlx;

import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenInputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlTokenOutputStream;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;

import javax.swing.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.Supplier;

public class MdlLoadSave {
	public static void loadMdl(MdlxModel mdlxModel, final ByteBuffer buffer) {
		String token;
		final MdlTokenInputStream stream = new MdlTokenInputStream(buffer);

		while ((token = stream.read()) != null) {
			try {
				switch (token) {
					case MdlUtils.TOKEN_VERSION -> loadVersionBlock(mdlxModel, stream);
					case MdlUtils.TOKEN_MODEL -> loadModelBlock(mdlxModel, stream);
					case MdlUtils.TOKEN_SEQUENCES -> loadNumberedObjectBlock(mdlxModel.version, mdlxModel.sequences, MdlxSequence::new, MdlUtils.TOKEN_ANIM, stream);
					case MdlUtils.TOKEN_GLOBAL_SEQUENCES -> loadGlobalSequenceBlock(mdlxModel.globalSequences, stream);
					case MdlUtils.TOKEN_TEXTURES -> loadNumberedObjectBlock(mdlxModel.version, mdlxModel.textures, MdlxTexture::new, MdlUtils.TOKEN_BITMAP, stream);
					case MdlUtils.TOKEN_MATERIALS -> loadNumberedObjectBlock(mdlxModel.version, mdlxModel.materials, MdlxMaterial::new, MdlUtils.TOKEN_MATERIAL, stream);
					case MdlUtils.TOKEN_TEXTURE_ANIMS -> loadNumberedObjectBlock(mdlxModel.version, mdlxModel.textureAnimations, MdlxTextureAnimation::new, MdlUtils.TOKEN_TVERTEX_ANIM_SPACE, stream);
					case MdlUtils.TOKEN_GEOSET -> loadObject(mdlxModel.version, mdlxModel.geosets, MdlxGeoset::new, stream);
					case MdlUtils.TOKEN_GEOSETANIM -> loadObject(mdlxModel.version, mdlxModel.geosetAnimations, MdlxGeosetAnimation::new, stream);
					case MdlUtils.TOKEN_BONE -> loadObject(mdlxModel.version, mdlxModel.bones, MdlxBone::new, stream);
					case MdlUtils.TOKEN_LIGHT -> loadObject(mdlxModel.version, mdlxModel.lights, MdlxLight::new, stream);
					case MdlUtils.TOKEN_HELPER -> loadObject(mdlxModel.version, mdlxModel.helpers, MdlxHelper::new, stream);
					case MdlUtils.TOKEN_ATTACHMENT -> loadObject(mdlxModel.version, mdlxModel.attachments, MdlxAttachment::new, stream);
					case MdlUtils.TOKEN_PIVOT_POINTS -> loadPivotPointBlock(mdlxModel.pivotPoints, stream);
					case MdlUtils.TOKEN_PARTICLE_EMITTER -> loadObject(mdlxModel.version, mdlxModel.particleEmitters, MdlxParticleEmitter::new, stream);
					case MdlUtils.TOKEN_PARTICLE_EMITTER2 -> loadObject(mdlxModel.version, mdlxModel.particleEmitters2, MdlxParticleEmitter2::new, stream);
					case MdlUtils.TOKEN_POPCORN_PARTICLE_EMITTER -> loadObject(mdlxModel.version, mdlxModel.particleEmittersPopcorn, MdlxParticleEmitterPopcorn::new, stream);
					case MdlUtils.TOKEN_RIBBON_EMITTER -> loadObject(mdlxModel.version, mdlxModel.ribbonEmitters, MdlxRibbonEmitter::new, stream);
					case MdlUtils.TOKEN_CAMERA -> loadObject(mdlxModel.version, mdlxModel.cameras, MdlxCamera::new, stream);
					case MdlUtils.TOKEN_EVENT_OBJECT -> loadObject(mdlxModel.version, mdlxModel.eventObjects, MdlxEventObject::new, stream);
					case MdlUtils.TOKEN_COLLISION_SHAPE -> loadObject(mdlxModel.version, mdlxModel.collisionShapes, MdlxCollisionShape::new, stream);
					case MdlUtils.TOKEN_FACE_FX -> loadObject(mdlxModel.version, mdlxModel.faceEffects, MdlxFaceEffect::new, stream);
					case MdlUtils.TOKEN_BIND_POSE -> loadBindPoseBlock(mdlxModel.bindPose, stream);
					default -> {
						if (token.startsWith("//")) {
							mdlxModel.comments.add(token.replaceFirst("// ?", ""));
						} else if (!token.matches("[\\d.{}\\-eE+]+")) {
							ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown chunk name: " + token);
						}
					}
				}
			} catch (Exception e) {
				ExceptionPopup.setFirstException(e);
				ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Exception while parsing chunk: " + token + ": " + e.toString());
				e.printStackTrace();
			}
		}
		new Thread(ExceptionPopup::displayIfNotEmpty).start();
	}

	private static void loadVersionBlock(MdlxModel mdlxModel, MdlTokenInputStream stream) {
		for (String token : stream.readBlock()) {
			if (MdlUtils.TOKEN_FORMAT_VERSION.equals(token)) {
				mdlxModel.version = stream.readInt();
			} else {
				int option = JOptionPane.showConfirmDialog(null,
						"Unknown token in Version: " + token + "\nTry to load anyway?",
						"Unknown Version", JOptionPane.YES_NO_OPTION);
				if (option != JOptionPane.YES_OPTION) {
					throw new IllegalStateException("Unknown token in Version: " + token);
				}
			}
		}
	}

	private static void loadModelBlock(MdlxModel mdlxModel, final MdlTokenInputStream stream) {
		mdlxModel.name = stream.read();
		for (final String token : stream.readBlock()) {
			if (token.startsWith("Num")) {
				/*-
				 * Don't care about the number of things, the arrays will grow as they wish.
				 * This includes:
				 *      NumGeosets
				 *      NumGeosetAnims
				 *      NumHelpers
				 *      NumLights
				 *      NumBones
				 *      NumAttachments
				 *      NumParticleEmitters
				 *      NumParticleEmitters2
				 *      NumRibbonEmitters
				 *      NumEvents
				 */
				stream.read();
			} else {
				switch (token) {
					case MdlUtils.TOKEN_BLEND_TIME -> mdlxModel.blendTime = stream.readUInt32();
					case MdlUtils.TOKEN_MINIMUM_EXTENT -> stream.readFloatArray(mdlxModel.extent.min);
					case MdlUtils.TOKEN_MAXIMUM_EXTENT -> stream.readFloatArray(mdlxModel.extent.max);
					case MdlUtils.TOKEN_BOUNDSRADIUS -> mdlxModel.extent.boundsRadius = stream.readFloat();
					default -> ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in Model: " + token);
				}
			}
		}
	}

	private static <E extends MdlxBlock> void loadNumberedObjectBlock(int version,
	                                                                  List<E> out, Supplier<E> constructor, String name, MdlTokenInputStream stream) {
		stream.read(); // Don't care about the number, the array will grow

		for (final String token : stream.readBlock()) {
			if (token.equals(name)) {
				final E object = constructor.get();

				object.readMdl(stream, version);

				out.add(object);
			} else {
				ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in " + name + ": " + token);
			}
		}
	}

	private static void loadGlobalSequenceBlock(List<Long> globalSequences, final MdlTokenInputStream stream) {
		stream.read(); // Don't care about the number, the array will grow

		for (final String token : stream.readBlock()) {
			if (token.equals(MdlUtils.TOKEN_DURATION)) {
				globalSequences.add(stream.readUInt32());
			} else {
				ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in GlobalSequences: " + token);
			}
		}
	}

	private static <E extends MdlxBlock> void loadObject(int version, List<E> out, Supplier<E> descriptor,
	                                                     final MdlTokenInputStream stream) {
		final E object = descriptor.get();

		object.readMdl(stream, version);

		out.add(object);
	}

	private static void loadPivotPointBlock(List<float[]> pivotPoints, final MdlTokenInputStream stream) {
		final int count = stream.readInt();

		stream.read(); // {

		for (int i = 0; i < count; i++) {
			pivotPoints.add(stream.readFloatArray(new float[3]));
		}

		stream.read(); // }
	}

	private static void loadBindPoseBlock(List<float[]> bindPose, final MdlTokenInputStream stream) {
		for (final String token : stream.readBlock()) {
			if (token.equals(MdlUtils.TOKEN_MATRICES)) {
				final int matrices = stream.readInt();

				stream.read(); // {

				for (int i = 0; i < matrices; i++) {
					bindPose.add(stream.readFloatArray(new float[12]));
				}

				stream.read(); // }
			} else {
				ExceptionPopup.addStringToShow("Line " + stream.getLineNumber() + ": Unknown token in BindPose: " + token);
			}
		}
	}

	public static ByteBuffer saveMdl(MdlxModel mdlxModel) {
		final MdlTokenOutputStream stream = new MdlTokenOutputStream();
		saveCommentBlock(mdlxModel.comments, stream);
		saveVersionBlock(mdlxModel.version, stream);
		saveModelBlock(mdlxModel, stream);
		saveStaticObjectsBlock(mdlxModel.version, stream, MdlUtils.TOKEN_SEQUENCES, mdlxModel.sequences);
		saveGlobalSequenceBlock(mdlxModel.globalSequences, stream);
		saveStaticObjectsBlock(mdlxModel.version, stream, MdlUtils.TOKEN_TEXTURES, mdlxModel.textures);
		saveStaticObjectsBlock(mdlxModel.version, stream, MdlUtils.TOKEN_MATERIALS, mdlxModel.materials);
		saveStaticObjectsBlock(mdlxModel.version, stream, MdlUtils.TOKEN_TEXTURE_ANIMS, mdlxModel.textureAnimations);
		saveObjects(mdlxModel.version, stream, mdlxModel.geosets);
		saveObjects(mdlxModel.version, stream, mdlxModel.geosetAnimations);
		saveObjects(mdlxModel.version, stream, mdlxModel.bones);
		saveObjects(mdlxModel.version, stream, mdlxModel.lights);
		saveObjects(mdlxModel.version, stream, mdlxModel.helpers);
		saveObjects(mdlxModel.version, stream, mdlxModel.attachments);
		savePivotPointBlock(mdlxModel.pivotPoints, stream);
		saveObjects(mdlxModel.version, stream, mdlxModel.particleEmitters);
		saveObjects(mdlxModel.version, stream, mdlxModel.particleEmitters2);

		if (mdlxModel.version > 800) {
			saveObjects(mdlxModel.version, stream, mdlxModel.particleEmittersPopcorn);
		}

		saveObjects(mdlxModel.version, stream, mdlxModel.ribbonEmitters);
		saveObjects(mdlxModel.version, stream, mdlxModel.cameras);
		saveObjects(mdlxModel.version, stream, mdlxModel.eventObjects);
		saveObjects(mdlxModel.version, stream, mdlxModel.collisionShapes);

		if (mdlxModel.version > 800) {
			saveObjects(mdlxModel.version, stream, mdlxModel.faceEffects);
			saveBindPoseBlock(mdlxModel.bindPose, stream);
		}

		return ByteBuffer.wrap(stream.buffer.toString().getBytes());
	}

	private static void saveCommentBlock(List<String> comments, final MdlTokenOutputStream stream) {
		for (String s : comments) {
			stream.writeLine("// " + s);
		}
	}

	private static void saveVersionBlock(int version, final MdlTokenOutputStream stream) {
		stream.startBlock(MdlUtils.TOKEN_VERSION);
		stream.writeAttrib(MdlUtils.TOKEN_FORMAT_VERSION, version);
		stream.endBlock();
	}

	private static void saveModelBlock(MdlxModel mdlxModel, final MdlTokenOutputStream stream) {
		stream.startObjectBlock(MdlUtils.TOKEN_MODEL, mdlxModel.name);
		stream.writeAttribUInt32(MdlUtils.TOKEN_BLEND_TIME, mdlxModel.blendTime);
		mdlxModel.extent.writeMdl(stream);
		stream.endBlock();
	}

	private static void saveStaticObjectsBlock(int version, final MdlTokenOutputStream stream, final String name,
	                                           final List<? extends MdlxBlock> objects) {
		if (!objects.isEmpty()) {
			stream.startBlock(name, objects.size());

			for (final MdlxBlock object : objects) {
				object.writeMdl(stream, version);
			}

			stream.endBlock();
		}
	}

	private static void saveGlobalSequenceBlock(List<Long> globalSequences, final MdlTokenOutputStream stream) {
		if (!globalSequences.isEmpty()) {
			stream.startBlock(MdlUtils.TOKEN_GLOBAL_SEQUENCES, globalSequences.size());

			for (final Long globalSequence : globalSequences) {
				stream.writeAttribUInt32(MdlUtils.TOKEN_DURATION, globalSequence);
			}

			stream.endBlock();
		}
	}

	private static void saveObjects(int version, final MdlTokenOutputStream stream, final List<? extends MdlxBlock> objects) {
		for (final MdlxBlock object : objects) {
			object.writeMdl(stream, version);
		}
	}

	private static void savePivotPointBlock(List<float[]> pivotPoints, final MdlTokenOutputStream stream) {
		if (!pivotPoints.isEmpty()) {
			stream.startBlock(MdlUtils.TOKEN_PIVOT_POINTS, pivotPoints.size());

			for (final float[] pivotPoint : pivotPoints) {
				stream.writeFloatArray(pivotPoint);
			}

			stream.endBlock();
		}
	}

	private static void saveBindPoseBlock(List<float[]> bindPose, final MdlTokenOutputStream stream) {
		if (!bindPose.isEmpty()) {
			stream.startBlock(MdlUtils.TOKEN_BIND_POSE);

			stream.startBlock(MdlUtils.TOKEN_MATRICES, bindPose.size());

			for (final float[] matrix : bindPose) {
				stream.writeFloatArray(matrix);
			}

			stream.endBlock();

			stream.endBlock();
		}
	}
}
