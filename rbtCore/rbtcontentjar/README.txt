Image Resize
------------
To resize the images, follow the below steps.

1.Modify the parameters in rbtcontentjar.properties

	a. Set image_resize_required to true in rbtcontentjar.properties
	b. Set image_base_directory_path to the parent directory of images folder.
	   For ex: If the images are like D:\rbtcontent\images\1.png. Configure 
	   D:\rbtcontent\images as image_base_directory_path
	c. Change image_height and image_width as required in rbtcontentjar.properties.
	d. Run resizeImages.bat or resizeImages.sh.

2. Execute the script, 
    
    C:\
    + ImageResizeScript
	    + lib/
	         - rbtcontentjar.jar
	         - log4j-1.2.12.jar 
	    - rbtcontentjar.properties
	    - resizeImages.sh
	    - resizeImages.bat
     