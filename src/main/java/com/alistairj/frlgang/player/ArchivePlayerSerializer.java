package com.alistairj.frlgang.player;

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
public class ArchivePlayerSerializer extends StdSerializer<ArchivePlayer> {

  public ArchivePlayerSerializer() {
    super(null, true);
  }

  public ArchivePlayerSerializer(Class<ArchivePlayer> t) {
    super(t);
  }

  @Override
  public void serialize(ArchivePlayer rp, JsonGenerator g, SerializerProvider provider)
      throws IOException {
    g.writeStartObject();

    ArchivedVideo av = rp.getCurrentVideo();
    g.writeObjectFieldStart("current");
    g.writeNumberField("playhead", rp.getCurrentPlayheadInSeconds());
    g.writeStringField("id", av.getId());
    g.writeStringField("title", av.getTitle());
    g.writeNumberField("duration_in_seconds", av.getDurationInSeconds());
    g.writeEndObject();

    rp.generateQueueJson(g);

    g.writeEndObject();
  }

}