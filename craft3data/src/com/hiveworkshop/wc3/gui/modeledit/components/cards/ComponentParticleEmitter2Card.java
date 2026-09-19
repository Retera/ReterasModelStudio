package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.IdObject.NodeFlags;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.Vertex;

public class ComponentParticleEmitter2Card extends ComponentNodeCard<ParticleEmitter2> {
	private static final List<String> FILTER_MODES = Arrays.asList("Blend", "Additive", "Modulate", "Modulate2x",
			"AlphaKey");
	private static final List<String> HEAD_TAIL = Arrays.asList("Head", "Tail", "Both");
	private static final String[] OPTION_FLAGS = { "SortPrimsFarZ", "Unshaded", "LineEmitter", "Unfogged",
			"ModelSpace", "XYQuad", "Squirt" };

	@Override
	protected void addTypeRows() {
		beginSection("Particle Emitter 2");
		addComboBox("Texture", this::textureOptions, this::describeTexture, ParticleEmitter2::getTexture,
				ParticleEmitter2::setTexture);
		addComboBox("Filter Mode", () -> FILTER_MODES, mode -> mode, emitter -> {
			for (final String mode : FILTER_MODES) {
				if (emitter.getFlags().contains(mode)) {
					return mode;
				}
			}
			return "Blend";
		}, (emitter, mode) -> {
			for (final String candidate : FILTER_MODES) {
				emitter.setKnownFlag(candidate, candidate.equals(mode));
			}
		});
		addComboBox("Head / Tail", () -> HEAD_TAIL, mode -> mode, emitter -> {
			if (emitter.isBoth()) {
				return "Both";
			}
			if (emitter.getFlags().contains("Tail")) {
				return "Tail";
			}
			return "Head";
		}, (emitter, mode) -> {
			for (final String candidate : HEAD_TAIL) {
				emitter.setKnownFlag(candidate, candidate.equals(mode));
			}
		});
		for (final String flag : OPTION_FLAGS) {
			addCheckBox(flag, emitter -> emitter.getFlags().contains(flag),
					(emitter, value) -> emitter.setKnownFlag(flag, value));
		}
		addIntSpinner("Rows", 1, Integer.MAX_VALUE, ParticleEmitter2::getRows, ParticleEmitter2::setRows);
		addIntSpinner("Columns", 1, Integer.MAX_VALUE, ParticleEmitter2::getColumns, ParticleEmitter2::setColumns);
		addIntSpinner("Replaceable ID", 0, Integer.MAX_VALUE, ParticleEmitter2::getReplaceableId,
				ParticleEmitter2::setReplaceableId);
		addIntSpinner("Priority Plane", Integer.MIN_VALUE, Integer.MAX_VALUE, ParticleEmitter2::getPriorityPlane,
				ParticleEmitter2::setPriorityPlane);
		addDoubleSpinner("Life Span", 0.1, ParticleEmitter2::getLifeSpan, ParticleEmitter2::setLifeSpan);
		addDoubleSpinner("Tail Length", 0.1, ParticleEmitter2::getTailLength, ParticleEmitter2::setTailLength);
		addDoubleSpinner("Time (segment split)", 0.05, ParticleEmitter2::getTime, ParticleEmitter2::setTime);
		endSection();
		addFloatValue("Speed", ParticleEmitter2::getSpeed, ParticleEmitter2::setSpeed, e -> e,
				ParticleEmitter2::getAnimFlags, "Speed");
		addFloatValue("Variation", ParticleEmitter2::getVariation, ParticleEmitter2::setVariation, e -> e,
				ParticleEmitter2::getAnimFlags, "Variation");
		addFloatValue("Latitude", ParticleEmitter2::getLatitude, ParticleEmitter2::setLatitude, e -> e,
				ParticleEmitter2::getAnimFlags, "Latitude");
		addFloatValue("Gravity", ParticleEmitter2::getGravity, ParticleEmitter2::setGravity, e -> e,
				ParticleEmitter2::getAnimFlags, "Gravity");
		addFloatValue("Emission Rate", ParticleEmitter2::getEmissionRate, ParticleEmitter2::setEmissionRate, e -> e,
				ParticleEmitter2::getAnimFlags, "EmissionRate");
		addFloatValue("Width", ParticleEmitter2::getWidth, ParticleEmitter2::setWidth, e -> e,
				ParticleEmitter2::getAnimFlags, "Width");
		addFloatValue("Length", ParticleEmitter2::getLength, ParticleEmitter2::setLength, e -> e,
				ParticleEmitter2::getAnimFlags, "Length");
		beginSection("Segments (start / middle / end)");
		for (int i = 0; i < 3; i++) {
			final int index = i;
			addColorButton("Color " + (i + 1), emitter -> emitter.getSegmentColor(index),
					(emitter, color) -> emitter.setSegmentColor(index, color));
		}
		addVertexRow("Alpha", 1.0, ParticleEmitter2::getAlpha, ParticleEmitter2::setAlpha);
		addVertexRow("Scaling", 1.0, ParticleEmitter2::getParticleScaling, ParticleEmitter2::setParticleScaling);
		endSection();
		beginSection("UV animation (start / end / repeat)");
		addVertexRow("Life Span", 1.0, ParticleEmitter2::getLifeSpanUVAnim, ParticleEmitter2::setLifeSpanUVAnim);
		addVertexRow("Decay", 1.0, ParticleEmitter2::getDecayUVAnim, ParticleEmitter2::setDecayUVAnim);
		addVertexRow("Tail", 1.0, ParticleEmitter2::getTailUVAnim, ParticleEmitter2::setTailUVAnim);
		addVertexRow("Tail Decay", 1.0, ParticleEmitter2::getTailDecayUVAnim, ParticleEmitter2::setTailDecayUVAnim);
		endSection();
	}

	@Override
	protected void setNodeFlag(final ParticleEmitter2 node, final NodeFlags flag, final boolean value) {
		node.setKnownFlag(flag.getMdlText(), value);
	}

	private List<Bitmap> textureOptions() {
		return new ArrayList<>(model().getTextures());
	}

	private String describeTexture(final Bitmap bitmap) {
		final int index = model().getTextures().indexOf(bitmap);
		return "Texture " + index + ": " + bitmap.getName();
	}

	@SuppressWarnings("unused")
	private static Vertex orZero(final Vertex vertex) {
		return vertex == null ? new Vertex(0, 0, 0) : vertex;
	}
}
