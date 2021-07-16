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
import com.hiveworkshop.rms.util.Mat4;
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
	private final TimeEnvironmentImpl animatedRenderEnvironment;

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

	public RenderModel(EditableModel model, ModelView modelView, TimeEnvironmentImpl timeEnvironment) {
		this.model = model;
		this.modelView = modelView;
		rootPosition = new RenderNode(this, new Bone("RootPositionHack"));
		this.animatedRenderEnvironment = timeEnvironment;
		// Some classes doesn't call refreshFromEditor which leads to null-pointers when these in nor initialised
		for (final Geoset geoset : modelView.getModel().getGeosets()) {
			RenderGeoset renderGeoset = renderGeosetMap.computeIfAbsent(geoset, k -> new RenderGeoset(geoset, this, modelView));
			renderGeoset.updateTransforms(false);
		}
	}

	public RenderModel updateGeosets(){
		if(renderGeosetMap.size() != model.getGeosets().size()){
			renderGeosetMap.clear();
		}
		for (final Geoset geoset : modelView.getModel().getGeosets()) {
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
		if(renderGeosetMap.size() != model.getGeosets().size()){
			renderGeosetMap.clear();
			for (Geoset geo : modelView.getModel().getGeosets()) {
				RenderGeoset renderGeoset = renderGeosetMap.computeIfAbsent(geo, k -> new RenderGeoset(geoset, this, modelView));
				renderGeoset.updateTransforms(shouldForceAnimation && ProgramGlobals.getSelectionItemType() == SelectionItemTypes.ANIMATE);
			}
		}
		return renderGeosetMap.get(geoset);
	}

	public RenderNode getRenderNodeByObjectId(int objectId) {
		return getRenderNode(model.getIdObject(objectId));
	}

	public TimeEnvironmentImpl getAnimatedRenderEnvironment() {
		return animatedRenderEnvironment;
	}

	public void refreshFromEditor(Particle2TextureInstance renderResourceAllocator) {
		particleEmitterViews2.clear();
		renderParticleEmitters2.clear();

		setBilllBoardVectors();

		sortedNodes.clear();
		fetchCameraSourceNodes();
		setupHierarchy2(null);
		fetchCameraTargetNodes();

//		if (renderResourceAllocator != null) {
//			for (ParticleEmitter2 particleEmitter : model.getParticleEmitter2s()) {
//				Particle2TextureInstance textureInstance = renderResourceAllocator.generate(particleEmitter.getTexture(), particleEmitter);
//				renderParticleEmitters2.add(new RenderParticleEmitter2(particleEmitter, textureInstance));
//			}
//			renderParticleEmitters2.sort(Comparator.comparingInt(RenderParticleEmitter2::getPriorityPlane));
//
//			for (RenderParticleEmitter2 renderParticleEmitter2 : renderParticleEmitters2) {
//				RenderParticleEmitter2View emitterView = new RenderParticleEmitter2View(this, renderParticleEmitter2);
//				particleEmitterViews2.add(emitterView);
//				emitterToRenderer.put(emitterView.getParticleEmitter2(), emitterView);
//			}
//		}

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
					objectToRenderNode.computeIfAbsent(object, k -> new RenderNode(this, object));
					setupHierarchy2(object);
				}
			}
		} else {
			for (IdObject object : parent.getChildrenNodes()) {
				sortedNodes.add(object);
				objectToRenderNode.computeIfAbsent(object, k -> new RenderNode(this, object));
				setupHierarchy2(object);
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
		if ((animatedRenderEnvironment == null) || (animatedRenderEnvironment.getCurrentAnimation() == null)) {
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
		boolean objectVisible = idObject.getRenderVisibility(animatedRenderEnvironment) >= MAGIC_RENDER_SHOW_CONSTANT;
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
//							e.printStackTrace();
						System.out.println("RenderModel#updateNodes: failed to update rotation for " + idObject.getName());
						lastConsoleLogTime = currentTime + 1000;
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

//	Quat camRotationX = new Quat().setFromAxisAngle(1,0,0,0f*1.57f);
//	Quat camRotationY = new Quat().setFromAxisAngle(0,1,0,0*1.57f);
//	Quat camRotationZ = new Quat().setFromAxisAngle(0,0,1,0*1.57f);

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
			Quat camRotation = new Quat(0, 0, 0, 1);
			Vec4 camNormal = new Vec4(0,0,1, 1);
			Vec4 xNormal = new Vec4(1,0,0, 1);
			Vec4 yNormal = new Vec4(0,1,0, 1);
			Vec4 zNormal = new Vec4(0,0,1, 1);
			Mat4 proj = new Mat4();
			wasDirty = true;

			if (node.billboarded) {
				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()); // WORKS!
			} else if (node.billboardedX) {

//				localRotation.mulInverse(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX());
//				localRotation.mulInverse(getInverseCameraRotYSpinY());
//				localRotation.invertRotation2().mulLeft(getInverseCameraRotXSpinY());
//				localRotation.mulInverse(getInverseCameraRotYSpinY()).invertRotation2().mulLeft(getInverseCameraRotZSpinX());
//				localRotation.mulInverse(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotYSpinY());
//				localRotation.mulLeft(getInverseCameraRotYSpinY());
//				localRotation.mul(getInverseCameraRotYSpinY());
//				localRotation.mul(getInverseCameraRotXSpinY()).invertRotation2().mulLeft(getInverseCameraRotZSpinX());
//				localRotation.mul(getInverseCameraRotXSpinY()).invertRotation2();//Close!
//				localRotation.mul(getInverseCameraRotXSpinY()).invertRotation2().mulLeft(getInverseCameraRot_XY_mulZX());
//				localRotation.mul(getInverseCameraRotXSpinY()).invertRotation2().mulLeft(getInverseCameraRotZSpinX());
//				localRotation.mul(getInverseCameraRotXSpinY()).invertRotation2().mulLeft(getInverseCameraRotYSpinX()).invertRotation();
//				localRotation.mul(getInverseCameraRotXSpinY()).mul(getInverseCameraRotYSpinX()).invertRotation();;
//				localRotation.mul(getInverseCameraRotXSpinY()).mulInverse(getInverseCameraRotYSpinX());;
//				localRotation.mul(getInverseCameraRotYSpinX()).mulInverse(getInverseCameraRotXSpinY());;
//				localRotation.mul(getInverseCameraRotZSpinX()).invertRotation().mul(getInverseCameraRotYSpinY());
//				localRotation.mul(getInverseCameraRotZSpinX()).invertRotation().mul(getInverseCameraRotYSpinY()).invertRotation();
//				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).invertRotation();
//				localRotation.mul(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX()).invertRotation();
//				localRotation.invertRotation2().mulLeft(getInverseCameraRotation0090());
				localRotation.invertRotation2().mulLeft(getInverseCameraRotZSpinX());
			} else if (node.billboardedY) {

//				localRotation.mulInverse(getInverseCameraRotYSpinY());//Nope
//				localRotation.mul(getInverseCameraRotYSpinY()); //NOPE
//				localRotation.mulLeft(getInverseCameraRotYSpinY()); //NOPE
//				localRotation.mul(getInverseCameraRotZSpinX()); //NOPE
//				localRotation.mulInverse(getInverseCameraRotZSpinX()); //NOPE
//				localRotation.mulLeft(getInverseCameraRotZSpinX()); //NOPE

//				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).mulInverse(getInverseCameraRotZSpinX()); //Nope
//				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX()); //Nope
//				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).invertRotation().mulLeft(getInverseCameraRotZSpinX()); //Nope
//				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).invertRotation().mulLeft(getInverseCameraRotYSpinY()); //Nope
//				localRotation.mul(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX()).invertRotation().mulLeft(getInverseCameraRotYSpinY()); //Nope
//				localRotation.mul(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX()).invertRotation(); //Nope

//				localRotation.mul(getInverseCameraRotYSpinY()).invertRotation().mul(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//				localRotation.mulLeft(getInverseCameraRotYSpinY()).invertRotation().mul(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//				localRotation.mulInverse(getInverseCameraRotYSpinY()).invertRotation().mul(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//				localRotation.mul(getInverseCameraRotYSpinY()).invertRotation().mulLeft(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//				localRotation.mulLeft(getInverseCameraRotYSpinY()).invertRotation().mulLeft(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//				localRotation.mulInverse(getInverseCameraRotYSpinY()).invertRotation().mulLeft(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//
//				localRotation.mul(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//				localRotation.mulLeft(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//				localRotation.mulInverse(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//				localRotation.mul(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//				localRotation.mulLeft(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//				localRotation.mulInverse(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX()).invertRotation(); //Nope
//
//				localRotation.mul(getInverseCameraRotZSpinX()).invertRotation().mul(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//				localRotation.mulLeft(getInverseCameraRotZSpinX()).invertRotation().mul(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//				localRotation.mulInverse(getInverseCameraRotZSpinX()).invertRotation().mul(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//				localRotation.mul(getInverseCameraRotZSpinX()).invertRotation().mulLeft(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//				localRotation.mulLeft(getInverseCameraRotZSpinX()).invertRotation().mulLeft(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//				localRotation.mulInverse(getInverseCameraRotZSpinX()).invertRotation().mulLeft(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//
//				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//				localRotation.mulLeft(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//				localRotation.mulInverse(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//				localRotation.mul(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//				localRotation.mulLeft(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotYSpinY()).invertRotation(); //Nope
//				localRotation.mulInverse(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotYSpinY()).invertRotation(); //Nope

				//////////
//				localRotation.mul(getInverseCameraRotYSpinY()).invertRotation().mul(getInverseCameraRotZSpinX());
//				localRotation.mulLeft(getInverseCameraRotYSpinY()).invertRotation().mul(getInverseCameraRotZSpinX());
//				localRotation.mulInverse(getInverseCameraRotYSpinY()).invertRotation().mul(getInverseCameraRotZSpinX());
//				localRotation.mul(getInverseCameraRotYSpinY()).invertRotation().mulLeft(getInverseCameraRotZSpinX());
//				localRotation.mulLeft(getInverseCameraRotYSpinY()).invertRotation().mulLeft(getInverseCameraRotZSpinX());
//				localRotation.mulInverse(getInverseCameraRotYSpinY()).invertRotation().mulLeft(getInverseCameraRotZSpinX());
//
//				localRotation.mul(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX()); //Z?
//				localRotation.mulLeft(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX());
//				localRotation.mulInverse(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX());
//				localRotation.mul(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX()); //GLOB
//				localRotation.mulLeft(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX()); //Glob
//				localRotation.mulInverse(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX());
//
//				localRotation.mul(getInverseCameraRotZSpinX()).invertRotation().mul(getInverseCameraRotYSpinY());
//				localRotation.mulLeft(getInverseCameraRotZSpinX()).invertRotation().mul(getInverseCameraRotYSpinY());
//				localRotation.mulInverse(getInverseCameraRotZSpinX()).invertRotation().mul(getInverseCameraRotYSpinY());//Glob?
//				localRotation.mul(getInverseCameraRotZSpinX()).invertRotation().mulLeft(getInverseCameraRotYSpinY());
//				localRotation.mulLeft(getInverseCameraRotZSpinX()).invertRotation().mulLeft(getInverseCameraRotYSpinY());
//				localRotation.mulInverse(getInverseCameraRotZSpinX()).invertRotation().mulLeft(getInverseCameraRotYSpinY());
//
//				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()); //GLOB
//				localRotation.mulLeft(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()); //GLOB
//				localRotation.mulInverse(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY());
//				localRotation.mul(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotYSpinY());
//				localRotation.mulLeft(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotYSpinY());
//				localRotation.mulInverse(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotYSpinY());

//				localRotation.setFromAxisAngle2(0,1,0,0);
//				localRotation.setFromAxisAngle2(1,0,0,0);
//				localRotation.setFromAxisAngle2(0,0,1,0);

//				localRotation.setFromAxisAngle2(0,1,0,(float)Math.PI/2);
//				localRotation.setFromAxisAngle2(1,0,0,(float)Math.PI/2);

//				localRotation.setFromAxisAngle2(0,0,1,(float)Math.PI/2);
//				localRotation.setFromAxisAngle2(0,0,1,(float)Math.PI/2);
				localRotation.invertRotation2().mulLeft(getInverseCameraRotXSpinY()); //I Think It Works :O

//				localRotation.setFromAxisAngle2(0,0,1,(float)Math.PI/2);//Almost Works!
//				localRotation.invertRotation2().mulLeft(getInverseCameraRotYSpinY());//Almost Works!
			} else if (node.billboardedZ) {
				localRotation.mul(getInverseCameraRotZSpinX()); // WORKS!
			}
			localRotation.normalize();

			node.setRotation(localRotation);
		}
		return wasDirty;
	}
	public boolean RotateAndStuffBillboarding2(RenderNode node, RenderNode parent) {
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
			Quat camRotation = new Quat(0, 0, 0, 1);
			Vec4 camNormal = new Vec4(0,0,1, 1);
			Vec4 xNormal = new Vec4(1,0,0, 1);
			Vec4 yNormal = new Vec4(0,1,0, 1);
			Vec4 zNormal = new Vec4(0,0,1, 1);
			Mat4 proj = new Mat4();
			wasDirty = true;

			if (node.billboarded) {
				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()); // WORKS!

//				System.out.println("invCS: " + localRotation.toAxisWithAngle() + " (" + (localRotation.toAxisWithAngle().w*57.3) + ")");
//				localRotation.mul(getInverseCameraRot_XY_mulZX());
//				localRotation.set(new Quat().setFromAxisAngle(-1,0,0,2*1.57f).mul(getInverseCameraRotation())); //Good but inverted?
//				localRotation.set(new Quat().setFromAxisAngle(1,-1,1,-1*1.57f).normalize());
//				localRotation.mul(getInverseCameraRotation());
//				localRotation.mulLeft(getInverseCameraRotation());//.mulInverse(getInverseCameraRotZSpinX());
//				localRotation.mul(getInverseCameraRotation().invertRotation());
//				getInverseCameraRotation().invertRotation();
			} else if (node.billboardedX) {
//				Vec3 euler = new Vec3().wikiToEuler(getInverseCameraRotation());
////				camRotation.setFromAxisAngle(1,0,0, getInverseCameraRotation().toEuler().x);
//				camRotation.setFromAxisAngle(1,0,0, euler.x);
////				localRotation.setFromAxisAngle(1,0,0,0f*1.57f).invertRotation().mulLeft(getInverseCameraRotation());
////				localRotation.setFromAxisAngle(1,0,0,0f*1.57f).mul(camRotation);
//				localRotation.mul(camRotation);
////				Quat camRotation = new Quat(0, 0, 0, 1);
////				camRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).normalize();
//////				localRotation.set(1,0,0,1).normalize().transform(getInverseCameraRotation());
////				localRotation.set(1,0,0,1).transform(camRotation).normalize();
//////				localRotation.mul(camRotation).mulInverse(getInverseCameraRotZSpinX()).normalize();
////				localRotation.mulInverse(getInverseCameraRotZSpinX()).normalize();
//////				localRotation.mul(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotZSpinX()).normalize();
//////				localRotation.mulInverse(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX()).normalize();
////
//////				localRotation.mul(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX()).normalize();
//////				localRotation.mul(getInverseCameraRotation0090());
//////				localRotation.mul(getInverseCameraRotation());

//				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).mulInverse(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotXSpinY()).normalize();
				localRotation.mul(getInverseCameraRotXSpinY());
			} else if (node.billboardedY) {
//////				localRotation.mul(getInverseCameraRotYSpinY()).mul(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX()).normalize();
////				localRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY()).mulLeft(getInverseCameraRotZSpinX()).mulLeft(getInverseCameraRotYSpinY()).normalize();
//////				localRotation.mul(getInverseCameraRotYSpinY()).normalize();
////
////				Quat camRotation = new Quat().set(localRotation);
////
////				localRotation.set(0, 0, 0, 1);
////				localRotation.mul(getInverseCameraRotZSpinX()).mulInverse(camRotation);
//
//				camRotation.mul(getInverseCameraRotZSpinX()).mul(getInverseCameraRotYSpinY());
//				camNormal.transform(camRotation);
//
//				Vec3 look = cameraHandler.getCameraPos().sub(node.getWorldLocation());
//
//				Vec3 right = camNormal.getVec3().cross(look);
//				Vec3 up = look.cross(right);
//
//				camNormal.transform(camRotation);
//				proj.setAsProjection(yNormal);
//				camNormal.transform(proj);
//
//
//
//				localRotation.setFromAxisAngle(0,1,0, (float) yNormal.getVec3().radAngleTo(camNormal.getVec3()));
////				localRotation.mul(getInverseCameraRotXSpinY());
////				localRotation.mul(getInverseCameraRotZSpinX()).mulInverse(getInverseCameraRotZSpinX());
				// TODO face camera, TODO have a camera

				localRotation.mulLeft(getInverseCameraRotYSpinY());
//				camRotation.setFromAxisAngle(0,1,0, (float) Math.atan2(billboardVectors[5].z, -billboardVectors[5].x));
//				localRotation.mul(camRotation);
//				Vec3 euler = new Vec3().wikiToEuler(getInverseCameraRotation());

//				camRotation.setFromAxisAngle(0,1,0, -euler.y);
//				camRotation.setFromAxisAngle(1,0,0, euler.z);
//				localRotation.setFromAxisAngle(1,0,0,0f*1.57f).invertRotation().mulLeft(getInverseCameraRotation());
//				localRotation.setFromAxisAngle(1,0,0,0f*1.57f).mul(camRotation);
//				localRotation.mul(camRotation);


			} else if (node.billboardedZ) {
				localRotation.mul(getInverseCameraRotZSpinX()); // WORKS!
			}
			localRotation.normalize();

			node.setRotation(localRotation);
		}
		return wasDirty;
	}


	public boolean RotateAndStuffBillboarding(RenderNode node, RenderNode parent) {
		boolean wasDirty = false;
		// If the instance is not attached to any scene, this is meaningless

		// To solve billboard Y, you must rotate to face camera in node local space only
		// around the node-local version of the Y axis. Imagine that we have a vector facing
		// outward from the plane that represents where the front of the plane will face
		// after we apply the node's rotation. We can easily do "billboarding", which is
		// to say we can construct a rotation that turns this facing to face the camera.
		// However, for BillboardLockY, we must instead take the projection of the vector
		// that would result from this -- "facing camera" vector, and take the projection
		// of that vector onto the plane perpendicular to the billboard lock axis.

		if(node.billboarded || node.billboardedX || node.billboardedY || node.billboardedZ){

			Quat localRotation = new Quat(0, 0, 0, 1);
//			Quat localRotation = node.getLocalRotation();
			wasDirty = true;

			// Cancel the parent's rotation;
			if (parent != null) {
				localRotation.set(parent.getInverseWorldRotation());
//				localRotation.invertRotation().mul(parent.getInverseWorldRotation());
			} else {
				localRotation.setIdentity();
			}


			if (node.billboarded) {
				localRotation.mul(getInverseCameraRotation());
			} else if (node.billboardedX) {
				localRotation.mul(getInverseCameraRotation());
			} else if (node.billboardedY) {
				localRotation.mul(getInverseCameraRotYSpinY());
			} else if (node.billboardedZ) {
				localRotation.mul(getInverseCameraRotZSpinX());
				// TODO face camera, TODO have a camera
			}
			node.setRotation(localRotation);
		}
		return wasDirty;
	}

	private void updateParticles() {
		setBilllBoardVectors();
//		if ((animatedRenderEnvironment == null) || (animatedRenderEnvironment.getCurrentAnimation() == null)) {
////			 not animating
//			if (allowInanimateParticles) {
//				for (RenderParticleEmitter2View renderParticleEmitter2View : particleEmitterViews2) {
//					if ((modelView == null) || modelView.getEditableIdObjects().contains(renderParticleEmitter2View.getParticleEmitter2())) {
//						renderParticleEmitter2View.fill();
//					}
//					renderParticleEmitter2View.update();
//				}
//				for (RenderParticleEmitter2 renderParticleEmitter2 : renderParticleEmitters2) {
//					renderParticleEmitter2.update();
//				}
//			}
//		} else {
//			for (RenderParticleEmitter2View renderParticleEmitter2View : particleEmitterViews2) {
//				renderParticleEmitter2View.update();
//			}
//			for (RenderParticleEmitter2 renderParticleEmitter2 : renderParticleEmitters2) {
//				renderParticleEmitter2.update();
//			}
//		}

		for (RenderParticleEmitter2View renderParticleEmitter2View : particleEmitterViews2) {
			if (allowInanimateParticles // not animating
					&& (animatedRenderEnvironment == null || animatedRenderEnvironment.getCurrentAnimation() == null)
					&& (modelView == null || modelView.getEditableIdObjects().contains(renderParticleEmitter2View.getParticleEmitter2()))) {
				renderParticleEmitter2View.fill();
			}
			renderParticleEmitter2View.update();
		}
//		for (RenderParticleEmitter2 renderParticleEmitter2 : renderParticleEmitters2) {
//			renderParticleEmitter2.update();
//		}
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

	public List<RenderParticleEmitter2View> getParticleEmitterViews2() {
		return particleEmitterViews2;
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
	private Quat getInverseCameraRotZSpinY() {
		if(cameraHandler != null){
			return cameraHandler.getInverseCameraRotZSpinY();
		}
		return IDENTITY;
	}
	private Quat getInverseCameraRot_XY_mulZX() {
		if(cameraHandler != null){
			return cameraHandler.getInverseCameraRot_XY_mulZX();
		}
		return IDENTITY;
	}
	private Quat getInverseCameraRotXSpinX() {
		if(cameraHandler != null){
			return cameraHandler.getInverseCameraRotXSpinX();
		}
		return IDENTITY;
	}

	private Quat getInverseCameraRotZSpinX() {
		if(cameraHandler != null){
			return cameraHandler.getInverseCameraRotZSpinX();
		}
		return IDENTITY;
	}

	private Quat getInverseCameraRotYSpinY() {
		if(cameraHandler != null){
			return cameraHandler.getInverseCameraRotYSpinY();
		}
		return IDENTITY;
	}

	private Quat getInverseCameraRotYSpinX() {
		if(cameraHandler != null){
			return cameraHandler.getInverseCameraRotYSpinX();
		}
		return IDENTITY;
	}

	private Quat getInverseCameraRotXSpinY() {
		if(cameraHandler != null){
			return cameraHandler.getInverseCameraRotXSpinY();
		}
		return IDENTITY;
	}
}