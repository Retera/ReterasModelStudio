/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

/* Included in MatrixEater by Retera for deployability */

package com.badlogic.gdx.utils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Provides utility methods to copy streams.
 */
public final class StreamUtils {

	/**
	 * Close and ignore all errors.
	 */
	public static void closeQuietly(final Closeable c) {
		if (c != null) {
			try {
				c.close();
			} catch (final Exception ignored) {
			}
		}
	}

	/**
	 * Returns true if the parent directories of the file can be created and the
	 * file can be written.
	 */
	public static boolean canWrite(final File file) {
		final File parent = file.getParentFile();
		final File testFile;
		if (file.exists()) {
			if (!file.canWrite() || !canExecute(file)) {
				return false;
			}
			// Don't overwrite existing file just to check if we can write to directory.
			testFile = new File(parent, UUID.randomUUID().toString());
		} else {
			parent.mkdirs();
			if (!parent.isDirectory()) {
				return false;
			}
			testFile = file;
		}
		try {
			new FileOutputStream(testFile).close();
			return canExecute(testFile);
		} catch (final Throwable ex) {
			return false;
		} finally {
			testFile.delete();
		}
	}

	public static boolean canExecute(final File file) {
		try {
			final Method canExecute = File.class.getMethod("canExecute");
			if ((Boolean) canExecute.invoke(file)) {
				return true;
			}

			final Method setExecutable = File.class.getMethod("setExecutable", boolean.class, boolean.class);
			setExecutable.invoke(file, true, false);

			return (Boolean) canExecute.invoke(file);
		} catch (final Exception ignored) {
		}
		return false;
	}
}
