package com.notification.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Value Object representing notification content
 * Follows Immutability principle for data integrity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationContent {
    
    private String subject;
    private String body;
    private String template;
    
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
    
    /**
     * Personalize content with user-specific data
     */
    public String personalizeContent(Map<String, String> userVariables) {
        String personalizedBody = body;
        if (userVariables != null) {
            for (Map.Entry<String, String> entry : userVariables.entrySet()) {
                String placeholder = "{{" + entry.getKey() + "}}";
                personalizedBody = personalizedBody.replace(placeholder, entry.getValue());
            }
        }
        return personalizedBody;
    }
    
    public void addMetadata(String key, String value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
}
