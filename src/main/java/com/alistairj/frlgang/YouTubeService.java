package com.alistairj.frlgang;

import static com.alistairj.frlgang.ApiManager.getYouTubeApi;

import com.alistairj.frlgang.player.archive.ArchivedVideo;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fetch information from YouTube.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class YouTubeService {

  private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);

  private static final String EFFERALGANG_RADIO_CHANNEL_ID = "UCEhyiFmy5c6MrTY1iLz2bAQ";
  //private static final String EFFERALGANG_RADIO_CHANNEL_ID = "UC5Z2eMviso2vnK9iHnmJO8w";

  /**
   * Fetch all upcoming and live show ids.
   */
  public static Set<String> getCurrentAndUpcomingLiveShowIds() throws IOException {
    YouTube youtubeService = getYouTubeApi();
    // Define and execute the API request

    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(50L)
        .setChannelId(EFFERALGANG_RADIO_CHANNEL_ID)
        .setType("video")
        .setEventType("live")
        .execute();

    Set<String> videoIds = new HashSet<>();

    for (SearchResult result : response.getItems()) {
      videoIds.add(result.getId().getVideoId());
    }

    request = youtubeService.search()
        .list("id");
    response = request
        .setMaxResults(50L)
        .setChannelId(EFFERALGANG_RADIO_CHANNEL_ID)
        .setType("video")
        .setEventType("upcoming")
        .execute();

    for (SearchResult result : response.getItems()) {
      videoIds.add(result.getId().getVideoId());
    }
    return videoIds;
  }


  /**
   * Fetch current live show.
   */
  public static List<String> getCurrentLiveShows() throws IOException {

    YouTube youtubeService = getYouTubeApi();
    // Define and execute the API request

    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(10L)
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

  /**
   * Search for upcoming shows.
   *
   * @return List of found upcoming shows
   * @throws IOException Thrown if there is an issue with the YouTube API
   */
  public static List<String> getUpcomingShows() throws IOException {

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

  /**
   * TODO: At the moment, this only takes a maximum of 50.
   *
   * @param videoIds Collection of video ids to fetch more information about
   * @return List of Video object containing the goods.
   * @throws IOException Thrown if there is an issue with the YouTube API
   */
  public static List<Video> getVideoDetails(Collection<String> videoIds)
      throws IOException {

    YouTube youtubeService = getYouTubeApi();
    // Define and execute the API request

    YouTube.Videos.List request = youtubeService.videos()
        .list("liveStreamingDetails,snippet");

    VideoListResponse response = request
        .setId(String.join(",", videoIds))
        .execute();

    logger.info("Fetched details about multiple videos, count:{}", videoIds.size());

    return response.getItems();
  }

  /**
   * Fetch random completed show.
   */
  public static List<ArchivedVideo> getCompletedShows()
      throws IOException {

    List<List<String>> videoIdBatches = new ArrayList<>();

    YouTube youtubeService = getYouTubeApi();

    // Define and execute the API request
    YouTube.Search.List request = youtubeService.search()
        .list("id")
        .setMaxResults(50L)
        .setPart("id")
        .setChannelId(EFFERALGANG_RADIO_CHANNEL_ID)
        .setType("video")
        .setEventType("completed");

    boolean moreArchiveRemaining = true;

    do {
      SearchListResponse response = request.execute();

      List<String> videoIds = new ArrayList<>();
      for (SearchResult item : response.getItems()) {
        videoIds.add(item.getId().getVideoId());
      }
      videoIdBatches.add(videoIds);

      if (response.getNextPageToken() != null) {
        request.setPageToken(response.getNextPageToken());
      } else {
        moreArchiveRemaining = false;
      }

    } while (moreArchiveRemaining);

    logger.info("Fetched {} batches of past shows", videoIdBatches.size());

    List<ArchivedVideo> archivedVideos = new ArrayList<>();

    long totalDurationInSeconds = 0;
    for (List<String> batch : videoIdBatches) {
      List<Video> videoDetails = YouTubeService.getVideoDetails(batch);
      for (Video v : videoDetails) {
        long startInstant = v.getLiveStreamingDetails().getActualStartTime().getValue();
        long endInstant = v.getLiveStreamingDetails().getActualEndTime().getValue();
        long duration = (endInstant - startInstant) / 1000;
        totalDurationInSeconds += duration;
        archivedVideos.add(new ArchivedVideo(v.getId(), v.getSnippet().getTitle(), duration));
      }
    }

    logger.info("Total duration of archive in seconds is {}!!", totalDurationInSeconds);
    Collections.shuffle(archivedVideos);

    return archivedVideos;
  }
}
