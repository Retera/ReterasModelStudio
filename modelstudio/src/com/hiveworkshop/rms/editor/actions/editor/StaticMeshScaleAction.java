package com.hiveworkshop.rms.editor.actions.editor;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;

public class StaticMeshScaleAction extends AbstractTransformAction {
	private final Vec3 center;
	private final Vec3 scale;
	private final ArrayList<GeosetVertex> selectedVertices;
	private final ArrayList<IdObject> selectedIdObjects;
	private final ArrayList<CameraNode> selectedCameraNodes;

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
	}

	@Override
	public StaticMeshScaleAction undo() {
		Vec3 revScale = new Vec3(1, 1, 1).divide(0 < scale.length() ? scale : new Vec3(0.1, 0.1, 0.1).scale(0.0000001f));
		rawScale(center, revScale);

		for (int i = 0; i<selectedVertices.size(); i++) {
			selectedVertices.get(i).set(opgPosVertices.get(i));
		}

		for (int i = 0; i<selectedIdObjects.size(); i++) {
			selectedIdObjects.get(i).setPivotPoint(opgPosIdObjects.get(i));
		}

		for (int i = 0; i<selectedCameraNodes.size(); i++) {
			selectedCameraNodes.get(i).setPosition(opgPosCameraNodes.get(i));
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

	private void rawScale(Vec3 center, Vec3 scale) {
		double avgScale = (scale.x + scale.y + scale.z) / 3;
		for (Vec3 vertex : selectedVertices) {
			vertex
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center);
		}
		for (IdObject object : selectedIdObjects) {
			scaleNodeStuff1(scale, avgScale, object);

			object.getPivotPoint()
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center);

			// todo fix the scaling of animated translations
			scaleAnimatedTranslations(object, scale);

		}
		for (CameraNode cameraNode : selectedCameraNodes) {
			cameraNode.getPosition()
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center);
			scaleAnimatedTranslations(cameraNode, scale);
		}
	}

	Vec3 tempVec = new Vec3();

	private void rawScale2(Vec3 center, Vec3 scale) {
		double avgScale = (scale.x + scale.y + scale.z) / 3;

		for (int i = 0; i<selectedVertices.size(); i++) {
			selectedVertices.get(i).set(opgPosVertices.get(i))
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center);
		}

		for (int i = 0; i<selectedIdObjects.size(); i++) {
			IdObject object = selectedIdObjects.get(i);
			object.getPivotPoint().set(opgPosIdObjects.get(i))
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center);

			// todo fix the scaling of animated translations
			scaleAnimatedTranslations(object, scale);

			scaleNodeStuff1(scale, avgScale, object);
		}

		for (int i = 0; i<selectedCameraNodes.size(); i++) {
			CameraNode cameraNode = selectedCameraNodes.get(i);
			cameraNode.getPivotPoint().set(opgPosCameraNodes.get(i))
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center);
			scaleAnimatedTranslations(cameraNode, scale);
		}
	}
	private void scaleNodeStuff(Vec3 center, Vec3 scale, double avgScale, IdObject object) {
		// ToDo check if these non-pivot-scaling should be done relative something or not...
		if (object instanceof CollisionShape) {
			CollisionShape shape = (CollisionShape) object;
			List<Vec3> vertices = shape.getVertices();
			for(Vec3 vertex : vertices){
				vertex
						.add(shape.getPivotPoint())
						.sub(center)
						.transform(rotMat, 1, true)
						.multiply(scale)
						.transform(invRotMat, 1, true)
						.add(center)
						.sub(shape.getPivotPoint());
			}
//				if ((scale.x == scale.z) && (scale.y == scale.z)) {
//					shape.setBoundsRadius(shape.getBoundsRadius() * scale.x);
//				}
			shape.setBoundsRadius(shape.getBoundsRadius() * avgScale);
		}
		if(object instanceof ParticleEmitter2){
			ParticleEmitter2 particle = (ParticleEmitter2) object;
			tempVec.set(particle.getLatitude(), particle.getWidth(), particle.getLength())
					.add(particle.getPivotPoint())
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center)
					.sub(particle.getPivotPoint());
			particle.setLatitude(tempVec.z);
			particle.setWidth(tempVec.y);
			particle.setLength(tempVec.x);

//				tempVec.set(0,0, particle.getSpeed())
			tempVec.set(1,0, 1)
					.add(particle.getPivotPoint())
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center)
					.sub(particle.getPivotPoint());
			particle.setSpeed(particle.getSpeed()*tempVec.z);
			particle.getParticleScaling().scale(tempVec.x);
			particle.setGravity(particle.getGravity() * tempVec.z);
		}
		if(object instanceof ParticleEmitter){
			ParticleEmitter particle = (ParticleEmitter) object;
			tempVec.set(1,0, 1)
					.add(particle.getPivotPoint())
					.sub(center)
					.transform(rotMat, 1, true)
					.multiply(scale)
					.transform(invRotMat, 1, true)
					.add(center)
					.sub(particle.getPivotPoint());
			particle.setLatitude(particle.getLatitude()*tempVec.z);
			particle.setLongitude(particle.getLongitude()*tempVec.y);
			particle.setInitVelocity(particle.getInitVelocity()*tempVec.z);
			particle.setGravity(particle.getGravity() * tempVec.z);

		}
	}

	private void scaleNodeStuff1(Vec3 scale, double avgScale, IdObject object) {
		if (object instanceof CollisionShape) {
			CollisionShape shape = (CollisionShape) object;
			if ((scale.x == scale.z) && (scale.y == scale.z)) {
				shape.setBoundsRadius(shape.getBoundsRadius() * scale.x);
			}
		}
		if(object instanceof ParticleEmitter2){
			((ParticleEmitter2) object).setLatitude(((ParticleEmitter2) object).getLatitude()* scale.z);
			((ParticleEmitter2) object).setWidth(((ParticleEmitter2) object).getWidth()* scale.y);
			((ParticleEmitter2) object).setLength(((ParticleEmitter2) object).getLength()* scale.x);
			((ParticleEmitter2) object).getParticleScaling().multiply(scale);
			((ParticleEmitter2) object).setSpeed(((ParticleEmitter2) object).getSpeed() * avgScale);
			((ParticleEmitter2) object).setGravity(((ParticleEmitter2) object).getGravity() * avgScale);
		}
		if(object instanceof ParticleEmitter){
			((ParticleEmitter) object).setLatitude(((ParticleEmitter) object).getLatitude()* scale.z);
			((ParticleEmitter) object).setLongitude(((ParticleEmitter) object).getLongitude()* scale.y);
			((ParticleEmitter) object).setInitVelocity(((ParticleEmitter) object).getInitVelocity()* avgScale);

		}
	}

	public void scaleAnimatedTranslations(AnimatedNode node, Vec3 scale) {
		Vec3AnimFlag translation = (Vec3AnimFlag) node.find(MdlUtils.TOKEN_TRANSLATION);
		if (translation != null) {
			for (TreeMap<Integer, Entry<Vec3>> entryMap : translation.getAnimMap().values()) {
				if (entryMap != null) {
					for (Entry<Vec3> entry : entryMap.values()) {
						entry.getValue().multiply(scale);
						if (translation.tans()) {
							entry.getInTan().multiply(scale);
							entry.getOutTan().multiply(scale);
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



	private void scaleExtent(ExtLog extents, Vec3 center, Vec3 scale, double avgScale) {
		if (extents == null) {
			return;
		}
		if (extents.getMaximumExtent() != null) {
			extents.getMaximumExtent().scale(center, scale);
		}
		if (extents.getMinimumExtent() != null) {
			extents.getMinimumExtent().scale(center, scale);
		}
		if(extents.hasBoundsRadius()){
			extents.setBoundsRadius(extents.getBoundsRadius() * avgScale);
		}
	}

}
