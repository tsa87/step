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

  public static void main(String[] args) {
      final Collection<Event> NO_EVENTS = Collections.emptySet();
      final Collection<String> NO_ATTENDEES = Collections.emptySet();

    // Some people that we can use in our tests.
      final String PERSON_A = "Person A";
      final String PERSON_B = "Person B";
      final String PERSON_C = "Person C";

    // All dates are the first day of the year 2020.
      final int TIME_0800AM = TimeRange.getTimeInMinutes(8, 0);
      final int TIME_0830AM = TimeRange.getTimeInMinutes(8, 30);
      final int TIME_0900AM = TimeRange.getTimeInMinutes(9, 0);
      final int TIME_0930AM = TimeRange.getTimeInMinutes(9, 30);
      final int TIME_1000AM = TimeRange.getTimeInMinutes(10, 0);
      final int TIME_1100AM = TimeRange.getTimeInMinutes(11, 00);

      final int DURATION_30_MINUTES = 30;
      final int DURATION_60_MINUTES = 60;
      final int DURATION_90_MINUTES = 90;
      final int DURATION_1_HOUR = 60;
      final int DURATION_2_HOUR = 120;

      FindMeetingQuery query = new FindMeetingQuery();

      Collection<Event> events = Arrays.asList(
          new Event("Event 1", TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
              Arrays.asList(PERSON_A)),
          new Event("Event 2", TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
              Arrays.asList(PERSON_B)),
          new Event("Event 3", TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true),
              Arrays.asList(PERSON_C)));

      MeetingRequest request =
          new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);
    
      request.addOptionalAttendee(PERSON_C);

      Collection<TimeRange> actual = query.query(events, request);
      Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

      for (TimeRange timeRange : actual) {
        System.out.println(timeRange);
      }

      System.out.println("-----");

      for (TimeRange timeRange : expected) {
        System.out.println(timeRange);
      }
  }


  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();
    long duration = request.getDuration();


    // Sorted Map:
    // Key [Int] represents StartTime
    // Value [Int] represents score to schedule event at this time.
    //    -1000000 if at least 1 required attendee is not free.
    //    -x       if x optional attendees are not free
    TreeMap<Integer, Integer> timeCutoffs = new TreeMap<>();

    // Initalize Time Cutoff with 0's
    timeCutoffs.put(TimeRange.START_OF_DAY, 0);

    for (Event event : events) {
      int startTime = event.getWhen().start();
      int endTime = event.getWhen().end();

    	timeCutoffs.put(startTime, 0);
      timeCutoffs.put(endTime, 0);
    }

    
	  for (Event event : events) {
      int startTime = event.getWhen().start();
      int endTime = event.getWhen().end();

      int unavaiableOptionalAttendeeCount = countAttendeeOverlap(event.getAttendees(), optionalAttendees);
      int unavaiableMandatoryAttendeeCount = countAttendeeOverlap(event.getAttendees(), attendees);

      // Modify the the time ranges with penalties
      if ((unavaiableOptionalAttendeeCount > 0) || (unavaiableMandatoryAttendeeCount > 0)) {

        SortedMap<Integer, Integer> timeSlots = timeCutoffs.subMap(startTime, endTime);
        
        // Build list of keys to avoid concurrent edit
        ArrayList<Integer> unavaiableTimes = new ArrayList<>();
        for (Map.Entry<Integer,Integer> entry : timeSlots.entrySet()) {
          Integer key = entry.getKey();
          unavaiableTimes.add(key);
        }

        for (Integer key : unavaiableTimes) {
          int value = timeCutoffs.get(key);

          if (unavaiableMandatoryAttendeeCount > 0) {
            value -= LARGE_INT; // Most severe penalty if manadatory attendee cannot come.
          } else {
            value -= unavaiableOptionalAttendeeCount;
          }

          System.out.println(key + "has" + value);

          timeCutoffs.put(key, value);
        }

      }
    }

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

        System.out.println(lastTimeSlot + "new has" + lastKeyScore);

        availableTimes.add(TimeRange.fromStartEnd(lastTimeSlot, key, isInclusive));
        timeSlotScore.add(lastKeyScore);
      }
      
      lastTimeSlot = key;
      lastKeyScore = score;
    }

    for (int score : timeSlotScore) System.out.println(score);

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
