package com.Habb.InventarisMSU.dto;

public class AvailabilityDTO {
    private Long itemId;
    private String itemName;
    private int available;

    public AvailabilityDTO() {
    }

    public AvailabilityDTO(Long id, String name, int av) {
        this.itemId = id;
        this.itemName = name;
        this.available = av;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }
}
