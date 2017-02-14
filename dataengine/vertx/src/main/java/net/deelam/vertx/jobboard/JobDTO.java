package net.deelam.vertx.jobboard;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Data
@ToString
public class JobDTO {
  String id;
  String type;

  public JobDTO(String id, String type) {
    this.id = id;
    this.type = type;
  }

  //  String inputPath, outputPath;

  String requesterAddr; // Vertx eventbus address; job worker can register itself to this address
  int progressPollInterval;

  Object request; // job-specific parameters

}
