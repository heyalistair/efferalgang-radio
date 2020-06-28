package com.alistairj.frlgang.player;

import static com.alistairj.frlgang.utils.RadioPlayerUtils.writeVideoInfo;

import com.alistairj.frlgang.player.archive.ArchivePlayer;
import com.alistairj.frlgang.player.archive.ArchivedVideo;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/**
 * Serialize FoundVideo into JSON.
 *
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class FoundVideoSerializer extends StdSerializer<FoundVideo> {

  public FoundVideoSerializer() {
    super(null, true);
  }

  public FoundVideoSerializer(Class<FoundVideo> t) {
    super(t);
  }

  @Override
  public void serialize(FoundVideo rp, JsonGenerator g, SerializerProvider provider)
      throws IOException {
    g.writeStartObject();

    g.writeObjectFieldStart("video");
    writeVideoInfo(g, rp.getVideo());
    g.writeBooleanField("is_new", rp.isNew());
    g.writeEndObject();

    g.writeEndObject();
  }
}