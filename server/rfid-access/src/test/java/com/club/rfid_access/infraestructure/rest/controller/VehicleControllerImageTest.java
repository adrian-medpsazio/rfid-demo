package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.domain.service.StorageService;
import com.club.rfid_access.infraestructure.persistence.entity.VehicleEntity;
import com.club.rfid_access.infraestructure.persistence.repository.MemberRepository;
import com.club.rfid_access.infraestructure.persistence.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
class VehicleControllerImageTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleRepository vehicleRepository;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private StorageService storageService;

    @Test
    void uploadImage_invalidFormat_shouldReturn400() throws Exception {
        var vehicle = createVehicle(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        var file = new MockMultipartFile("file", "test.gif", "image/gif", "fake".getBytes());

        mockMvc.perform(multipart("/api/v1/vehicles/1/image").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid image format. Only JPEG and PNG are allowed"));
    }

    @Test
    void uploadImage_vehicleNotFound_shouldReturn404() throws Exception {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        var file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake".getBytes());

        mockMvc.perform(multipart("/api/v1/vehicles/99/image").file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadImage_validJpeg_shouldReturn200() throws Exception {
        var vehicle = createVehicle(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(storageService.uploadWithThumbnail(anyString(), any())).thenReturn("vehicles/1/image.jpg");
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        var file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake-image-data".getBytes());

        mockMvc.perform(multipart("/api/v1/vehicles/1/image").file(file))
                .andExpect(status().isOk());
    }

    @Test
    void getImage_noImage_shouldReturn404() throws Exception {
        var vehicle = createVehicle(1L);
        vehicle.setImageKey(null);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        mockMvc.perform(get("/api/v1/vehicles/1/image"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImage_withImage_shouldReturn302Redirect() throws Exception {
        var vehicle = createVehicle(1L);
        vehicle.setImageKey("vehicles/1/image.jpg");
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(storageService.getPresignedUrl("vehicles/1/image.jpg"))
                .thenReturn("http://minio.test/rfid/vehicles/1/image.jpg?token=abc");

        mockMvc.perform(get("/api/v1/vehicles/1/image"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://minio.test/rfid/vehicles/1/image.jpg?token=abc"));
    }

    private static VehicleEntity createVehicle(Long id) {
        var v = new VehicleEntity();
        v.setId(id);
        v.setPlate("ABC-123");
        v.setCreatedAt(Instant.now());
        return v;
    }
}
