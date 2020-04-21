package com.alistairj.frlgang;

import static com.alistairj.frlgang.ApiManager.getYouTubeApi;


import com.alistairj.frlgang.player.archive.ArchivedVideo;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetch information from YouTube.
 * <p>
 * TODO: change this to do the so that the YouTube objectS are behind a Singleton
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class YouTubeService {

  private static Logger logger = LoggerFactory.getLogger(YouTubeService.class);

  private static final String EFFERALGANG_RADIO_CHANNEL_ID = "UCEhyiFmy5c6MrTY1iLz2bAQ";
  //private static final String EFFERALGANG_RADIO_CHANNEL_ID = "UC5Z2eMviso2vnK9iHnmJO8w";

  private static List<String> API_KEY = null;

  private static YouTube service = null;


  /**
   * Fetch current live show.
   * TODO: Cache this
   */
  public static List<String> getCurrentLiveShows() throws GeneralSecurityException, IOException {

    YouTube youtubeService = getYouTubeApi();
    // Define and execute the API request

    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(10L)
        .setOrder("date")
        .setChannelId(EFFERALGANG_RADIO_CHANNEL_ID)
        .setType("video")
        .setEventType("live")
        .execute();

    List<String> videoIds = new ArrayList<>();

    for (SearchResult result : response.getItems()) {
      videoIds.add(result.getId().getVideoId());
    }

    logger.info("Fetched current live shows, count:{}", videoIds.size());

    return videoIds;
  }

  public static List<String> getUpcomingShows() throws GeneralSecurityException, IOException {

    YouTube youtubeService = getYouTubeApi();
    // Define and execute the API request

    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(5L)
        .setOrder("date")
        .setChannelId(EFFERALGANG_RADIO_CHANNEL_ID)
        .setType("video")
        .setEventType("upcoming")
        .execute();

    List<String> videoIds = new ArrayList<>();

    for (SearchResult result : response.getItems()) {
      videoIds.add(result.getId().getVideoId());
    }

    logger.info("Fetched current upcoming shows, count:{}", videoIds.size());

    return videoIds;
  }

  public static List<Video> getVideoDetails(List<String> videoIds) throws GeneralSecurityException, IOException {

    YouTube youtubeService = getYouTubeApi();
    // Define and execute the API request

    YouTube.Videos.List request = youtubeService.videos()
        .list("liveStreamingDetails,snippet");

    VideoListResponse response = request
        .setMaxResults(1L)
        .setId(String.join(",", videoIds))
        .execute();

    logger.info("Fetched details about multiple videos, count:{}", videoIds.size());

    return response.getItems();
  }

  /**
   * Fetch random completed show.
   */
  public static List<ArchivedVideo> getCompletedShows() throws GeneralSecurityException, IOException {

    YouTube youtubeService = getYouTubeApi();

    // Define and execute the API request
    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(50L)
        .setPart("id")
        .setOrder("date")
        .setChannelId(EFFERALGANG_RADIO_CHANNEL_ID)
        .setType("video")
        .setEventType("completed")
        .execute();

    List<String> videoIds = new ArrayList<>();

    for (SearchResult item : response.getItems()) {
      videoIds.add(item.getId().getVideoId());
    }
    logger.info("Fetched {} past shows", videoIds.size());
    // TODO: get all shows, not just 50

    List<Video> videoDetails = YouTubeService.getVideoDetails(videoIds);

    List<ArchivedVideo> archivedVideos = new ArrayList<>();
    for (Video v : videoDetails) {
      long startInstant = v.getLiveStreamingDetails().getActualStartTime().getValue();
      long endInstant = v.getLiveStreamingDetails().getActualEndTime().getValue();
      long duration = endInstant - startInstant;
      archivedVideos.add(new ArchivedVideo(v.getId(), v.getSnippet().getTitle(), duration));
    }

    return archivedVideos;
  }
}
