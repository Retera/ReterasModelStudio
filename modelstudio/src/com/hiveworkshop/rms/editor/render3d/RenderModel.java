package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.Camera.SourceNode;
import com.hiveworkshop.rms.editor.model.Camera.TargetNode;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.renderparts.RenderGeoset;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.application.viewer.Particle2TextureInstance;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionItemTypes;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.util.*;

/**
 * For rendering. Copied from ghostwolf's stuff
 */
public final class RenderModel {
	Quat IDENTITY = new Quat();
	private final EditableModel model;
	public static final double MAGIC_RENDER_SHOW_CONSTANT = 0.75;
	private final List<AnimatedNode> sortedNodes = new ArrayList<>();
	private final TimeEnvironmentImpl timeEnvironment;

	private final Map<AnimatedNode, RenderNode> objectToRenderNode = new HashMap<>();
	private final Map<Geoset, RenderGeoset> renderGeosetMap = new HashMap<>();
	private final Map<ParticleEmitter2, RenderParticleEmitter2View> emitterToRenderer = new HashMap<>();
	private final List<RenderParticleEmitter2> renderParticleEmitters2 = new ArrayList<>();// TODO one per model, not instance
	private final List<RenderParticleEmitter2View> particleEmitterViews2 = new ArrayList<>();
	private final ParticleEmitterShader particleShader = new ParticleEmitterShader();

	private final RenderNode rootPosition;

	private boolean shouldForceAnimation = false;

	private boolean spawnParticles = true;
	//	private boolean allowInanimateParticles = false;
	private boolean allowInanimateParticles = true;

	private long lastConsoleLogTime = 0;
	private CameraHandler cameraHandler;

