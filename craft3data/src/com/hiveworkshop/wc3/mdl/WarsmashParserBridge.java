package com.hiveworkshop.wc3.mdl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import com.hiveworkshop.rms.parsers.mdlx.MdlLoadSave;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxParseLog;
import com.hiveworkshop.rms.parsers.mdlx.MdxLoadSave;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.wc3data.stream.BlizzardDataInputStream;

/**
 * Loads models through the Warsmash-derived parser ({@code com.hiveworkshop.rms.parsers.mdlx}, the mdx-m3-viewer
 * port from tw1lac's fork). The parser produces a plain {@link MdlxModel}; rather than a second object-model
 * converter, the bridge serialises that to binary MDX in memory and hands the bytes to this program's own MDX
 * chunk reader, so the resulting {@link EditableModel} is exactly what opening the equivalent .mdx would give.
 * Parser warnings are printed to stderr; nothing here opens a dialog.
 */
public final class WarsmashParserBridge {
	private WarsmashParserBridge() {
	}

	public static EditableModel read(final File file) {
		final byte[] bytes;
		try {
			bytes = Files.readAllBytes(file.toPath());
		}
		catch (final IOException e) {
			throw new RuntimeException("Unable to read " + file + ": " + e.getMessage(), e);
		}
		final EditableModel model = read(bytes, isBinaryMdx(bytes));
		model.setFileRef(file);
		return model;
	}

	/**
	 * @param bytes  the whole file
	 * @param binary true for MDX, false for MDL text
	 */
	public static EditableModel read(final byte[] bytes, final boolean binary) {
		MdlxParseLog.clear();
		final MdlxModel mdlx = new MdlxModel();
		if (binary) {
			MdxLoadSave.loadMdx(mdlx, ByteBuffer.wrap(bytes));
		}
		else {
			MdlLoadSave.loadMdl(mdlx, ByteBuffer.wrap(bytes));
		}
		final ByteBuffer mdxBuffer = MdxLoadSave.saveMdx(mdlx);
		final byte[] mdxBytes = new byte[mdxBuffer.limit()];
		mdxBuffer.rewind();
		mdxBuffer.get(mdxBytes);

		final List<String> warnings = MdlxParseLog.drain();
		final Throwable failure = MdlxParseLog.getFirstException();
		MdlxParseLog.clear();
		for (final String warning : warnings) {
			System.err.println("[warsmash parser] " + warning);
		}
		final boolean empty = mdlx.geosets.isEmpty() && mdlx.bones.isEmpty() && mdlx.helpers.isEmpty()
				&& mdlx.sequences.isEmpty();
		if ((failure != null) && empty) {
			throw new RuntimeException("The Warsmash-derived parser could not read the model: " + failure, failure);
		}

		final EditableModel model;
		try (BlizzardDataInputStream in = new BlizzardDataInputStream(new ByteArrayInputStream(mdxBytes))) {
			final MdxModel mdx = MdxUtils.loadModel(in);
			model = new EditableModel(mdx);
		}
		catch (final IOException e) {
			throw new RuntimeException("The Warsmash-derived parser produced MDX this program could not read: " + e, e);
		}
		for (final String comment : mdlx.comments) {
			model.addToHeader("// " + comment);
		}
		if (!warnings.isEmpty()) {
			model.addToHeader("// Warsmash-derived parser reported " + warnings.size() + " message(s); see the console");
		}
		return model;
	}

	/** MDX files start with the "MDLX" magic; anything else is treated as MDL text. */
	public static boolean isBinaryMdx(final byte[] bytes) {
		return (bytes.length >= 4) && "MDLX".equals(new String(bytes, 0, 4, StandardCharsets.US_ASCII));
	}
}
