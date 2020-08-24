package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.Attachment;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.CollisionShape;
import com.hiveworkshop.rms.editor.model.EventObject;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.Helper;
import com.hiveworkshop.rms.editor.model.Light;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.ParticleEmitter;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.RibbonEmitter;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;

public abstract class ModelVisitorAdapter implements ModelVisitor {

	@Override
	public void bone(final Bone object) {

	}

	@Override
	public void light(final Light light) {

	}

	@Override
	public void helper(final Helper object) {

	}

	@Override
	public void attachment(final Attachment attachment) {

	}

	@Override
	public void particleEmitter(final ParticleEmitter particleEmitter) {

	}

	@Override
	public void particleEmitter2(final ParticleEmitter2 particleEmitter) {

	}

	@Override
	public void ribbonEmitter(final RibbonEmitter particleEmitter) {

	}

	@Override
	public void eventObject(final EventObject eventObject) {

	}

	@Override
	public void collisionShape(final CollisionShape collisionShape) {

	}

	@Override
	public void camera(final Camera camera) {

	}

	@Override
	public GeosetVisitor beginGeoset(final int geosetId, final Material material, final GeosetAnim geosetAnim) {
		return GeosetVisitor.NO_ACTION;
	}

}
