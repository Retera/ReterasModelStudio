package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.Camera.SourceNode;
import com.hiveworkshop.rms.editor.model.Camera.TargetNode;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.*;

/**
 * For rendering. Copied from ghostwolf's stuff
 */
public final class RenderModel {
	private final EditableModel model;
	public static final double MAGIC_RENDER_SHOW_CONSTANT = 0.75;
	private final List<AnimatedNode> sortedNodes = new ArrayList<>();
	private Quat inverseCameraRotation = new Quat(0, 0, 0, 1);
	private Quat inverseCameraRotationYSpin = new Quat(0, 0, 0, 1);
	private Quat inverseCameraRotationZSpin = new Quat(0, 0, 0, 1);
	private AnimatedRenderEnvironment animatedRenderEnvironment;

	private final Map<AnimatedNode, RenderNode> objectToRenderNode = new HashMap<>();
	private final Map<ParticleEmitter2, RenderParticleEmitter2View> emitterToRenderer = new HashMap<>();
	private final List<RenderParticleEmitter2> particleEmitters2 = new ArrayList<>();// TODO one per model, not instance
	private final List<RenderParticleEmitter2View> particleEmitterViews2 = new ArrayList<>();
	private final SoftwareParticleEmitterShader particleShader = new SoftwareParticleEmitterShader();

	private final RenderNode rootPosition;

	private boolean spawnParticles = true;
	//	private boolean allowInanimateParticles = false;
	private boolean allowInanimateParticles = true;

	private long lastConsoleLogTime = 0;

	// These guys form the corners of a 2x2 rectangle, for use in Ghostwolf particle emitter algorithm
	private final Vec4[] spacialVectors = {
			new Vec4(-1, 1, 0, 1),
			new Vec4(1, 1, 0, 1),
			new Vec4(1, -1, 0, 1),
			new Vec4(-1, -1, 0, 1),
			new Vec4(1, 0, 0, 1),
			new Vec4(0, 1, 0, 1),
			new Vec4(0, 0, 1, 1)};
	private final Vec4[] billboardBaseVectors = {
			new Vec4(0, 1, -1, 1),
			new Vec4(0, -1, -1, 1),
			new Vec4(0, -1, 1, 1),
			new Vec4(0, 1, 1, 1),
			new Vec4(0, 1, 0, 1),
			new Vec4(0, 0, 1, 1),
			new Vec4(1, 0, 0, 1)};
	private final Vec4[] billboardVectors = {
			new Vec4(0, 1, -1, 1),
			new Vec4(0, -1, -1, 1),
			new Vec4(0, -1, 1, 1),
			new Vec4(0, 1, 1, 1),
			new Vec4(0, 1, 0, 1),
			new Vec4(0, 0, 1, 1),
			new Vec4(1, 0, 0, 1)};
	private final ModelView modelView;

	public RenderModel(final EditableModel model, final ModelView modelView) {
		this.model = model;
		this.modelView = modelView;
		rootPosition = new RenderNode(this, new Bone("RootPositionHack"));
		// Some classes doesn't call refreshFromEditor which leads to null-pointers when these in nor initialised
	}

	public void setSpawnParticles(final boolean spawnParticles) {
		this.spawnParticles = spawnParticles;
	}

	public void setAllowInanimateParticles(final boolean allowInanimateParticles) {
		this.allowInanimateParticles = allowInanimateParticles;
	}

	public RenderNode getRenderNode(final AnimatedNode idObject) {
		final RenderNode renderNode = objectToRenderNode.get(idObject);
		if (renderNode == null) {
			return rootPosition;
		}
		return renderNode;
	}

	public RenderNode getRenderNodeByObjectId(final int objectId) {
		return getRenderNode(model.getIdObject(objectId));
	}

	public AnimatedRenderEnvironment getAnimatedRenderEnvironment() {
		return animatedRenderEnvironment;
	}

