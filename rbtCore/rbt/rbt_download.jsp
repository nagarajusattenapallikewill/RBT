<%java.io.FileInputStream fis = null;
			try{

				String filename = (String) session.getAttribute("File");
				session.removeAttribute("File");
				java.io.File file  = new java.io.File(filename);
				fis = new java.io.FileInputStream(file);
  				int iRead;
				while ((iRead = fis.read()) != -1) {
					out.write(iRead);
				}	
				out.close();
			}
			catch(Exception E){
			}
			finally{
				if(fis != null)
				{
					try
					{
						fis.close();
					}
					catch(Exception e)
					{
					}
				}
			}
%>