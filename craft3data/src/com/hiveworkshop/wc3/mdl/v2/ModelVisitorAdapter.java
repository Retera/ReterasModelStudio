package com.hiveworkshop.wc3.mdl.v2;

import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.v2.visitor.GeosetVisitor;
import com.hiveworkshop.wc3.mdl.v2.visitor.ModelVisitor;

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
	public GeosetVisitor beginGeoset(final int geosetId, final MaterialView material, final GeosetAnim geosetAnim) {
		return GeosetVisitor.NO_ACTION;
	}

}
