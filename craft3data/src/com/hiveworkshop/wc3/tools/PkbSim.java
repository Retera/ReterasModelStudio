package com.hiveworkshop.wc3.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

import com.hiveworkshop.wc3.pkb.PkbEffect;
import com.hiveworkshop.wc3.pkb.PkbReader;
import com.hiveworkshop.wc3.pkb.bind.EffectBinder;
import com.hiveworkshop.wc3.pkb.bind.EffectPlan;
import com.hiveworkshop.wc3.pkb.bind.EventRoute;
import com.hiveworkshop.wc3.pkb.bind.ExternalBinding;
import com.hiveworkshop.wc3.pkb.bind.FunctionBinding;
import com.hiveworkshop.wc3.pkb.bind.LayerProgram;
import com.hiveworkshop.wc3.pkb.bind.LayerRenderer;
import com.hiveworkshop.wc3.pkb.bind.ProgramDescriptor;
import com.hiveworkshop.wc3.pkb.bind.SamplerResource;
import com.hiveworkshop.wc3.pkb.sim.EffectRuntime;
import com.hiveworkshop.wc3.pkb.sim.RenderPacket;
import com.hiveworkshop.wc3.pkb.vm.Instruction;

/**
 * Headless check of the PopcornFX runtime port: binds a .pkb, prints the
 * layer programs, ticks the effect and reports what the render packets hold.
 *
 * <pre>
 * PkbSim effect.pkb [--ticks N] [--disasm] [--dt seconds] [--quiet]
 * </pre>
 */
public final class PkbSim {
	private PkbSim() {
	}

