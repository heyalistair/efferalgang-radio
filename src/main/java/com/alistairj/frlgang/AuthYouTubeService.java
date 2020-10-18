package com.alistairj.frlgang;

import static com.alistairj.frlgang.ApiManager.getAuthYouTubeApi;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.LiveBroadcastListResponse;
import com.google.api.services.youtube.model.SearchResult;
import java.io.IOException;
import java.util.ArrayList;
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
  public static Set<LiveBroadcast> getCurrentAndUpcomingLiveShowIds() throws IOException {
    Set<LiveBroadcast> broadcasts = new HashSet<>();

    YouTube youtubeService = getAuthYouTubeApi();

    // get all live videos
    YouTube.LiveBroadcasts.List request = youtubeService.liveBroadcasts()
        .list("id,snippet,contentDetails,status")
        .setMaxResults(50L)
        .setBroadcastType("all")
        .setMine(true);

    boolean moreBroadcastsRemaining = true;

    do {
      LiveBroadcastListResponse response = request.execute();

      for (LiveBroadcast item : response.getItems()) {
        if (item.getStatus().getRecordingStatus().equals("recorded") == false) {
          broadcasts.add(item);
        }
      }

      if (response.getNextPageToken() != null) {
        request.setPageToken(response.getNextPageToken());
      } else {
        moreBroadcastsRemaining = false;
      }

    } while (moreBroadcastsRemaining);


    return broadcasts;
  }
}