	public void refreshFromEditor(final AnimatedRenderEnvironment animatedRenderEnvironment, final Quat inverseCameraRotation, final Quat inverseCameraRotationYSpin, final Quat inverseCameraRotationZSpin, final RenderResourceAllocator renderResourceAllocator) {
		particleEmitterViews2.clear();
		particleEmitters2.clear();
		this.animatedRenderEnvironment = animatedRenderEnvironment;
		this.inverseCameraRotation = inverseCameraRotation;
		this.inverseCameraRotationYSpin = inverseCameraRotationYSpin;
		this.inverseCameraRotationZSpin = inverseCameraRotationZSpin;

		for (int i = 0; i < billboardVectors.length; i++) {
//			inverseCameraRotation.transform(billboardBaseVectors[i], billboardVectors[i]);
			billboardVectors[i].set(Vec4.getTransformed(billboardBaseVectors[i], inverseCameraRotation));
		}

		sortedNodes.clear();
		for (final Camera camera : model.getCameras()) {
			final SourceNode object = camera.getSourceNode();
			sortedNodes.add(object);
			RenderNode renderNode = objectToRenderNode.get(object);
			if (renderNode == null) {
				renderNode = new RenderNode(this, object);
				objectToRenderNode.put(object, renderNode);
			}
		}
		setupHierarchy2(null);
		for (final Camera camera : model.getCameras()) {
			final TargetNode object = camera.getTargetNode();
			sortedNodes.add(object);
			RenderNode renderNode = objectToRenderNode.get(object);
			if (renderNode == null) {
				renderNode = new RenderNode(this, object);
				objectToRenderNode.put(object, renderNode);
			}
		}

		for (final ParticleEmitter2 particleEmitter : model.getParticleEmitter2s()) {
			particleEmitters2.add(new RenderParticleEmitter2(particleEmitter, renderResourceAllocator.allocateTexture(particleEmitter.getTexture(), particleEmitter)));
		}
		particleEmitters2.sort(Comparator.comparingInt(RenderParticleEmitter2::getPriorityPlane));
//		System.out.println("refresh from renderer, partEm: " + particleEmitters2.size());

		for (final RenderParticleEmitter2 particleEmitter : particleEmitters2) {
			final RenderParticleEmitter2View emitterView = new RenderParticleEmitter2View(this, particleEmitter);
//			System.out.println("emitterView: " + emitterView + " emitterView.em: " + emitterView.getEmitter());
			particleEmitterViews2.add(emitterView);
			emitterToRenderer.put(emitterView.getEmitter(), emitterView);
		}
		for (final AnimatedNode node : sortedNodes) {
			getRenderNode(node).refreshFromEditor();
		}
	}

	private void setupHierarchy(IdObject parent) {
		for (IdObject object : model.getIdObjects()) {
			if (object.getParent() == parent) {
				sortedNodes.add(object);
				RenderNode renderNode = objectToRenderNode.get(object);
				if (renderNode == null) {
					renderNode = new RenderNode(this, object);
					objectToRenderNode.put(object, renderNode);
				}
				setupHierarchy(object);
			}
		}
	}

	private void setupHierarchy2(IdObject parent) {
		if (parent == null) {
			for (IdObject object : model.getIdObjects()) {
				if (object.getParent() == null) {
					sortedNodes.add(object);
					RenderNode renderNode = objectToRenderNode.get(object);
					if (renderNode == null) {
						renderNode = new RenderNode(this, object);
						objectToRenderNode.put(object, renderNode);
					}
					setupHierarchy2(object);
				}
			}
		} else {
			for (IdObject object : parent.getChildrenNodes()) {
				sortedNodes.add(object);
				RenderNode renderNode = objectToRenderNode.get(object);
				if (renderNode == null) {
					renderNode = new RenderNode(this, object);
					objectToRenderNode.put(object, renderNode);
				}
				setupHierarchy2(object);
			}
		}
	}

