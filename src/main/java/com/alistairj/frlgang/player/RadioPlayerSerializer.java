package com.alistairj.frlgang.player;

import static com.alistairj.frlgang.utils.RadioPlayerUtils.writeVideoInfo;

import com.alistairj.frlgang.player.archive.ArchivePlayer;
import com.alistairj.frlgang.player.archive.ArchivedVideo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.api.services.youtube.model.Video;
import java.io.IOException;
import java.util.List;

/**
 * Serialize radio player status into JSON.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class RadioPlayerSerializer extends StdSerializer<RadioPlayer> {

  public RadioPlayerSerializer() {
    super(null, true);
  }

  public RadioPlayerSerializer(Class<RadioPlayer> t) {
    super(t);
  }

  @Override
  public void serialize(RadioPlayer rp, JsonGenerator g, SerializerProvider provider)
      throws IOException {
    g.writeStartObject();
    g.writeStringField("status", rp.getStatus().toString());

    ArchivePlayer ap = rp.getArchivePlayer();
    g.writeObjectFieldStart("archive_player");

    ArchivedVideo av = ap.getCurrentVideo();
    g.writeObjectFieldStart("current");
    g.writeNumberField("playhead", ap.getCurrentPlayheadInSeconds());
    g.writeStringField("id", av.getId());
    g.writeStringField("title", av.getTitle());
    g.writeNumberField("duration_in_seconds", av.getDurationInSeconds());
    g.writeEndObject();

    av = ap.peekNextVideo();
    g.writeObjectFieldStart("next");
    g.writeStringField("id", av.getId());
    g.writeStringField("title", av.getTitle());
    g.writeNumberField("duration_in_seconds", av.getDurationInSeconds());
    g.writeEndObject();

    g.writeEndObject(); // end archive_player

    LivePlayer lp = rp.getLivePlayer();
    g.writeObjectFieldStart("live_player");

    Video v = lp.getCurrentLiveVideo();
    if (v == null) {
      g.writeNullField("current");
    } else {
      g.writeObjectFieldStart("current");
      writeVideoInfo(g, v);
      g.writeEndObject();
    } // end live_player current

    List<Video> vs = lp.getUpcomingVideos();
    g.writeArrayFieldStart("upcoming");
    for (Video video : vs) {
      g.writeStartObject();
      writeVideoInfo(g, video);
      g.writeEndObject();
    }
    g.writeEndArray(); // end live_player upcoming
    g.writeEndObject(); // end live_player
    g.writeEndObject();
  }


}