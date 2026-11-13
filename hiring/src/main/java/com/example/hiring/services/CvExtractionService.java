package com.example.hiring.services;

import com.example.hiring.entities.dto.CvDataDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CvExtractionService {

    private static final String[] EMAIL_PATTERNS = {
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    };

    private static final String[] PHONE_PATTERNS = {
            "\\+?\\d{1,3}[\\s-]?\\d{2,3}[\\s-]?\\d{6,8}"
    };

    private static final String[] LINKEDIN_PATTERNS = {
            "(?:https?://)?(?:www\\.)?linkedin\\.com/in/[a-zA-Z0-9-]+"
    };

    private static final String[] GITHUB_PATTERNS = {
            "(?:https?://)?(?:www\\.)?github\\.com/[a-zA-Z0-9-]+"
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CvDataDto extractCvData(MultipartFile file) throws IOException {
        CvDataDto cvData = new CvDataDto();
        String content = extractTextFromFile(file);

        cvData.setName(extractName(content));
        cvData.setEmail(extractEmail(content));
        cvData.setPhone(extractPhone(content));
        cvData.setAddress(extractAddress(content));
        cvData.setLinkedin(extractLinkedin(content));
        cvData.setGithub(extractGithub(content));
        cvData.setPhotoPath("");
        cvData.setExperiences(objectMapper.writeValueAsString(extractExperiences(content)));
        cvData.setProjects(objectMapper.writeValueAsString(extractProjects(content)));
        cvData.setSkills(objectMapper.writeValueAsString(extractSkills(content)));
        cvData.setEducation(objectMapper.writeValueAsString(extractEducation(content)));

        return cvData;
    }

    private String extractTextFromFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null) throw new IOException("File name is null");

        if (fileName.toLowerCase().endsWith(".pdf")) {
            return extractTextFromPdf(file.getInputStream());
        } else if (fileName.toLowerCase().endsWith(".docx")) {
            return extractTextFromDocx(file.getInputStream());
        } else {
            throw new IOException("Unsupported file format. Please upload PDF or DOCX");
        }
    }

    private String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        XWPFDocument document = new XWPFDocument(inputStream);
        StringBuilder text = new StringBuilder();
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            text.append(paragraph.getText()).append("\n");
        }
        document.close();
        return text.toString();
    }

    private String extractName(String content) {
        String[] lines = content.split("\\r?\\n");
        for (int i = 0; i < Math.min(5, lines.length); i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && line.matches("^[a-zA-Z\\s-]+$")) {
                return line; // Return exact match with original spacing
            }
        }
        return "Unknown";
    }

    private String extractEmail(String content) {
        for (String pattern : EMAIL_PATTERNS) {
            Pattern emailPattern = Pattern.compile(pattern);
            Matcher matcher = emailPattern.matcher(content);
            if (matcher.find()) return matcher.group().trim();
        }
        return "";
    }

    private String extractPhone(String content) {
        for (String pattern : PHONE_PATTERNS) {
            Pattern phonePattern = Pattern.compile(pattern);
            Matcher matcher = phonePattern.matcher(content);
            if (matcher.find()) {
                String phone = matcher.group().replaceAll("[\\s-]", "");
                if (phone.length() >= 8 && phone.length() <= 15) {
                    return phone;
                }
            }
        }
        return "";
    }

    private String extractAddress(String content) {
        Pattern addressPattern = Pattern.compile("([A-Za-zÀ-ÿ]{3,}),\\s*([A-Za-zÀ-ÿ]{3,})");
        Matcher matcher = addressPattern.matcher(content);

        while (matcher.find()) {
            String city = matcher.group(1).trim();
            String country = matcher.group(2).trim();

            if (city.length() >= 3 && city.length() <= 30 &&
                    country.length() >= 3 && country.length() <= 30 &&
                    !city.matches(".*[0-9@].*") &&
                    !country.matches(".*[0-9@].*")) {
                return city + ", " + country;
            }
        }
        return "";
    }

    private String extractLinkedin(String content) {
        for (String pattern : LINKEDIN_PATTERNS) {
            Pattern linkedinPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = linkedinPattern.matcher(content);
            if (matcher.find()) {
                String url = matcher.group().trim();
                if (!url.startsWith("http")) {
                    url = "https://" + url;
                }
                return url;
            }
        }
        return "";
    }

    private String extractGithub(String content) {
        for (String pattern : GITHUB_PATTERNS) {
            Pattern githubPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = githubPattern.matcher(content);
            if (matcher.find()) {
                String url = matcher.group().trim();
                if (!url.startsWith("http")) {
                    url = "https://" + url;
                }
                return url;
            }
        }
        return "";
    }

    private List<Map<String, String>> extractExperiences(String content) {
        List<Map<String, String>> experiences = new ArrayList<>();

        // Find Experiences section
        Pattern sectionPattern = Pattern.compile(
                "(?i)Expériences(.*?)(?=(?:Projets|$))",
                Pattern.DOTALL
        );
        Matcher sectionMatcher = sectionPattern.matcher(content);

        if (sectionMatcher.find()) {
            String expSection = sectionMatcher.group(1).trim();
            String[] lines = expSection.split("\\r?\\n");

            Map<String, String> currentExp = null;
            StringBuilder descriptionBuilder = new StringBuilder();
            String lastLine = "";

            for (String line : lines) {
                line = line.trim();

                if (line.isEmpty()) continue;

                // Detect position: starts with "Stagiaire"
                if (line.startsWith("Stagiaire")) {
                    if (currentExp != null) {
                        currentExp.put("description", descriptionBuilder.toString().trim());
                        experiences.add(currentExp);
                    }
                    currentExp = new HashMap<>();
                    currentExp.put("position", line);
                    descriptionBuilder = new StringBuilder();
                    continue;
                }

                // After position, next non-empty line is company
                if (currentExp != null && currentExp.get("company") == null && line.matches("[A-Za-zÀ-ÿ\\s&.-]+")) {
                    currentExp.put("company", line);
                    continue;
                }

                // After company, next is period if matches date pattern
                if (currentExp != null && currentExp.get("period") == null && line.matches("\\d{2}/\\d{4} - \\d{2}/\\d{4}")) {
                    currentExp.put("period", line);
                    continue;
                }

                // Rest is description
                if (currentExp != null) {
                    descriptionBuilder.append(line).append(" ");
                }
            }

            // Add last experience
            if (currentExp != null) {
                currentExp.put("description", descriptionBuilder.toString().trim());
                experiences.add(currentExp);
            }
        }

        return experiences;
    }

    private List<Map<String, String>> extractProjects(String content) {
        List<Map<String, String>> projects = new ArrayList<>();

        Pattern sectionPattern = Pattern.compile(
                "(?i)Projets(.*?)$",
                Pattern.DOTALL
        );
        Matcher sectionMatcher = sectionPattern.matcher(content);

        if (sectionMatcher.find()) {
            String projSection = sectionMatcher.group(1).trim();
            String[] lines = projSection.split("\\r?\\n");

            Pattern datePattern = Pattern.compile("^[A-ZÀ-Ÿ][a-zà-ÿ]+ \\d{4} - [A-ZÀ-Ÿ][a-zà-ÿ]+ \\d{4}$");

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();

                if (line.isEmpty()) continue;

                if (line.length() >10 && i +1 < lines.length && datePattern.matcher(lines[i +1].trim()).matches()) {
                    Map<String, String> proj = new HashMap<>();
                    proj.put("title", line);
                    i++;
                    proj.put("period", lines[i].trim());
                    StringBuilder desc = new StringBuilder();
                    for (i++; i < lines.length; i++) {
                        String descLine = lines[i].trim();
                        if (descLine.isEmpty()) continue;
                        if (descLine.length() >10 && i +1 < lines.length && datePattern.matcher(lines[i +1].trim()).matches()) {
                            break;
                        }
                        desc.append(descLine).append(" ");
                    }
                    proj.put("description", desc.toString().trim());
                    projects.add(proj);
                }
            }
        }

        return projects;
    }

    private List<String> extractSkills(String content) {
        List<String> skills = new ArrayList<>();

        Pattern sectionPattern = Pattern.compile(
                "(?i)Compétences(.*?)(?=Expériences)",
                Pattern.DOTALL
        );
        Matcher sectionMatcher = sectionPattern.matcher(content);

        if (sectionMatcher.find()) {
            String skillsSection = sectionMatcher.group(1).trim();
            String[] lines = skillsSection.split("\\r?\\n");

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || !line.contains(":")) continue;

                String[] categorySplit = line.split(":", 2);
                if (categorySplit.length < 2) continue;

                String items = categorySplit[1].trim();
                String[] itemList = items.split(",");

                for (String item : itemList) {
                    String skill = item.trim();
                    if (!skill.isEmpty()) {
                        skills.add(skill);
                    }
                }
            }
        }

        return skills;
    }

    private List<Map<String, String>> extractEducation(String content) {
        List<Map<String, String>> education = new ArrayList<>();

        Pattern sectionPattern = Pattern.compile(
                "(?i)Formation(.*?)(?=Compétences)",
                Pattern.DOTALL
        );
        Matcher sectionMatcher = sectionPattern.matcher(content);

        if (sectionMatcher.find()) {
            String eduSection = sectionMatcher.group(1).trim();
            String[] lines = eduSection.split("\\r?\\n");

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                Map<String, String> edu = new HashMap<>();

                Pattern periodPattern = Pattern.compile("(\\d{4}) - (\\d{4})$");
                Matcher periodMatcher = periodPattern.matcher(line);

                if (periodMatcher.find()) {
                    edu.put("period", periodMatcher.group(1) + " - " + periodMatcher.group(2));
                    edu.put("degree", line.substring(0, periodMatcher.start()).trim());
                } else {
                    edu.put("degree", line);
                }

                if (edu.get("degree") != null && !edu.get("degree").isEmpty()) {
                    education.add(edu);
                }
            }
        }

        return education;
    }

    public String saveCvFile(MultipartFile file, Long userId) throws IOException {
        String fileName = "cv_user_" + userId + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String uploadDir = "Uploads/cvs/";

        Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath);
        return filePath.toString();
    }
}