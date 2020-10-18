package com.alistairj.frlgang.player;

import static com.alistairj.frlgang.utils.RadioPlayerUtils.hasLiveVideoEnded;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.isFirstVideoScheduledAfterSecond;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.isUpcomingVideoPending;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.isVideoLive;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.printUpcomingShows;

import com.alistairj.frlgang.ApiManager;
import com.alistairj.frlgang.AuthYouTubeService;
import com.alistairj.frlgang.YouTubeService;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.LiveBroadcast;
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

  private LiveBroadcast currentLiveVideo;

  /**
   * These get add constantly. They only way they will get mo
   */
  private Set<String> relevantVideoIds = new HashSet<>();

  private List<LiveBroadcast> upcomingVideos = new ArrayList<>();

  private final RadioPlayer rp;

  /**
   * Build a new LivePlayer.
   *
   * @param rp The LivePlayer needs a reference to his daddy because he will tell it about the
   *           latest news from the Youtube API.
   */
  public LivePlayer(RadioPlayer rp) {
    this.rp = rp;
  }

  public void fetchStatusOfBroadcasts() {

    try {
      logger.info("Fetching information live and upcoming broadcasts");

      Set<LiveBroadcast> videos = AuthYouTubeService.getCurrentAndUpcomingLiveShowIds();

      List<LiveBroadcast> upcomers = new ArrayList<>();
      boolean hasFoundLive = false;
      for (LiveBroadcast v : videos) {

        if (v.getSnippet().getActualEndTime() == null) {
          if (v.getStatus().getRecordingStatus().equals("recording")) {
            currentLiveVideo = v;
            hasFoundLive = true;
          } else {
            upcomers.add(v);
          }
        }
      }

      if (hasFoundLive == false) {
        currentLiveVideo = null;
      }

      upcomers.sort((o1, o2) -> {

        if (o1.getSnippet() == null
            || o1.getSnippet().getScheduledStartTime() == null) {
          return -1;
        }

        if (o2.getSnippet() == null
            || o2.getSnippet().getScheduledStartTime() == null) {
          return 1;
        }
        DateTime d1 = o1.getSnippet().getScheduledStartTime();
        DateTime d2 = o2.getSnippet().getScheduledStartTime();
        return (int) (d1.getValue() - d2.getValue());
      });

      upcomingVideos = upcomers;

    } catch (IOException e) {
      logger.error("Unable to get search for upcoming and live shows!", e);
    }

    if (currentLiveVideo == null) {
      logger.debug("Testing if first video is upcoming imminently...");
      LiveBroadcast upcoming = isUpcomingVideoPending(upcomingVideos);
      if (upcoming == null) {
        rp.setStatusArchivedPlay();
      } else {
        logger.debug("Video is upcoming");
        rp.setStatusUpcoming();
      }
    } else {
      rp.setStatusLive();
    }

    logger.info("Fetching information live and upcoming broadcasts... complete!");

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

      //TODO
//      Set<String> show = new HashSet<>();
//      show.add(v.getId());
//      fetchStatusOfBroadcasts(show);
    }

    return fv;
  }

  public LiveBroadcast getCurrentLiveVideo() {
    return currentLiveVideo;
  }

  public List<LiveBroadcast> getUpcomingVideos() {
    return upcomingVideos;
  }

}