	public static void main(final String[] args) throws IOException {
		if (args.length < 1) {
			System.err.println("usage: PkbSim effect.pkb [--ticks N] [--disasm] [--dt seconds] [--quiet]");
			System.exit(2);
		}
		int ticks = 120;
		boolean disasm = false;
		boolean quiet = false;
		float dt = 1.0f / 60.0f;
		for (int i = 1; i < args.length; i++) {
			switch (args[i]) {
			case "--ticks":
				ticks = Integer.parseInt(args[++i]);
				break;
			case "--dt":
				dt = Float.parseFloat(args[++i]);
				break;
			case "--disasm":
				disasm = true;
				break;
			case "--quiet":
				quiet = true;
				break;
			default:
				System.err.println("unknown option " + args[i]);
				System.exit(2);
			}
		}
		final byte[] bytes = Files.readAllBytes(Paths.get(args[0]));
		final PkbEffect effect = PkbReader.read(bytes);
		final EffectPlan plan = EffectBinder.bind(effect);
		System.out.println(args[0] + ": v" + plan.versionMajor + "." + plan.versionMinor + ", " + plan.layers.length
				+ " layers, " + plan.routes.length + " routes");
		for (final EventRoute r : plan.routes) {
			System.out.println("  route '" + r.channel + "' slot " + r.globalEventSlotId + " -> layer " + r.targetLayer
					+ " (parent layer slot " + r.parentLayerSlot + ")");
		}
		for (final LayerProgram lp : plan.layers) {
			System.out.println("layer " + lp.id + " '" + lp.name + "' renderers=" + lp.renderers.length + " samplers="
					+ lp.samplers.length + (lp.isSpawner() ? " [spawner]" : "")
					+ (plan.isKickTarget(lp.id) ? " [kick target]" : " [root]"));
			for (final LayerRenderer r : lp.renderers) {
				System.out.println("    renderer class=" + r.rendererClass + " blend=" + r.blendMode + " bb="
						+ r.billboardingMode + " atlas=" + r.atlasSubDivX + "x" + r.atlasSubDivY + " tex="
						+ r.diffuseTexturePath + " inputs=" + r.particleInputs.length);
			}
			for (final SamplerResource s : lp.samplers) {
				System.out.println("    sampler '" + s.name + "' kind=" + s.kind
						+ (s.kind == SamplerResource.KIND_CURVE ? " keys=" + s.curve.times.length + " comps="
								+ s.curve.components : "")
						+ (s.kind == SamplerResource.KIND_SHAPE ? " shape=" + s.shape.type + " dim="
								+ s.shape.dimensionality + " r=" + s.shape.radius : ""));
			}
			final String[] scopeNames = { "init", "physics", "timeFixed", "timeVarying" };
			final ProgramDescriptor[] scopes = lp.scopePrograms();
			for (int s = 0; s < scopes.length; s++) {
				final ProgramDescriptor p = scopes[s];
				if (p.isEmpty()) {
					continue;
				}
				System.out.println("    " + scopeNames[s] + ": " + p.instructions.length + " instructions, "
						+ p.bytecode.length + " bytes, const " + p.constantsPool.length + " bytes, regs "
						+ java.util.Arrays.toString(p.registerCounts) + ", " + p.externals.length + " externals, "
						+ p.functions.length + " functions");
				if (!quiet) {
					for (final ExternalBinding b : p.externals) {
						System.out.println("        " + b);
					}
					for (final FunctionBinding f : p.functions) {
						System.out.println("        " + f);
					}
				}
				if (disasm) {
					for (final Instruction ins : p.instructions) {
						System.out.println("        " + ins);
					}
				}
			}
		}
		final EffectRuntime runtime = new EffectRuntime(plan);
		runtime.setAttribute("__a_Game.LifespanMultiplier", 1, 0, 0, 0);
		runtime.setAttribute("__a_Game.EmissionRateMultiplier", 1, 0, 0, 0);
		runtime.setAttribute("__a_Game.SpeedMultiplier", 1, 0, 0, 0);
		runtime.setAttribute("__a_Game.ColorMultiplier", 1, 1, 1, 1);
		runtime.setAttribute("__a_Game.TeamColor", 1, 0, 0, 1);
		runtime.setAttribute("__a_Game.Scale", 1, 0, 0, 0);
		final EffectRuntime.FrameInputs inputs = new EffectRuntime.FrameInputs();
		inputs.dt = dt;
		inputs.camera.position[0] = 0;
		inputs.camera.position[1] = -10;
		inputs.camera.position[2] = 5;
		inputs.camera.basis[1][0] = 0;
		inputs.camera.basis[1][1] = 1;
		inputs.camera.basis[1][2] = 0;
		inputs.camera.basis[2][0] = 0;
		inputs.camera.basis[2][1] = 0;
		inputs.camera.basis[2][2] = 1;
		final long start = System.nanoTime();
		for (int t = 0; t < ticks; t++) {
			runtime.tick(inputs);
			inputs.effectAge += dt;
			if (!quiet && ((t < 5) || ((t % 30) == 29) || (t == (ticks - 1)))) {
				final StringBuilder sb = new StringBuilder();
				sb.append(String.format(Locale.US, "t=%3d age=%.3f", t + 1, inputs.effectAge));
				for (int i = 0; i < runtime.layerCount(); i++) {
					sb.append(String.format(Locale.US, " L%d[pool=%d alive=%d spawned=%d]", i,
							runtime.pool(i).size(), runtime.aliveCount(i), runtime.spawnedTotal(i)));
				}
				System.out.println(sb);
			}
		}
		final long elapsed = System.nanoTime() - start;
		System.out.println(String.format(Locale.US, "%d ticks in %.1f ms (%.2f ms/tick)", ticks, elapsed / 1e6,
				elapsed / 1e6 / ticks));
		for (final RenderPacket packet : runtime.lastPackets()) {
			int alive = 0;
			for (final boolean a : packet.alive) {
				if (a) {
					alive++;
				}
			}
			System.out.println("packet layer " + packet.layerIndex + " renderer " + packet.rendererIndex + ": "
					+ packet.particleCount + " slots, " + alive + " alive; pos="
					+ (packet.slots[RenderPacket.SLOT_POSITION] != null) + " size="
					+ (packet.slots[RenderPacket.SLOT_SIZE] != null) + " color="
					+ (packet.slots[RenderPacket.SLOT_COLOR] != null) + " texId="
					+ (packet.slots[RenderPacket.SLOT_TEXTURE_ID] != null) + " rot="
					+ (packet.slots[RenderPacket.SLOT_ROTATION] != null) + " axis0="
					+ (packet.slots[RenderPacket.SLOT_AXIS0] != null) + " enabled="
					+ (packet.slots[RenderPacket.SLOT_ENABLED] != null));
			int shown = 0;
			for (int p = 0; (p < packet.particleCount) && (shown < 5); p++) {
				if (!packet.alive[p]) {
					continue;
				}
				shown++;
				final StringBuilder sb = new StringBuilder("    p" + p);
				final float[] pos = packet.slots[RenderPacket.SLOT_POSITION];
				if (pos != null) {
					sb.append(String.format(Locale.US, " pos=(%.3f %.3f %.3f)", pos[p * 3], pos[(p * 3) + 1],
							pos[(p * 3) + 2]));
				}
				final float[] size = packet.slots[RenderPacket.SLOT_SIZE];
				if (size != null) {
					sb.append(String.format(Locale.US, " size=%.3f", size[p]));
				}
				final float[] color = packet.slots[RenderPacket.SLOT_COLOR];
				if (color != null) {
					sb.append(String.format(Locale.US, " color=(%.2f %.2f %.2f %.2f)", color[p * 4], color[(p * 4) + 1],
							color[(p * 4) + 2], color[(p * 4) + 3]));
				}
				final float[] tex = packet.slots[RenderPacket.SLOT_TEXTURE_ID];
				if (tex != null) {
					sb.append(String.format(Locale.US, " tex=%.2f", tex[p]));
				}
				System.out.println(sb);
			}
		}
		if (!runtime.getIssues().messages().isEmpty()) {
			System.out.println("issues: " + runtime.getIssues().messages());
		}
	}
}
