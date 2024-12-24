package com.example.hiring.quizModule.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document(collection = "quizzes")
public class QuizModal {
        @Id
        private String id;
        private String title;
        private List<Question> questions;

        public static class Question {
                private String id = UUID.randomUUID().toString();
                private String text;
                private String type; // "radio", "checkbox", "text"
                private List<Option> options;
                private int points;

                public static class Option {
                        private String id = UUID.randomUUID().toString();
                        private String label;
                        private boolean isCorrect;

                        // Getters et Setters
                        public String getId() {
                                return id;
                        }

                        public void setId(String id) {
                                this.id = id;
                        }

                        public String getLabel() {
                                return label;
                        }

                        public void setLabel(String label) {
                                this.label = label;
                        }

                        public boolean isCorrect() {
                                return isCorrect;
                        }

                        public void setCorrect(boolean correct) {
                                isCorrect = correct;
                        }
                }

                // Getters et Setters
                public String getId() {
                        return id;
                }

                public void setId(String id) {
                        this.id = id;
                }

                public String getText() {
                        return text;
                }

                public void setText(String text) {
                        this.text = text;
                }

                public String getType() {
                        return type;
                }

                public void setType(String type) {
                        this.type = type;
                }

                public List<Option> getOptions() {
                        return options;
                }

                public void setOptions(List<Option> options) {
                        this.options = options;
                }

                public int getPoints() {
                        return points;
                }

                public void setPoints(int points) {
                        this.points = points;
                }
        }

        // Getters et Setters
        public String getId() {
                return id;
        }

        public void setId(String id) {
                this.id = id;
        }

        public String getTitle() {
                return title;
        }

        public void setTitle(String title) {
                this.title = title;
        }

        public List<Question> getQuestions() {
                return questions;
        }

        public void setQuestions(List<Question> questions) {
                this.questions = questions;
        }
}