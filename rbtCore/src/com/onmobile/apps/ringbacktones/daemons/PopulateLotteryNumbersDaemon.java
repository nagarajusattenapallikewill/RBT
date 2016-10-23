package com.onmobile.apps.ringbacktones.daemons;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.RBTLotteries;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

/**
 * @author sridhar.sindiri
 *
 */
public class PopulateLotteryNumbersDaemon
{
	private static final Logger logger = Logger.getLogger(PopulateLotteryNumbersDaemon.class);
	private static Map<Integer, RBTLotteries> lotteryIDEntriesNumberMap = new LinkedHashMap<Integer, RBTLotteries>();

	private static Map<Integer, Set<Long>> lotteryIDLotteryNumbersMap = new LinkedHashMap<Integer, Set<Long>>();

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		validateAllLotteries();

		populateLotteryNumbers();
		logger.info("Populating lottery numbers is done.");
	}

	/**
	 * 
	 */
	private static void validateAllLotteries()
	{
		RBTLotteries[] rbtLotteries = RBTDBManager.getInstance().getAllLotteries();
		if (rbtLotteries == null)
			return;

		for (RBTLotteries rbtLottery : rbtLotteries) {
			int lotteryID = rbtLottery.lotteryID();
			int lotteryNumberSize = rbtLottery.lotteryNumberSize();
			long maxEntries = rbtLottery.maxEntries();

			if (lotteryNumberSize < 1)
			{
				logger.warn("Lottery NumberSize is not valid for lotteryID : " + lotteryID);
				continue;
			}

			long maxEntriesPossible = (long) Math.pow(10, lotteryNumberSize) - (long) Math.pow(10, lotteryNumberSize - 1);
			if (maxEntriesPossible < maxEntries)
			{
				logger.warn("Lottery MaxEntries is not valid for lotteryID : " + lotteryID);
				continue;
			}

			long entriesPresent = RBTDBManager.getInstance().getCountByLotteryID(lotteryID);
			if (entriesPresent != -1)
			{
				maxEntries = maxEntries - entriesPresent;
				rbtLottery.setMaxEntries(maxEntries);
			}

			if (maxEntries > 0)
				lotteryIDEntriesNumberMap.put(lotteryID, rbtLottery);
		}

		return;
	}

	/**
	 * 
	 */
	private static void populateLotteryNumbers()
	{
		Set<Entry<Integer, RBTLotteries>> lotteryEntriesSet = lotteryIDEntriesNumberMap.entrySet();
		boolean run = true;
		Random randomObj= new Random();
		while (run)
		{
			Iterator<Entry<Integer, RBTLotteries>> lotteryIter = lotteryEntriesSet.iterator();
			while (lotteryIter.hasNext())
			{
				Entry<Integer, RBTLotteries> nextEntry = lotteryIter.next();
				int lotteryID = nextEntry.getKey();
				RBTLotteries rbtLottery = nextEntry.getValue();

				int numberSize = rbtLottery.lotteryNumberSize();
				long maxEntries = rbtLottery.maxEntries();
				Set<Long> numbersSet = lotteryIDLotteryNumbersMap.get(lotteryID);
				if (numbersSet == null)
					numbersSet = new HashSet<Long>();

				long num = getRandomNumber((long) Math.pow(10, numberSize - 1), (long) Math.pow(10, numberSize), randomObj);
				while (!numbersSet.add(num))
				{
					num = getRandomNumber((long) Math.pow(10, numberSize - 1), (long) Math.pow(10, numberSize), randomObj);
				}

				lotteryIDLotteryNumbersMap.put(lotteryID, numbersSet);
				boolean success = RBTDBManager.getInstance().insertRbtLotteryNumber(lotteryID, String.valueOf(num));
				if (success)
				{
					maxEntries--;
					rbtLottery.setMaxEntries(maxEntries);
					if (maxEntries == 0)
						lotteryIter.remove();
				}
			}

			if (lotteryIDLotteryNumbersMap.isEmpty())
				run = false;
		}
	}

	/**
	 * @param minNum
	 * @param maxNum
	 * @param random
	 * @return
	 */
	private static Long getRandomNumber(long minNum, long maxNum, Random random)
	{
		long range = maxNum - minNum;
		long fraction = (long) (range * random.nextDouble());
		long randomNumber = fraction + minNum;

		return randomNumber;
	}
}
