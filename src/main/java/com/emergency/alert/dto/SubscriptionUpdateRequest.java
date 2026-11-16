package com.emergency.alert.dto;

import com.emergency.alert.model.AlertCategory;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

public class SubscriptionUpdateRequest {

    @NotNull(message = "Categories set is required")
    private Set<AlertCategory> categories = new HashSet<>();

    public SubscriptionUpdateRequest() {}

    public SubscriptionUpdateRequest(Set<AlertCategory> categories) {
        this.categories = categories;
    }

    public Set<AlertCategory> getCategories() { return categories; }
    public void setCategories(Set<AlertCategory> categories) { this.categories = categories; }
}
