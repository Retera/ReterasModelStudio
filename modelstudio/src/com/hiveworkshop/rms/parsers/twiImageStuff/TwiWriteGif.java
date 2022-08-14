package com.hiveworkshop.rms.parsers.twiImageStuff;

import com.hiveworkshop.rms.ui.application.viewer.ViewportRenderExporter;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.GifExportSettings;
import org.w3c.dom.Node;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class TwiWriteGif {

	public static byte[] getAsBytes(ByteBuffer[] byteBuffers, int[] delays, GifExportSettings settings){
		System.out.println("generating!");
		BufferedImage[] images = ViewportRenderExporter.getBufferedImagesForGif(byteBuffers, settings.getExpH(), settings.getExpW(), settings.getAlphaCutoff());

		return getAsBytes(images, delays);
	}

	public static byte[] getAsBytes(BufferedImage[] frames, int[] delayTimes) {
		Iterator<ImageWriter> gifWriters = ImageIO.getImageWritersByFormatName("gif");
		if(gifWriters.hasNext()){
			ImageWriter iw = gifWriters.next();

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)){
				iw.setOutput(ios);
				iw.prepareWriteSequence(null);

				for (int i = 0; i < frames.length; i++) {
					BufferedImage frame = frames[i];
					ImageWriteParam iwp = iw.getDefaultWriteParam();
					IIOMetadata metadata = iw.getDefaultImageMetadata(new ImageTypeSpecifier(frame), iwp);
					configure(metadata, "" + delayTimes[i], i);
					IIOImage ii = new IIOImage(frame, null, metadata);
					iw.writeToSequence(ii, null);
				}
				iw.endWriteSequence();

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			byte[] bytes = outputStream.toByteArray();
			try {
				outputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return bytes;
		}
		return null;
	}

	public static void configure(IIOMetadata meta, String delayTime, int imageIndex) {
		String metaFormat = meta.getNativeMetadataFormatName();
		Node root = meta.getAsTree(metaFormat);

		// find the GraphicControlExtension node
		IIOMetadataNode gce = getLastValidChildNode(root);
		gce.setAttribute("userDelay", "FALSE");
		gce.setAttribute("delayTime", delayTime);
		gce.setAttribute("disposalMethod", "restoreToBackgroundColor");

		// only the first node needs the ApplicationExtensions node
		if (imageIndex == 0) {
			root.appendChild(getApplicationExtNode());
		}

		try {
			meta.setFromTree(metaFormat, root);
		} catch (IIOInvalidTreeException e) {
			//shouldn't happen
			throw new Error(e);
		}
	}

	private static IIOMetadataNode getApplicationExtNode() {
		IIOMetadataNode aes = new IIOMetadataNode("ApplicationExtensions");
		IIOMetadataNode ae = new IIOMetadataNode("ApplicationExtension");
		ae.setAttribute("applicationID", "NETSCAPE");
		ae.setAttribute("authenticationCode", "2.0");
		// last two bytes is an unsigned short (little endian)
		// that indicates the number of times to loop.
		// 0 means loop forever.
		byte[] uo = new byte[]{0x1, 0x0, 0x0};
		ae.setUserObject(uo);
		aes.appendChild(ae);
		return aes;
	}

	private static IIOMetadataNode getLastValidChildNode(Node root) {
		Node child = root.getFirstChild();
		while (child != null && !"GraphicControlExtension".equals(child.getNodeName())) {
			child = child.getNextSibling();
		}

		return (IIOMetadataNode) child;
	}
}
