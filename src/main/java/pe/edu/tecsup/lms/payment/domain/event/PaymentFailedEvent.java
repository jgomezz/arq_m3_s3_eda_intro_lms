package pe.edu.tecsup.lms.payment.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pe.edu.tecsup.lms.shared.domain.event.DomainEvent;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentFailedEvent extends DomainEvent {

    private String enrollmentId;
    private String reason;
    private String errorCode;
    private LocalDateTime timestamp;

    @Override
    public String getKey() {
        return enrollmentId;
    }
}
