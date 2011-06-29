/*
 * Copyright (c) <2011> <Marco Tarasconi>
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

package it.unipr.aotlab.TwitterMiner.dataminer;

import java.io.FileNotFoundException;

import it.unipr.aotlab.TwitterMiner.database.RedisBackend;

/**
 * This class is made only to test DataMiner's methods
 *
 * @author Marco Tarasconi
 */
public class DataMinerMain {

	private static RedisBackend redisDB = null;
	private static DataMiner miner = null;
	
	private static String socialNetworkTopic = "obama";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		redisDB = new RedisBackend();
		miner = new DataMiner();
		
		try {
			
			miner.printSocialNetworkGraph(redisDB, socialNetworkTopic);
			miner.printHashtagGraph(redisDB, socialNetworkTopic);
			System.out.println("Users binary cosine similarity= "+miner.computeUserBinaryCosineSimilarity(redisDB, "linksub", "interaria"));
			System.out.println("Hashtags binary cosine similarity= "+miner.computeHashtagsBinaryCosineSimilarity(redisDB, "android", "facebook"));
			
			System.out.println("Users tf-idf weighted cosine similarity= "+miner.computeUsersTfIdfCosineSimilarity(redisDB, "linksub", "interaria"));
			System.out.println("Hashtags tf-idf weighted cosine similarity= "+miner.computeHashtagsTfIdfCosineSimilarity(redisDB, "android", "facebook"));
			miner.printCoOccurenceRates(redisDB, socialNetworkTopic);
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}

}
