/*
 * Copyright (c) 2002-2008 LWJGL Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'LWJGL' nor the names of
 *   its contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.Pbuffer;
import org.lwjgl.opengl.PixelFormat;
import org.lwjgl.opengl.SharedDrawable;

import java.awt.*;
import java.awt.event.ComponentEvent;

/**
 * <p/>
 * An AWT rendering context.
 * <p/>
 *
 * @author $Author$ $Id$
 * @version $Revision$
 */
public class SmarterAWTGLCanvas extends AWTGLCanvas {
	private static SharedDrawable sharedDrawable;
	private static Pbuffer pbuffer;
	private static PixelFormat pixelFormat = new PixelFormat();
	private static GraphicsDevice defaultScreenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	static {
		try {
			pbuffer = new Pbuffer(200, 200, pixelFormat, null);
			sharedDrawable = new SharedDrawable(pbuffer);
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}
	}

	public SmarterAWTGLCanvas() throws LWJGLException {
		super(defaultScreenDevice, pixelFormat, sharedDrawable);
	}

	@Override
	public void componentShown(final ComponentEvent e) {
		super.componentResized(e);
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
		super.componentHidden(e);
	}
}