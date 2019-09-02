package ar.com.localizart.android.report.service;

import java.io.IOException;
import java.util.ArrayList;

import android.util.EventLog;
import android.util.Log;

public class PowerClickEvent {
	private static final int TIME_INTERVAL = 5000;
	private static final int TOTAL_CLICKS = 3;
	private final String EVENT_LOG_TAG_POWER_SCREEN_STATE = "power_screen_state";
	private int IS_POWER_STATE_ASLEEP = 0;
	private final int IS_STATE_CHANGE_USER_TRIGERRED = 2;

	private Long firstEventTime;
	private int clicks = 0;
	private boolean isActivated;

	public void reset() {
		firstEventTime = null;
		clicks = 0;
	}

	public void registerClick(Long eventTime) {
		if (isFirstClick() || notWithinLimit(eventTime)
				|| !isPowerClickBecauseOfUser()) {
			Log.v(">>>>>>", "PowerClickEvent click within limits");
			resetClicks(eventTime);
			return;
		} else {
			clicks++;
			Log.v(">>>>>>", "PowerClickEvent clicks = " + clicks);
			if (clicks >= TOTAL_CLICKS) {
				isActivated = true;
				return;
			}
		}
	}

	private void resetClicks(Long eventTime) {
		firstEventTime = eventTime;
		clicks = 1;
		Log.v(">>>>>>", "PowerClickEvent Reset clicks = " + clicks);
	}

	private boolean notWithinLimit(long current) {
		return (current - firstEventTime) > TIME_INTERVAL;
	}

	private boolean isFirstClick() {
		return firstEventTime == null;
	}

	public boolean isActivated() {
		return isActivated;
	}

	// TODO: move this to a class like PowerStateEventLogReader
	// - event logs can't be read post Android 4.1
	private boolean isPowerClickBecauseOfUser() {
		ArrayList<EventLog.Event> events = new ArrayList<EventLog.Event>();
		try {
			int powerScreenStateTagCode = EventLog
					.getTagCode(EVENT_LOG_TAG_POWER_SCREEN_STATE);
			EventLog.readEvents(new int[] { powerScreenStateTagCode }, events);
			if (!events.isEmpty()) {
				EventLog.Event event = events.get(events.size() - 1);
				try {
					Object[] powerEventLogData = (Object[]) event.getData();
					if (powerEventLogData.length > 2
							&& (Integer) powerEventLogData[0] == IS_POWER_STATE_ASLEEP) {
						boolean isPowerChangeUserTriggered = (Integer) powerEventLogData[1] == IS_STATE_CHANGE_USER_TRIGERRED;
						Log.v(">>>>>> is Power change user triggered? ", ""
								+ isPowerChangeUserTriggered);
						return isPowerChangeUserTriggered;
					}
				} catch (ClassCastException ce) {
					Object data = event.getData();
					Log.v(">>>>>>", "could not read event data" + data);
				}
			}
		} catch (IOException e) {
			Log.v(">>>>>>", "could not read event logs");
		}
		return true;
	}

}
