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

import java.util.Collection;
import java.util.Collections;
import java.util.List;  
import java.util.Map;
import java.util.TreeMap;
import java.util.SortedMap;      
import java.util.ArrayList;
import java.util.Arrays;

import com.google.sps.TimeRange;
import com.google.sps.TimeRangeManager;
import com.google.sps.Event;

public final class FindMeetingQuery {

  private final int LARGE_INT = 1000000;

  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long duration = request.getDuration();

    // Initalize a dictionary <start time, availability score>
    TreeMap<Integer, Integer> timeCutoffs = getTimeCutoffs(events);

    // Penalize each time periods score based on the availability of attendees.
    scoreTimeCutoffs(timeCutoffs, events, attendees, optionalAttendees);
	  
    ArrayList<TimeRange> availableTimes = new ArrayList<>();
    ArrayList<Integer> timeSlotScore = new ArrayList<>();

    // Edges padding for the subsequent forloop
    timeCutoffs.put(TimeRange.END_OF_DAY+1, 0);
    Integer lastTimeSlot = 0;
    Integer lastKeyScore = timeCutoffs.get(lastTimeSlot); 
    
    for (Map.Entry<Integer,Integer> entry : timeCutoffs.entrySet()) {
      Integer key = entry.getKey();
      Integer score = entry.getValue();

      if (key > lastTimeSlot) { //skip the first
        Boolean isInclusive = (key == TimeRange.END_OF_DAY);


        availableTimes.add(TimeRange.fromStartEnd(lastTimeSlot, key, isInclusive));
        timeSlotScore.add(lastKeyScore);
      }
      
      lastTimeSlot = key;
      lastKeyScore = score;
    }

    int timeSegmentCount = availableTimes.size();
    int maxScore = -1*LARGE_INT;

    for (int i = 0; i < timeSegmentCount; i++) {

      long durationRemaining = duration;
      int j = i;
      int minScore = 0;
      
      System.out.println(availableTimes.get(i));
      System.out.println(timeSlotScore.get(i));

      while ((durationRemaining > 0) && (j < timeSegmentCount)) {
        minScore = Math.min(minScore, timeSlotScore.get(j));  // Get min score
        durationRemaining -= (availableTimes.get(j).end() - availableTimes.get(j).start());
        
        // System.out.println(availableTimes.get(j) + " minscore " + minScore); 
        j ++;
      }
      if (durationRemaining > 0) minScore = -1*LARGE_INT;
      maxScore = Math.max(maxScore, minScore);
    }

    if (maxScore <= -1*LARGE_INT) {
      return new ArrayList<>();
    }

    ArrayList<TimeRange> availableTimesFiltered = TimeRangeManager.filterScore(availableTimes, timeSlotScore, maxScore);
    ArrayList<TimeRange> availableTimesMerged = TimeRangeManager.mergeTimeRangeOverlap(availableTimesFiltered);
    ArrayList<TimeRange> availableTimesDuration = TimeRangeManager.filterDuration(availableTimesMerged, duration);

    return availableTimesDuration;
  }

  // Initalize a sorted dictionary
  // 
  // Key {Int} represents StartTime
  // Value {Int} represents score to schedule event at this time.
  //    -1000000 <=> least 1 required attendee is not free.
  //    -x       <=> optional attendees are not free
  //
  // Example: 
  // if from 00:00 ~ 01:35, 5 optional guest can't attend
  // from 01:35 ~ 24:00 at least one mandatory guest can't attend
  //
  // |-----(-5)-----|----------------(-100000)---------------|
  // 00:00        01:35                                    24:00
  // 
  // timeCutoffs Dict: {0: -5, 95: -100000}
  private TreeMap<Integer, Integer> getTimeCutoffs(Collection<Event> events) {

    TreeMap<Integer, Integer> timeCutoffs = new TreeMap<>();

    // Initalize Time Cutoff with 0's
    timeCutoffs.put(TimeRange.START_OF_DAY, 0);

    for (Event event : events) {
      int startTime = event.getWhen().start();
      int endTime = event.getWhen().end();

      timeCutoffs.put(startTime, 0);
      timeCutoffs.put(endTime, 0);
    }

    return timeCutoffs;
  }

  // Penalize 100000 points if at least one mandatory guest can't attend
  // Penalize 1 point for each optional guest who can't attend
  private void scoreTimeCutoffs(
      TreeMap<Integer, Integer> timeCutoffs,
      Collection<Event> events,
      Collection<String> attendees,
      Collection<String> optionalAttendees
    ) {

    for (Event event : events) {
      int startTime = event.getWhen().start();
      int endTime = event.getWhen().end();

      int unavaiableOptionalAttendeeCount = countAttendeeOverlap(event.getAttendees(), optionalAttendees);
      int unavaiableMandatoryAttendeeCount = countAttendeeOverlap(event.getAttendees(), attendees);

      // Modify the the time ranges with penalties
      if ((unavaiableOptionalAttendeeCount > 0) || (unavaiableMandatoryAttendeeCount > 0)) {

        SortedMap<Integer, Integer> eventTimePeriod = timeCutoffs.subMap(startTime, endTime);
        
        // Build list of keys to avoid concurrent edit
        ArrayList<Integer> affectedTimes = new ArrayList<>();
        for (Map.Entry<Integer,Integer> entry : eventTimePeriod.entrySet()) {
          Integer cutoffStartTime = entry.getKey();
          affectedTimes.add(cutoffStartTime);
        }

        for (Integer cutoffStartTime : affectedTimes) {
          int score = timeCutoffs.get(cutoffStartTime);

          if (unavaiableMandatoryAttendeeCount > 0) {
            score -= LARGE_INT; // Most severe penalty if manadatory attendee cannot come.
          } else {
            score -= unavaiableOptionalAttendeeCount; // -1 penalty per optional attendee
          }

          timeCutoffs.put(cutoffStartTime, score);
        }
      }
    }
  }

  public static int countAttendeeOverlap(Collection<String> attendeeListA, Collection<String> attendeeListB) {
    int count = 0;
    
    for (String attendee : attendeeListA) {
      if (attendeeListB.contains(attendee)) {
        count++;
      }
    }

    return count;
  }

}
