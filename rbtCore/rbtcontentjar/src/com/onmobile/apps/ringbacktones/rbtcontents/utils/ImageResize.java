/**
 * Ring Back Tone 
 * Copyright OnMobile 2011
 *
 * Author: rajesh.karavadi 
 * Id: ImageResize.java 
 * Created on: 11-Oct-2011 3:12:01 PM
 */
package com.onmobile.apps.ringbacktones.rbtcontents.utils;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;

/**
 * Resize the images from specified folder.
 * 
 * @Since 11-Oct-2011
 */
public class ImageResize {
	private static Logger LOG = Logger.getLogger(ImageResize.class);
	private static Logger FAIL_LOG = Logger.getLogger("F");

	private static final RBTContentJarParameters RBT_CONTENT_JAR_PARAMETERS = RBTContentJarParameters
			.getInstance();
	private static final String IMAGE_RESIZE_REQUIRED_STR = RBT_CONTENT_JAR_PARAMETERS
			.getParameter("image_resize_required");
	private static final String IMAGE_BASE_DIRECTORY_PATH = RBT_CONTENT_JAR_PARAMETERS
			.getParameter("image_base_directory_path");
	private static final String IMAGE_WIDTH = RBT_CONTENT_JAR_PARAMETERS
			.getParameter("image_width");
	private static final String IMAGE_HEIGHT = RBT_CONTENT_JAR_PARAMETERS
			.getParameter("image_height");

	/**
	 * Resizes the given image.
	 * 
	 * @param paramFile
	 *            - name of the file
	 * @param height
	 *            - height of the image
	 * @param width
	 *            - width of the image
	 * @param imageFormat
	 *            - format type(PNG/JPEG)
	 * 
	 * @throws IOException
	 */
	public static void resizeImage(File paramFile, int height, int width) {
		BufferedImage readBufferedImage = null;
		try {
			readBufferedImage = ImageIO.read(paramFile);
			// No need to resize for the images which are already had the same
			// size.
			if (readBufferedImage.getWidth() != width
					&& readBufferedImage.getHeight() != height) {
				BufferedImage writerBufferedImage = new BufferedImage(width,
						height, BufferedImage.TYPE_INT_RGB);
				Graphics2D localGraphics2D = writerBufferedImage
						.createGraphics();
				localGraphics2D.setComposite(AlphaComposite.Src);
				localGraphics2D.setRenderingHint(
						RenderingHints.KEY_INTERPOLATION,
						RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				localGraphics2D.setRenderingHint(RenderingHints.KEY_RENDERING,
						RenderingHints.VALUE_RENDER_QUALITY);
				localGraphics2D.setRenderingHint(
						RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);
				localGraphics2D.drawImage(readBufferedImage, 0, 0, width,
						height, null);
				localGraphics2D.dispose();
				File newFile = new File(paramFile.getParent(),
						paramFile.getName());
				String imageFormat = getImageFormat(paramFile.getName());
				try {
					ImageIO.write(writerBufferedImage, imageFormat, newFile);
					if (!paramFile.getName().equals(newFile.getName())) {
						paramFile.delete();
					}
					LOG.debug("Successfully resized image: " + paramFile);
				} catch (IOException ioe) {
					LOG.error("Failed to write image: " + paramFile);
				}
			} else {
				LOG.debug("Image is already in the required size. Image name:"
						+ paramFile);
			}
		} catch (IOException ioe) {
			LOG.error("Failed to read image: " + paramFile);
		}
	}

	private static String getImageFormat(String file) {
		int index = file.lastIndexOf(".");
		return file.substring(index + 1, file.length());
	}

	private static void iterateFilesAndResize(String basePath, int height,
			int width) throws IOException {
		int i = 0;
		File localFile = new File(basePath);
		if (localFile.isDirectory()) {
			String[] arrayOfString = localFile.list();
			LOG.debug("Started Resizing......");
			for (i = 0; i < arrayOfString.length; i++) {
				File paramFile = new File(basePath + File.separator
						+ arrayOfString[i]);
				if (paramFile.isFile()) {
					try {
						resizeImage(paramFile, height, width);
					} catch (Exception e) {
						FAIL_LOG.error("Failed to resize: " + paramFile);
					}
				} else {
					String str = new String();
					str = str.concat(basePath).concat(File.separator)
							.concat(paramFile.getName());
					iterateFilesAndResize(str, height, width);
				}

			}
			LOG.debug("Number of files tried to resize are: " + i + " to "
					+ width + "x" + height);
		} else {
			LOG.debug("PATH cannot be a file name");
		}
	}

	public static void main(String[] paramArrayOfString) {
		boolean imageResizeRequired = Boolean
				.valueOf(IMAGE_RESIZE_REQUIRED_STR);
		if (imageResizeRequired) {
			try {
				int height = 0;
				int width = 0;
				try {
					width = Integer.parseInt(IMAGE_WIDTH);
					height = Integer.parseInt(IMAGE_HEIGHT);
				} catch (NumberFormatException localNumberFormatException) {
					LOG.warn("Please provide valid height and width");
					System.exit(1);
				}
				iterateFilesAndResize(IMAGE_BASE_DIRECTORY_PATH, height, width);
				LOG.debug("Finished.....");
			} catch (IOException ioe) {
				LOG.error(" IOException " + ioe);
			} catch (Exception e) {
				LOG.error(" Exception " + e);
			}
		} else {
			LOG.warn("property image_resize_required is set to false");
		}

	}
}
