package dk.dma.msiproxy.provider.dkmsi.twitter;

import dk.dma.msiproxy.common.settings.DefaultSetting;
import dk.dma.msiproxy.common.settings.Setting;
import dk.dma.msiproxy.common.settings.Settings;
import org.slf4j.Logger;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import javax.annotation.PostConstruct;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;

/**
 * Provides access to Twitter
 */
@Singleton
@Lock(LockType.READ)
public class TwitterProvider {

    public static Setting TWITTER_API_KEY = new DefaultSetting("publishTwitterApiKey", "CqgrxkIiBA3sC35TmoZ5F5Oru");
    public static Setting TWITTER_API_SECRET = new DefaultSetting("publishTwitterApiSecret", "xZXl9vsW3LCtX1Py6U2VqYUmyAK0GGYZ4RINFyXgNwV7PPcQip");
    public static Setting TWITTER_ACCESS_TOKEN = new DefaultSetting("publishTwitterAccessToken", "2829892014-kqkkQLD88xhfakDlbxY0rUPdRA72Nw14e6KED0n");
    public static Setting TWITTER_ACCESS_TOKEN_SECRET = new DefaultSetting("publishTwitterAccessTokenSecret", "9brE9Ed6qak2UqluvvVG1CAShqaeezEUv5pqdQ5QZQlAG");

    public static final int MAX_TWEET_LENGTH = 140;

    @Inject
    Settings settings;

    TwitterFactory twitterFactory;

    /**
     * Instantiate a Twitter factory
     */
    @PostConstruct
    public void init() {
        //Instantiate a re-usable and thread-safe factory
        twitterFactory = new TwitterFactory();
    }

    /**
     * Get a new Twitter instance
     * @return a new Twitter instance
     */
    public Twitter getInstance() {
        //Instantiate a new Twitter instance
        Twitter twitter = twitterFactory.getInstance();

        //setup OAuth Consumer Credentials
        String twitterApiKey=settings.get(TWITTER_API_KEY);
        String twitterApiSecret=settings.get(TWITTER_API_SECRET);
        twitter.setOAuthConsumer(twitterApiKey, twitterApiSecret);

        //setup OAuth Access Token
        String twitterAccessToken=settings.get(TWITTER_ACCESS_TOKEN);
        String twitterAccessTokenSecret=settings.get(TWITTER_ACCESS_TOKEN_SECRET);
        twitter.setOAuthAccessToken(new AccessToken(twitterAccessToken, twitterAccessTokenSecret));

        return twitter;
    }
}

