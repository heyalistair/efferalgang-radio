package com.alistairj.frlgang.player;

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

  private String currentLiveId;

  private List<Video> upcomingVideos = new ArrayList<>();

  private RadioPlayer rp;

  /**
   * Build a new LivePlayer.
   *
   * @param rp The LivePlayer needs a reference to his daddy because he will tell it about the
   *           latest news from the Youtube API.
   */
  public LivePlayer(RadioPlayer rp) {
    this.rp = rp;
  }

  @Scheduled(cron = "15,45 0,1,2,3,4,5,6,7,8,9,31,32,33,35,37 * ? * *")
  public void getLiveShowStatus() throws GeneralSecurityException, IOException {
    List<String> currentLiveIds = YouTubeService.getCurrentLiveShows();

    if (currentLiveIds.size() == 0) {
      // nothing is live, so check to see if something is pending or just play from the archive
      currentLiveId = null;

      Video upcoming = isUpcomingVideoPending(upcomingVideos);
      if (upcoming == null) {
        rp.setStatusArchivedPlay();
      } else {
        rp.setStatusUpcoming(upcoming);
      }
    } else {
      // something is live!
      Video currentLive;

      if (currentLiveIds.size() > 1) {
        // oh god, more than one this is live! Figure out which one should be the canonical live

        List<Video> currentLives = YouTubeService.getVideoDetails(currentLiveIds);

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

        if (currentLiveId.equals(currentLive.getId()) == false) {
          rp.setStatusLive(currentLiveId);
        }
      }
    }
  }

  @Scheduled(cron = "15 45 * ? * *")
  public void getUpcomingShowStatus() throws GeneralSecurityException, IOException {
    List<String> currentUpcomingIds = YouTubeService.getUpcomingShows();

    List<Video> fetchedVideos = YouTubeService.getVideoDetails(currentUpcomingIds);

    fetchedVideos.sort((o1, o2) -> {
      DateTime d1 = o1.getLiveStreamingDetails().getScheduledStartTime();
      DateTime d2 = o2.getLiveStreamingDetails().getScheduledStartTime();
      return (int) (d1.getValue() - d2.getValue());
    });

    upcomingVideos = fetchedVideos;

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

  /**
   * Returns the next upcoming video, if there is one.
   *
   * @param upcomingList The list of videos sorted by scheduled time.
   * @return video Upcoming video, or null if there is none.
   */
  private static Video isUpcomingVideoPending(List<Video> upcomingList) {
    if (upcomingList == null) {
      return null;
    }

    if (upcomingList.size() > 0) {
      DateTime dt = upcomingList.get(0).getLiveStreamingDetails().getScheduledStartTime();
      ZonedDateTime scheduled = getDateTime(dt);
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime tenMinutesAgo = now.minusMinutes(10);
      ZonedDateTime inTenMinutes = now.plusMinutes(10);
      if (scheduled.isBefore(inTenMinutes) && scheduled.isAfter(tenMinutesAgo)) {
        return upcomingList.get(0);
      }
    }

    return null;
  }

  public static ZonedDateTime getDateTime(DateTime dt) {
    Instant instant = Instant.ofEpochSecond(dt.getValue());
    return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
  }

}
