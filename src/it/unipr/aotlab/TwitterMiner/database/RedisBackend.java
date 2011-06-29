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

package it.unipr.aotlab.TwitterMiner.database;

import java.util.Set;

import redis.clients.jedis.Jedis;
import twitter4j.HashtagEntity;
import twitter4j.UserMentionEntity;

/**
 * This class builds the Redis database architecture and returns the most important mining parameters 
 *
 * @author Marco Tarasconi
 */
public class RedisBackend implements Backend {

	private static Jedis jedis = null;
	
	public RedisBackend(){
		jedis = new Jedis("localhost");
		jedis.connect();
	}
	
	/**
	 * Updates the database with the name of the user that published a tweet, the hashtags and the mentions that he used.
	 * This method creates the keys'architecture of Redis database
	 */
	@Override
	public void update(String username, HashtagEntity[] hashtag, UserMentionEntity[] mentions) {
		
		for(int i=0; i < hashtag.length; i++){
			
			/**updates how many times a hashtag has been used */
			jedis.incr("hashtag:"+hashtag[i].getText().toLowerCase()+":weight");
			
			/**updates co-occurrence between hashtags*/
			for(int k=0; k < hashtag.length; k++){
				if(jedis.exists("hashtag:"+hashtag[i].getText().toLowerCase()+":"+hashtag[k].getText().toLowerCase()+":co_occurrence"))
					jedis.incr("hashtag:"+hashtag[i].getText().toLowerCase()+":"+hashtag[k].getText().toLowerCase()+":co_occurrence");
				else if (jedis.exists("hashtag:"+hashtag[k].getText().toLowerCase()+":"+hashtag[i].getText().toLowerCase()+":co_occurrence"))
					jedis.incr("hashtag:"+hashtag[k].getText().toLowerCase()+":"+hashtag[i].getText().toLowerCase()+":co_occurrence");
				else
					jedis.incr("hashtag:"+hashtag[i].getText().toLowerCase()+":"+hashtag[k].getText().toLowerCase()+":co_occurrence");
			}
				
			/**updates how many time a user cited a hashtag*/
			jedis.incr("user:"+username+":cited:"+hashtag[i].getText().toLowerCase()+":how_many");
				
			/**updates the set of used hashtags*/
			jedis.sadd("hashtag:all", hashtag[i].getText().toLowerCase());

			/**updates the set of hashtags used by a specific user*/
		   	jedis.sadd("user:"+username+":hashtags", hashtag[i].getText().toLowerCase());
		   	
		   	/**updates the set of users using a specific hashtag*/
		   	jedis.sadd("hashtag:"+hashtag[i].getText().toLowerCase()+":used_by", username);
		   	
		   	/**updates the set of user that twitted at least once*/
		   	jedis.sadd("user:twitterer", username);
		   	jedis.sadd("user:all", username);
		   	
		   	/**updates the set of hashtags used together with a specific one*/
		   	for(int j=0; j < hashtag.length; j++){
		   		if(j!=i)
		   			jedis.sadd("hashtag:"+hashtag[i].getText().toLowerCase()+":used_with", hashtag[j].getText().toLowerCase());
		   	}
		   	
		   	/**updates the set of users that cite or are mentioned with a specific hashtag*/
		   	for(int y=0; y < mentions.length; y++){
		   		jedis.sadd("hashtag:"+hashtag[i].getText().toLowerCase()+":involves", mentions[y].getScreenName());
		   	}
			jedis.sadd("hashtag:"+hashtag[i].getText().toLowerCase()+":involves", username);
			
		}
		
		for(int i=0; i < mentions.length; i++){
			
			/**updates the total set of users (both twitterer or mentioned) */
			jedis.sadd("user:all", mentions[i].getScreenName());
			
			/**updates the set of users mentioned by a specific one*/
			jedis.sadd("user:"+username+":mentions", mentions[i].getScreenName());
			
			/**updates the set of users that mentioned a specific user*/
			jedis.sadd("user:"+mentions[i].getScreenName()+":mentioned_by", username);
			
			/**updates the set of users mentioned together with a specific one*/
		   	for(int j=0; j < mentions.length; j++){
		   		if(j!=i)
		   			jedis.sadd("user:"+mentions[i].getScreenName()+":mentioned_with", mentions[j].getScreenName());
		   	}
		}
	}
	
