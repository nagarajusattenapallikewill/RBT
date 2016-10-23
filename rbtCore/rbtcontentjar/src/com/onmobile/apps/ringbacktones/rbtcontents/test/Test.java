package com.onmobile.apps.ringbacktones.rbtcontents.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;

public class Test {

	public static void saveClip() throws DataAccessException {
		Clip clip = new Clip();
		ClipsDAO.saveClip(clip);
	}
	
	public static void updateClip() {
		
	}

	public static void getActiveClips() throws DataAccessException {
		List<Clip> clips = ClipsDAO.getAllActiveClips();
		System.out.println("Size: " + clips.size());
	}
	
	public static void main(String[] args) {
		try {
			//init cache
//			RBTCache.initClipsCache();
//			RBTCache.initCategoryCache();
//			RBTCache.initCircleCategoryCache();
//			
			System.out.println("************* List of options *************");
			System.out.println("1-Get clip by clip id: ");
			System.out.println("2-Get clip by promo id: ");
			System.out.println("3-Get clip by wav file: ");
			System.out.println("4-Get clip by sms alias: ");
			System.out.println("5-Get clips by album: ");
			System.out.println("6-Get category by category id: ");
			System.out.println("7-Get category-id by category promo id: ");
			System.out.println("8-Get category-id by category name: ");
			System.out.println("9-Get category by category smsalias: ");
			System.out.println("10-Get category by mm number: ");
			System.out.println("11-Get categories by category-type: ");
			System.out.println("12-Get clips in category id: ");
			System.out.println("13-Get active clips in category id: ");
			System.out.println("14-Get categories with circleid-parentcategoryid-prepaidyes: ");
			System.out.println("15-Get categories with circleid-prepaidyes-categorytype: ");
			System.out.println("16-Get categories with circleid-prepaidyes-promoid: ");
			System.out.println("17-Get clip by masterPromoCode - promoType: ");
			System.out.println("18-Get clip by masterPromoCode: ");
			System.out.println("19-Get active clips by category-type: ");
			System.out.println("20-Get Active categories with circleid-parentcategoryid-prepaidyes: ");
			System.out.println("21-Get Artist names for all clips:");
			System.out.println("22-Get Album names for language-starting_letter_of_album-offset-rowcount: ");
			System.out.println("23-Search Clips for albumName-language-offset-rowcount: ");
			System.out.println("******************************************");

			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String input = " ";
			while(null != input && input.length() > 0) {
				System.out.println("Enter your option:");
				input = br.readLine();
				int option = Integer.parseInt(input);
//				getActiveClips();
				long start = System.currentTimeMillis();
//				System.out.println("Input: " + input + " Clip: " + RBTCacheManager.getInstance().getClip(input));
				switch(option) {
				case 1: 
					System.out.println("1-Get clip by clip id: ");
					input = br.readLine();
					System.out.println("Input: " + input + " Clip: " + RBTCacheManager.getInstance().getClip(input));
					break;
				case 2:
					System.out.println("2-Get clip by promo id: ");
					input = br.readLine();
					System.out.println("Input: " + input + " Clip: " + RBTCacheManager.getInstance().getClipByPromoId(input));
					break;
				case 3:
					System.out.println("3-Get clip by wav file: ");
					input = br.readLine();
					System.out.println("Input: " + input + " Clip: " + RBTCacheManager.getInstance().getClipByRbtWavFileName(input));
					break;
				case 4:
					System.out.println("4-Get clip by sms alias: ");
					input = br.readLine();
					System.out.println("Input: " + input + " Clip: " + RBTCacheManager.getInstance().getClipBySMSAlias(input));
					break;
				case 5:
					System.out.println("5-Get clips by album: ");
					input = br.readLine();
					Clip[] clips1 = RBTCacheManager.getInstance().getClipsByAlbum(input);
					if(clips1 != null && clips1.length != 0) {
						for (int i = 0; i < clips1.length; i++) {
							System.out.println("Input: " + input + " Clip: " + clips1[i]);
						}
					} else {
						System.out.println("Input: " + input + " Clips not found");
					}
					break;
				case 6:
					System.out.println("6-Get category by category id: ");
					input = br.readLine();
					System.out.println("Input: " + input + " Category: " + RBTCacheManager.getInstance().getCategory(Integer.parseInt(input)));
					break;
				case 7:
					System.out.println("7-Get category-id by category promo id: ");
					input = br.readLine();
					System.out.println("Input: " + input + " category-id: " + RBTCacheManager.getInstance().getCategoryByPromoId(input));
					break;
				case 8:
					System.out.println("8-Get category-id by category name: ");
					input = br.readLine();
					System.out.println("Input: " + input + " category-id: " + RBTCacheManager.getInstance().getCategoryByName(input));
					break;
				case 9:
					System.out.println("9-Get category by category smsalias: ");
					input = br.readLine();
					System.out.println("Input: " + input + " category: " + RBTCacheManager.getInstance().getCategoryBySMSAlias(input));
					break;
				case 10:
					System.out.println("10-Get category by mm number: ");
					input = br.readLine();
					System.out.println("Input: " + input + " category: " + RBTCacheManager.getInstance().getCategoryByMmNumber(input));
					break;
				case 11:
					System.out.println("11-Get categories by category-type: ");
					input = br.readLine();
					Category[] categories = RBTCacheManager.getInstance().getCategoryByType(input);
					if(categories != null && categories.length != 0) {
						for (int i = 0; i < categories.length; i++) {
							System.out.println("Input: " + input + " category: " + categories[i]);
						}
					} else {
						System.out.println("Input: " + input + " Categories not found");
					}
					break;
				case 12:
					System.out.println("12-Get clips in category id: ");
					input = br.readLine();
					Clip[] clips2 = RBTCacheManager.getInstance().getClipsInCategory(Integer.parseInt(input));
					if(clips2 != null && clips2.length != 0) {
						for (int i = 0; i < clips2.length; i++) {
							System.out.println("Input: " + input + " Clip: " + clips2[i]);
						}
					} else {
						System.out.println("Input: " + input + " Clips in category not found");
					}
					break;
				case 13:
					System.out.println("13-Get active clips in category id: ");
					input = br.readLine();
					Clip[] clips3 = RBTCacheManager.getInstance().getActiveClipsInCategory(Integer.parseInt(input));
					if(clips3 != null && clips3.length != 0) {
						for (int i = 0; i < clips3.length; i++) {
							System.out.println("Input: " + input + " Active Clip: " + clips3[i]);
						}
					} else {
						System.out.println("Input: " + input + " Acitve Clips in category not found");
					}
					break;
				case 14:
					System.out.println("14-Get categories with circleid-parentcategoryid-prepaidyes: ");
					input = br.readLine();
					String circleId1 = input.substring(0, input.indexOf("-"));
					int parentCategoryId1 = Integer.parseInt(input.substring(input.indexOf('-') + 1, input.lastIndexOf('-')));
					char prepaidYes1 = input.substring(input.lastIndexOf('-') + 1).charAt(0);
					Category[] categories1 = RBTCacheManager.getInstance().getCategoriesInCircle(circleId1, parentCategoryId1, prepaidYes1);
					if(categories1 != null && categories1.length != 0) {
						for (int i = 0; i < categories1.length; i++) {
							System.out.println("Input: " + input + " Category: " + categories1[i]);
						}
					} else {
						System.out.println("Input: " + input + " categories in circle not found");
					}
					break;
				case 15:
					System.out.println("15-Get categories with circleid-prepaidyes-categorytype: ");
					input = br.readLine();
					String circleId2 = input.substring(0, input.indexOf("-"));
					char prepaidYes2 = input.substring(input.indexOf('-') + 1).charAt(0);
					String categoryType2 = input.substring(input.lastIndexOf('-') + 1);
					Category[] categories2 = RBTCacheManager.getInstance().getCategoryByType(circleId2, prepaidYes2, categoryType2);
					if(categories2 != null && categories2.length != 0) {
						for (int i = 0; i < categories2.length; i++) {
							System.out.println("Input: " + input + " Category: " + categories2[i]);
						}
					} else {
						System.out.println("Input: " + input + " categories in circle not found");
					}
					break;
				case 16:
					System.out.println("16-Get categories with circleid-prepaidyes-promoid: ");
					input = br.readLine();
					String circleId3 = input.substring(0, input.indexOf("-"));
					char prepaidYes3 = input.substring(input.indexOf('-') + 1).charAt(0);
					String promoId3 = input.substring(input.lastIndexOf('-') + 1);
					Category[] categories3 = RBTCacheManager.getInstance().getCategoryByPromoId(circleId3, prepaidYes3, promoId3);
					if(categories3 != null && categories3.length != 0) {
						for (int i = 0; i < categories3.length; i++) {
							System.out.println("Input: " + input + " Category: " + categories3[i]);
						}
					} else {
						System.out.println("Input: " + input + " categories in circle not found");
					}
					break;
				case 17:
					System.out.println("17-Get clip by masterPromoCode - promoType: ");
					input = br.readLine();
					String promoCode1 = input.substring(0, input.indexOf("-"));
					String promoType1 = input.substring(input.lastIndexOf('-') + 1);
					System.out.println("Input: " + input + " clip: " + RBTCacheManager.getInstance().getClipFromPromoMaster(promoType1, promoCode1));
					break;
				case 18:
					System.out.println("18-Get clip by masterPromoCode: ");
					input = br.readLine();
					System.out.println("Input: " + input + " clip: " + RBTCacheManager.getInstance().getClipFromPromoMaster(input));
					break;
				case 19:
					System.out.println("19-Get active clips by category-type: ");
					input = br.readLine();
					Category[] categories4 = RBTCacheManager.getInstance().getCategoryByType(input);
					System.out.println("Input: " + input + " Categories: " + categories4.length);
					TreeMap<String, Clip> clipsMap = new TreeMap<String, Clip>();
					for (int idx = 0; idx < categories4.length; idx++) {
						Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(categories4[idx].getCategoryId());
						System.out.println("Category: " + categories4[idx] + " clips: " + clips);
						for (int j = 0; null != clips && j < clips.length; j++) {
							System.out.println("Clip: " + clips[j]);
							clipsMap.put(clips[j].getClipName(), clips[j]);
						}
					}
					System.out.println(clipsMap);
					break;
				case 20:
					System.out.println("20-Get Active categories with circleid-parentcategoryid-prepaidyes: ");
					input = br.readLine();
					String circleId20 = input.substring(0, input.indexOf("-"));
					int parentCategoryId20 = Integer.parseInt(input.substring(input.indexOf('-') + 1, input.lastIndexOf('-')));
					char prepaidYes20 = input.substring(input.lastIndexOf('-') + 1).charAt(0);
					Category[] categories20 = RBTCacheManager.getInstance().getActiveCategoriesInCircle(circleId20, parentCategoryId20, prepaidYes20);
					if(categories20 != null && categories20.length != 0) {
						for (int i = 0; i < categories20.length; i++) {
							System.out.println("Input: " + input + " Category: " + categories20[i]);
						}
					} else {
						System.out.println("Input: " + input + " categories in circle not found");
					}
					break;			
					
				case 21:
					System.out.println("21-Get Artist names for all clips:");
					input = br.readLine();
					Set<String> artist21 = RBTCacheManager.getInstance().getClipArtistIndex('A');
					if(artist21 != null && artist21.size() != 0) {
						Iterator<String> i3 = artist21.iterator();
						while(i3.hasNext()){
							System.out.println("Artist is:"+i3.next());
						}
					} else {
						System.out.println("Artist not found");
					}
					break;	
				case 22: {
					System.out.println("Get Album names for language-starting_letter_of_album-offset-rowcount: ");
					input = br.readLine();
					String language = input.substring(0, input.indexOf("-"));
					input = input.substring(input.indexOf("-") + 1);
					char startingLetter = input.substring(0, input.indexOf("-")).charAt(0);
					input = input.substring(input.indexOf("-") + 1);
					int offset = Integer.parseInt(input.substring(0, input.indexOf("-")));
					input = input.substring(input.indexOf("-") + 1);
					int rowCount = Integer.parseInt(input);
					ArrayList<Object> list = RBTCacheManager.getInstance().getAlbumNameByLanguage(language, startingLetter, offset, rowCount);
					System.out.println("Total size: " + list.get(0));
					@SuppressWarnings("unchecked")
					Set<String> albumNames = (Set<String>) list.get(1);
					System.out.print("AlbumNames (Result size - " + albumNames.size() +"): ");
					for (String albumName: albumNames) {
						System.out.print(albumName + ", ");
					}
					System.out.println();
					break;
				}
				case 23: {
					System.out.println("Search Clips for albumName-language-offset-rowcount: ");
					input = br.readLine();
					String albumName = input.substring(0, input.indexOf("-"));
					input = input.substring(input.indexOf("-") + 1);
					String language = input.substring(0, input.indexOf("-"));
					input = input.substring(input.indexOf("-") + 1);
					int offset = Integer.parseInt(input.substring(0, input.indexOf("-")));
					input = input.substring(input.indexOf("-") + 1);
					int rowCount = Integer.parseInt(input);
					ArrayList<Object> list = RBTCacheManager.getInstance().searchClipsByAlbumName(albumName, language, offset, rowCount);
					System.out.println("Total size: " + list.get(0));
					Clip[] clips = (Clip[]) list.get(1);
					if (clips != null && clips.length > 0) {
						System.out.println("Clips (Result size - " + clips.length + "): ");
						for (Clip clip: clips) {
							System.out.println("Clip: " + clip + ", ");
						}
						System.out.println();
					} else {
						System.out.println("Empty Result!");
					}
					break;
				}
				// TODO: Please add option details in 'List of options' section
				default:
						System.out.println("default");
				}
				System.out.println("Processed in " + (System.currentTimeMillis() - start) + "ms.");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