	public void setCameraRotations(Quat inverseCameraRotation, Quat inverseCameraRotationYSpin, Quat inverseCameraRotationZSpin) {
		this.inverseCameraRotation = inverseCameraRotation;
		this.inverseCameraRotationYSpin = inverseCameraRotationYSpin;
		this.inverseCameraRotationZSpin = inverseCameraRotationZSpin;
	}

	public void updateNodes(boolean soft, boolean particles) {
		updateNodes(false, soft, particles);
	}

	public void updateNodes(boolean particles) {
		updateNodes(true, false, particles);
	}

	// Soft is to only update billborded
	public void updateNodes(boolean forced, boolean soft, boolean particles) {
		if ((animatedRenderEnvironment == null) || (animatedRenderEnvironment.getCurrentAnimation() == null)) {
			for (final AnimatedNode idObject : sortedNodes) {
				getRenderNode(idObject).resetTransformation();
				getRenderNode(idObject).getWorldMatrix().setIdentity();
			}
			if (particles && allowInanimateParticles) {
				updateParticles();
			}
			return;
		}
		for (AnimatedNode idObject : sortedNodes) {
			RenderNode node = getRenderNode(idObject);
			AnimatedNode idObjectParent = null;
			if (idObject instanceof IdObject) {
				idObjectParent = ((IdObject) idObject).getParent();
			}
			RenderNode parent = idObjectParent == null ? null : getRenderNode(idObjectParent);
			boolean objectVisible = idObject.getRenderVisibility(animatedRenderEnvironment) >= MAGIC_RENDER_SHOW_CONSTANT;

			boolean nodeVisible = forced || objectVisible;
//			boolean nodeVisible = forced || (((parent == null) || parent.visible) && objectVisible);

			node.visible = nodeVisible;

			// Every node only needs to be updated if this is a forced update, or if both
			// the parent node and the generic object corresponding to this node are visible.
			// Incoming messy code for optimizations!
			// --- All copied from Ghostwolf
			boolean dirty = forced || (parent != null && parent.dirty) || node.billboarded;
//			if (nodeVisible) {
			if (nodeVisible || forced || !soft) {
				// TODO variants
				Vec3 localLocation = new Vec3(0, 0, 0);
				Quat localRotation = new Quat(0, 0, 0, 1);
				Vec3 localScale = new Vec3(1, 1, 1);

				// Only update the local data if there is a need to
				if (forced || (!soft && idObject.getAnimFlags().size() > 0)) {
					dirty = true;

					// Translation
					Vec3 renderTranslation = idObject.getRenderTranslation(animatedRenderEnvironment);
					if (renderTranslation != null) {
						localLocation.set(renderTranslation);
					}

					// Rotation
					try {
						Quat renderRotation = idObject.getRenderRotation(animatedRenderEnvironment);
						if (renderRotation != null) {
							localRotation.set(renderRotation);
						}
					} catch (Exception e) {
						long currentTime = System.currentTimeMillis();
						if (lastConsoleLogTime < currentTime) {
							System.out.println("RenderModel#updateNodes: failed to update rotation for " + idObject.getName());
							lastConsoleLogTime = currentTime + 500;
						}
					}

					// Scale
					Vec3 renderScale = idObject.getRenderScale(animatedRenderEnvironment);
					if (renderScale != null) {
						localScale.set(renderScale);
					}

					node.setTransformation(localLocation, localRotation, localScale);
				}
				node.dirty = dirty;
				// Billboarding
				boolean wasDirty = RotateAndStuffBillboarding(node, parent, localRotation);

				boolean wasReallyDirty = forced || dirty || wasDirty || (parent != null && parent.wasDirty);
				node.wasDirty = wasReallyDirty;

				// If this is a forced upate, or this node's local data was updated, or the
				// parent node updated, do a full world update.

				if (wasReallyDirty) {
					node.recalculateTransformation();
				}

				// If there is an instance object associated with this node, and the node is
				// visible (which might not be the case for a forced update!), update the object.
				// This includes attachments and emitters.

				// TODO instanced rendering in 2090
				if (objectVisible) {
					if (particles) {
						final RenderParticleEmitter2View renderer = emitterToRenderer.get(idObject);
//						System.out.println("render: " + renderer);
						if (renderer != null) {
							if ((modelView == null) || modelView.getEditableIdObjects().contains(idObject) || modelView.isVetoOverrideParticles()) {
								renderer.fill();
							}
						}
					}
				}
			}
		}
		if (particles) {
			updateParticles();
		}

	}

