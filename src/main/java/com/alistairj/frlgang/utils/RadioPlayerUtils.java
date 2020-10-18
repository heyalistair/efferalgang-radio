package com.alistairj.frlgang.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.LiveBroadcast;
import com.google.api.services.youtube.model.Video;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Radio play general utils.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class RadioPlayerUtils {

  private static final Logger logger = LoggerFactory.getLogger(RadioPlayerUtils.class);

  private static final String DEFAULT_IMG =
      "https://img.discogs.com/oAOHL8Zan84rK6JiRUs60breU68=/fit-in/600x601/filters:strip_icc():format(jpeg):mode_rgb():quality(90)/discogs-images/R-3471433-1331680516.jpeg.jpg";

  private static final int UPCOMING_START_BEFORE_MINUTES = 2;

  private static final int UPCOMING_END_AFTER_MINUTES = 10;

  private RadioPlayerUtils() {
    // private constructor
  }

  /**
   * Print some nice log.
   *
   * @param upcomers The upcoming videos to print.
   */
  public static void printUpcomingShows(List<LiveBroadcast> upcomers) {
    if (upcomers.size() > 0) {
      printUpcomingShow(upcomers.get(0), "1st");
    }
    if (upcomers.size() > 1) {
      printUpcomingShow(upcomers.get(1), "2nd");
    }
    if (upcomers.size() > 2) {
      printUpcomingShow(upcomers.get(2), "3rd");
    }
  }

  private static void printUpcomingShow(LiveBroadcast video, String ordinal) {
    logger.debug("{} upcoming video scheduled at:{}, id:{}, name:{}", ordinal,
        showHasStartTime(video)
            ? video.getSnippet().getScheduledStartTime().toStringRfc3339() : "null",
        video.getId(), video.getSnippet().getTitle());
  }

  private static boolean showHasStartTime(LiveBroadcast video) {
    return video.getSnippet() != null
        && video.getSnippet().getScheduledStartTime() != null;
  }

  public static void writeVideoInfo(JsonGenerator g, Video v) throws IOException {
    g.writeStringField("id", v.getId());
    g.writeStringField("title", v.getSnippet().getTitle());
    try {
      g.writeStringField("scheduled_at",
          v.getLiveStreamingDetails().getScheduledStartTime().toStringRfc3339());
    } catch (NullPointerException e) {
      g.writeStringField("scheduled_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
          .format(new Date()));
    }

    g.writeObjectFieldStart("thumbnail");

    try {
      g.writeStringField("url", v.getSnippet().getThumbnails().getStandard().getUrl());
      g.writeNumberField("w", v.getSnippet().getThumbnails().getStandard().getWidth());
      g.writeNumberField("h", v.getSnippet().getThumbnails().getStandard().getHeight());

    } catch (NullPointerException e) {
      g.writeStringField("url", DEFAULT_IMG);
      g.writeNumberField("w", 600);
      g.writeNumberField("h", 600);
    }
    g.writeEndObject();
  }

  public static void writeVideoInfo(JsonGenerator g, LiveBroadcast v) throws IOException {
    g.writeStringField("id", v.getId());
    g.writeStringField("title", v.getSnippet().getTitle());
    try {
      g.writeStringField("scheduled_at",
          v.getSnippet().getScheduledStartTime().toStringRfc3339());
    } catch (NullPointerException e) {
      g.writeStringField("scheduled_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
          .format(new Date()));
    }

    g.writeObjectFieldStart("thumbnail");

    try {
      g.writeStringField("url", v.getSnippet().getThumbnails().getStandard().getUrl());
      g.writeNumberField("w", v.getSnippet().getThumbnails().getStandard().getWidth());
      g.writeNumberField("h", v.getSnippet().getThumbnails().getStandard().getHeight());

    } catch (NullPointerException e) {
      g.writeStringField("url", DEFAULT_IMG);
      g.writeNumberField("w", 600);
      g.writeNumberField("h", 600);
    }
    g.writeEndObject();
  }

  /**
   * Examines the scheduled times in the list to see if there is a video that is starting
   * imminently.
   *
   * @param upcomingList The list of videos sorted by scheduled time.
   * @return video Upcoming video, or null if there is none.
   */
  public static LiveBroadcast isUpcomingVideoPending(List<LiveBroadcast> upcomingList) {
    if (upcomingList == null) {
      return null;
    }

    for (LiveBroadcast v : upcomingList) {
      DateTime dt = v.getSnippet().getScheduledStartTime();
      ZonedDateTime scheduled = getDateTime(dt);
      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime fiveMinutesBefore = scheduled.minusMinutes(UPCOMING_START_BEFORE_MINUTES);
      ZonedDateTime fiveMinutesAfter = scheduled.plusMinutes(UPCOMING_END_AFTER_MINUTES);

      if (now.isBefore(fiveMinutesAfter) && now.isAfter(fiveMinutesBefore)) {
        return v;
      }
    }

    return null;
  }

  public static boolean isFirstVideoScheduledAfterSecond(Video v1, Video v2) {

    DateTime video1time = v1.getLiveStreamingDetails().getScheduledStartTime();
    DateTime video2time = v2.getLiveStreamingDetails().getScheduledStartTime();

    if (video1time == null) {
      return false;
    }

    if (video2time == null) {
      return true;
    }

    ZonedDateTime d1 = getDateTime(video1time);
    ZonedDateTime d2 = getDateTime(video2time);

    return d1.isAfter(d2);
  }

  /**
   * Has a live video ended. It must have at least the "liveStreamingDetails" and "contentDetails"
   * defined.
   *
   * @param v Video to check
   * @return true if it has ended.
   */
  public static boolean hasLiveVideoEnded(Video v) {

    if (v.getContentDetails().getDuration().equals("P0D")) {  // it is a live stream

      return v.getLiveStreamingDetails().getActualEndTime() != null;

    } else { // it is a premiere or the weird direct livestream bug in youtube

      if (v.getLiveStreamingDetails().getActualEndTime() != null) {
        return true;
      }

      logger
          .debug("Checking premiere video id:{} to see if it has ended", v.getSnippet().getTitle());
      DateTime dt = v.getLiveStreamingDetails().getScheduledStartTime();
      long duration = Duration.parse(v.getContentDetails().getDuration()).getSeconds();

      // + 2 minutes because of youtube's cheesy countdown
      ZonedDateTime scheduledEndTime = getDateTime(dt).plusMinutes(2L).plusSeconds(duration);
      ZonedDateTime now = ZonedDateTime.now();

      return now.isAfter(scheduledEndTime);
    }
  }

  public static boolean isVideoLive(Video v) {

    if (v.getContentDetails().getDuration().equals("P0D")) {  // it is a live stream

      return v.getLiveStreamingDetails().getActualStartTime() != null;

    } else { // it is a premiere

      DateTime dt = v.getLiveStreamingDetails().getScheduledStartTime();

      ZonedDateTime now = ZonedDateTime.now();
      ZonedDateTime scheduledStartTime = getDateTime(dt);

      return now.isAfter(scheduledStartTime);
    }
  }

  public static ZonedDateTime getDateTime(DateTime dt) {
    Instant instant;
    if (dt == null) {
      instant = Instant.now();
    } else {
      instant = Instant.ofEpochSecond(dt.getValue() / 1000);
    }
    return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
  }

  public static String printDurationInHours(long seconds) {
    return String.format("%.2f", (seconds / 3600f));
  }

  public static String YOUTUBE_URL_PATTERN =
      "(?<=watch\\?v=|/videos/|/video/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*";
  public static Pattern COMPILED_PATTERN = Pattern.compile(YOUTUBE_URL_PATTERN);

  /**
   * Returns the video id part of a youtube url.
   *
   * <p>
   * It is based on
   * <p>
   * Should handle:
   * http://www.youtube.com/watch?v=dQw4w9WgXcQ&a=GxdCwVVULXctT2lYDEPllDR0LRTutYfW
   * http://www.youtube.com/watch?v=dQw4w9WgXcQ
   * http://youtu.be/dQw4w9WgXcQ
   * http://www.youtube.com/embed/dQw4w9WgXcQ
   * http://www.youtube.com/v/dQw4w9WgXcQ
   * http://www.youtube.com/e/dQw4w9WgXcQ
   * http://www.youtube.com/watch?v=dQw4w9WgXcQ
   * http://www.youtube.com/watch?feature=player_embedded&v=dQw4w9WgXcQ
   * http://www.youtube-nocookie.com/v/6L3ZvIMwZFM?version=3&hl=en_US&rel=0
   * https://studio.youtube.com/video/WNMfvKiYtiw/livestreaming
   * </p>
   *
   * @param urlOrId
   * @return String
   */
  public static String parseVideoId(String urlOrId) {

    if (urlOrId == null) {
      return "";
    }

    urlOrId = urlOrId.trim();

    // example of an id is "qAvSXHxE_SM"
    if (urlOrId.length() == 11) {
      return urlOrId;
    }

    Matcher matcher = COMPILED_PATTERN
        .matcher(urlOrId); //url is youtube url for which you want to extract the id.
    if (matcher.find()) {
      return matcher.group().substring(0, 11);
    }

    return "";
  }
}
