package com.google.sps;

import com.google.sps.TimeRange;
import com.google.sps.TimeRangeAttendance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.javatuples.Pair;

/* TimeRangeManager is a collection of TimeRange that represents available/taken timeslots during a day */ 
public class TimeRangeManager {

  public static ArrayList<TimeRange> mergeTimeRangeOverlap(Collection<TimeRange> timeRanges) {
    
    // Shallow copy of timeRanges
    ArrayList<TimeRange> mergedTimeRanges = new ArrayList<>();

    for (TimeRange timeRange : timeRanges) {

      if (!isMergeable(mergedTimeRanges, timeRange)) {
        mergedTimeRanges.add(timeRange);
      } 
      // Merge the overlaps
      else { 
        int size = mergedTimeRanges.size();
        TimeRange lastTimeRange = mergedTimeRanges.get(size - 1);

        int newStart = Math.min(lastTimeRange.start(), timeRange.start());
        int newEnd = Math.max(lastTimeRange.end(), timeRange.end());

        Boolean isInclusive = (newEnd == TimeRange.END_OF_DAY);

        TimeRange mergedTimeRange = TimeRange.fromStartEnd(newStart, newEnd, isInclusive);
        
        mergedTimeRanges.set(size - 1, mergedTimeRange); 
      }
    }

    return mergedTimeRanges; 
  }

  public static ArrayList<TimeRange> filterScore(
    ArrayList<TimeRangeAttendance> timeRangeAttendanceListScored,
    int maxOptionalGuestUnavailableCount
  ) {

    ArrayList<TimeRange> result = new ArrayList<>();

    for (TimeRangeAttendance timeRangeAttendance : timeRangeAttendanceListScored) {
			if (timeRangeAttendance.isAllMandatoryGuestFree) {
				if (timeRangeAttendance.numOptionalGuestUnavailable <= maxOptionalGuestUnavailableCount) {
					result.add(timeRangeAttendance);
				}
			}
    }

    return result;
  }

  public static ArrayList<TimeRange> filterDuration(ArrayList<TimeRange> timeRanges, long duration) {
    ArrayList<TimeRange> result = new ArrayList<>();
    for (TimeRange timeRange : timeRanges) {
        if ((timeRange.end() - timeRange.start()) >= duration) {
            result.add(timeRange);
        }
    }
    return result;
  }

  /* Determine if new timerange ((overlaps with) or (is consecutive to)) the last one */
  private static Boolean isMergeable(ArrayList<TimeRange> timeRanges, TimeRange timeRange) {

    int size = timeRanges.size();

    // No overlap if the list is empty.
    if (size == 0) {
      return false;
    } 
    
    TimeRange lastTimeRange = timeRanges.get(size - 1);
    return (lastTimeRange.overlaps(timeRange) || (lastTimeRange.end() == timeRange.start()));
  }

}