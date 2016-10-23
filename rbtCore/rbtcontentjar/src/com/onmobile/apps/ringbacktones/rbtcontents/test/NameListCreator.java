package com.onmobile.apps.ringbacktones.rbtcontents.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipBoundary;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;

public class NameListCreator {
	
	private static Logger logger = Logger.getLogger("NameListCreator");
	private static int noOfClipsPerIteration = 100000;	
	
	 public static void main(String args[]) {
		 PrintWriter printWriter = null;
		 try {
			 noOfClipsPerIteration = Integer.parseInt(RBTContentJarParameters.getInstance()
					.getParameter("no_of_clips_per_iteration"));
		 } catch (NumberFormatException nfe) {
			 logger.error("Invalid value entered for parameter \'no_of_clips_per_iteration\'");
		 }
		 try{
			 String filePath = RBTContentJarParameters.getInstance().getParameter("ClipNameListFilePath");
			 printWriter = new PrintWriter(new FileWriter(filePath));
			 printWriter.print("[");
			 	 
			 TreeSet<ClipBoundary> clipBoundaries = ClipsDAO.getClipBoundariesUsingBinaryAlg(noOfClipsPerIteration);
			 clipBoundaries = mergeClipBoundaries(clipBoundaries, noOfClipsPerIteration);
			 for(ClipBoundary clipBoundary : clipBoundaries){
				 logger.info("ClipBoundary: " + clipBoundary);
			     List<Clip> clips = ClipsDAO.getClipsInBetween(clipBoundary.getStartIndex(), clipBoundary.getEndIndex());
			     for(Clip clip : clips) {
			    	 printWriter.print("\"" + clip.getClipName().replace("\"", "\\\"") + "\",");
			     }
			 }
			 printWriter.print("\"\"]");
			 printWriter.flush();
		 }
		 catch(IOException e) {
			 logger.error("Error",e);
		 }
		 catch(DataAccessException e) {
			 logger.error("Error", e);
		 }
		 finally{
			 if(printWriter != null) {
				 printWriter.close();
			 }
		 }		
	}

	private static TreeSet<ClipBoundary> mergeClipBoundaries(TreeSet<ClipBoundary> clipBoundaries, int noOfClipsPerIteration) {
		TreeSet<ClipBoundary> result = new TreeSet<ClipBoundary>(ClipBoundary.getClipBoundaryComparator());
		ClipBoundary cbPrev = null;
		for (ClipBoundary cb: clipBoundaries) {
			if(cbPrev != null) {
				if (noOfClipsPerIteration >= (cbPrev.getCount() + cb.getCount())) {
					cbPrev.setEndIndex(cb.getEndIndex());
					cbPrev.setCount(cbPrev.getCount() + cb.getCount());
					clipBoundaries.remove(cb);
				} else {
					cbPrev = cb;
					result.add(cb);
				}
			} else {
				cbPrev = cb;
				result.add(cb);
			}
		}
		return result;
	}
}
