package com.hiveworkshop.scripts;

import java.io.File;
import java.util.function.Consumer;

import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class GhostwolfSlave {
	public static void main(final String[] args) {
		writeModel("ParticleEmitter2", model -> {
			model.add(new Animation("Stand", 0, 100));
			model.add(new ParticleEmitter2("BlizParticle001") {
				{
					setTexture(new Bitmap("Textures\\Clouds8x8.blp"));
					setEmissionRate(20);
					setLatitude(0);
					setColumns(8);
					setRows(8);
					setParticleScaling(new Vertex(100, 50, 0));
					setAlpha(new Vertex(255, 255, 255));
					setSpeed(30);
					setLifeSpan(20);
					setPivotPoint(new Vertex(0, 0, 0));
					for (int i = 0; i < 3; i++) {
						setSegmentColor(i, new Vertex(1, 1, 1));
					}
					add("Head");
					add("Blend");
				}
			});
		});
	}

	public static void writeModel(final String name, final Consumer<MDL> modelCallback) {
		final MDL model = new MDL(name);
		modelCallback.accept(model);
		model.printTo(new File("output/" + name + ".mdl"));
		System.out.println(model.sortedIdObjects(ParticleEmitter2.class));
	}

	private GhostwolfSlave() {
	}
}
