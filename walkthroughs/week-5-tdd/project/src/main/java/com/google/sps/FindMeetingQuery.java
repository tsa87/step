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
import com.google.sps.TimeRangeManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;  
import java.util.Map;
import java.util.SortedMap;      
import java.util.TreeMap;
import org.javatuples.Pair;

public final class FindMeetingQuery {

  static final int UNAVAILABLE_GUEST_PENALTY = -1000000;
  static final int UNAVAILABLE_OPTIONAL_GUEST_PENALTY = -1;

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long duration = request.getDuration();

    int mandatoryAttendeeCount = attendees.size();
    int optionalAttendeeCount = optionalAttendees.size();

    // Initalize a dictionary <start time, availability score>
    // StartTime (Integer) used as a key over TimeRange because subMap operation is much easier later on.
    TreeMap<Integer, Integer> timeCutoffs = getTimeCutoffs(events);

    // Penalize each time periods score based on the availability of attendees.
    TreeMap<Integer, Integer> timeCutoffScored = scoreTimeCutoffs(timeCutoffs, events, attendees, optionalAttendees);
	  
    // Covert timeCutOffs startTime to a TimeRange
    ArrayList<Pair<TimeRange, Integer>> timeRangeScorePairList = treeMapToTimeRangeScorePairList(timeCutoffScored);
    
    // Find the best availability score we can achieve for time periods equal or longer than the required duration.
    int bestAvailabilityScore = findBestAvailabilityScore(timeRangeScorePairList, duration);

    // Availability Score need to surpass the threshold
    // All mandatory guests must be able to attend.
    // If no mandatory guests, at least 1 optional attendee should be there
    int minimumScoreThreshold = UNAVAILABLE_GUEST_PENALTY + 1;
    if ((mandatoryAttendeeCount == 0) && (optionalAttendeeCount > 0)) {
        minimumScoreThreshold = optionalAttendeeCount*UNAVAILABLE_OPTIONAL_GUEST_PENALTY + 1;
    }
    if (bestAvailabilityScore < minimumScoreThreshold) {
        return new ArrayList<>();
    }

    // Filter Time Ranges with a score lower than the best availability score
    ArrayList<TimeRange> candidateTimeRangesFiltered = TimeRangeManager.filterScore(timeRangeScorePairList, bestAvailabilityScore);
   
    // Merge Time Ranges that are consecutive or overlap 
    ArrayList<TimeRange> candidateTimeRangesMerged = TimeRangeManager.mergeTimeRangeOverlap(candidateTimeRangesFiltered);

    // Remove Time Ranges shorter than the required duration
    ArrayList<TimeRange> candidateTimeRangesDuration = TimeRangeManager.filterDuration(candidateTimeRangesMerged, duration);

