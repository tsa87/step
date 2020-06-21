package com.google.sps;

import com.google.sps.TimeRange;
import java.util.Comparator;
import java.util.HashSet;

/**
 * Class inherits all properties of TimeRange class, and in addition, stores
 * the availability infomation of the attendees. 
 */
public class TimeRangeAttendance extends TimeRange {

	public Boolean isAllMandatoryGuestFree;
	public Integer numOptionalGuestUnavailable;

	// avoid double count of optional guest if they are 
	// involved in 2+ events during the same period
	public HashSet<String> unavailableOptionalGuestList; 

	public TimeRangeAttendance(int start, int duration) {
		super(start, duration);

		isAllMandatoryGuestFree = true;
		numOptionalGuestUnavailable = 0;
		unavailableOptionalGuestList = new HashSet<>();
	}

	public static TimeRangeAttendance fromStartEnd(int start, int end, boolean inclusive) {
		return inclusive ? new TimeRangeAttendance(start, end - start + 1) : new TimeRangeAttendance(start, end - start);
	}

	public static final Comparator<TimeRangeAttendance> ORDER_BY_START = new Comparator<TimeRangeAttendance>() {
		@Override
		public int compare(TimeRangeAttendance a, TimeRangeAttendance b) {
			return Long.compare(a.start(), b.start());
		}
	};

    @Override
    public String toString() {
        return String.format("Range: [%d, %d)", start(), end()) + 
            " Mandatory Guest Free: " + isAllMandatoryGuestFree +
            " Num Optional Guest Unavailable: " + numOptionalGuestUnavailable;
    }

	// public void setNumOptionalGuestAvailable(int count) {
	// 	if (count >= 0) {
	// 		numOptionalGuestAvailable = count;
	// 	}		
	// }

	// public void setIsAllMandatoryGuestFree(bool isFree) {
	// 	isAllMandatoryGuestFree = isFree;
	// }

}