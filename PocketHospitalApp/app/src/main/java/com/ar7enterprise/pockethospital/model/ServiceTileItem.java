package com.ar7enterprise.pockethospital.model;

import android.view.View;
import android.widget.ImageView;

public class ServiceTileItem {
    private String name;
    private String description;
    private int icon;

    public ServiceTileItem(String name, String description, int icon) {
        this.name = name;
        this.description = description;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }
}
