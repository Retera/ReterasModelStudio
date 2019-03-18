package com.hiveworkshop.wc3.jworldedit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;

import mpq.ArchivedFile;
import mpq.ArchivedFileExtractor;
import mpq.ArchivedFileStream;
import mpq.HashLookup;
import mpq.MPQArchive;
import mpq.MPQException;

public class TestMain3 {

	public static void main(final String[] args) {
		final ArchivedFileExtractor extractor = new ArchivedFileExtractor();

		try {
			final Path path = Paths.get("C:\\Program Files (x86)\\HeavensFall", "War3Patch.mpq");
			if (Files.exists(path)) {
				final SeekableByteChannel sbc = Files.newByteChannel(path, EnumSet.of(StandardOpenOption.READ));
				final MpqGuy temp = new MpqGuy(new MPQArchive(sbc), sbc);

				final MPQArchive mpq = temp.getArchive();
				// System.out.println("getting it from the outside: " +
				// filepath);
				ArchivedFile file = null;
				try {
					file = mpq.lookupHash2(new HashLookup("UI\\war3skins.txt"));
				} catch (final MPQException exc) {
					if (exc.getMessage().equals("lookup not found")) {
						return;
					} else {
						throw new IOException(exc);
					}
				}
				final ArchivedFileStream stream = new ArchivedFileStream(temp.getInputChannel(), extractor, file);
				final InputStream newInputStream = Channels.newInputStream(stream);
				final File tempProduct = new File("C:\\Program Files (x86)\\HeavensFall\\UI\\FrameDef\\war3skins.txt");
				tempProduct.delete();
				tempProduct.getParentFile().mkdirs();
				Files.copy(newInputStream, tempProduct.toPath());
			}
		} catch (final MPQException e) {
//			ExceptionPopup.display("Warcraft installation archive reading error occurred. Check your MPQs.\n" + mpq, e);
			e.printStackTrace();
		} catch (final IOException e) {
//			ExceptionPopup.display("Warcraft installation archive reading error occurred. Check your MPQs.\n" + mpq, e);
			e.printStackTrace();
		}
	}

	private static final class MpqGuy {
		private final MPQArchive archive;
		private final SeekableByteChannel inputChannel;

		public MpqGuy(final MPQArchive archive, final SeekableByteChannel inputChannel) {
			this.archive = archive;
			this.inputChannel = inputChannel;
		}

		public MPQArchive getArchive() {
			return archive;
		}

		public SeekableByteChannel getInputChannel() {
			return inputChannel;
		}

		public boolean has(final String file) {
			try {
				archive.lookupPath(file);
				return true;
			} catch (final MPQException exc) {
				if (exc.getMessage().equals("lookup not found")) {
					return false;
				} else {
					throw new RuntimeException(exc);
				}
			}
		}
	}
}
