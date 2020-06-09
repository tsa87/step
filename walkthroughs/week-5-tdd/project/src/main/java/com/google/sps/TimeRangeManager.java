package com.google.sps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import com.google.sps.TimeRange;

/* TimeRangeManager is a collection of TimeRange that represents available/taken timeslots during a day */ 
public class TimeRangeManager {
	ArrayList<TimeRange> timeRanges;

  public TimeRangeManager() {
    timeRanges = new ArrayList<>();
  }

  public void add(TimeRange timeRange) {
    timeRanges.add(timeRange);
  }

  private ArrayList<TimeRange> mergeTimeRangeOverlap() {
    
    Collections.sort(timeRanges, TimeRange.ORDER_BY_START);

    // Shallow copy of timeRanges
    ArrayList<TimeRange> mergedTimeRanges = new ArrayList<>(timeRanges);

    for (TimeRange timeRange : timeRanges) {

      if (!overlapLastTimeRange(timeRange)) {
        mergedTimeRanges.add(timeRange);
      } 
      // Merge the overlaps
      else { 
        int size = mergedTimeRanges.size();
        TimeRange lastTimeRange = mergedTimeRanges.get(size - 1);

        int newStart = lastTimeRange.start();
        int newEnd = Math.max(lastTimeRange.end(), timeRange.end());

        TimeRange mergedTimeRange = TimeRange.fromStartEnd(newStart, newEnd, false);
        mergedTimeRanges.set(size - 1, mergedTimeRange); 
      }
    }

    return mergedTimeRanges; 
  }

  // Return the available time ranges (if the current list is taken timeslots)
  public Collection<TimeRange> invertTimeRanges(long minimumDuration) {

    ArrayList<TimeRange> invertTimeRangeManger = new ArrayList<>();

    int start = TimeRange.START_OF_DAY;
    int end = TimeRange.END_OF_DAY;

    ArrayList<TimeRange> mergedTimeRanges = mergeTimeRangeOverlap();

    for (TimeRange timeRange : mergedTimeRanges) {

      // The start of taken time slot == the end of time aval.
      if ((timeRange.start() - start) >= minimumDuration) {
        invertTimeRangeManger.add(TimeRange.fromStartEnd(start, timeRange.start(), false));
      }

      start = timeRange.end();
    }

    if ((end - start) > minimumDuration) {
      invertTimeRangeManger.add(TimeRange.fromStartEnd(start, end, true));
    }
    
    return invertTimeRangeManger;
  }

  /* Determine if new timerange overlaps with the last one */
  private Boolean overlapLastTimeRange(TimeRange timeRange) {

    int size = timeRanges.size();

    // No overlap if the list is empty.
    if (size == 0) {
      return false;
    } 
    
    TimeRange lastTimeRange = timeRanges.get(size - 1);
    return lastTimeRange.overlaps(timeRange);
  }

}