	// These guys form the corners of a 2x2 rectangle, for use in Ghostwolf particle emitter algorithm
	private static final Vec4[] SPACIAL_VECTORS = {
			new Vec4(-1, 1, 0, 1),
			new Vec4(1, 1, 0, 1),
			new Vec4(1, -1, 0, 1),
			new Vec4(-1, -1, 0, 1),
			new Vec4(1, 0, 0, 1),
			new Vec4(0, 1, 0, 1),
			new Vec4(0, 0, 1, 1)};
	private static final Vec4[] BILLBOARD_BASE_VECTORS = {
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

	public RenderModel(EditableModel model, ModelView modelView) {
		this.model = model;
		this.modelView = modelView;
		rootPosition = new RenderNode(this, new Bone("RootPositionHack"));
		this.timeEnvironment = new TimeEnvironmentImpl();
		for (final Geoset geoset : model.getGeosets()) {
			RenderGeoset renderGeoset = renderGeosetMap.computeIfAbsent(geoset, k -> new RenderGeoset(geoset, this, modelView));
			renderGeoset.updateTransforms(false);
		}
	}

	public RenderModel updateGeosets() {
		if (renderGeosetMap.size() != model.getGeosets().size()) {
			renderGeosetMap.clear();
		}
		for (final Geoset geoset : model.getGeosets()) {
			RenderGeoset renderGeoset = renderGeosetMap.computeIfAbsent(geoset, k -> new RenderGeoset(geoset, this, modelView));
			renderGeoset.updateTransforms(shouldForceAnimation && ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE);
		}
		return this;
	}

	public RenderModel setShouldForceAnimation(boolean shouldForceAnimation) {
		this.shouldForceAnimation = shouldForceAnimation;
		return this;
	}

	public void setSpawnParticles(boolean spawnParticles) {
		this.spawnParticles = spawnParticles;
	}

	public void setAllowInanimateParticles(boolean allowInanimateParticles) {
		this.allowInanimateParticles = allowInanimateParticles;
	}

	public RenderNode getRenderNode(AnimatedNode idObject) {
		RenderNode renderNode = objectToRenderNode.get(idObject);
		if (renderNode == null) {
			return rootPosition;
		}
		return renderNode;
	}

	public RenderGeoset getRenderGeoset(Geoset geoset){
		if(renderGeosetMap.size() != model.getGeosets().size()) {
			renderGeosetMap.clear();
			for (Geoset geo : model.getGeosets()) {
				RenderGeoset renderGeoset = renderGeosetMap.computeIfAbsent(geo, k -> new RenderGeoset(geoset, this, modelView));
				renderGeoset.updateTransforms(shouldForceAnimation && ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE);
			}
		}
		return renderGeosetMap.get(geoset);
	}

	public TimeEnvironmentImpl getTimeEnvironment() {
		return timeEnvironment;
	}

	public void refreshFromEditor(Particle2TextureInstance renderResourceAllocator) {
		particleEmitterViews2.clear();
		renderParticleEmitters2.clear();

		setBilllBoardVectors();

		sortedNodes.clear();
		fetchCameraSourceNodes();
		setupHierarchy(null);
		fetchCameraTargetNodes();

		if (renderResourceAllocator != null) {
			for (ParticleEmitter2 particleEmitter2 : model.getParticleEmitter2s()) {
				Particle2TextureInstance textureInstance = renderResourceAllocator.generate(particleEmitter2.getTexture(), particleEmitter2);
				RenderParticleEmitter2View emitterView = new RenderParticleEmitter2View(this, particleEmitter2, textureInstance);
				particleEmitterViews2.add(emitterView);
				renderParticleEmitters2.add(emitterView.getRenderEmitter());
				emitterToRenderer.put(particleEmitter2, emitterView);
			}
			renderParticleEmitters2.sort(Comparator.comparingInt(RenderParticleEmitter2::getPriorityPlane));
		}

		for (AnimatedNode node : sortedNodes) {
			getRenderNode(node).refreshFromEditor();
		}

		if (shouldForceAnimation && ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE) {
			updateNodes(false);
		}
	}

	private void fetchCameraTargetNodes() {
		for (Camera camera : model.getCameras()) {
			TargetNode object = camera.getTargetNode();
			sortedNodes.add(object);
			objectToRenderNode.computeIfAbsent(object, k -> new RenderNode(this, object));
		}
	}

	private void fetchCameraSourceNodes() {
		for (Camera camera : model.getCameras()) {
			SourceNode object = camera.getSourceNode();
			sortedNodes.add(object);
			objectToRenderNode.computeIfAbsent(object, k -> new RenderNode(this, object));
		}
	}

	public void setBilllBoardVectors() {
		for (int i = 0; i < billboardVectors.length; i++) {
			billboardVectors[i].set(BILLBOARD_BASE_VECTORS[i]).transform(getInverseCameraRotation());
		}
	}

	private void setupHierarchy(IdObject parent) {
		if (parent == null) {
			for (IdObject object : model.getIdObjects()) {
				if (object.getParent() == null) {
					sortedNodes.add(object);
					objectToRenderNode.computeIfAbsent(object, k -> new RenderNode(this, object));
					setupHierarchy(object);
				}
			}
		} else {
			for (IdObject object : parent.getChildrenNodes()) {
				sortedNodes.add(object);
				objectToRenderNode.computeIfAbsent(object, k -> new RenderNode(this, object));
				setupHierarchy(object);
			}
		}
	}

	public void setCameraHandler(CameraHandler cameraHandler) {
		this.cameraHandler = cameraHandler;
	}

	public void updateNodes(boolean soft, boolean particles) {
		updateNodes(false, soft, particles);
	}

	public void updateNodes(boolean particles) {
		updateNodes(true, false, particles);
	}

	// Soft is to only update billborded
	public void updateNodes(boolean forced, boolean soft, boolean renderParticles) {
		if ((timeEnvironment == null) || (timeEnvironment.getCurrentSequence() == null)) {
			for (AnimatedNode idObject : sortedNodes) {
				getRenderNode(idObject).resetTransformation();
			}
			if (renderParticles && allowInanimateParticles) {
				updateParticles();
			}
			return;
		}
		for (AnimatedNode idObject : sortedNodes) {
			updateNode(forced, soft, renderParticles, idObject);
		}
		if (renderParticles) {
			updateParticles();
		}

	}

	private void updateNode(boolean forced, boolean soft, boolean renderParticles, AnimatedNode idObject) {
		RenderNode node = getRenderNode(idObject);
		AnimatedNode idObjectParent = null;
		if (idObject instanceof IdObject) {
			idObjectParent = ((IdObject) idObject).getParent();
		}
		RenderNode parent = idObjectParent == null ? null : getRenderNode(idObjectParent);
		boolean objectVisible = idObject.getRenderVisibility(timeEnvironment) >= MAGIC_RENDER_SHOW_CONSTANT;
		boolean nodeVisible = forced || objectVisible;
//		boolean nodeVisible = forced || (((parent == null) || parent.visible) && objectVisible);
		node.visible = nodeVisible;

		// Every node only needs to be updated if this is a forced update, or if both
		// the parent node and the generic object corresponding to this node are visible.
		// Incoming messy code for optimizations!
		// --- All copied from Ghostwolf
		boolean dirty = forced || (parent != null && parent.dirty) || node.billboarded;
//			if (nodeVisible) {
		if (nodeVisible || forced || !soft) {
			// TODO variants

			// Only update the local data if there is a need to
			if (forced || (!soft && idObject.getAnimFlags().size() > 0)) {
				Vec3 localLocation = new Vec3(0, 0, 0);
				Quat localRotation = new Quat(0, 0, 0, 1);
				Vec3 localScale = new Vec3(1, 1, 1);
				dirty = true;

				// Translation
				Vec3 renderTranslation = idObject.getRenderTranslation(timeEnvironment);
				if (renderTranslation != null) {
					localLocation.set(renderTranslation);
				}

				// Rotation
				try {
					Quat renderRotation = idObject.getRenderRotation(timeEnvironment);
					if (renderRotation != null) {
						localRotation.set(renderRotation);
					}
				} catch (Exception e) {
					long currentTime = System.currentTimeMillis();
					if (lastConsoleLogTime < currentTime) {
//							e.printStackTrace();
						System.out.println("RenderModel#updateNodes: failed to update rotation for " + idObject.getName());
						lastConsoleLogTime = currentTime + 1000;
					}
				}

				// Scale
				Vec3 renderScale = idObject.getRenderScale(timeEnvironment);
				if (renderScale != null) {
					localScale.set(renderScale);
				}

				node.setTransformation(localLocation, localRotation, localScale);
			}
			node.dirty = dirty;
			// Billboarding
			boolean wasDirty = RotateAndStuffBillboarding3(node, parent);

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
			if (objectVisible && renderParticles && idObject instanceof ParticleEmitter2) {
				if (emitterToRenderer.get(idObject) != null) {
					if ((modelView == null) || modelView.getEditableIdObjects().contains(idObject) || modelView.isVetoOverrideParticles()) {
						emitterToRenderer.get(idObject).fill();
					}
				}
			}
		}
	}

	public boolean RotateAndStuffBillboarding3(RenderNode node, RenderNode parent) {
		boolean wasDirty = false;
		// If the instance is not attached to any scene, this is meaningless

		// To solve billboard Y, you must rotate to face camera in node local space only
		// around the node-local version of the Y axis.
		// Imagine that we have a vector facing outward from the plane that represents
		// where the front of the plane will face after we apply the node's rotation.
		// We can easily do "billboarding", which is to say we can construct a rotation
		// that turns this facing to face the camera.
		// However, for BillboardLockY, we must instead take the projection of the vector
		// that would result from this -- "facing camera" vector,
		// and take the projection of that vector onto the plane perpendicular to the billboard lock axis.


		// To solve billboard Y, you must rotate to face camera in node local space
		// only around the node-local version of the Y axis.

		// Imagine the normal of the plane resulting from apply the node's rotation.
		// With this we can easily do "billboarding", which is to say we can
		// construct a quaternation that turns this facing to face the camera.

		// However, for BillboardLockY, we must instead take the projection of the normal of the billboarded plane,
		// and project it onto the plane perpendicular to the billboard lock axis.

		if(node.billboarded || node.billboardedX || node.billboardedY || node.billboardedZ){

			Quat localRotation = new Quat(0, 0, 0, 1);
			wasDirty = true;

			if (node.billboarded) {
				localRotation.mul(getInverseCameraRotZSpinZ()).mul(getInverseCameraRotYSpinY()); // WORKS!
			} else if (node.billboardedX) {
				localRotation.invertRotation2().mulLeft(getInverseCameraRotZSpinX());
			} else if (node.billboardedY) {
				localRotation.invertRotation2().mulLeft(getInverseCameraRotXSpinY()); //I Think It Works :O
			} else if (node.billboardedZ) {
				localRotation.mul(getInverseCameraRotZSpinZ());

			}
			localRotation.normalize();

			node.setRotation(localRotation);
		}
		return wasDirty;
	}


	private void updateParticles() {
		setBilllBoardVectors();

		for (RenderParticleEmitter2View renderParticleEmitter2View : particleEmitterViews2) {
			if (allowInanimateParticles // not animating
					&& (timeEnvironment == null || timeEnvironment.getCurrentSequence() == null)
					&& (modelView == null || modelView.getEditableIdObjects().contains(renderParticleEmitter2View.getParticleEmitter2()))) {
				renderParticleEmitter2View.fill();
			}
			renderParticleEmitter2View.update();
		}
	}

	public Vec4[] getBillboardVectors() {
		return billboardVectors;
	}

	public Vec4[] getSpacialVectors() {
		return SPACIAL_VECTORS;
	}

	public List<RenderParticleEmitter2> getRenderParticleEmitters2() {
		return renderParticleEmitters2;
	}

	public ParticleEmitterShader getParticleShader() {
		return particleShader;
	}

	public boolean allowParticleSpawn() {
		return spawnParticles;
	}


	private Quat getInverseCameraRotation() {
		if(cameraHandler != null){
			return cameraHandler.getInverseCameraRotation();
		}
		return IDENTITY;
	}

	private Quat getInverseCameraRotXSpinY() {
		if (cameraHandler != null) {
			return cameraHandler.getInverseCameraRotXSpinY();
		}
		return IDENTITY;
	}

	private Quat getInverseCameraRotYSpinY() {
		if (cameraHandler != null) {
			return cameraHandler.getInverseCameraRotYSpinY();
		}
		return IDENTITY;
	}

	private Quat getInverseCameraRotZSpinX() {
		if (cameraHandler != null) {
			return cameraHandler.getInverseCameraRotZSpinX();
		}
		return IDENTITY;
	}

	private Quat getInverseCameraRotZSpinZ() {
		if (cameraHandler != null) {
			return cameraHandler.getInverseCameraRotZSpinZ();
		}
		return IDENTITY;
	}
}