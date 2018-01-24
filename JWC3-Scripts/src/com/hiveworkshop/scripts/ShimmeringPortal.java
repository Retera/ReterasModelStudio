package com.hiveworkshop.scripts;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

public class ShimmeringPortal {

	public static void main(final String[] args) {
		final MDL portalMdl = MDL.read(new File("input/ShimmeringPortal.mdx"));

		final QuaternionRotation rotationTransform = new QuaternionRotation(Math.sqrt(0.5), 0, -Math.sqrt(0.5), 0);

		// transform pivots
		final IdObjectVisitor objectTransform = new IdObjectVisitor() {
			@Override
			public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			}

			@Override
			public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
				AnimFlag rotationFlag = null;
				for (final AnimFlag flag : particleEmitter.getAnimFlags()) {
					if (flag.getTypeId() == AnimFlag.ROTATION) {
						rotationFlag = flag;
					}
				}
				if (rotationFlag == null) {
					rotationFlag = new AnimFlag("Rotation");
					rotationFlag.setInterpType(InterpolationType.LINEAR);
					for (final Animation anim : portalMdl.getAnims()) {
						rotationFlag.addEntry(anim.getStart(),
								new QuaternionRotation(new Vertex(0, 1, 0), Math.PI / 2));
					}
					particleEmitter.add(rotationFlag);
				}
				particleEmitter.getParticleScaling().scale(0, 0, 0, 0.5, 0.5, 0.5);
				particleEmitter.setSpeed(particleEmitter.getSpeed() / 2);
			}

			@Override
			public void particleEmitter(final ParticleEmitter particleEmitter) {
			}

			@Override
			public void light(final Light light) {
			}

			@Override
			public void helper(final Helper object) {
			}

			@Override
			public void eventObject(final EventObject eventObject) {
			}

			@Override
			public void collisionShape(final CollisionShape collisionShape) {
				for (final Vertex vertex : collisionShape.getVertices()) {
					halfSizeThenFlipUpward(vertex);
				}
			}

			@Override
			public void camera(final Camera camera) {
				halfSizeThenFlipUpward(camera.getPosition());
				halfSizeThenFlipUpward(camera.getTargetPosition());
			}

			@Override
			public void bone(final Bone object) {
			}