    return candidateTimeRangesDuration;
  }

  /* Count size of unions of the two attendee lists*/
  public static int countAttendeeOverlap(Collection<String> attendeeListA, Collection<String> attendeeListB) {
    int count = 0;
    for (String attendee : attendeeListA) {
      if (attendeeListB.contains(attendee)) {
        count++;
      }
    }
    return count;
  }

  /* Return the best availability score of a time period over the duration length from a list of TimeRanges */
  private int findBestAvailabilityScore(ArrayList<Pair<TimeRange, Integer>> timeRangeScorePairList, long duration) {

    int timeSegmentCount = timeRangeScorePairList.size();
    int bestAvailabilityScore = UNAVAILABLE_GUEST_PENALTY;

    for (int startTimeSlotIndex = 0; startTimeSlotIndex < timeSegmentCount; startTimeSlotIndex++) {

      long durationRemaining = duration;
      int minScore = 0;
      int currTimeSlotIndex = startTimeSlotIndex;
      
      while ((durationRemaining > 0) && (currTimeSlotIndex < timeSegmentCount)) {
        
        Pair<TimeRange, Integer> timeRangeScorePair = timeRangeScorePairList.get(currTimeSlotIndex);
        TimeRange timeRange = timeRangeScorePair.getValue0();
        int timeSlotScore = timeRangeScorePair.getValue1();
        
        // If event duration spans more than 1 time slot
        // We should record the lowest availabilty score of the span.
        minScore = Math.min(minScore, timeSlotScore);  

        int timeSlotStartTime = timeRange.start();
        int timeSlotEndTime = timeRange.end();
        int timeSlotLength = timeSlotEndTime - timeSlotStartTime;

        durationRemaining -= timeSlotLength;

        currTimeSlotIndex++;
      }

      // We reached the end of the day
      // but cannot find enough duration with a starting time of startTimeSlotIndex.
      if (durationRemaining > 0) minScore = UNAVAILABLE_GUEST_PENALTY;

      // Record the best score we have achieved.
      bestAvailabilityScore = Math.max(bestAvailabilityScore, minScore);
    }

    return bestAvailabilityScore;
  }

  /* Return a list of TimeRange and associated Availability Score Pair from the previous TreeMap */
  private ArrayList<Pair<TimeRange, Integer>> treeMapToTimeRangeScorePairList(TreeMap<Integer, Integer> timeCutoffs) {

    ArrayList<Pair<TimeRange, Integer>> timeRangeScorePairList = new ArrayList<>();

    Integer startTime = 0;
    Integer score = timeCutoffs.get(startTime); 

    // Edges padding for the subsequent forloop
    for (Map.Entry<Integer,Integer> entry : timeCutoffs.entrySet()) {
      Integer endTime = entry.getKey();
      Integer nextScore = entry.getValue();

      if (endTime > startTime) { //skip the first
        Boolean isInclusive = (endTime == TimeRange.END_OF_DAY);
        TimeRange timeRange = TimeRange.fromStartEnd(startTime, endTime, isInclusive);

        Pair<TimeRange, Integer> timeRangeScorePair = Pair.with(timeRange, score);
        timeRangeScorePairList.add(timeRangeScorePair);
      }
      
      startTime = endTime;
      score = nextScore;
    }

    return timeRangeScorePairList;
  }

  /**
    * Initalize a sorted dictionary of start time and the associated score
    * Example: 
    *   if from 00:00 ~ 01:35, 5 optional guest can't attend
    *   from 01:35 ~ 24:00 at least one mandatory guest can't attend
    *
    *   |-----(-5)-----|----------------(-100000)---------------|
    *   00:00        01:35                                    24:00
    *
    *   timeCutoffs: {0: -5, 95: -100000}
    *
    * @return timeCutoffs [Dict]  
    *           - Key [Int] represents StartTime
    *           - Value [Int] represents score to schedule event at this time
    */
  private TreeMap<Integer, Integer> getTimeCutoffs(Collection<Event> events) {
    
    TreeMap<Integer, Integer> timeCutoffs = new TreeMap<>();

    // Initalize Time Cutoff with 0's
    timeCutoffs.put(TimeRange.START_OF_DAY, 0);
    timeCutoffs.put(TimeRange.END_OF_DAY+1, 0);

    for (Event event : events) {
      int startTime = event.getWhen().start();
      int endTime = event.getWhen().end();

      timeCutoffs.put(startTime, 0);
      timeCutoffs.put(endTime, 0);
    }

    return timeCutoffs;
  }

  /* Return a scored TreeMap based on the Final Penalties*/
  private TreeMap<Integer, Integer> scoreTimeCutoffs(
      TreeMap<Integer, Integer> timeCutoffs,
      Collection<Event> events,
      Collection<String> attendees,
      Collection<String> optionalAttendees
    ) {

    for (Event event : events) {
      int startTime = event.getWhen().start();
      int endTime = event.getWhen().end();

      int unavailableOptionalAttendeeCount = countAttendeeOverlap(event.getAttendees(), optionalAttendees);
      int unavailableMandatoryAttendeeCount = countAttendeeOverlap(event.getAttendees(), attendees);

      // Modify the the time ranges with penalties
      if ((unavailableOptionalAttendeeCount > 0) || (unavailableMandatoryAttendeeCount > 0)) {

        SortedMap<Integer, Integer> eventTimePeriod = timeCutoffs.subMap(startTime, endTime);
        
        // Build list of keys to avoid concurrent edit
        ArrayList<Integer> affectedTimes = new ArrayList<>();
        for (Map.Entry<Integer,Integer> entry : eventTimePeriod.entrySet()) {
          Integer cutoffStartTime = entry.getKey();
          affectedTimes.add(cutoffStartTime);
        }

        for (Integer cutoffStartTime : affectedTimes) {
          int score = timeCutoffs.get(cutoffStartTime);

          if (unavailableMandatoryAttendeeCount > 0) {
            // Most severe penalty if manadatory attendee cannot come.
            score += UNAVAILABLE_GUEST_PENALTY; 
          } else {
            // -1 penalty per optional attendee
            score += unavailableOptionalAttendeeCount * UNAVAILABLE_OPTIONAL_GUEST_PENALTY; 
          }

          timeCutoffs.put(cutoffStartTime, score);
        }
      }
    }
    return timeCutoffs;
  }

}
