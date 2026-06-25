package com.club.rfid_access.infraestructure.rest.controller;

import com.club.rfid_access.domain.service.StorageService;
import com.club.rfid_access.infraestructure.persistence.entity.MemberEntity;
import com.club.rfid_access.infraestructure.persistence.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
class MemberControllerPhotoTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberRepository memberRepository;

    @MockitoBean
    private StorageService storageService;

    @Test
    void uploadPhoto_invalidFormat_shouldReturn400() throws Exception {
        var member = createMember(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        var file = new MockMultipartFile("file", "test.gif", "image/gif", "fake-image".getBytes());

        mockMvc.perform(multipart("/api/v1/members/1/photo").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid image format. Only JPEG and PNG are allowed"));
    }

    @Test
    void uploadPhoto_oversize_shouldReturn413() throws Exception {
        var member = createMember(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        byte[] bigFile = new byte[11 * 1024 * 1024];
        var file = new MockMultipartFile("file", "test.jpg", "image/jpeg", bigFile);

        mockMvc.perform(multipart("/api/v1/members/1/photo").file(file))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void uploadPhoto_memberNotFound_shouldReturn404() throws Exception {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        var file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "fake".getBytes());

        mockMvc.perform(multipart("/api/v1/members/99/photo").file(file))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPhoto_memberNotFound_shouldReturn404() throws Exception {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/members/99/photo"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPhoto_noPhoto_shouldReturn404() throws Exception {
        var member = createMember(1L);
        member.setPhotoUrl(null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        mockMvc.perform(get("/api/v1/members/1/photo"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPhoto_storageUnavailable_shouldReturn503() throws Exception {
        var member = createMember(1L);
        member.setPhotoUrl("members/1/photo.jpg");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(storageService.getPresignedUrl("members/1/photo.jpg")).thenReturn(null);

        mockMvc.perform(get("/api/v1/members/1/photo"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void getPhoto_withPhoto_shouldReturn302Redirect() throws Exception {
        var member = createMember(1L);
        member.setPhotoUrl("members/1/photo.jpg");
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(storageService.getPresignedUrl("members/1/photo.jpg"))
                .thenReturn("http://minio.test/rfid/members/1/photo.jpg?token=abc");

        mockMvc.perform(get("/api/v1/members/1/photo"))
                .andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, "http://minio.test/rfid/members/1/photo.jpg?token=abc"));
    }

    private static MemberEntity createMember(Long id) {
        var m = new MemberEntity();
        m.setId(id);
        m.setFirstName("Test");
        m.setLastName("User");
        m.setActive(true);
        m.setCreatedAt(Instant.now());
        m.setUpdatedAt(Instant.now());
        return m;
    }
}
