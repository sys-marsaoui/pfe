package com.example.hiring.controllers;

import com.example.hiring.entities.CvData;
import com.example.hiring.entities.Users;
import com.example.hiring.entities.dto.CvDataDto;
import com.example.hiring.repositories.CvDataRepository;
import com.example.hiring.services.CvExtractionService;
import com.example.hiring.services.UserService;
import com.example.hiring.services.UserProgressService;
import com.example.hiring.services.GeminiCvService;
import com.example.hiring.services.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/profile")
public class CvUploadController {

    @Autowired
    private CvExtractionService cvExtractionService;

    @Autowired
    private UserService userService;

    @Autowired
    private CvDataRepository cvDataRepository;

    @Autowired
    private UserProgressService userProgressService;

    @Autowired
    private GeminiCvService geminiCvService;

    @Autowired
    private EmailServiceImpl emailService;

    @PostMapping("/generate-cv")
    public ResponseEntity<CvDataDto> generateCv(@RequestParam(required = false) String postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Users user = userService.getUserByEmail(authentication.getName());
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(geminiCvService.generateDraft(user, postId));
    }

    @PostMapping("/upload-cv")
    public ResponseEntity<?> uploadCv(@RequestParam("file") MultipartFile file) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Users user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            CvData existing = cvDataRepository.findByUser(user);
            if (existing != null && existing.isIsValidated()) {
                return ResponseEntity.badRequest().body("Cannot upload new CV, current CV is validated");
            }

            CvDataDto cvDataDto = cvExtractionService.extractCvData(file);
            String filePath = cvExtractionService.saveCvFile(file, user.getId().longValue());

            String oldPath = null;
            if (existing != null) {
                oldPath = existing.getCvPath();
            } else {
                existing = new CvData();
            }

            existing.setUser(user);
            existing.setName(cvDataDto.getName());
            existing.setEmail(cvDataDto.getEmail());
            existing.setPhone(cvDataDto.getPhone());
            existing.setAddress(cvDataDto.getAddress());
            existing.setLinkedin(cvDataDto.getLinkedin());
            existing.setGithub(cvDataDto.getGithub());
            existing.setCvPath(filePath);
            existing.setPhotoPath(cvDataDto.getPhotoPath());
            existing.setExperiences(cvDataDto.getExperiences());
            existing.setProjects(cvDataDto.getProjects());
            existing.setSkills(cvDataDto.getSkills());
            existing.setEducation(cvDataDto.getEducation());
            existing.setIsValidated(false);

            userService.saveCvData(existing);

            if (oldPath != null) {
                new File(oldPath).delete();
            }

            userProgressService.updateProgress(user, "CV_UPLOADED");

