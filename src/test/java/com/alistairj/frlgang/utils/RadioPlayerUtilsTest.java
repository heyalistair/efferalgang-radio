package com.alistairj.frlgang.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Alistair Jones (alistair@ohalo.co)
 */
public class RadioPlayerUtilsTest {

  @Test
  public void testParseVideoId() {
    String id = RadioPlayerUtils.parseVideoId("http://youtu.be/dQw4w9WgXcQ");
    Assert.assertEquals("dQw4w9WgXcQ", id);
  }

  @Test
  public void testParseVideoId_httpsAndParams() {
    String id = RadioPlayerUtils.parseVideoId("http://www.youtube.com/watch?v=dQw4w9WgXcQ&a=GxdCwVVULXctT2lYDEPllDR0LRTutYfW");
    Assert.assertEquals("dQw4w9WgXcQ", id);
  }

  @Test
  public void testParseVideoId_studio() {
    String id = RadioPlayerUtils.parseVideoId("https://studio.youtube.com/video/WNMfvKiYtiw/livestreaming");
    Assert.assertEquals("WNMfvKiYtiw", id);
  }

}