	public boolean RotateAndStuffBillboarding(RenderNode node, RenderNode parent, Quat localRotation) {
		boolean wasDirty = false;
		// If the instance is not attached to any scene, this is meaningless
		if (node.billboarded || node.billboardedX) {
			wasDirty = true;

			// Cancel the parent's rotation;
			if (parent != null) {
				localRotation.set(parent.inverseWorldRotation);
			} else {
				localRotation.setIdentity();
			}

			localRotation.mul(inverseCameraRotation);
		} else if (node.billboardedY) {
			// To solve billboard Y, you must rotate to face camera in node local space only
			// around the node-local version of the Y axis. Imagine that we have a vector facing
			// outward from the plane that represents where the front of the plane will face
			// after we apply the node's rotation. We can easily do "billboarding", which is
			// to say we can construct a rotation that turns this facing to face the camera.
			// However, for BillboardLockY, we must instead take the projection of the vector
			// that would result from this -- "facing camera" vector, and take the projection
			// of that vector onto the plane perpendicular to the billboard lock axis.

			wasDirty = true;

			// Cancel the parent's rotation;
			if (parent != null) {
				localRotation.mul(parent.inverseWorldRotation);
			} else {
				localRotation.setIdentity();
			}

			localRotation.mul(inverseCameraRotation);


			// TODO face camera, TODO have a camera
		} else if (node.billboardedZ) {
			wasDirty = true;

			// Cancel the parent's rotation;
			if (parent != null) {
				localRotation.set(parent.inverseWorldRotation);
			} else {
				localRotation.setIdentity();
			}

			localRotation.mul(inverseCameraRotationZSpin);

			// TODO face camera, TODO have a camera
		}
		return wasDirty;
	}

	private void updateParticles() {
		for (int i = 0; i < billboardVectors.length; i++) {
//			inverseCameraRotation.transform(billboardBaseVectors[i], billboardVectors[i]);
			billboardVectors[i].set(Vec4.getTransformed(billboardBaseVectors[i], inverseCameraRotation));
		}
		if ((animatedRenderEnvironment == null) || (animatedRenderEnvironment.getCurrentAnimation() == null)) {
			// not animating
			if (allowInanimateParticles) {
				for (final RenderParticleEmitter2View renderParticleEmitter2View : particleEmitterViews2) {
					if ((modelView == null) || modelView.getEditableIdObjects().contains(renderParticleEmitter2View.getEmitter())) {
						renderParticleEmitter2View.fill();
					}
					renderParticleEmitter2View.update();
				}
				for (final RenderParticleEmitter2 renderParticleEmitter2 : particleEmitters2) {
					renderParticleEmitter2.update();
				}
			}
		} else {
			for (final RenderParticleEmitter2View renderParticleEmitter2View : particleEmitterViews2) {
				renderParticleEmitter2View.update();
			}
			for (final RenderParticleEmitter2 renderParticleEmitter2 : particleEmitters2) {
				renderParticleEmitter2.update();
			}
		}
	}

	public Vec4[] getBillboardVectors() {
		return billboardVectors;
	}

	public Vec4[] getSpacialVectors() {
		return spacialVectors;
	}

	public List<RenderParticleEmitter2> getParticleEmitters2() {
		return particleEmitters2;
	}

	public List<RenderParticleEmitter2View> getParticleEmitterViews2() {
		return particleEmitterViews2;
	}

	public SoftwareParticleEmitterShader getParticleShader() {
		return particleShader;
	}

	public boolean allowParticleSpawn() {
		return spawnParticles;
	}
}