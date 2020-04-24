package com.alistairj.frlgang.player;

import static com.alistairj.frlgang.player.RadioPlayerUtils.isUpcomingVideoPending;

import com.alistairj.frlgang.YouTubeService;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Video;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * On initialization, getUpcomingShows should always be called be called before getLiveShows.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class LivePlayer {

  private static final Logger logger = LoggerFactory.getLogger(LivePlayer.class);

  private Video currentLiveVideo;

  private List<Video> upcomingVideos = new ArrayList<>();

  private final RadioPlayer rp;

  /**
   * Build a new LivePlayer.
   *
   * @param rp The LivePlayer needs a reference to his daddy because he will tell it about the
   *           latest news from the Youtube API.
   */
  public LivePlayer(RadioPlayer rp) {
    this.rp = rp;

    // init
    fetchUpcomingShowStatus();
    fetchLiveShowStatus();
  }

  /**
   * Get list of live shows.
   */
  public void fetchLiveShowStatus() {
    List<String> currentLiveIds;
    try {
      currentLiveIds = YouTubeService.getCurrentLiveShows();
    } catch (GeneralSecurityException | IOException e) {
      logger.error("Unable to get current live shows!", e);
      return;
    }

    if (currentLiveIds.size() == 0) {
      // nothing is live, so check to see if something is pending or just play from the archive
      currentLiveVideo = null;

      logger.debug("Testing if first video is upcoming imminently...");
      Video upcoming = isUpcomingVideoPending(upcomingVideos);
      if (upcoming == null) {
        rp.setStatusArchivedPlay();
      } else {
        logger.debug("Video is upcoming");
        rp.setStatusUpcoming();
      }
    } else {
      // something is live!
      Video currentLive;

      try {
        if (currentLiveIds.size() > 1) {
          // oh god, more than one this is live! Figure out which one should be the canonical live

          List<Video> currentLives = new ArrayList<>();
          currentLives = YouTubeService.getVideoDetails(currentLiveIds);

          Video mostRecentlyScheduledLive = null;
          ZonedDateTime now = ZonedDateTime.now();
          ZonedDateTime mostRecentlyScheduledTime = ZonedDateTime.now().minusYears(10);
          for (Video v : currentLives) {
            ZonedDateTime scheduled =
                ZonedDateTime.ofInstant(Instant.ofEpochSecond(
                    v.getLiveStreamingDetails().getScheduledStartTime().getValue()),
                    ZoneOffset.UTC);

            if (now.isAfter(scheduled)) { // i
              if (scheduled.isAfter(mostRecentlyScheduledTime)) {
                mostRecentlyScheduledLive = v;
                mostRecentlyScheduledTime = scheduled;
              }
            }
          }

          if (mostRecentlyScheduledLive == null) {
            logger.warn("There are two different videos that have started before their scheduled "
                + "time... the DJs must be whipped. Just pick a current live at random.");
            currentLive = currentLives.get(0);
          } else {
            currentLive = mostRecentlyScheduledLive;
          }

        } else {
          currentLive = YouTubeService.getVideoDetails(currentLiveIds).get(0);
        }
      } catch (GeneralSecurityException | IOException e) {
        logger.error("Unable to get information about current live shows!", e);
        currentLive = null;
      }

      if (currentLive != null &&
          (currentLiveVideo == null
              || currentLiveVideo.getId().equals(currentLive.getId()) == false)) {
        currentLiveVideo = currentLive;

        // remove from upcoming as this information will always be the most recent.
        upcomingVideos.removeIf(v -> v.getId().equals(currentLiveVideo.getId()));

        rp.setStatusLive();
      }
    }
  }

  /**
   * Get list of upcoming shows.
   */
  @Scheduled(cron = "15 45 * ? * *")
  public void fetchUpcomingShowStatus() {
    List<Video> fetchedVideos = new ArrayList<>();

    try {
      List<String> currentUpcomingIds = YouTubeService.getUpcomingShows();
      fetchedVideos = YouTubeService.getVideoDetails(currentUpcomingIds);
    } catch (GeneralSecurityException | IOException e) {
      logger.error("Unable to get upcoming live shows!", e);
    }

    fetchedVideos.sort((o1, o2) -> {
      DateTime d1 = o1.getLiveStreamingDetails().getScheduledStartTime();
      DateTime d2 = o2.getLiveStreamingDetails().getScheduledStartTime();
      return (int) (d1.getValue() - d2.getValue());
    });

    // assign to global member only after sorting
    upcomingVideos = fetchedVideos;

    // print some nice log
    Video upcoming;
    if (fetchedVideos.size() > 0) {
      upcoming = fetchedVideos.get(0);
      logger.debug("1st upcoming video scheduled at:{}, id:{}, name:{}",
          upcoming.getLiveStreamingDetails().getScheduledStartTime().toStringRfc3339(),
          upcoming.getId(), upcoming.getSnippet().getTitle());
    }

    if (fetchedVideos.size() > 1) {
      upcoming = fetchedVideos.get(1);
      logger.debug("2nd upcoming video scheduled at:{}, id:{}, name:{}",
          upcoming.getLiveStreamingDetails().getScheduledStartTime().toStringRfc3339(),
          upcoming.getId(), upcoming.getSnippet().getTitle());
    }

    if (fetchedVideos.size() > 2) {
      upcoming = fetchedVideos.get(2);
      logger.debug("3rd upcoming video scheduled at:{}, id:{}, name:{}",
          upcoming.getLiveStreamingDetails().getScheduledStartTime().toStringRfc3339(),
          upcoming.getId(), upcoming.getSnippet().getTitle());
    }
  }

  public Video getCurrentLiveVideo() {
    return currentLiveVideo;
  }

  public List<Video> getUpcomingVideos() {
    return upcomingVideos;
  }

}
