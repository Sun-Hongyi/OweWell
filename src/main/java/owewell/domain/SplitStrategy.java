package owewell.domain;

import java.util.Map;

public interface SplitStrategy {
    Map<UserId, Money> split(Money total);
}
