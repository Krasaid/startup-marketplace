package com.startup.marketplace.controller;

import com.startup.marketplace.dto.ProfileDtos.ChangePasswordRequest;
import com.startup.marketplace.dto.ProfileDtos.ProfileResponse;
import com.startup.marketplace.dto.ProfileDtos.UpdateProfileRequest;
import com.startup.marketplace.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            profileService.getProfile(userDetails.getUsername()));
    }

    @PutMapping
    public ResponseEntity<ProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            profileService.updateProfile(userDetails.getUsername(), req));
    }

    @PatchMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        profileService.changePassword(userDetails.getUsername(), req);
        return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente"));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        profileService.deleteAccount(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
