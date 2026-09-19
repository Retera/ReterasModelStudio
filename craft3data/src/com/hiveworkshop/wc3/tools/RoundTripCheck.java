package com.hiveworkshop.wc3.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import com.hiveworkshop.wc3.gui.datachooser.CascDataSource;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.gui.datachooser.DataSourceDescriptor;
import com.hiveworkshop.wc3.gui.datachooser.FolderDataSourceDescriptor;
import com.hiveworkshop.wc3.gui.datachooser.WarcraftInstallDetector;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

/**
 * Headless regression check for the model format layer.
 * <p>
 * For every .mdx reachable from the given sources it loads the model through
 * the editor's classes, writes it back as MDX, reloads that, writes again, and
 * also pushes it through MDL text. A model is STABLE when the second MDX save
 * equals the first (the writer is a fixed point), EXACT when the first save is
 * byte-identical to the original file, and otherwise a failure class names the
 * step that broke. Run it before and after touching wc3/mdl or wc3/mdx.
 *
 * <pre>
 * ./gradlew :matrixeater:roundTrip -PrtArgs="--install '/path/to/Warcraft III' --limit 500"
 * ./gradlew :matrixeater:roundTrip -PrtArgs="--folder /extracted/models --filter units/human"
 * java -cp matrixeater-*.jar com.hiveworkshop.wc3.tools.RoundTripCheck --install /path/to/War3_1.22
 * </pre>
 */
public final class RoundTripCheck {
	enum Outcome {
		EXACT, STABLE, UNSTABLE, LOAD_FAIL, SAVE_FAIL, RELOAD_FAIL, MDL_FAIL, DIALOG
	}

	private RoundTripCheck() {
	}

