package com.alistairj.frlgang.player;

import static com.alistairj.frlgang.utils.RadioPlayerUtils.hasLiveVideoEnded;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.isFirstVideoScheduledAfterSecond;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.isUpcomingVideoPending;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.isVideoLive;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.printUpcomingShows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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

  private static final String URL_STATS = "http://167.172.160.213/stat";

  private boolean isLive;

  /**
   * Build a new LivePlayer.
   */
  public LivePlayer() {
  }

  public void fetchBroadcastStatus() {
    try {
      String xml = this.getXml();
      logger.info(xml);
    } catch (IOException e) {
      logger.error("Cannot fetch XML");
    }
  }

  OkHttpClient client = new OkHttpClient();

  private String getXml() throws IOException {
    Request request = new Request.Builder()
        .url(URL_STATS)
        .build();

    try (Response response = client.newCall(request).execute()) {
      return response.body().string();
    }
  }
}
