package com.example.library.exception;

import com.example.library.exception.messageError.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ErrorCodeRegistry {
    private final Map<String, BaseErrorCode> allCodes = new HashMap<>();

    @PostConstruct
    public void init() {
        register(ErrorCode.values());
        register(PermissionErrorCode.values());
        register(RoleErrorCode.values());
        register(UserErrorCode.values());
    }

    private void register(BaseErrorCode[] values) {
        for (BaseErrorCode v : values) {
            allCodes.put(((Enum<?>) v).name(), v);
        }
    }

    public BaseErrorCode find(String name) {
        return allCodes.get(name);
    }
}