			@Override
			public void attachment(final Attachment attachment) {
			}
		};
		for (final IdObject object : portalMdl.getIdObjects()) {
			final Vertex pivotPoint = object.getPivotPoint();
			halfSizeThenFlipUpward(pivotPoint);
			object.apply(objectTransform);
		}
		for (final Camera camera : portalMdl.getCameras()) {
			objectTransform.camera(camera);
		}

		// transform mesh
		final Set<QuaternionRotation> unchangedQuats = new HashSet<>();

		int geosetId = 0;
		final Set<IdObject> bonesToRotateBack = new HashSet<>();
		for (final Geoset geoset : portalMdl.getGeosets()) {
			for (final GeosetVertex vertex : geoset.getVertices()) {
				halfSizeThenFlipUpward(vertex);
				if (geosetId == 0 || geosetId == 1 || geosetId == 2 || geosetId == 5) {
					final List<Bone> boneAttachments = vertex.getBoneAttachments();
					bonesToRotateBack.addAll(boneAttachments);
					for (final Bone bone : boneAttachments) {
						final IdObject parent = bone.getParent();
						if (parent != null) {
							bonesToRotateBack.add(parent);
						}
						if (bone.getName().startsWith("ball")) {
							bone.add("DontInherit { Rotation }");
						}
					}
				}
			}
			if (geosetId == 0 || geosetId == 1 || geosetId == 2 || geosetId == 5) {
				for (final GeosetVertex vertex : geoset.getVertices()) {
					final Vertex pivotPoint = vertex.getBoneAttachments().get(0).getPivotPoint();
					vertex.rotate(pivotPoint.x, pivotPoint.y, pivotPoint.z, -Math.PI / 2, (byte) 0, (byte) 2);
				}
			}
			geosetId++;
		}
		for (final IdObject bone : bonesToRotateBack) {
			for (final AnimFlag flag : bone.getAnimFlags()) {
				if (flag.getTypeId() == AnimFlag.ROTATION) {
					for (int i = 0; i < flag.size(); i++) {
						final QuaternionRotation keyFrameValue = (QuaternionRotation) flag.getValues().get(i);
						unchangedQuats.add(keyFrameValue);
					}
					if (flag.tans()) {
						for (int i = 0; i < flag.size(); i++) {
							final QuaternionRotation inTanFrameValue = (QuaternionRotation) flag.getInTans().get(i);
							unchangedQuats.add(inTanFrameValue);
							final QuaternionRotation outTanFrameValue = (QuaternionRotation) flag.getOutTans().get(i);
							unchangedQuats.add(outTanFrameValue);
						}
					}
				}
			}
			if (bone.getName().equals("Dummy02")) {
				final Iterator<AnimFlag> iterator = bone.getAnimFlags().iterator();
				while (iterator.hasNext()) {
					final AnimFlag flag = iterator.next();
					if (flag.getTypeId() == AnimFlag.SCALING) {
						iterator.remove();
					}
				}
			}
		}
		for (final AnimFlag flag : portalMdl.getAllAnimFlags()) {
			if (flag.getTypeId() == AnimFlag.TRANSLATION) {
				for (int i = 0; i < flag.size(); i++) {
					final Vertex keyFrameValue = (Vertex) flag.getValues().get(i);
					halfSizeThenFlipUpward(keyFrameValue);
				}
				if (flag.tans()) {
					for (int i = 0; i < flag.size(); i++) {
						final Vertex inTanFrameValue = (Vertex) flag.getInTans().get(i);
						halfSizeThenFlipUpward(inTanFrameValue);
						final Vertex outTanFrameValue = (Vertex) flag.getOutTans().get(i);
						halfSizeThenFlipUpward(outTanFrameValue);
					}
				}
			} else if (flag.getTypeId() == AnimFlag.SCALING) {
				for (int i = 0; i < flag.size(); i++) {
					final Vertex keyFrameValue = (Vertex) flag.getValues().get(i);
					flipUpwardScale(keyFrameValue);
				}
				if (flag.tans()) {
					for (int i = 0; i < flag.size(); i++) {
						final Vertex inTanFrameValue = (Vertex) flag.getInTans().get(i);
						flipUpwardScale(inTanFrameValue);
						final Vertex outTanFrameValue = (Vertex) flag.getOutTans().get(i);
						flipUpwardScale(outTanFrameValue);
					}
				}
			} else if (flag.getTypeId() == AnimFlag.ROTATION) {
				for (int i = 0; i < flag.size(); i++) {
					final QuaternionRotation keyFrameValue = (QuaternionRotation) flag.getValues().get(i);
					if (!unchangedQuats.contains(keyFrameValue)) {
						flipUpward(keyFrameValue, rotationTransform);
					}
				}
				if (flag.tans()) {
					for (int i = 0; i < flag.size(); i++) {
						final QuaternionRotation inTanFrameValue = (QuaternionRotation) flag.getInTans().get(i);
						if (!unchangedQuats.contains(inTanFrameValue)) {
							flipUpward(inTanFrameValue, rotationTransform);
						}
						final QuaternionRotation outTanFrameValue = (QuaternionRotation) flag.getOutTans().get(i);
						if (!unchangedQuats.contains(outTanFrameValue)) {
							flipUpward(outTanFrameValue, rotationTransform);
						}
					}
				}
			}
		}

		portalMdl.printTo(new File("output/CustomPortal.mdx"));
	}

	public static void halfSizeThenFlipUpward(final Vertex vertex) {
		vertex.scale(0, 0, 0, 0.5, 0.5, 0.5);
		final double temp = vertex.x;
		vertex.x = -vertex.z;
		vertex.z = temp;
	}

	public static void flipUpwardScale(final Vertex vertex) {
		final double temp = vertex.x;
		vertex.x = vertex.z;
		vertex.z = temp;
	}

	public static void flipUpward(final Vertex vertex) {
		final double temp = vertex.x;
		vertex.x = -vertex.z;
		vertex.z = temp;
	}

	public static void flipUpward(final QuaternionRotation rotation, final QuaternionRotation sourceRotation) {
		final double angleAroundAxis = rotation.getAngleAroundAxis();
		final Vertex axisOfRotation = rotation.getAxisOfRotation();
		rotation.set(sourceRotation.applyToVertex(new Vertex(0, 0, 0), axisOfRotation), angleAroundAxis);
		rotation.normalize();
		// final Vertex euler = vertex.toEuler();
		// // roll, pitch, yaw
		// final double temp = euler.z;// yaw
		// euler.z = -euler.x;
		// euler.x = temp;
		// final QuaternionRotation resultQuaternion = new QuaternionRotation(euler);
		// vertex.a = resultQuaternion.a;
		// vertex.b = resultQuaternion.b;
		// vertex.c = resultQuaternion.c;
		// vertex.d = resultQuaternion.d;
		// final QuaternionRotation resultQuaternion = rotation.hamiltonianProduct(sourceRotation);
		// rotation.a = resultQuaternion.a;
		// rotation.b = resultQuaternion.b;
		// rotation.c = resultQuaternion.c;
		// rotation.d = resultQuaternion.d;
	}

}
