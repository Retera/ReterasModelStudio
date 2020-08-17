package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;

import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdx.MdxUtils;

public class FtlopMaker3 {

	public static void main(final String[] args) throws IOException {
		traverse(new File("E:\\Games\\FtlopMod\\_dev\\BuildArchive"));
	}

	public static void traverse(final File file) throws IOException {
		if (file.isDirectory()) {
			for (final File subFile : file.listFiles()) {
				traverse(subFile);
			}
		} else {
			if (file.getName().toLowerCase().endsWith(".mdx")) {
				final EditableModel model = MdxUtils.loadEditableModel(file);
				for (final Bitmap tex : model.getTextures()) {
					final String path = tex.getPath();
					if (path != null) {
						tex.setPath(path.replace('/', '\\'));
					}
				}
				for (final ParticleEmitter emitter : model.sortedIdObjects(ParticleEmitter.class)) {
					final String path = emitter.getPath();
					if (path != null) {
						emitter.setPath(path.replace('/', '\\'));
					}
				}
				for (final Attachment emitter : model.sortedIdObjects(Attachment.class)) {
					final String path = emitter.getPath();
					if (path != null) {
						emitter.setPath(path.replace('/', '\\'));
					}
				}
				MdxUtils.saveEditableModel(model, file);
			}
		}
	}
}
