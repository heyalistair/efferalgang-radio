package com.alistairj.frlgang.player;

import static com.alistairj.frlgang.utils.RadioPlayerUtils.isFirstVideoScheduledAfterSecond;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.isUpcomingVideoPending;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.printUpcomingShows;

import com.alistairj.frlgang.ApiManager;
import com.alistairj.frlgang.YouTubeService;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Video;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * On initialization, getUpcomingShows should always be called be called before getLiveShows.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class LivePlayer {

  private static final Logger logger = LoggerFactory.getLogger(LivePlayer.class);

  private Video currentLiveVideo;

  /**
   * These get add constantly. They only way they will get mo
   */
  private Set<String> relevantVideoIds = new HashSet<>();

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
    fetchUpcomingAndLiveShowIds();
  }

  /**
   * LivePlayer will update the list of relevant ids.
   */
  public void fetchUpcomingAndLiveShowIds() {
    try {
      logger.trace("Getting information about relevant ids");
      Set<String> unverifiedVideoIds = YouTubeService.getCurrentAndUpcomingLiveShowIds();
      fetchBroadcastStatusOfRelevantIds(unverifiedVideoIds);
    } catch (IOException e) {
      logger.error("Unable to get search for upcoming and live shows!", e);
    }

    printUpcomingShows(upcomingVideos);
  }

  // this needs to be done once every minute (except when fetchUpcomingAndLiveShowIds fires)
  public void fetchBroadcastStatusOfRelevantIds() {
    fetchBroadcastStatusOfRelevantIds(new HashSet<>());
  }

  private void fetchBroadcastStatusOfRelevantIds(Set<String> unverifiedVideoIds) {

    unverifiedVideoIds.addAll(relevantVideoIds);

    try {
      logger.info("Getting information for {} ids: {}", unverifiedVideoIds.size(),
          String.join(",", unverifiedVideoIds));

      List<Video> videos = YouTubeService.getUpcomingShowDetails(unverifiedVideoIds);

      Video current = null;
      List<Video> upcomers = new ArrayList<>();
      for (Video v : videos) {
        if (v.getLiveStreamingDetails().getActualEndTime() != null) {
          // it ended! It's no longer live, and we don't have to monitor it
          unverifiedVideoIds.remove(v.getId());
        } else if (v.getLiveStreamingDetails().getActualStartTime() != null) {
          // here it has a start time and no end time - It's live!
          if (current != null) {
            if (isFirstVideoScheduledAfterSecond(v, current)) {
              current = v;
            }
          } else {
            current = v;
          }
        } else {
          upcomers.add(v);
        }
      }

      upcomers.sort((o1, o2) -> {
        DateTime d1 = o1.getLiveStreamingDetails().getScheduledStartTime();
        DateTime d2 = o2.getLiveStreamingDetails().getScheduledStartTime();
        return (int) (d1.getValue() - d2.getValue());
      });

      relevantVideoIds = unverifiedVideoIds;

      currentLiveVideo = current;
      upcomingVideos = upcomers;

    } catch (IOException e) {
      logger.error("Unable to get search for upcoming and live shows!", e);
    }

    if (currentLiveVideo == null) {
      logger.debug("Testing if first video is upcoming imminently...");
      Video upcoming = isUpcomingVideoPending(upcomingVideos);
      if (upcoming == null) {
        rp.setStatusArchivedPlay();
      } else {
        logger.debug("Video is upcoming");
        rp.setStatusUpcoming();
      }
    } else {
      rp.setStatusLive();
    }
  }

  /**
   * Get list of live shows.
   */
  @Deprecated
  public void fetchLiveShowStatus() {
    Set<String> currentLiveIds;
    try {
      currentLiveIds = YouTubeService.getCurrentAndUpcomingLiveShowIds();
    } catch (IOException e) {
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

          List<Video> currentLives;
          currentLives = YouTubeService.getUpcomingShowDetails(currentLiveIds);

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
          currentLive = YouTubeService.getUpcomingShowDetails(currentLiveIds).get(0);
        }
      } catch (IOException e) {
        logger.error("Unable to get information about current live shows!", e);
        currentLive = null;
      }

      if (currentLive != null
          && (currentLiveVideo == null
          || currentLiveVideo.getId().equals(currentLive.getId()) == false)) {
        currentLiveVideo = currentLive;

        // remove from upcoming as this information will always be the most recent.
        upcomingVideos.removeIf(v -> v.getId().equals(currentLiveVideo.getId()));

        rp.setStatusLive();
      }
    }
  }

  private Video fetchVideo(String videoId) throws IOException {

    List<Video> videos = YouTubeService.getUpcomingShowDetails(Collections.singletonList(videoId));

    if (videos.isEmpty()) {
      throw new IOException("I can't find " + videoId);
    }

    Video v = videos.get(0);

    if (v.getSnippet().getChannelId().equals(ApiManager.getChannelId()) == false) {
      throw new IOException("I can't find " + videoId);
    }

    return v;
  }

  public FoundVideo checkVideoId(String videoId) throws IOException {

    Video v = fetchVideo(videoId);

    boolean foundInUpcoming = upcomingVideos.stream().anyMatch(x -> x.getId().equals(v.getId()));
    boolean foundAsLive = currentLiveVideo != null && currentLiveVideo.getId().equals(v.getId());

    // already known to the player
    FoundVideo fv;
    if (foundInUpcoming || foundAsLive) {
      fv = new FoundVideo(v, false);
    } else {
      fv = new FoundVideo(v, true);

      Set<String> show = new HashSet<>();
      show.add(v.getId());
      fetchBroadcastStatusOfRelevantIds(show);
    }

    return fv;
  }

  /**
   * Get list of upcoming shows.
   */
  @Deprecated
  public void fetchUpcomingShowStatus() {
    List<Video> fetchedVideos = new ArrayList<>();

    try {
      List<String> currentUpcomingIds = YouTubeService.getUpcomingShowIds();
      fetchedVideos = YouTubeService.getUpcomingShowDetails(currentUpcomingIds);
    } catch (IOException e) {
      logger.error("Unable to get upcoming live shows!", e);
    }

    fetchedVideos.sort((o1, o2) -> {
      DateTime d1 = o1.getLiveStreamingDetails().getScheduledStartTime();
      DateTime d2 = o2.getLiveStreamingDetails().getScheduledStartTime();
      return (int) (d1.getValue() - d2.getValue());
    });

    // assign to global member only after sorting
    upcomingVideos = fetchedVideos;
  }

  public Video getCurrentLiveVideo() {
    return currentLiveVideo;
  }

  public List<Video> getUpcomingVideos() {
    return upcomingVideos;
  }

}
