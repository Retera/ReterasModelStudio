package com.hiveworkshop.wc3.mdl.render3d;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.gui.animedit.BasicTimeBoundProvider;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.Vertex;

/**
 * Per-instance state of a popcorn emitter preview: the emission accumulator
 * and the animation visibility guide ("Always=off, Stand Lumber=on").
 */
public class RenderPopcornEmitterView extends EmitterView {
	/** Particles per second per renderer before the emitter's multiplier. */
	private static final double BASE_EMISSION_RATE = 10;

	private final ParticleEmitterPopcorn emitter;
	private final RenderPopcornEmitter renderEmitter;
	private final Map<String, Boolean> visibilityGuide = new LinkedHashMap<>();
	private boolean guideDefault = true;
	private String parsedGuide;

	public RenderPopcornEmitterView(final RenderModel instance, final RenderPopcornEmitter emitter) {
		this.instance = instance;
		this.renderEmitter = emitter;
		this.emitter = emitter.getEmitter();
		this.currentEmission = 0;
	}

	public ParticleEmitterPopcorn getEmitter() {
		return emitter;
	}

	public RenderPopcornEmitter getRenderEmitter() {
		return renderEmitter;
	}

	public void update() {
	}

	public void fill() {
		if (!instance.allowParticleSpawn() || !isEnabledForCurrentAnimation()) {
			return;
		}
		final double visibility = emitter.getRenderVisibility(instance.getAnimatedRenderEnvironment());
		if (visibility < 0.5) {
			return;
		}
		final double rate = BASE_EMISSION_RATE * getEmissionRateMultiplier();
		currentEmission += rate * AnimatedRenderEnvironment.FRAMES_PER_UPDATE * 0.001;
		renderEmitter.fill(this);
	}

	@Override
	public void addToScene(final InternalInstance internalInstance) {
		throw new UnsupportedOperationException();
	}

	private double track(final String name, final double fallback) {
		final AnimFlag flag = AnimFlag.find(emitter.getAnimFlags(), name);
		if (flag != null) {
			final Object value = flag.interpolateAt(instance.getAnimatedRenderEnvironment());
			if (value instanceof Number) {
				return ((Number) value).doubleValue();
			}
		}
		return fallback;
	}

	public double getEmissionRateMultiplier() {
		return track("EmissionRate", emitter.getEmissionRate());
	}

	public double getLifeSpanMultiplier() {
		return track("LifeSpan", emitter.getLifeSpan());
	}

	public double getSpeedMultiplier() {
		return track("Speed", emitter.getSpeed());
	}

	public double getAlpha() {
		return track("Alpha", emitter.getAlpha());
	}

	/** Colour as stored in the model: BGR (x = blue). */
	public Vertex getColor() {
		final AnimFlag flag = AnimFlag.find(emitter.getAnimFlags(), "Color");
		if (flag != null) {
			final Object value = flag.interpolateAt(instance.getAnimatedRenderEnvironment());
			if (value instanceof Vertex) {
				return (Vertex) value;
			}
		}
		return emitter.getColor();
	}

	/**
	 * The guide names sequences that switch the effect on or off; a token
	 * "Always" sets the default. Matching is a case-insensitive substring test
	 * against the sequence name, like the game's.
	 */
	private boolean isEnabledForCurrentAnimation() {
		final String guide = emitter.getAnimVisibilityGuide();
		if ((guide == null) || guide.trim().isEmpty()) {
			return true;
		}
		if (!guide.equals(parsedGuide)) {
			parseGuide(guide);
		}
		final AnimatedRenderEnvironment environment = instance.getAnimatedRenderEnvironment();
		final BasicTimeBoundProvider current = environment == null ? null : environment.getCurrentAnimation();
		if (!(current instanceof Animation)) {
			return guideDefault || !visibilityGuide.isEmpty();
		}
		final String name = ((Animation) current).getName().toLowerCase(Locale.US);
		boolean enabled = guideDefault;
		for (final Map.Entry<String, Boolean> entry : visibilityGuide.entrySet()) {
			if (name.contains(entry.getKey())) {
				enabled = entry.getValue();
			}
		}
		return enabled;
	}

	private void parseGuide(final String guide) {
		parsedGuide = guide;
		visibilityGuide.clear();
		boolean sawAlways = false;
		boolean sawEnable = false;
		for (final String token : guide.split(",")) {
			final String trimmed = token.trim();
			if (trimmed.isEmpty()) {
				continue;
			}
			final int equals = trimmed.indexOf('=');
			final String key = (equals < 0 ? trimmed : trimmed.substring(0, equals)).trim().toLowerCase(Locale.US);
			final String value = equals < 0 ? "on" : trimmed.substring(equals + 1).trim().toLowerCase(Locale.US);
			final boolean on = !value.equals("off") && !value.equals("false") && !value.equals("0");
			if (key.equals("always")) {
				guideDefault = on;
				sawAlways = true;
			} else {
				visibilityGuide.put(key, on);
				sawEnable |= on;
			}
		}
		if (!sawAlways) {
			// a guide that only lists the sequences to turn on is off everywhere else
			guideDefault = !sawEnable;
		}
	}
}
