/**
 * 
 */
package com.onmobile.apps.ringbacktones.common;

import java.awt.image.BufferedImage;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.ByteMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * This class provides the APIs to generate the QRCode by using Google's ZXing
 * APIs.
 * 
 * @author vinayasimha.patil
 */
public class QRCodeGenerator
{
	/**
	 * Default QRCode width.
	 */
	private static final int DEFAULT_WIDTH = 350;

	/**
	 * Default QRCode height.
	 */
	private static final int DEFAULT_HEIGHT = 350;

	private static final int BLACK = 0xFF000000;
	private static final int WHITE = 0xFFFFFFFF;

	/**
	 * Creates the BufferedImage of QRCode representing the <tt>sms</tt> with
	 * default size ({@link #DEFAULT_WIDTH} X {@link #DEFAULT_HEIGHT}).
	 * 
	 * @param sendTo
	 *            the number to which sms has to be sent.
	 * @param smsText
	 *            the sms text.
	 * @return the BufferedImage representing the sms in QRCode.
	 * @throws RBTException
	 *             if not able to encode the contents.
	 */
	public static BufferedImage generateQRCodeImageForSMS(String sendTo,
			String smsText) throws RBTException
	{
		return generateQRCodeImageForSMS(sendTo, smsText, DEFAULT_WIDTH,
				DEFAULT_HEIGHT);
	}

	/**
	 * Creates the BufferedImage of QRCode representing the <tt>sms</tt>.
	 * 
	 * @param sendTo
	 *            the number to which sms has to be sent.
	 * @param smsText
	 *            the sms text.
	 * @param width
	 *            required width of the QRCode image.
	 * @param height
	 *            required height of the QRCode image.
	 * @return the BufferedImage representing the sms in QRCode.
	 * @throws RBTException
	 *             if not able to encode the contents.
	 */
	public static BufferedImage generateQRCodeImageForSMS(String sendTo,
			String smsText, int width, int height) throws RBTException
	{
		String contents = "smsto:" + sendTo + ":" + smsText;
		return generateQRCodeImage(contents, width, height);
	}

	/**
	 * Creates the BufferedImage of QRCode representing the <tt>phoneNumber</tt>
	 * with default size ({@link #DEFAULT_WIDTH} X {@link #DEFAULT_HEIGHT}).
	 * 
	 * @param phoneNumber
	 *            the number for which QRCode has to be generated.
	 * @return the BufferedImage representing the phoneNumber in QRCode.
	 * @throws RBTException
	 *             if not able to encode the contents.
	 */
	public static BufferedImage generateQRCodeImageForPhoneNumber(
			String phoneNumber) throws RBTException
	{
		return generateQRCodeImageForPhoneNumber(phoneNumber, DEFAULT_WIDTH,
				DEFAULT_HEIGHT);
	}

	/**
	 * Creates the BufferedImage of QRCode representing the <tt>phoneNumber</tt>
	 * .
	 * 
	 * @param phoneNumber
	 *            the number for which QRCode has to be generated.
	 * @param width
	 *            required width of the QRCode image.
	 * @param height
	 *            required height of the QRCode image.
	 * @return the BufferedImage representing the phoneNumber in QRCode.
	 * @throws RBTException
	 *             if not able to encode the contents.
	 */
	public static BufferedImage generateQRCodeImageForPhoneNumber(
			String phoneNumber, int width, int height) throws RBTException
	{
		String contents = "tel:" + phoneNumber;
		return generateQRCodeImage(contents, width, height);
	}

	/**
	 * Creates the BufferedImage of QRCode representing the <tt>url</tt> with
	 * default size ({@link #DEFAULT_WIDTH} X {@link #DEFAULT_HEIGHT}).
	 * 
	 * @param url
	 *            the url for which QRCode has to be generated.
	 * @return the BufferedImage representing the url in QRCode.
	 * @throws RBTException
	 *             if not able to encode the contents.
	 */
	public static BufferedImage generateQRCodeImageForURL(String url)
			throws RBTException
	{
		return generateQRCodeImageForURL(url, DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * Creates the BufferedImage of QRCode representing the <tt>url</tt>.
	 * 
	 * @param url
	 *            the url for which QRCode has to be generated.
	 * @param width
	 *            required width of the QRCode image.
	 * @param height
	 *            required height of the QRCode image.
	 * @return the BufferedImage representing the url in QRCode.
	 * @throws RBTException
	 *             if not able to encode the contents.
	 */
	public static BufferedImage generateQRCodeImageForURL(String url,
			int width, int height) throws RBTException
	{
		String contents = url;
		if (!url.toLowerCase().startsWith("http://"))
			contents = "http://" + url;

		return generateQRCodeImage(contents, width, height);
	}

	/**
	 * Creates the BufferedImage of QRCode representing the <tt>contents</tt>.
	 * 
	 * @param contents
	 *            for which QRCode has to be generated.
	 * @param width
	 *            required width of the QRCode image.
	 * @param height
	 *            required height of the QRCode.
	 * @return the QRCode in BufferedImage.
	 * @throws RBTException
	 *             if not able to encode the contents.
	 */
	private static BufferedImage generateQRCodeImage(String contents,
			int width, int height) throws RBTException
	{
		try
		{
			QRCodeWriter qrCodeWriter = new QRCodeWriter();
			ByteMatrix byteMatrix = qrCodeWriter.encode(contents,
					BarcodeFormat.QR_CODE, width, height);

			BufferedImage bufferedImage = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					bufferedImage.setRGB(x, y,
							byteMatrix.get(x, y) == 0 ? BLACK : WHITE);
				}
			}

			return bufferedImage;
		}
		catch (WriterException e)
		{
			throw new RBTException("Unable to create QRCode");
		}
	}
}
