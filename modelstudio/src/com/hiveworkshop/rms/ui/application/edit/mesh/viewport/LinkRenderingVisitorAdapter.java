package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableAnimatedIdObjectParentLinkRenderer;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;

public class LinkRenderingVisitorAdapter implements ModelVisitor {
	private ResettableAnimatedIdObjectParentLinkRenderer linkRenderer;

	public LinkRenderingVisitorAdapter(ProgramPreferences programPreferences) {
		linkRenderer = new ResettableAnimatedIdObjectParentLinkRenderer(programPreferences.getVertexSize());
	}

	public ResettableAnimatedIdObjectParentLinkRenderer reset(CoordinateSystem coordinateSystem, Graphics2D graphics, RenderModel renderModel) {

		linkRenderer.reset(coordinateSystem, graphics, NodeIconPalette.HIGHLIGHT, renderModel);
		return linkRenderer;
	}

	@Override
	public GeosetVisitor beginGeoset(int geosetId, Material material, GeosetAnim geosetAnim) {
		return GeosetVisitor.NO_ACTION;
	}

	@Override
	public void bone(Bone object) {
		linkRenderer.bone(object);
	}

	@Override
	public void helper(Helper object) {
		linkRenderer.helper(object);
	}

	@Override
	public void ribbonEmitter(RibbonEmitter object) {
		linkRenderer.ribbonEmitter(object);
	}

	@Override
	public void particleEmitter2(ParticleEmitter2 object) {
		linkRenderer.particleEmitter2(object);
	}

	@Override
	public void particleEmitter(ParticleEmitter object) {
		linkRenderer.particleEmitter(object);
	}

	@Override
	public void popcornFxEmitter(ParticleEmitterPopcorn object) {
		linkRenderer.popcornFxEmitter(object);
	}

	@Override
	public void light(Light object) {
		linkRenderer.light(object);
	}

	@Override
	public void eventObject(EventObject object) {
		linkRenderer.eventObject(object);
	}

	@Override
	public void collisionShape(CollisionShape object) {
		linkRenderer.collisionShape(object);
	}

	@Override
	public void attachment(Attachment object) {
		linkRenderer.attachment(object);
	}

	@Override
	public void camera(Camera camera) {
	}
}
