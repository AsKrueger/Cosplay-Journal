package com.cosplayjournal.application.port.out;

import com.cosplayjournal.application.dto.ExternalEventData;

import java.util.List;

public interface ExternalEventSourcePort {
    List<ExternalEventData> fetchEvents();
}
