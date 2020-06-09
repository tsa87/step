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
import java.util.ArrayList;
import com.google.sps.TimeRange;

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {

	Collection<String> attendees =  request.getAttendees();
    long duration = request.getDuration();

    TimeRangeManager takenTimeManager = new TimeRangeManager();
 
    for (Event event : events) {
      if (attendeeOverlap(event.getAttendees(), attendees)) {
        takenTimeManager.add(event.getWhen());
      }
    }

    Collection<TimeRange> availableTimes = takenTimeManager.invertTimeRanges(duration);

    return availableTimes;
  }

  public static Boolean attendeeOverlap(Collection<String> attendeeListA, Collection<String> attendeeListB) {
    for (String attendee : attendeeListA) {
      if (attendeeListB.contains(attendee)) {
        return true;
      }
    }
    return false;
  }

}
