package com.hiveworkshop.wc3.pkb.sim;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.pkb.bind.LayerProgram;
import com.hiveworkshop.wc3.pkb.vm.ExecContext;
import com.hiveworkshop.wc3.pkb.vm.Interpreter;

/** All particles of one layer plus their shared external storage. */
public final class ParticlePool {
	private final List<LayerTickHarness> particles = new ArrayList<>();
	private final ExternalStore externals = new ExternalStore();
	private int[] layout;
	private int layoutExternalCount;

	public int size() {
		return particles.size();
	}

	public LayerTickHarness particle(final int i) {
		return particles.get(i);
	}

	public ExternalStore externals() {
		return externals;
	}

	private void rebindExternals() {
		for (int i = 0; i < particles.size(); i++) {
			particles.get(i).bindExternals(externals, i, externals.slotCount());
		}
	}

	public void resize(final int count) {
		while (particles.size() > count) {
			particles.remove(particles.size() - 1);
		}
		while (particles.size() < count) {
			particles.add(new LayerTickHarness(layout));
		}
		externals.resize(Math.max(externals.slotCount(), layoutExternalCount), particles.size());
		rebindExternals();
	}

	public void resizeForLayer(final LayerProgram layer) {
		layoutExternalCount = LayerTickHarness.externalStorageSizeFor(layer);
		layout = LayerTickHarness.registersPerBankFor(layer);
		externals.resize(layoutExternalCount, Math.max(1, particles.size()));
		rebindExternals();
		for (final LayerTickHarness p : particles) {
			p.resizeForLayer(layer);
		}
	}

	public boolean initRange(final LayerProgram layer, final int baseSeed, final int startIdx, final int count,
			final Interpreter vm, final ExecContext ctx, final SimIssues issues) {
		if ((startIdx >= particles.size()) || (count == 0)) {
			return true;
		}
		final int end = Math.min(startIdx + count, particles.size());
		for (int i = startIdx; i < end; i++) {
			particles.get(i).setRngSeed(baseSeed + i);
			if (!particles.get(i).initParticle(layer, vm, ctx, issues)) {
				return false;
			}
		}
		return true;
	}

	public boolean tickBatch(final LayerProgram layer, final Interpreter vm, final ExecContext ctx,
			final SimIssues issues) {
		for (final LayerTickHarness p : particles) {
			if (p.wasDeadAtFrameStart()) {
				continue;
			}
			if (!p.tick(layer, vm, ctx, issues)) {
				return false;
			}
		}
		return true;
	}

	public int aliveCount() {
		int n = 0;
		for (final LayerTickHarness p : particles) {
			if (!p.isDead()) {
				n++;
			}
		}
		return n;
	}
}
