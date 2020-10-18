package com.alistairj.frlgang;

import static com.alistairj.frlgang.ApiManager.getAuthYouTubeApi;
import static com.alistairj.frlgang.ApiManager.getYouTubeApi;

import com.alistairj.frlgang.player.BroadcastStatus;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class AuthYouTubeService {

  private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);

  /**
   * Fetch all upcoming and live show ids.
   *
   * <p>
   * EXECUTED: once an hour
   * COST: 100 x 2
   * </p>
   */
  public static Set<String> getCurrentAndUpcomingLiveShowIds() throws IOException {
    YouTube youtubeService = getAuthYouTubeApi();

    // get all live videos
    YouTube.LiveBroadcasts.List request = youtubeService.liveBroadcasts()
        .list("id");
    LiveBroadcastListResponse response = request
        .setMaxResults(50L)
        .setBroadcastType("all")
        .setMine(true)
        .execute();

    Set<String> videoIds = new HashSet<>();

    for (LiveBroadcast result : response.getItems()) {
      videoIds.add(result.getId());
    }

    return videoIds;
  }

}
