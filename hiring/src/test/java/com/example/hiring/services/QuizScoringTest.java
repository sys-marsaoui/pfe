package com.example.hiring.services;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuizScoringTest {

    @Test
    void acceptsScoreAtSeventyPercentThreshold() {
        assertTrue(QuizScoring.isPassingScore(7, 10));
    }

    @Test
    void rejectsScoreBelowThreshold() {
        assertFalse(QuizScoring.isPassingScore(6, 10));
    }

    @Test
    void zeroTotalCannotPass() {
        assertFalse(QuizScoring.isPassingScore(0, 0));
        assertEquals(0.0, QuizScoring.percentage(0, 0));
    }
}
