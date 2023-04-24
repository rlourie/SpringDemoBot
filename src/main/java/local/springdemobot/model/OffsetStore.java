package local.springdemobot.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Getter
@Setter
@Component
public class OffsetStore {
    private Long offset = 0L;

    public Optional<Long> tryReadOffset() {
        return Optional.ofNullable(offset);
    }
}
