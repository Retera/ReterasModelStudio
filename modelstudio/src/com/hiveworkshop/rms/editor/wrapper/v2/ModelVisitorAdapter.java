package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;

public abstract class ModelVisitorAdapter implements ModelVisitor {

	@Override
	public void visitIdObject(IdObject idObject) {
	}

	@Override
	public void camera(Camera camera) {
	}

	@Override
	public GeosetVisitor beginGeoset(int geosetId, Material material, GeosetAnim geosetAnim) {
		return GeosetVisitor.NO_ACTION;
	}

}
