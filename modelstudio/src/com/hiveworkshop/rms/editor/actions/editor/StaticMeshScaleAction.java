package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.actions.nodes.ScaleCollisionExtents;
import com.hiveworkshop.rms.editor.actions.nodes.ScaleParticleAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.render3d.EmitterIdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class StaticMeshScaleAction extends AbstractTransformAction {
	private final Vec3 center;
	private final Vec3 scale;
	private final ArrayList<GeosetVertex> selectedVertices;
	private final ArrayList<IdObject> selectedIdObjects;
	private final ArrayList<CameraNode> selectedCameraNodes;

	private final Map<TimelineContainer, Vec3AnimFlag> opgTranslation;
//	private final ArrayList<ScaleParticleAction> particleActions;
	private final ArrayList<AbstractTransformAction> otherScaleActions;
	private final ArrayList<Vec3> opgPosVertices;
	private final ArrayList<Vec3> opgPosIdObjects;
	private final ArrayList<Vec3> opgPosCameraNodes;
	private final Mat4 invRotMat = new Mat4();
	private final Mat4 rotMat = new Mat4();

	public StaticMeshScaleAction(ModelView modelView, Vec3 center, Mat4 rotMat) {
		this(modelView, center, rotMat, new Vec3(1, 1, 1));
	}

	public StaticMeshScaleAction(ModelView modelView, Vec3 center, Mat4 rotMat, Vec3 scale) {
		this(modelView.getSelectedVertices(),
				modelView.getSelectedIdObjects(),
				modelView.getSelectedCameraNodes(),
				center, scale, rotMat);
	}

	public StaticMeshScaleAction(Collection<GeosetVertex> selectedVertices,
	                             Collection<IdObject> selectedIdObjects,
	                             Collection<CameraNode> selectedCameraNodes,
	                             Vec3 center,
	                             Vec3 scale, Mat4 rotMat) {
		this.center = center;
		this.scale = scale;
		this.rotMat.set(rotMat);
		this.invRotMat.set(rotMat).invert();
		this.selectedVertices = new ArrayList<>(selectedVertices);
		this.selectedIdObjects = new ArrayList<>(selectedIdObjects);
		this.selectedCameraNodes = new ArrayList<>(selectedCameraNodes);

		this.opgPosVertices = new ArrayList<>();
		this.selectedVertices.forEach(v -> opgPosVertices.add(new Vec3(v)));
		this.opgPosIdObjects = new ArrayList<>();
		this.selectedIdObjects.forEach(o -> opgPosIdObjects.add(new Vec3(o.getPivotPoint())));
		this.opgPosCameraNodes = new ArrayList<>();
		this.selectedCameraNodes.forEach(c -> opgPosCameraNodes.add(new Vec3(c.getPosition())));


		this.opgTranslation = new HashMap<>();
//		this.particleActions = new ArrayList<>();
		this.otherScaleActions = new ArrayList<>();
		for (IdObject node : selectedIdObjects) {
			AnimFlag<?> flag = node.find(MdlUtils.TOKEN_TRANSLATION);
			if (flag instanceof Vec3AnimFlag translation) {
				this.opgTranslation.put(node, (Vec3AnimFlag)translation.deepCopy());
			}

			if (node instanceof EmitterIdObject emitter){
//				particleActions.add(new ScaleParticleAction(emitter, center, rotMat, scale, false));
				otherScaleActions.add(new ScaleParticleAction(emitter, center, rotMat, scale, false));
			}
			if (node instanceof CollisionShape shape) {
				otherScaleActions.add(new ScaleCollisionExtents(shape, center, rotMat, scale, false, null));
			}
		}
		for (CameraNode node : selectedCameraNodes) {
			AnimFlag<?> flag = node.find(MdlUtils.TOKEN_TRANSLATION);
			if (flag instanceof Vec3AnimFlag translation) {
				this.opgTranslation.put(node, (Vec3AnimFlag)translation.deepCopy());
			}
		}


//		this.opgAnimatedFloats = new HashMap<>();
//		for (IdObject node : selectedIdObjects) {
//			if(node instanceof ParticleEmitter){
//				String[] flagNames = new String[] {MdlUtils.TOKEN_LATITUDE, MdlUtils.TOKEN_LONGITUDE, MdlUtils.TOKEN_INIT_VELOCITY, MdlUtils.TOKEN_GRAVITY};
//				for(String s : flagNames){
//					AnimFlag<?> flag = node.find(s);
//					if (flag instanceof FloatAnimFlag floatAnimFlag) {
//						this.opgAnimatedFloats.put(node, (FloatAnimFlag)floatAnimFlag.deepCopy());
//					}
//				}
//			}
//			if(node instanceof ParticleEmitter2){
//				String[] flagNames = new String[] {MdlUtils.TOKEN_LATITUDE, MdlUtils.TOKEN_WIDTH, MdlUtils.TOKEN_LENGTH, MdlUtils.TOKEN_SPEED, MdlUtils.TOKEN_GRAVITY};
//				for(String s : flagNames){
//					AnimFlag<?> flag = node.find(s);
//					if (flag instanceof FloatAnimFlag floatAnimFlag) {
//						this.opgAnimatedFloats.put(node, (FloatAnimFlag)floatAnimFlag.deepCopy());
//					}
//				}
//			}
//		}

	}

	@Override
	public StaticMeshScaleAction undo() {
		Vec3 revScale = new Vec3(1, 1, 1).divide(0 < scale.length() ? scale : new Vec3(0.1, 0.1, 0.1).scale(0.0000001f));
		rawScale(center, revScale);

		for (IdObject node : selectedIdObjects) {
			Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
			if (translation != null) {
				translation.setSequenceMap(opgTranslation.get(node).getAnimMap());
			}
		}
		for (CameraNode node : selectedCameraNodes) {
			Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
			if (translation != null) {
				translation.setSequenceMap(opgTranslation.get(node).getAnimMap());
			}
		}


		for (int i = 0; i < selectedVertices.size(); i++) {
			selectedVertices.get(i).set(opgPosVertices.get(i));
		}

		for (int i = 0; i < selectedIdObjects.size(); i++) {
			selectedIdObjects.get(i).setPivotPoint(opgPosIdObjects.get(i));
		}

		for (int i = 0; i < selectedCameraNodes.size(); i++) {
			selectedCameraNodes.get(i).setPosition(opgPosCameraNodes.get(i));
		}

//		for (ScaleParticleAction particleAction : particleActions) {
//			particleAction.undo();
//		}
		for (AbstractTransformAction scaleAction : otherScaleActions) {
			scaleAction.undo();
		}
		return this;
	}

	@Override
	public StaticMeshScaleAction redo() {
		rawScale(center, scale);
		return this;
	}

	@Override
	public String actionName() {
		return "Scale";
	}

	@Override
	public StaticMeshScaleAction updateScale(Vec3 scale) {
		this.scale.multiply(scale);
		rawScale(center, scale);
		return this;
	}
	@Override
	public StaticMeshScaleAction setScale(Vec3 scale) {
		this.scale.set(scale);
		rawScale2(center, scale);
		return this;
	}

	private void rawScale(Vec3 center, Vec3 deltaScale) {
		double avgScale = (deltaScale.x + deltaScale.y + deltaScale.z) / 3;
		for (Vec3 vertex : selectedVertices) {
			vertex
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(deltaScale)
					.transform(invRotMat, 1, true)
					.add(center);
		}
		for (IdObject object : selectedIdObjects) {
			if (object instanceof CollisionShape shape) {
				List<Vec3> vertices = shape.getVertices();
				for(Vec3 vertex : vertices) {
					vertex
							.add(shape.getPivotPoint())
							.sub(center)
							.transform(rotMat, 1, true)
							.multiply(scale)
							.transform(invRotMat, 1, true)
							.add(center)
							.sub(shape.getPivotPoint());
				}
				if ((scale.x == scale.z) && (scale.y == scale.z)) {
					shape.setBoundsRadius(shape.getBoundsRadius() * scale.x);
				}
			}

			object.getPivotPoint()
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(deltaScale)
					.transform(invRotMat, 1, true)
					.add(center);

			// todo fix the scaling of animated translations
			scaleAnimatedTranslations(object, deltaScale);

		}
		for (CameraNode cameraNode : selectedCameraNodes) {
			cameraNode.getPosition()
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(deltaScale)
					.transform(invRotMat, 1, true)
					.add(center);
			scaleAnimatedTranslations(cameraNode, deltaScale);
		}
	}

	Vec3 tempVec = new Vec3();

	private void rawScale2(Vec3 center, Vec3 totScale) {
		double avgScale = (totScale.x + totScale.y + totScale.z) / 3;

		for (int i = 0; i < selectedVertices.size(); i++) {
			selectedVertices.get(i).set(opgPosVertices.get(i))
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(totScale)
					.transform(invRotMat, 1, true)
					.add(center);
		}

		for (int i = 0; i < selectedIdObjects.size(); i++) {
			IdObject object = selectedIdObjects.get(i);
			object.getPivotPoint().set(opgPosIdObjects.get(i))
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(totScale)
					.transform(invRotMat, 1, true)
					.add(center);

			// todo fix the scaling of animated translations
			scaleAnimatedTranslations(object, totScale);

			if (object instanceof CollisionShape shape) {
				List<Vec3> vertices = shape.getVertices();
				for(Vec3 vertex : vertices) {
					vertex
							.add(shape.getPivotPoint())
							.sub(center)
							.transform(rotMat, 1, true)
							.multiply(scale)
							.transform(invRotMat, 1, true)
							.add(center)
							.sub(shape.getPivotPoint());
				}
				if ((scale.x == scale.z) && (scale.y == scale.z)) {
					shape.setBoundsRadius(shape.getBoundsRadius() * scale.x);
				}
			}
		}

//		for (ScaleParticleAction particleAction : particleActions) {
//			particleAction.setScale(scale);
//		}

		for (AbstractTransformAction scaleAction : otherScaleActions) {
			scaleAction.setScale(scale);
		}

		for (int i = 0; i < selectedCameraNodes.size(); i++) {
			CameraNode cameraNode = selectedCameraNodes.get(i);
			cameraNode.getPivotPoint().set(opgPosCameraNodes.get(i))
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(totScale)
					.transform(invRotMat, 1, true)
					.add(center);
			scaleAnimatedTranslations(cameraNode, totScale);
		}
	}



	public void resetAnimatedTranslations() {
		for (IdObject node : selectedIdObjects) {
			Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
			if (translation != null) {
				translation.setSequenceMap(opgTranslation.get(node).getAnimMap());
			}
		}
		for (CameraNode node : selectedCameraNodes) {
			Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
			if (translation != null) {
				translation.setSequenceMap(opgTranslation.get(node).getAnimMap());
			}
		}
	}
	public void resetAnimatedTranslations(AnimatedNode node, Vec3 totScale) {
		Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation != null) {
			translation.setSequenceMap(opgTranslation.get(node).getAnimMap());
		}
	}

	public void scaleAnimatedTranslations(AnimatedNode node, Vec3 totScale) {
		Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation != null) {
			translation.setSequenceMap(opgTranslation.get(node).getAnimMap());
			for (TreeMap<Integer, Entry<Vec3>> entryMap : translation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.getValue().multiply(totScale);
						if (translation.tans()) {
							entry.getInTan().multiply(totScale);
							entry.getOutTan().multiply(totScale);
						}
					}
				}
			}
		}
	}

	public void scaleAnimatedTranslations1(AnimatedNode node, Vec3 deltaScale) {
		Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation != null) {
			for (TreeMap<Integer, Entry<Vec3>> entryMap : translation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.getValue().multiply(deltaScale);
						if (translation.tans()) {
							entry.getInTan().multiply(deltaScale);
							entry.getOutTan().multiply(deltaScale);
						}
					}
				}
			}
		}
	}

	public void translateNodeAlt(AnimatedNode node, Vec3 scale) {
		// not sure if this is correct or not...
		Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation != null) {
			for (TreeMap<Integer, Entry<Vec3>> entryMap : translation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
//						entry.getValue().multiply(scale);
						entry.getValue()
								.add(node.getPivotPoint())
								.sub(center)
								.transform(rotMat, 1, true)
								.multiply(scale)
								.transform(invRotMat, 1, true)
								.add(center)
								.sub(node.getPivotPoint());
						if (translation.tans()) {
//							entry.getInTan().multiply(scale);
//							entry.getOutTan().multiply(scale);
							entry.getInTan()
									.add(node.getPivotPoint())
									.sub(center)
									.transform(rotMat, 1, true)
									.multiply(scale)
									.transform(invRotMat, 1, true)
									.add(center)
									.sub(node.getPivotPoint());
							entry.getOutTan()
									.add(node.getPivotPoint())
									.sub(center)
									.transform(rotMat, 1, true)
									.multiply(scale)
									.transform(invRotMat, 1, true)
									.add(center)
									.sub(node.getPivotPoint());
						}
					}
				}
			}
		}
	}
}
