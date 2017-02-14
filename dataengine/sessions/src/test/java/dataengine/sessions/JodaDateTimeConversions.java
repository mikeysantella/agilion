package dataengine.sessions;

import static org.junit.Assert.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.joda.time.DateTime;
import org.junit.Test;

public class JodaDateTimeConversions {

  @Test
  public void test() {
    OffsetDateTime offsetDateTime=OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
    DateTime jdt = SessionDB_FrameHelper.toJodaDateTime(offsetDateTime);
    
    OffsetDateTime odt = SessionDB_FrameHelper.toOffsetDateTime(jdt);
    assertEquals(offsetDateTime.toString(), odt.toString());
  }

}
