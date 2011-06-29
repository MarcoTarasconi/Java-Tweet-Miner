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

import it.unipr.aotlab.TwitterMiner.database.RedisBackend;
import twitter4j.HashtagEntity;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.UserMentionEntity;

/**
 * This class listen for events on TwitterStream
 *
 * @author Marco Tarasconi
 */
public class TwitterStreamListener implements StatusListener {
	
	private static HashtagEntity hashtag[] = null;
	private static UserMentionEntity mentions[] = null;
	private RedisBackend redisDB = null;
	
	public TwitterStreamListener(RedisBackend redisDB){
		this.redisDB = redisDB;
	}

	/**
	 * This method calls the update function of the database to store
	 * informations about received tweets
	 */
	@Override
	public void onStatus(Status status) {
		
        System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());

        hashtag = status.getHashtagEntities();
        mentions = status.getUserMentionEntities();

        redisDB.update(status.getUser().getScreenName(), hashtag, mentions);
    }

	@Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
    }

	@Override
    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
        System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
    }

	@Override
    public void onScrubGeo(long userId, long upToStatusId) {
        System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
    }

	@Override
    public void onException(Exception ex) {
        ex.printStackTrace();
    }
}
