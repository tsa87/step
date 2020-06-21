// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.sps;

import com.google.sps.Event;
import com.google.sps.TimeRange;
import com.google.sps.TimeRangeAttendance;
import com.google.sps.TimeRangeManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;


public final class FindMeetingQuery {

  public Collection < TimeRange > query(Collection < Event > events, MeetingRequest request) {

    Collection < String > attendees = request.getAttendees();
    Collection < String > optionalAttendees = request.getOptionalAttendees();
    long duration = request.getDuration();

    Integer mandatoryAttendeeCount = attendees.size();
    Integer optionalAttendeeCount = optionalAttendees.size();

    // Initalize a list of time ranges with attendance infomation
    ArrayList < TimeRangeAttendance > timeRangeAttendanceList = 
      getTimeRangeAttendanceList(events, attendees, optionalAttendees);

    // minmumLoss is the number of optional guests unavailable if all mandatory guest can attend
    // minmumLoss is positive infinity if mandatory guest cannot attend
    Integer minimumLoss =
      getMinimumTimeRangeLossMetric(timeRangeAttendanceList, duration);

    // Availability Score need to surpass the threshold
    // All mandatory guests must be able to attend.
    // If no mandatory guests, at least 1 optional attendee should be there
    Integer maxLossThreshold = Integer.MAX_VALUE;
    if ((mandatoryAttendeeCount == 0) && (optionalAttendeeCount > 0)) {
      maxLossThreshold = optionalAttendeeCount;
    }
    if (minimumLoss >= maxLossThreshold) {
      return new ArrayList < > ();
    }

    // Filter Time Ranges with a score higher than the minimum loss.
    ArrayList < TimeRange > candidateTimeRangesFiltered = 
      TimeRangeManager.filterScore(timeRangeAttendanceList, minimumLoss);

    // Merge Time Ranges that are consecutive or overlap 
    ArrayList < TimeRange > candidateTimeRangesMerged = TimeRangeManager.mergeTimeRangeOverlap(candidateTimeRangesFiltered);

    // Remove Time Ranges shorter than the required duration
    ArrayList < TimeRange > candidateTimeRangesDuration = TimeRangeManager.filterDuration(candidateTimeRangesMerged, duration);

    return candidateTimeRangesDuration;
  }

  /**
   * Returns a list of disjoInteger time ranges with initialised attendence value
   */
  private ArrayList < TimeRangeAttendance > getTimeRangeAttendanceList(
    Collection < Event > events,
    Collection < String > attendees,
    Collection < String > optionalAttendees
  ) {

    ArrayList < TimeRangeAttendance > timeRangeAttendanceList = new ArrayList < > ();

    TreeSet < Integer > significantEndTimes = new TreeSet < > ();

    significantEndTimes.add(TimeRange.END_OF_DAY);
    for (Event event: events) {
      Integer startTime = event.getWhen().start();
      Integer endTime = event.getWhen().end();

      significantEndTimes.add(startTime); // The commencement of a new period marks the end of another period.
      significantEndTimes.add(endTime);
    }

    Integer begin = TimeRange.START_OF_DAY;
    for (Integer end : significantEndTimes) {
      if (end > begin) {
        Boolean isInclusive = (end == TimeRange.END_OF_DAY) ? true : false;
        timeRangeAttendanceList.add(TimeRangeAttendance.fromStartEnd(begin, end, isInclusive));
      }
      begin = end;
    }

    Comparator < TimeRangeAttendance > c = TimeRangeAttendance.ORDER_BY_START;

    for (Event event: events) {
      Integer startTime = event.getWhen().start();
      Integer endTime = event.getWhen().end();

      Collection < String > unavailableOptionalAttendees = getAttendeeOverlap(event.getAttendees(), optionalAttendees);

      Integer unavailableOptionalAttendeeCount = unavailableOptionalAttendees.size();
      Integer unavailableMandatoryAttendeeCount = getAttendeeOverlap(event.getAttendees(), attendees).size();

      // Modify the the time ranges with penalties
      if ((unavailableOptionalAttendeeCount > 0) || (unavailableMandatoryAttendeeCount > 0)) {

        Integer startIndex =
          Collections.binarySearch(timeRangeAttendanceList, new TimeRangeAttendance(startTime, 0), c);
        Integer endIndex =
          Collections.binarySearch(timeRangeAttendanceList, new TimeRangeAttendance(endTime, 0), c);

        endIndex = endIndex < -1 ? Math.abs(endIndex) - 1 : endIndex;

        for (Integer index = startIndex; index < endIndex; index++) {

          TimeRangeAttendance timeRangeAttendance = timeRangeAttendanceList.get(index);

          if (unavailableMandatoryAttendeeCount > 0) {
            timeRangeAttendance.setIsAllMandatoryGuestFree(false);
          }
          for (String attendee: unavailableOptionalAttendees) {
            // avoid double counting an optional guest who signed up for 2 events in the same period
            if (!(timeRangeAttendance.isInUnavailableOptionalGuestList(attendee))) {
              timeRangeAttendance.incrementNumOptionalGuestUnavailable();
              timeRangeAttendance.addUnavailableOptionalGuest(attendee);
            }
          }

          timeRangeAttendanceList.set(index, timeRangeAttendance);
        }
      }
    }
    return timeRangeAttendanceList;
  }

