package com.example.hiring.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cv_data")
public class CvData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private Users user;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "linkedin")
    private String linkedin;

    @Column(name = "github")
    private String github;

    @Column(name = "photo_path")
    private String photoPath;

    @Column(name = "cv_path")
    private String cvPath;

    @Column(name = "experiences", columnDefinition = "JSON")
    private String experiences;

    @Column(name = "projects", columnDefinition = "JSON")
    private String projects;

    @Column(name = "skills", columnDefinition = "JSON")
    private String skills;

    @Column(name = "education", columnDefinition = "JSON")
    private String education;

    @Column(name = "is_validated")
    private boolean isValidated;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public CvData() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Users getUser() { return user; }
    public void setUser(Users user) { this.user = user; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }
    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }
    public String getExperiences() { return experiences; }
    public void setExperiences(String experiences) { this.experiences = experiences; }
    public String getProjects() { return projects; }
    public void setProjects(String projects) { this.projects = projects; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public boolean isIsValidated() { return isValidated; }
    public void setIsValidated(boolean isValidated) { this.isValidated = isValidated; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}