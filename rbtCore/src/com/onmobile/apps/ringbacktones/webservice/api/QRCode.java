package com.onmobile.apps.ringbacktones.webservice.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author sridhar.sindiri
 *
 */
public class QRCode extends HttpServlet implements WebServiceConstants
{
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(QRCode.class);

	public QRCode()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("image/png");
		try
		{
			HashMap<String, String> requestParams = Utility.getRequestParamsMap(getServletConfig(), request, response, api_Subscription);
			WebServiceContext task = Utility.getTask(requestParams);
			logger.info("RBT:: task: " + task);

			BufferedImage bufferedImage = RBTAdminFacade.generateQRCode(task);

			if (bufferedImage != null)
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, "png", baos);
				byte[] imageInBytes = baos.toByteArray();
				response.getOutputStream().write(imageInBytes);

				baos.close();
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
