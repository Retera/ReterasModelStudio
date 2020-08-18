package com.hiveworkshop.wc3.gui.modeledit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.etheller.warsmash.parsers.mdlx.MdlxModel;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdx.MdxUtils;

public class ModelScale {

	public static void main(final String[] args) throws FileNotFoundException, IOException {
		final File santaFile = new File(
				"C:\\Users\\Eric\\Documents\\Warcraft\\Models\\Hayate\\Warcraft_Santa\\Warcraft Santa\\SantaClausFull.mdx");
		final EditableModel model = MdxUtils.loadEditableModel(santaFile);
		scale(model, 0.35, 0.35, 0.35);
		model.toMdlx().saveMdx(new FileOutputStream(new File(
				"C:\\Users\\Eric\\Documents\\Warcraft\\Models\\Hayate\\Warcraft_Santa\\Warcraft Santa\\SantaClausFull_scaled.mdx")));
	}

	public static void scale(final EditableModel mdl, final double x, final double y, final double z) {
		scale(mdl, x, y, z, 0, 0, 0);
	}

	public static void scale(final EditableModel mdl, final double x, final double y, final double z, final double centerX,
			final double centerY, final double centerZ) {
		final double avgScale = (x + y + z) / 3;
		for (final AnimFlag flag : mdl.getAllAnimFlags()) {
			if (flag.getTypeId() == AnimFlag.TRANSLATION) {
				for (int i = 0; i < flag.size(); i++) {
					final Vertex value = (Vertex) flag.getValues().get(i);
					value.scale(centerX, centerY, centerZ, x, y, z);
					if (flag.tans()) {
						final Vertex inTan = (Vertex) flag.getInTans().get(i);
						inTan.scale(centerX, centerY, centerZ, x, y, z);
						final Vertex outTan = (Vertex) flag.getOutTans().get(i);
						outTan.scale(centerX, centerY, centerZ, x, y, z);
					}
				}
			}
		}
		for (final Geoset geoset : mdl.getGeosets()) {
			for (final Vertex vertex : geoset.getVertices()) {
				vertex.scale(centerX, centerY, centerZ, x, y, z);
			}
			for (final Animation anim : geoset.getAnims()) {
				scale(centerX, centerY, centerZ, x, y, z, anim.getExtents());
			}
		}
		for (final Vertex vertex : mdl.getPivots()) {
			vertex.scale(centerX, centerY, centerZ, x, y, z);
		}
		for (final Camera camera : mdl.getCameras()) {
			camera.getPosition().scale(centerX, centerY, centerZ, x, y, z);
			camera.getTargetPosition().scale(centerX, centerY, centerZ, x, y, z);
		}
		for (final CollisionShape collision : mdl.sortedIdObjects(CollisionShape.class)) {
			for (final Vertex vertex : collision.getVertices()) {
				vertex.scale(centerX, centerY, centerZ, x, y, z);
			}
			final ExtLog extents = collision.getExtents();
			scale(centerX, centerY, centerZ, x, y, z, extents);
		}
		for (final ParticleEmitter2 particle : mdl.sortedIdObjects(ParticleEmitter2.class)) {
			particle.setLength(particle.getLength() * avgScale);
			particle.setWidth(particle.getWidth() * avgScale);
			particle.getParticleScaling().scale(0, 0, 0, avgScale, avgScale, avgScale);
			particle.setSpeed(particle.getSpeed() * avgScale);
			particle.setGravity(particle.getGravity() * avgScale);
		}
		scale(centerX, centerY, centerZ, x, y, z, mdl.getExtents());
		for (final Animation anim : mdl.getAnims()) {
			scale(centerX, centerY, centerZ, x, y, z, anim.getExtents());
		}
	}

	private static void scale(final double centerX, final double centerY, final double centerZ, final double x,
			final double y, final double z, final ExtLog extents) {
		if (extents == null) {
			return;
		}
		if (extents.getMaximumExtent() != null) {
			extents.getMaximumExtent().scale(centerX, centerY, centerZ, x, y, z);
		}
		if (extents.getMinimumExtent() != null) {
			extents.getMinimumExtent().scale(centerX, centerY, centerZ, x, y, z);
		}
	}

}
