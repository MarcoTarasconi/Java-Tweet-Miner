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

package it.unipr.aotlab.TwitterMiner.twitter.client;

import java.io.FileNotFoundException;

import it.unipr.aotlab.TwitterMiner.database.RedisBackend;
import twitter4j.FilterQuery;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

/**
 * This class connects to Twitter and gets a stream of tweets about some chosen topics
 * 
 * @author Marco Tarasconi
 */
public class TwitterMain {

	private static TwitterStreamListener listener = null;
	private static RedisBackend redisDB = null;

	/**
	 * The args[] are used to filter twitter stream (example: "#twitter #facebook #social #redis").
	 * If no filter is specified, the default "#twitter" filter is used
	 * 
	 * @param args
	 * @throws FileNotFoundException 
	 * @throws TwitterException 
	 */
	public static void main(String[] args) throws FileNotFoundException{
		
		redisDB = new RedisBackend();
		listener = new TwitterStreamListener(redisDB); 
		
		/**Number of old tweets to catch before starting to listen live stream*/
		int count = 0; 
		
        TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.count(count);
        if(args.length == 0){
        	args = new String[1];
        	args[0]="#twitter";
        }
        filterQuery.track(args);
        twitterStream.filter(filterQuery);        
	}
	
}
