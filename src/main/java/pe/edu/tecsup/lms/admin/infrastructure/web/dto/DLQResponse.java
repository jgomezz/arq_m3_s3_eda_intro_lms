package pe.edu.tecsup.lms.admin.infrastructure.web.dto;

import lombok.Builder;
import lombok.Getter;
import pe.edu.tecsup.lms.shared.infrastructure.dlq.FailedEvent;

import java.util.List;

@Getter
@Builder
public class DLQResponse {
    private List<FailedEvent> failedEvents;
}
