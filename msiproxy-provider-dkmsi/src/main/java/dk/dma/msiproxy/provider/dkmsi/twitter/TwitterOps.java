package dk.dma.msiproxy.provider.dkmsi.twitter;

import dk.dma.msiproxy.common.MsiProxyApp;
import dk.dma.msiproxy.common.settings.DefaultSetting;
import dk.dma.msiproxy.common.settings.Settings;
import dk.dma.msiproxy.model.msi.Message;
import dk.dma.msiproxy.model.msi.Point;
import dk.dma.msiproxy.provider.dkmsi.conf.DkMsiDB;
import dk.dma.msiproxy.provider.dkmsi.model.Tweet;
import org.slf4j.Logger;
import twitter4j.*;
import twitter4j.api.TweetsResources;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by hje on 9/29/15.
 * <p>
 * Used to isolate tgransaction for en twitter update
 */
@Lock(LockType.READ)
public class TwitterOps {

    @Inject
    @DkMsiDB
    EntityManager em;

    @Inject
    MsiProxyApp msiProxyApp;

    @Inject
    TwitterProvider twitterProvider;

    @Inject
    Logger log;

    @Inject
    Settings settings;

    /**
     * Compute the approximate center location of the message
     *
     * @param message the message
     * @return the approximate center location of the message
     */
    private GeoLocation computeLocation(Message message) {
        if (message.getLocations() == null || message.getLocations().size() == 0) {
            return null;
        }
        Point minPt = new Point(90, 180);
        Point maxPt = new Point(-90, -180);
        message.getLocations().forEach(loc -> loc.getPoints().forEach(pt -> {
                    maxPt.setLat(Math.max(maxPt.getLat(), pt.getLat()));
                    maxPt.setLon(Math.max(maxPt.getLon(), pt.getLon()));
                    minPt.setLat(Math.min(minPt.getLat(), pt.getLat()));
                    minPt.setLon(Math.min(minPt.getLon(), pt.getLon()));
                }
        ));

        return new GeoLocation((maxPt.getLat() + minPt.getLat()) / 2.0, (maxPt.getLon() + minPt.getLon()) / 2.0);
    }

    //Instantiate and initialize a new twitter status update https://msi-proxy.e-navigation.net/message-map-image/dkmsi/14423.png

    private twitter4j.Status sendUpdate(Message message, String tweetText) throws TwitterException {

        //get a uri from the settings if available otherwise go with this apps uri
        dk.dma.msiproxy.common.settings.Setting PROXY_BASE_URI = new DefaultSetting("proxyBaseUri", msiProxyApp.getBaseUri());
        String url = settings.get(PROXY_BASE_URI) + "/message-map-image/dkmsi/" + message.getId() + ".png";

        try {
            StatusUpdate statusUpdate = new StatusUpdate(tweetText);

            statusUpdate.setMedia(
                    message.getSeriesIdentifier().getFullId(),
                    new URL(url).openStream());

            // Compute the location
            GeoLocation location = computeLocation(message);
            if (location != null) {
                statusUpdate.setLocation(location);
            }

            return twitterProvider.getInstance().updateStatus(statusUpdate);
        } catch (MalformedURLException mue) {
            log.error("Malformed URL, tweet not created: " + url + "/dkmsi/da/details/" + message.getId());
        } catch (IOException ioe) {
            log.error("IO exception, tweet not created: " + message.getMessageId());
        }
        return null;
    }

    public ResponseList<Status> listTweets() throws TwitterException {
        Paging paging = new Paging();
        paging.setCount(200);
        ResponseList responseList=twitterProvider.getInstance().getHomeTimeline(paging);
        return responseList;
    }


    public twitter4j.Status deleteTweet(Long tweetId) throws TwitterException {
        log.info("Deleting Tweet: " + tweetId);
        return twitterProvider.getInstance().destroyStatus(tweetId);
    }

    public void updateTwitter(Message msg, String tweetText) {
        try {
            twitter4j.Status status = sendUpdate(msg, tweetText);
            Tweet tweet = new Tweet(
                    msg.getMessageId(),
                    status.getId(),
                    tweetText,
                    msg.getValidFrom(),
                    msg.getValidTo()
            );
            em.persist(tweet);
        } catch (TwitterException te) {
            log.error("Adding single tweet fails, warning ignored: " + tweetText + " messageId: " + msg.getMessageId() + "Twitter Exception: "+te.getErrorMessage() );
        }
    }
}
