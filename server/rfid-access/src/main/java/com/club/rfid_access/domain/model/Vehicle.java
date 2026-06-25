package com.club.rfid_access.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    private String plate;
    private String brand;
    private String model;
    private String color;
}