  /* Return the best availability score of a time period over the duration length from a list of TimeRanges */
  private Integer getMinimumTimeRangeLossMetric(
    ArrayList < TimeRangeAttendance > timeRangeAttendanceListScored,
    long duration
  ) {

    Integer timeRangeCount = timeRangeAttendanceListScored.size();
    Integer minimumTimeRangeLoss = Integer.MAX_VALUE;

    for (Integer startIndex = 0; startIndex < timeRangeCount; startIndex++) {

      long durationRemaining = duration;
      Integer currentIndex = startIndex;

      Integer currentLoss = 0;
      Boolean isAllMandatoryGuestFree = true;

      while ((durationRemaining > 0) && (currentIndex < timeRangeCount)) {

        TimeRangeAttendance timeRangeAttendance = timeRangeAttendanceListScored.get(currentIndex);

        // If event duration spans more than 1 time slot
        // We should record the lowest availabilty score of the span.
        Integer optionalGuestUnavailableCount = timeRangeAttendance.getNumOptionalGuestUnavailable();
        currentLoss = Math.max(currentLoss, optionalGuestUnavailableCount);

        isAllMandatoryGuestFree =
          timeRangeAttendance.getIsAllMandatoryGuestFree() ? isAllMandatoryGuestFree : false;

        Integer timeSlotStartTime = timeRangeAttendance.start();
        Integer timeSlotEndTime = timeRangeAttendance.end();
        Integer timeSlotLength = timeSlotEndTime - timeSlotStartTime;

        durationRemaining -= timeSlotLength;

        currentIndex++;
      }

      // We reached the end of the day; If we cannot find enough duration 
      // with a starting time of TimeRange at startIndex or if not all
      // mandatory guest are free during some required TimeRanges.
      if ((durationRemaining > 0) || (!(isAllMandatoryGuestFree))) {
        currentLoss = Integer.MAX_VALUE;
      }

      // Record the minimum loss we have achieved.
      minimumTimeRangeLoss = Math.min(minimumTimeRangeLoss, currentLoss);
    }

    return minimumTimeRangeLoss;
  }

  /* Count size of unions of the two attendee lists*/
  public static Collection < String > getAttendeeOverlap(Collection < String > attendeeListA, Collection < String > attendeeListB) {
    Collection < String > overlap = new ArrayList < > ();

    for (String attendee: attendeeListA) {
      if (attendeeListB.contains(attendee)) {
        overlap.add(attendee);
      }
    }

    return overlap;
  }
}