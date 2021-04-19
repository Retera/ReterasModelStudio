package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;

public abstract class ModelVisitorAdapter implements ModelVisitor {

	@Override
	public void bone(Bone object) {
	}

	@Override
	public void light(Light light) {
	}

	@Override
	public void helper(Helper object) {
	}

	@Override
	public void attachment(Attachment attachment) {
	}

	@Override
	public void particleEmitter(ParticleEmitter particleEmitter) {
	}

	@Override
	public void particleEmitter2(ParticleEmitter2 particleEmitter) {
	}

	@Override
	public void ribbonEmitter(RibbonEmitter particleEmitter) {
	}

	@Override
	public void eventObject(EventObject eventObject) {
	}

	@Override
	public void collisionShape(CollisionShape collisionShape) {
	}

	@Override
	public void camera(Camera camera) {
	}

	@Override
	public GeosetVisitor beginGeoset(int geosetId, Material material, GeosetAnim geosetAnim) {
		return GeosetVisitor.NO_ACTION;
	}

}
