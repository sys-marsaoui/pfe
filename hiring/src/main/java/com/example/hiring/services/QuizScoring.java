package com.example.hiring.services;

public final class QuizScoring {
    public static final double PASS_THRESHOLD_PERCENT = 70.0;

    private QuizScoring() {}

    public static boolean isPassingScore(int score, int totalScore) {
        return totalScore > 0 && score * 100.0 / totalScore >= PASS_THRESHOLD_PERCENT;
    }

    public static double percentage(int score, int totalScore) {
        return totalScore > 0 ? score * 100.0 / totalScore : 0.0;
    }
}
