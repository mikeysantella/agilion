package dataengine.workers;

import dataengine.apis.ProgressState;

public interface HasProgress {
  ProgressState getProgress();
}
