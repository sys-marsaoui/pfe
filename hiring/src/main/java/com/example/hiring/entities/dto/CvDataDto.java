package com.example.hiring.entities.dto;

public class CvDataDto {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String linkedin;
    private String github;
    private String photoPath;
    private String cvPath;
    private String experiences; // JSON string
    private String projects; // JSON string
    private String skills; // JSON string
    private String education; // JSON string
    private boolean isValidated;

    public CvDataDto() {}

    public CvDataDto(String name, String email, String phone, String address, String linkedin, String github,
                     String photoPath, String cvPath, String experiences, String projects, String skills, String education, boolean isValidated) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.linkedin = linkedin;
        this.github = github;
        this.photoPath = photoPath;
        this.cvPath = cvPath;
        this.experiences = experiences;
        this.projects = projects;
        this.skills = skills;
        this.education = education;
        this.isValidated = isValidated;
    }

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
}