	/**
	 * Returns how many times an hashtag has been used
	 */
	public long getTagsWeight(String hashtag){
		if(jedis.exists("hashtag:"+hashtag+":weight"))
			return Long.parseLong(jedis.get("hashtag:"+hashtag+":weight"));
		else
			return 0;
	}
	/**
	 * Returns how many times a hashtag has been used together with another 
	 */
	public long getTagsCoOccurrence(String hashtag1, String hashtag2){
		if(jedis.exists("hashtag:"+hashtag1+":"+hashtag2+":co_occurrence"))
			return Long.parseLong(jedis.get("hashtag:"+hashtag1+":"+hashtag2+":co_occurrence"));
		else if (jedis.exists("hashtag:"+hashtag2+":"+hashtag1+":co_occurrence"))
			return Long.parseLong(jedis.get("hashtag:"+hashtag2+":"+hashtag1+":co_occurrence"));
		else
			return 0;
	}
	/**
	 * Returns how many times a user cited a hashtag
	 */
	public long getUserHashtagCoOccurrence(String username, String hashtag){
		if(jedis.exists("user:"+username+":cited:"+hashtag+":how_many"))
			return Long.parseLong(jedis.get("user:"+username+":cited:"+hashtag+":how_many"));
		else
			return 0;
	}
	/**
	 * Returns the set of hashtags used by a user
	 */
	public Set<String> getTagsUsedBy(String username){
		return jedis.smembers("user:"+username+":hashtags");
	}
	/**
	 * Returns the set of users that cited a specific hashtag
	 */
	public Set<String> getUsersThatCited(String hashtag){
		return jedis.smembers("hashtag:"+hashtag+":used_by");
	}
	/**
	 * Returns the set of hashtags used at least once together with a specific hashtag
	 */
	public Set<String> getTagsUsedWith(String hashtag){
		return jedis.smembers("hashtag:"+hashtag+":used_with");
	}
	/**
	 * Returns the set of users that cited or that are mentioned together with a specific hashtag
	 */
	public Set<String> getUsersInvolvedIn(String hashtag){
		return jedis.smembers("hashtag:"+hashtag+":involves");
	}
	/**
	 * Returns the set of users mentioned by a specific user
	 */
	public Set<String> getUsersMentionedBy(String username){
		return jedis.smembers("user:"+username+":mentions");
	}
	/**
	 * Returns the set of users that mentioned a specific user
	 */
	public Set<String> getUsersThatMentioned(String username){
		return jedis.smembers("user:"+username+":mentioned_by");
	}
	/**
	 * Returns the set of users mentioned at least once together with a specific user
	 */
	public Set<String> getUsersMentionedWith(String username){
		return jedis.smembers("user:"+username+":mentioned_with");
	}
	/**
	 * Returns the number of users that published at least one tweet
	 */
	public long getTwetterersCardinality(){
		if(jedis.exists("user:twitterer"))
			return (jedis.scard("user:twitterer"));
		else
			return 0;
	}
	/**
	 * Returns the set of users that published at least one tweet
	 */
	public Set<String> getTwetterersNames(){
		return jedis.smembers("user:twitterer");
	}
	/**
	 * Returns the number of all saved users.
	 * I.e. the number of users that published at least one tweet or that are mentioned in at least one tweet
	 */
	public long getUsersTotalCardinality(){
		if(jedis.exists("user:all"))
			return (jedis.scard("user:all"));
		else
			return 0;
	}
	/**
	 * Returns the set of all saved users.
	 * I.e. the set of users that published at least one tweet or that are mentioned in at least one tweet
	 */
	public Set<String> getAllUsersNames(){
		return jedis.smembers("user:all");
	}
	/**
	 * Returns the number of all saved hashtags
	 */
	public long getHashtagsTotalCardinality(){
		if(jedis.exists("hashtag:all"))
			return (jedis.scard("hashtag:all"));
		else
			return 0;
	}
	/**
	 * Returns the set of all saved hashtags
	 */
	public Set<String> getAllHashtags(){
		return jedis.smembers("hashtag:all");
	}
	/**
	 * Returns true if a user cited at least once a specific hashtag, false if not
	 */
	public boolean isCitedByUser(String hashtag, String username){
		return jedis.sismember("user:"+username+":hashtags", hashtag);
	}
	/**
	 * Returns the number of users that cited at least once a specific hashtag
	 */
	public long getHowManyUsersCite(String hashtag){
		return jedis.scard("hashtag:"+hashtag+":used_by");
	}
	/**
	 * Returns the number of hashtags cited at least once by a specific user
	 */
	public long getHowManyHashtagsAreCitedBy(String username){
		return jedis.scard("user:"+username+":hashtag");
	}
}
