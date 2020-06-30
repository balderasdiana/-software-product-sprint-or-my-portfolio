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

public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    
    ArrayList<TimeRange> busyTimeRanges = new ArrayList();
    ArrayList<TimeRange> freeTimeRanges = new ArrayList();

    // No attendees, free the whole day
    if(request.getAttendees().isEmpty()){
        freeTimeRanges.add(TimeRange.WHOLE_DAY);
        return freeTimeRanges;
    }

   
    // Meeting last more than a day, there are other free times 
    if(request.getDuration() > TimeRange.WHOLE_DAY.duration()){
        return freeTimeRanges;
    }

    busyTimeRanges.addAll(getTimesRequestedAttendeesAreBusy(events, request));

    if(busyTimeRanges.size() > 0){
        Collections.sort(busyTimeRanges, TimeRange.ORDER_BY_START);

        findFreeTimeBeforeFirstMeeting(busyTimeRanges, freeTimeRanges, request);
        findFreeTimeInBetweenMeetings(busyTimeRanges, freeTimeRanges, request);
        findFreeTimeAfterLastMeeting(busyTimeRanges, freeTimeRanges, request);

    } else {
        // Not busy, free the whole day
        freeTimeRanges.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true));
    }

    return freeTimeRanges;
  }


    private ArrayList<TimeRange> getTimesRequestedAttendeesAreBusy(Collection<Event> events, MeetingRequest request){
        ArrayList<TimeRange> busyTimes = new ArrayList();
        for(Event event : events){
            // Add busy time, when attending an event
            if(!Collections.disjoint(event.getAttendees(), request.getAttendees())){
                busyTimes.add(event.getWhen());
            }
        }

        return busyTimes;
    }

    private void findFreeTimeBeforeFirstMeeting(ArrayList<TimeRange> busyTimeRanges, ArrayList<TimeRange> freeTimeRanges, MeetingRequest request){
        if((busyTimeRanges.get(0).start() - TimeRange.START_OF_DAY) >= request.getDuration()){
            freeTimeRanges.add(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, busyTimeRanges.get(0).start(), false));
        }
    }

    private void findFreeTimeInBetweenMeetings(ArrayList<TimeRange> busyTimeRanges, ArrayList<TimeRange> freeTimeRanges, MeetingRequest request){
        for(int i = 0; i < busyTimeRanges.size()-1; i++){
            if(busyTimeRanges.get(i+1).start() - busyTimeRanges.get(i).end() >= request.getDuration()){
                freeTimeRanges.add(TimeRange.fromStartEnd(busyTimeRanges.get(i).end(), busyTimeRanges.get(i+1).start(),false));
            }
        }
    }

    private void findFreeTimeAfterLastMeeting(ArrayList<TimeRange> busyTimeRanges, ArrayList<TimeRange> freeTimeRanges, MeetingRequest request){
        int endOfLastMeeting = 0;

        for(TimeRange tr : busyTimeRanges){
            endOfLastMeeting = tr.end() > endOfLastMeeting ? tr.end() : endOfLastMeeting;
        }

        if((TimeRange.END_OF_DAY - endOfLastMeeting) >= request.getDuration()){
            freeTimeRanges.add(TimeRange.fromStartEnd(endOfLastMeeting, TimeRange.END_OF_DAY,true));
        }
    }
}
