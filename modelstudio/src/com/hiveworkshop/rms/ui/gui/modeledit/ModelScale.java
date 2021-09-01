package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Vec3;

import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

public class ModelScale {

	public static void main(String[] args) throws IOException {
		final File santaFile = new File(
				"C:\\Users\\Eric\\Documents\\Warcraft\\Models\\Hayate\\Warcraft_Santa\\Warcraft Santa\\SantaClausFull.mdx");
		final EditableModel model = MdxUtils.loadEditable(santaFile);
		Vec3 scale = new Vec3(0.35, 0.35, 0.35);
		scale(model, scale);
		MdxUtils.saveMdx(model, new File(
				"C:\\Users\\Eric\\Documents\\Warcraft\\Models\\Hayate\\Warcraft_Santa\\Warcraft Santa\\SantaClausFull_scaled.mdx"));
	}

	public static void scale(EditableModel mdl, Vec3 scale) {
		Vec3 center = new Vec3(0, 0, 0);
		scale(mdl, scale, center);
	}

	public static void scale(EditableModel mdl, Vec3 scale, Vec3 center) {
		for (final AnimFlag<?> flag : mdl.getAllAnimFlags()) {
			if (flag.getTypeId() == AnimFlag.TRANSLATION) {
				for(Sequence anim : flag.getAnimMap().keySet()){
					TreeMap<Integer, Entry<Vec3>> entryMap = ((Vec3AnimFlag)flag).getEntryMap(anim);
					if(entryMap != null){
						for(Entry<Vec3> entry : entryMap.values()){
							entry.getValue().scale(center, scale);

							if (flag.tans()) {
								entry.getInTan().scale(center, scale);
								entry.getOutTan().scale(center, scale);
							}
						}
					}
				}
			}
		}
		for (final Geoset geoset : mdl.getGeosets()) {
			for (final Vec3 vertex : geoset.getVertices()) {
				vertex.scale(center, scale);
			}
			for (final Animation anim : geoset.getAnims()) {
				scale(center, scale, anim.getExtents());
			}
		}
		for (final IdObject object : mdl.getAllObjects()) {
			object.getPivotPoint().scale(center, scale);
		}
//		for (final Vec3 vertex : mdl.getPivots()) {
//			vertex.scale(centerX, centerY, centerZ, x, y, z);
//		}
		for (final Camera camera : mdl.getCameras()) {
			camera.getPosition().scale(center, scale);
			camera.getTargetPosition().scale(center, scale);
		}
		for (CollisionShape collision : mdl.getColliders()) {
			for (Vec3 vertex : collision.getVertices()) {
				vertex.scale(center, scale);
			}
			ExtLog extents = collision.getExtents();
			scale(center, scale, extents);
		}
		double avgScale = (scale.x + scale.y + scale.z) / 3;
		for (ParticleEmitter2 particle : mdl.getParticleEmitter2s()) {
			particle.setLength(particle.getLength() * avgScale);
			particle.setWidth(particle.getWidth() * avgScale);
			particle.getParticleScaling().scale(0, 0, 0, avgScale, avgScale, avgScale);
			particle.setSpeed(particle.getSpeed() * avgScale);
			particle.setGravity(particle.getGravity() * avgScale);
		}
		scale(center, scale, mdl.getExtents());
		for (Animation anim : mdl.getAnims()) {
			scale(center, scale, anim.getExtents());
		}
	}

	private static void scale(Vec3 center, Vec3 scale,
	                          final ExtLog extents) {
		if (extents == null) {
			return;
		}
		if (extents.getMaximumExtent() != null) {
			extents.getMaximumExtent().scale(center, scale);
		}
		if (extents.getMinimumExtent() != null) {
			extents.getMinimumExtent().scale(center, scale);
		}
	}

}
