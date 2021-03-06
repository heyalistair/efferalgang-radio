package com.alistairj.frlgang;

import static com.alistairj.frlgang.ApiManager.getYouTubeApi;
import static com.alistairj.frlgang.utils.RadioPlayerUtils.getDateTime;

import com.alistairj.frlgang.player.archive.ArchivedVideo;
import com.alistairj.frlgang.utils.RadioPlayerUtils;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
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
 * <p>
 * Note the default quota is 10,000 per day.
 * </p>
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class YouTubeService {

  private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);

  private static final List<String> blacklistVideoIds = new ArrayList<>();

  static {
    blacklistVideoIds.add("h8BmJYyeziM"); // Alistair reads you a bedtime story, ep 1
    blacklistVideoIds.add("w6JKc2M41IE"); // Alistair reads you a bedtime story, ep 2
    blacklistVideoIds.add("vZoAtj7ADtU"); // Alistair reads you a bedtime story, ep 3
    blacklistVideoIds.add("o43qo-6uHQA"); // Alistair reads you a bedtime story, ep 4
    blacklistVideoIds.add("r4wI_7i3ixw"); // Alistair reads you a bedtime story, ep 5
    blacklistVideoIds.add("tJ28g77B0nE"); // Le Dessus des Cartes E01
    blacklistVideoIds.add("vGb5UyBz6y0"); // Le Dessus des Cartes E02
    blacklistVideoIds.add("4qfVdFNNcGc"); // Le Dessus des Cartes E03
    blacklistVideoIds.add("zfEuZN0Rlew"); // Giro
  }

  /**
   * Fetch all upcoming and live show ids.
   *
   * <p>
   * EXECUTED: once an hour
   * COST: 100 x 2
   * </p>
   */
  public static Set<String> getCurrentAndUpcomingLiveShowIds() throws IOException {
    YouTube youtubeService = getYouTubeApi();

    // get all live videos
    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(50L)
        .setChannelId(ApiManager.getChannelId())
        .setType("video")
        .setEventType("live")
        .execute();

    Set<String> videoIds = new HashSet<>();

    for (SearchResult result : response.getItems()) {
      videoIds.add(result.getId().getVideoId());
    }

    // get all upcoming livestreamVideos videos
    request = youtubeService.search()
        .list("id");
    response = request
        .setMaxResults(50L)
        .setChannelId(ApiManager.getChannelId())
        .setType("video")
        .setEventType("upcoming")
        .execute();

    for (SearchResult result : response.getItems()) {
      videoIds.add(result.getId().getVideoId());
    }

    // get all upcoming premiere videos
    videoIds.addAll(getUpcomingPremieres());

    return videoIds;
  }

  /**
   * Get information for multiple videos.
   *
   * <p>
   * EXECUTED: once an hour
   * COST: 1
   * -- and --
   * EXECUTED: 37 times an hour (ideally as much as possible)
   * COST: 1
   * -- and --
   * EXECUTED: 1-2 times on track id call (if
   * COST: 1
   * TOTAL COST 38-40 an hour.
   * </p>
   *
   * @param videoIds Collection of video ids to fetch more information about
   * @return List of Video object containing the goods.
   * @throws IOException Thrown if there is an issue with the YouTube API
   */
  public static List<Video> getUpcomingShowDetails(Collection<String> videoIds)
      throws IOException {

    if (videoIds == null || videoIds.size() == 0) {
      return new ArrayList<>();
    }

    if (videoIds.size() > 50) {
      logger.warn("Video content list is too big to fetch in one call, trimming video id list...");
      Collection<String> trimmedVideoIds = new ArrayList<>();

      for (String id : videoIds) {
        trimmedVideoIds.add(id);

        if (trimmedVideoIds.size() >= 50) {
          break;
        }
      }

      videoIds = trimmedVideoIds;
    }

    YouTube youtubeService = getYouTubeApi();
    // Define and execute the API request

    YouTube.Videos.List request = youtubeService.videos()
        .list("liveStreamingDetails,contentDetails,snippet");

    VideoListResponse response = request
        .setId(String.join(",", videoIds))
        .execute();

    logger.trace("Fetched details about multiple videos, count:{}", videoIds.size());

    return response.getItems();
  }

  /**
   * Get information for multiple videos.
   *
   * <p>
   * Useful for archived videos because it fetches the duration.
   * </p>
   *
   * <p>
   * A known limitation is that it will only work on 50 ids at a time, but a channel will rarely
   * have 50 live and upcoming livestreams registered so that's never an issue.
   * </p>
   *
   * @param videoIds Collection of video ids to fetch more information about
   * @return List of Video object containing content details.
   * @throws IOException Thrown if there is an issue with the YouTube API
   */
  public static List<Video> getVideoContentDetails(Collection<String> videoIds)
      throws IOException {

    if (videoIds == null || videoIds.size() == 0) {
      return new ArrayList<>();
    }

    if (videoIds.size() > 50) {
      logger.warn("Video content list is too big to fetch in one call, trimming video id list...");
      Collection<String> trimmedVideoIds = new ArrayList<>();

      for (String id : videoIds) {
        trimmedVideoIds.add(id);

        if (trimmedVideoIds.size() >= 50) {
          break;
        }
      }

      videoIds = trimmedVideoIds;
    }

    YouTube youtubeService = getYouTubeApi();
    // Define and execute the API request

    YouTube.Videos.List request = youtubeService.videos()
        .list("contentDetails,snippet");

    VideoListResponse response = request
        .setId(String.join(",", videoIds))
        .execute();

    logger.info("Fetched details about multiple videos, count:{}", videoIds.size());

    return response.getItems();
  }


  /**
   * Fetch completed show and randomize.
   */
  public static List<ArchivedVideo> getCompletedShows()
      throws IOException {

    if (ApiManager.isArchivePlaylistActive()) {
      return getCompletedShowsUsingArchivePlaylist();
    } else {
      return getCompletedShowsUsingChannelSearch();
    }
  }

  /**
   * Get upload playlist id of a channel
   *
   * @returns upload playlist id for channel or null if unsuccessful.
   */
  public static String getUploadPlaylistId() throws IOException {

    YouTube youtubeService = getYouTubeApi();

    YouTube.Channels.List request = youtubeService.channels()
        .list("contentDetails");
    ChannelListResponse response = request.setId(ApiManager.getChannelId()).execute();
    List<Channel> channelList = response.getItems();

    if (channelList.size() > 0) {
      return channelList.get(0).getContentDetails().getRelatedPlaylists().getUploads();
    } else {
      return null;
    }
  }

  public static List<String> getUpcomingPremieres() throws IOException {

    List<List<String>> videoIdBatches = new ArrayList<>();

    YouTube youtubeService = getYouTubeApi();

    YouTube.PlaylistItems.List request = youtubeService.playlistItems()
        .list("contentDetails,snippet");

    boolean moreArchiveRemaining = true;

    do {
      PlaylistItemListResponse response =
          request
              .setMaxResults(50L)
              .setPlaylistId(ApiManager.getUploadPlaylistId())
              .execute();

      List<String> videoIds = new ArrayList<>();
      for (PlaylistItem item : response.getItems()) {
        videoIds.add(item.getContentDetails().getVideoId());
        logger.debug("id: {} title: {}", item.getId(), item.getSnippet().getTitle());
      }

      videoIdBatches.add(videoIds);

      if (response.getNextPageToken() != null) {
        request.setPageToken(response.getNextPageToken());
      } else {
        moreArchiveRemaining = false;
      }

    } while (moreArchiveRemaining);

    logger.info("Fetched {} batches of upcoming premiere shows", videoIdBatches.size());

    List<String> upcomingPremiereIds = new ArrayList<>();

    for (List<String> batch : videoIdBatches) {

      List<Video> videoDetails = YouTubeService.getUpcomingShowDetails(batch);

      for (Video v : videoDetails) {

        if (v.getLiveStreamingDetails() != null) {

          DateTime dt = v.getLiveStreamingDetails().getScheduledStartTime();

          if (dt != null) {
            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime scheduled = getDateTime(dt);

            if (now.isBefore(scheduled)) {
              logger.debug("FOUND UPCOMING PREMIERE VIDEO: {} ", v.getSnippet().getTitle());
              upcomingPremiereIds.add(v.getId());
            }
          }
        }
      }
    }

    return upcomingPremiereIds;

  }

  /**
   * Fetch completed show and randomize.
   *
   * <p>
   * Note that the playlistItem's contentDetails does not include duration so I have to get that
   * with seperate batched calls for maximum efficiency.
   * </p>
   *
   * @return List of ArchiveVideo shows
   */
  public static List<ArchivedVideo> getCompletedShowsUsingArchivePlaylist()
      throws IOException {

    List<List<String>> videoIdBatches = new ArrayList<>();

    YouTube youtubeService = getYouTubeApi();

    YouTube.PlaylistItems.List request = youtubeService.playlistItems()
        .list("contentDetails,snippet");

    boolean moreArchiveRemaining = true;

    do {
      PlaylistItemListResponse response =
          request
              .setMaxResults(50L)
              .setPlaylistId(ApiManager.getArchivePlaylistId())
              .execute();

      List<String> videoIds = new ArrayList<>();
      for (PlaylistItem item : response.getItems()) {
        videoIds.add(item.getContentDetails().getVideoId());
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

    long pastStreamedShowsDuration = 0;
    long pastStreamedShowsCount = 0;
    long archivePlayerDuration = 0;
    long archivePlayerCount = 0;
    for (List<String> batch : videoIdBatches) {

      List<Video> videoDetails = YouTubeService.getVideoContentDetails(batch);

      for (Video v : videoDetails) {
        long duration = Duration.parse(v.getContentDetails().getDuration()).getSeconds();
        pastStreamedShowsDuration += duration;
        pastStreamedShowsCount++;

        archivePlayerDuration += duration;
        archivePlayerCount++;
        archivedVideos.add(new ArchivedVideo(v.getId(), v.getSnippet().getTitle(), duration,
            v.getSnippet().getThumbnails().getStandard()));

      }
    }

    logger.info("Total count of Efferalgang past streamed shows: {} shows", pastStreamedShowsCount);
    logger.info("Total count of shows in the archive player:     {} shows", archivePlayerCount);
    logger.info("Total duration of Efferalgang past streamed shows: {} hours",
        RadioPlayerUtils.printDurationInHours(pastStreamedShowsDuration));
    logger.info("Total duration of shows in the archive player:     {} hours",
        RadioPlayerUtils.printDurationInHours(archivePlayerDuration));

    Collections.shuffle(archivedVideos);

    return archivedVideos;
  }

  /**
   * This is a very expensive way of building the backlog. Every call requires 100 quota. It doesn't
   * always even produce the same results.
   *
   * @return List of ArchiveVideo shows
   */
  @Deprecated
  public static List<ArchivedVideo> getCompletedShowsUsingChannelSearch()
      throws IOException {

    List<List<String>> videoIdBatches = new ArrayList<>();

    YouTube youtubeService = getYouTubeApi();

    // Define and execute the API request
    YouTube.Search.List request = youtubeService.search()
        .list("id")
        .setMaxResults(50L)
        .setPart("id")
        .setChannelId(ApiManager.getChannelId())
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

    long pastStreamedShowsDuration = 0;
    long pastStreamedShowsCount = 0;
    long archivePlayerDuration = 0;
    long archivePlayerCount = 0;
    for (List<String> batch : videoIdBatches) {
      List<Video> videoDetails = YouTubeService.getVideoContentDetails(batch);
      for (Video v : videoDetails) {
        long duration = Duration.parse(v.getContentDetails().getDuration()).getSeconds();
        pastStreamedShowsDuration += duration;
        pastStreamedShowsCount++;

        if (blacklistVideoIds.contains(v.getId()) == false) {
          archivePlayerDuration += duration;
          archivePlayerCount++;
          archivedVideos.add(new ArchivedVideo(v.getId(), v.getSnippet().getTitle(), duration,
              v.getSnippet().getThumbnails().getStandard()));
        } else {
          logger.info("Skipping {} for the archive", v.getSnippet().getTitle());
        }
      }
    }

    logger.info("Total count of Efferalgang past streamed shows: {} shows", pastStreamedShowsCount);
    logger.info("Total count of shows in the archive player:     {} shows", archivePlayerCount);
    logger.info("Total duration of Efferalgang past streamed shows: {} hours",
        RadioPlayerUtils.printDurationInHours(pastStreamedShowsDuration));
    logger.info("Total duration of shows in the archive player:     {} hours",
        RadioPlayerUtils.printDurationInHours(archivePlayerDuration));

    Collections.shuffle(archivedVideos);

    return archivedVideos;
  }

  /**
   * Search for upcoming shows.
   *
   * @return List of found upcoming shows
   * @throws IOException Thrown if there is an issue with the YouTube API
   */
  @Deprecated
  public static List<String> getUpcomingShowIds() throws IOException {

    YouTube youtubeService = getYouTubeApi();
    // Define and execute the API request

    YouTube.Search.List request = youtubeService.search()
        .list("id");
    SearchListResponse response = request
        .setMaxResults(5L)
        .setOrder("date")
        .setChannelId(ApiManager.getChannelId())
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
}