            return ResponseEntity.ok(cvDataDto);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Error processing CV file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error saving CV data: " + e.getMessage());
        }
    }

    @PutMapping("/update-cv")
    public ResponseEntity<?> updateCvData(@RequestBody CvDataDto cvDataDto) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Users user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            CvData cvData = cvDataRepository.findByUser(user);
            boolean isNewCv = cvData == null;
            if (cvData == null) {
                cvData = new CvData();
                cvData.setUser(user);
                cvData.setName(user.getFirstName() + " " + user.getLastName());
                cvData.setEmail(user.getEmail());
                cvData.setPhone(user.getTel());
                cvData.setAddress(user.getAddress());
                cvData.setIsValidated(false);
            }
            if (cvData.isIsValidated()) {
                return ResponseEntity.badRequest().body("Cannot edit validated CV");
            }

            cvData.setName(cvDataDto.getName());
            cvData.setEmail(cvDataDto.getEmail());
            cvData.setPhone(cvDataDto.getPhone());
            cvData.setAddress(cvDataDto.getAddress());
            cvData.setLinkedin(cvDataDto.getLinkedin());
            cvData.setGithub(cvDataDto.getGithub());
            cvData.setPhotoPath(cvDataDto.getPhotoPath());
            cvData.setExperiences(cvDataDto.getExperiences());
            cvData.setProjects(cvDataDto.getProjects());
            cvData.setSkills(cvDataDto.getSkills());
            cvData.setEducation(cvDataDto.getEducation());

            userService.saveCvData(cvData);
            if (isNewCv) {
                userProgressService.updateProgress(user, "CV_UPLOADED");
            }

            return ResponseEntity.ok(cvDataDto);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating CV data: " + e.getMessage());
        }
    }

    @PostMapping("/validate-cv")
    public ResponseEntity<?> validateCv() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Users user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            CvData cvData = cvDataRepository.findByUser(user);
            if (cvData == null) {
                return ResponseEntity.badRequest().body("No CV found");
            }
            if (cvData.isIsValidated()) {
                return ResponseEntity.badRequest().body("CV already validated");
            }

            cvData.setIsValidated(true);
            userService.saveCvData(cvData);

            userProgressService.updateProgress(user, "CV_VERIFIED");
            try {
                emailService.sendCvVerifiedEmail(user);
            } catch (Exception e) {
                System.err.println("CV verification email could not be sent: " + e.getMessage());
            }

            return ResponseEntity.ok("CV validated successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error validating CV: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete-cv")
    public ResponseEntity<?> deleteCv() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Users user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            CvData cvData = cvDataRepository.findByUser(user);
            if (cvData == null) {
                return ResponseEntity.badRequest().body("No CV found");
            }
            if (cvData.isIsValidated()) {
                return ResponseEntity.badRequest().body("Cannot delete validated CV");
            }

            String cvPath = cvData.getCvPath();
            cvDataRepository.delete(cvData);

            if (cvPath != null) {
                new File(cvPath).delete();
            }

            return ResponseEntity.ok("CV deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting CV: " + e.getMessage());
        }
    }

    @GetMapping("/cv-data/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCvData(@PathVariable int userId) {
        try {
            Users user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            CvData cvData = cvDataRepository.findByUser(user);
            if (cvData == null) {
                return ResponseEntity.ok(new CvDataDto());
            }

            CvDataDto cvDataDto = new CvDataDto();
            cvDataDto.setName(cvData.getName());
            cvDataDto.setEmail(cvData.getEmail());
            cvDataDto.setPhone(cvData.getPhone());
            cvDataDto.setAddress(cvData.getAddress());
            cvDataDto.setLinkedin(cvData.getLinkedin());
            cvDataDto.setGithub(cvData.getGithub());
            cvDataDto.setPhotoPath(cvData.getPhotoPath());
            cvDataDto.setCvPath(cvData.getCvPath());
            cvDataDto.setExperiences(cvData.getExperiences());
            cvDataDto.setProjects(cvData.getProjects());
            cvDataDto.setSkills(cvData.getSkills());
            cvDataDto.setEducation(cvData.getEducation());
            cvDataDto.setIsValidated(cvData.isIsValidated());

            return ResponseEntity.ok(cvDataDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving CV data: " + e.getMessage());
        }
    }

    @GetMapping("/download-cv/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadCv(@PathVariable int userId) {
        try {
            Users user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(null);
            }

            CvData cvData = cvDataRepository.findByUser(user);
            if (cvData == null || cvData.getCvPath() == null) {
                return ResponseEntity.badRequest().body(null);
            }

            File file = new File(cvData.getCvPath());
            if (!file.exists()) {
                return ResponseEntity.badRequest().body(null);
            }

            FileSystemResource resource = new FileSystemResource(file);
            String contentType = cvData.getCvPath().endsWith(".pdf") ? "application/pdf" : "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cv_user_" + userId + "." + (cvData.getCvPath().endsWith(".pdf") ? "pdf" : "docx") + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    @GetMapping("/cv-data")
    public ResponseEntity<?> getCurrentUserCvData() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Users user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            CvData cvData = cvDataRepository.findByUser(user);
            if (cvData == null) {
                return ResponseEntity.ok(new CvDataDto());
            }

            CvDataDto cvDataDto = new CvDataDto();
            cvDataDto.setName(cvData.getName());
            cvDataDto.setEmail(cvData.getEmail());
            cvDataDto.setPhone(cvData.getPhone());
            cvDataDto.setAddress(cvData.getAddress());
            cvDataDto.setLinkedin(cvData.getLinkedin());
            cvDataDto.setGithub(cvData.getGithub());
            cvDataDto.setPhotoPath(cvData.getPhotoPath());
            cvDataDto.setCvPath(cvData.getCvPath());
            cvDataDto.setExperiences(cvData.getExperiences());
            cvDataDto.setProjects(cvData.getProjects());
            cvDataDto.setSkills(cvData.getSkills());
            cvDataDto.setEducation(cvData.getEducation());
            cvDataDto.setIsValidated(cvData.isIsValidated());

            return ResponseEntity.ok(cvDataDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving CV data: " + e.getMessage());
        }
    }
}
