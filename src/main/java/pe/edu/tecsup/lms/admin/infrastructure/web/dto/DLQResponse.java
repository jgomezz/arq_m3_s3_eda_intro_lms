package pe.edu.tecsup.lms.admin.infrastructure.web.dto;

import lombok.Builder;
import lombok.Data;
import pe.edu.tecsup.lms.shared.infrastructure.dlq.FailedEvent;

import java.util.List;

//@Data
@Builder
public class DLQResponse {
    private List<FailedEvent> failedEvents;
}
