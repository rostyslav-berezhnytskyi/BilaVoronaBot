package com.telegram.bilavorona.util;

import com.telegram.bilavorona.model.User;

public interface AiMessageLimitValidator {
    boolean checkAIMessageLimit(User user, int limit);
}