	public static void main(final String[] args) throws Exception {
		System.setProperty("java.awt.headless", "true");
		final List<String> installs = new ArrayList<>();
		final List<String> folders = new ArrayList<>();
		final List<String> files = new ArrayList<>();
		String filter = null;
		String locale = null;
		int limit = Integer.MAX_VALUE;
		boolean verbose = false;
		CascDataSource.Product product = CascDataSource.Product.WARCRAFT_III;
		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
			case "--install":
				installs.add(args[++i]);
				break;
			case "--folder":
				folders.add(args[++i]);
				break;
			case "--file":
				files.add(args[++i]);
				break;
			case "--filter":
				filter = args[++i].toLowerCase(Locale.US).replace('/', '\\');
				break;
			case "--locale":
				locale = args[++i];
				break;
			case "--limit":
				limit = Integer.parseInt(args[++i]);
				break;
			case "--ptr":
				product = CascDataSource.Product.WARCRAFT_III_PUBLIC_TEST;
				break;
			case "--verbose":
				verbose = true;
				break;
			default:
				System.err.println("Unknown argument: " + args[i]);
				usage();
				return;
			}
		}
		if (installs.isEmpty() && folders.isEmpty() && files.isEmpty()) {
			usage();
			return;
		}
		final PrintStream out = System.out;
		final Map<Outcome, Integer> totals = new EnumMap<>(Outcome.class);
		for (final String file : files) {
			final byte[] bytes = Files.readAllBytes(Paths.get(file));
			record(totals, out, file, check(bytes), verbose);
		}
		for (final String folder : folders) {
			final List<DataSourceDescriptor> descriptors = new ArrayList<>();
			descriptors.add(new FolderDataSourceDescriptor(folder));
			runSources(out, "folder " + folder, descriptors, filter, limit, verbose, totals);
		}
		for (final String install : installs) {
			final Path path = Paths.get(install);
			final List<DataSourceDescriptor> descriptors = WarcraftInstallDetector.detect(path, product, locale);
			if (descriptors.isEmpty()) {
				out.println("No data sources found in " + install);
				continue;
			}
			runSources(out, "install " + install, descriptors, filter, limit, verbose, totals);
		}
		out.println();
		out.println("TOTAL");
		printTotals(out, totals);
		final int failures = totals.getOrDefault(Outcome.LOAD_FAIL, 0) + totals.getOrDefault(Outcome.SAVE_FAIL, 0)
				+ totals.getOrDefault(Outcome.RELOAD_FAIL, 0) + totals.getOrDefault(Outcome.MDL_FAIL, 0)
				+ totals.getOrDefault(Outcome.DIALOG, 0) + totals.getOrDefault(Outcome.UNSTABLE, 0);
		System.exit(failures == 0 ? 0 : 1);
	}

	private static void usage() {
		System.err.println("RoundTripCheck [--install <war3 dir>]... [--folder <dir>]... [--file <mdx>]...");
		System.err.println("               [--filter <path substring>] [--limit <n>] [--locale enUS] [--ptr] [--verbose]");
	}

	private static void runSources(final PrintStream out, final String label,
			final List<DataSourceDescriptor> descriptors, final String filter, final int limit,
			final boolean verbose, final Map<Outcome, Integer> grandTotals) throws IOException {
		out.println("== " + label);
		for (final DataSourceDescriptor descriptor : descriptors) {
			out.println("   source: " + descriptor.getDisplayName());
		}
		final Map<Outcome, Integer> totals = new EnumMap<>(Outcome.class);
		final long start = System.currentTimeMillis();
		int seen = 0;
		for (final DataSourceDescriptor descriptor : descriptors) {
			final DataSource source;
			try {
				source = descriptor.createDataSource();
			} catch (final Throwable t) {
				out.println("   cannot open " + descriptor.getDisplayName() + ": " + t);
				continue;
			}
			try {
				final java.util.Collection<String> listfile = source.getListfile();
				if (listfile == null) {
					out.println("   (no listfile in " + descriptor.getDisplayName() + ", skipped)");
					continue;
				}
				final List<String> models = listfile.stream()
						.filter(p -> p.toLowerCase(Locale.US).endsWith(".mdx")).sorted()
						.collect(Collectors.toList());
				for (final String model : models) {
					final String key = model.toLowerCase(Locale.US).replace('/', '\\');
					if ((filter != null) && !key.contains(filter)) {
						continue;
					}
					if (seen >= limit) {
						break;
					}
					seen++;
					byte[] bytes;
					try (InputStream stream = source.getResourceAsStream(model)) {
						if (stream == null) {
							record(totals, out, model, new Result(Outcome.LOAD_FAIL, "not readable from source"),
									verbose);
							continue;
						}
						bytes = stream.readAllBytes();
					} catch (final Exception e) {
						record(totals, out, model, new Result(Outcome.LOAD_FAIL, "read: " + e), verbose);
						continue;
					}
					record(totals, out, model, check(bytes), verbose);
				}
			} finally {
				source.close();
			}
		}
		out.println("   " + seen + " models in " + ((System.currentTimeMillis() - start) / 1000) + "s");
		printTotals(out, totals);
		for (final Map.Entry<Outcome, Integer> entry : totals.entrySet()) {
			grandTotals.merge(entry.getKey(), entry.getValue(), Integer::sum);
		}
	}

	private static void printTotals(final PrintStream out, final Map<Outcome, Integer> totals) {
		final StringBuilder line = new StringBuilder("   ");
		for (final Outcome outcome : Outcome.values()) {
			line.append(outcome).append('=').append(totals.getOrDefault(outcome, 0)).append("  ");
		}
		out.println(line);
	}

	private static void record(final Map<Outcome, Integer> totals, final PrintStream out, final String model,
			final Result result, final boolean verbose) {
		totals.merge(result.outcome, 1, Integer::sum);
		final boolean bad = (result.outcome != Outcome.EXACT) && (result.outcome != Outcome.STABLE);
		if (bad || verbose) {
			out.println("   " + result.outcome + "  " + model + (result.detail == null ? "" : "  : " + result.detail));
		}
	}

	static final class Result {
		final Outcome outcome;
		final String detail;

		Result(final Outcome outcome, final String detail) {
			this.outcome = outcome;
			this.detail = detail;
		}
	}

	/** The core check on one model's bytes. */
	public static Result check(final byte[] original) {
		final EditableModel model;
		try {
			model = load(original);
		} catch (final Throwable t) {
			return failure(Outcome.LOAD_FAIL, t);
		}
		final byte[] saved;
		try {
			saved = saveMdx(model);
		} catch (final Throwable t) {
			return failure(Outcome.SAVE_FAIL, t);
		}
		final EditableModel reloaded;
		final byte[] savedAgain;
		try {
			reloaded = load(saved);
			savedAgain = saveMdx(reloaded);
		} catch (final Throwable t) {
			return failure(Outcome.RELOAD_FAIL, t);
		}
		try {
			final ByteArrayOutputStream text = new ByteArrayOutputStream();
			load(saved).printTo(text, false);
			final EditableModel fromText = EditableModel.read(new ByteArrayInputStream(text.toByteArray()));
			if (fromText == null) {
				return new Result(Outcome.MDL_FAIL, "MDL text did not parse");
			}
			saveMdx(fromText);
		} catch (final Throwable t) {
			return failure(Outcome.MDL_FAIL, t);
		}
		if (!Arrays.equals(saved, savedAgain)) {
			return new Result(Outcome.UNSTABLE, "second save differs at byte " + firstDifference(saved, savedAgain)
					+ " (" + saved.length + " vs " + savedAgain.length + " bytes)");
		}
		if (Arrays.equals(original, saved)) {
			return new Result(Outcome.EXACT, null);
		}
		return new Result(Outcome.STABLE, "differs from original at byte " + firstDifference(original, saved) + " ("
				+ original.length + " vs " + saved.length + " bytes)");
	}

	private static Result failure(final Outcome outcome, final Throwable t) {
		if (t instanceof java.awt.HeadlessException) {
			String where = "";
			for (final StackTraceElement frame : t.getStackTrace()) {
				if (frame.getClassName().startsWith("com.hiveworkshop.wc3.md")) {
					where = " from " + frame;
					break;
				}
			}
			return new Result(Outcome.DIALOG, "the format layer tried to open a dialog" + where);
		}
		final StackTraceElement[] trace = t.getStackTrace();
		final String where = trace.length > 0 ? " at " + trace[0] : "";
		return new Result(outcome, t.getClass().getSimpleName() + ": " + t.getMessage() + where);
	}

	private static int firstDifference(final byte[] a, final byte[] b) {
		final int n = Math.min(a.length, b.length);
		for (int i = 0; i < n; i++) {
			if (a[i] != b[i]) {
				return i;
			}
		}
		return n;
	}

	static EditableModel load(final byte[] bytes) throws IOException {
		try (BlizzardDataInputStream in = new BlizzardDataInputStream(new ByteArrayInputStream(bytes))) {
			final MdxModel mdx = MdxUtils.loadModel(in);
			return new EditableModel(mdx);
		}
	}

	static byte[] saveMdx(final EditableModel model) throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		try (BlizzardDataOutputStream out = new BlizzardDataOutputStream(bytes)) {
			new MdxModel(model, false).save(out);
		}
		return bytes.toByteArray();
	}

	/** Convenience for other tools: check one file on disk. */
	public static Result checkFile(final File file) throws IOException {
		return check(Files.readAllBytes(file.toPath()));
	}

	static List<String> sorted(final List<String> list) {
		final List<String> copy = new ArrayList<>(list);
		Collections.sort(copy);
		return copy;
	}
}
