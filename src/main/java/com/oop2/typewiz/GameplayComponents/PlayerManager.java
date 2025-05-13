package com.oop2.typewiz.GameplayComponents;

import com.oop2.typewiz.database.DatabaseConnection;
import com.oop2.typewiz.util.SoundManager;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import javafx.scene.layout.VBox;

/**
 * Manages player state including health, score, and typing statistics.
 */
public class PlayerManager {

    private static final int MAX_HEALTH = 100;
    private static final int HEALTH_LOSS_PER_MISS = 20;
    private static final int STATS_UPDATE_INTERVAL = 5000; // 5 seconds

    private int playerHealth;
    private int score;
    private static int highScore = 0;
    private String username;

    private static PlayerManager instance;

    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }

    // Typing statistics
    private int totalKeystrokes;
    private int correctKeystrokes;
    private int incorrectKeystrokes;
    private int totalCharactersTyped;
    private int totalWords;
    private long typingStartTime;
    private long totalTypingTime;
    private long lastStatsUpdate;
    private List<Double> wpmOverTime;
    private List<Double> accuracyOverTime;
    private List<Long> keystrokeTimings;
    private long lastKeystrokeTime;

    // UI references
    private Text healthText;
    private Text scoreText;
    private VBox healthDisplay;

    /**
     * Creates a new PlayerManager with default values
     */
    public PlayerManager() {
        this.playerHealth = MAX_HEALTH;
        this.score = 0;

        // Initialize typing statistics
        this.totalKeystrokes = 0;
        this.correctKeystrokes = 0;
        this.incorrectKeystrokes = 0;
        this.totalCharactersTyped = 0;
        this.totalWords = 0;
        this.typingStartTime = System.currentTimeMillis();
        this.lastStatsUpdate = typingStartTime;
        this.wpmOverTime = new ArrayList<>();
        this.accuracyOverTime = new ArrayList<>();
        this.keystrokeTimings = new ArrayList<>();
        this.lastKeystrokeTime = 0;
    }

    /**
     * Gets the player's current health
     *
     * @return The player's health
     */
    public int getHealth() {
        return playerHealth;
    }

    /**
     * Gets the maximum health value
     *
     * @return The maximum health
     */
    public int getMaxHealth() {
        return MAX_HEALTH;
    }

    /**
     * Gets the player's current score
     *
     * @return The player's score
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the health text UI element
     *
     * @param healthText The health text UI element
     */
    public void setHealthText(Text healthText) {
        this.healthText = healthText;
    }

    /**
     * Sets the health display VBox
     *
     * @param healthDisplay The health display VBox
     */
    public void setHealthDisplay(VBox healthDisplay) {
        this.healthDisplay = healthDisplay;
    }

    /**
     * Sets the score text UI element
     *
     * @param scoreText The score text UI element
     */
    public void setScoreText(Text scoreText) {
        this.scoreText = scoreText;
        updateScoreText();
    }

    /**
     * Decreases the player's health when they miss a word
     *
     * @return true if the player is still alive, false if health reached zero
     */
    public boolean decreaseHealth() {
        playerHealth = Math.max(0, playerHealth - HEALTH_LOSS_PER_MISS);

        // Play damage sound
        if (playerHealth > 0) {
            SoundManager.getInstance().playDamage();
        }

        // Update health text if available
        if (healthText != null) {
            healthText.setText(playerHealth + "/" + MAX_HEALTH);

            // Also update the health color to make it more visible when damage is taken
            healthText.setFill(playerHealth > 60 ? Color.WHITE :
                    playerHealth > 30 ? Color.YELLOW : Color.RED);
        } else {
            System.out.println("WARNING: healthText is null, cannot update display");
        }

        // Update the health bar visually
        if (healthDisplay != null) {
            UIFactory.updateHealthBar(healthDisplay, playerHealth);
            System.out.println("Updated health bar to: " + playerHealth);
        } else {
            System.out.println("WARNING: healthDisplay is null, cannot update health bar");
        }

        return playerHealth > 0;
    }

    /**
     * Resets the player's health to maximum
     */
    public void resetHealth() {
        playerHealth = MAX_HEALTH;

        // Update health display
        if (healthText != null) {
            healthText.setText(playerHealth + "/" + MAX_HEALTH);
            healthText.setFill(Color.WHITE);
        }

        // Update the health bar
        if (healthDisplay != null) {
            UIFactory.updateHealthBar(healthDisplay, playerHealth);
        }
    }

    /**
     * Adds points to the player's score
     *
     * @param points The number of points to add
     */
    public void addScore(int points) {
        score += points;
        updateScoreText();

        // Play a sound for score increase
        SoundManager.getInstance().playButtonClick(); // Use click for now, can add specific score sound later
    }

    /**
     * Updates the score text UI element
     */
    private void updateScoreText() {
        if (scoreText != null) {
            scoreText.setText(Integer.toString(score));
        }
    }

    public void updateHighScoreInDatabaseIfNeeded() {
        if (score > highScore) {
            highScore = score;
            System.out.println("New high score: " + highScore);

            // Ensure the username is set before updating
            String username = PlayerManager.getInstance().getUsername();

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE users SET high_score = ? WHERE username = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, highScore);
                stmt.setString(2, username);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }

    public void setHighScore(int highScore) {
        this.highScore = highScore;
    }

    public static int getHighScore() {
        return highScore;
    }

    /**
     * Records a keystroke and updates typing statistics
     *
     * @param typedChar The character that was typed
     * @param isCorrect Whether the keystroke was correct
     */
    public void recordKeystroke(char typedChar, boolean isCorrect) {
        // Update keystroke timing for consistency calculation
        long currentTime = System.currentTimeMillis();
        if (lastKeystrokeTime > 0) {
            long timeDiff = currentTime - lastKeystrokeTime;

            // Only record timings that are within reasonable range (20ms to 2000ms)
            // Skip if too fast or too slow (likely pause or system delay)
            if (timeDiff >= 20 && timeDiff <= 2000) {
                keystrokeTimings.add(timeDiff);
                System.out.println("DEBUG - Added keystroke timing: " + timeDiff + " ms, total timings: " + keystrokeTimings.size());
            } else {
                System.out.println("DEBUG - Skipped outlier keystroke timing: " + timeDiff + " ms (outside 20-2000ms range)");
            }
        } else {
            System.out.println("DEBUG - First keystroke, no timing recorded yet");
        }
        lastKeystrokeTime = currentTime;

        // Track total keystrokes
        totalKeystrokes++;

        if (isCorrect) {
            correctKeystrokes++;
            totalCharactersTyped++;
        } else {
            incorrectKeystrokes++;
        }

        // Update typing statistics periodically
        updateTypingStats();
    }

    /**
     * Completely resets keystroke timings (for menu option to reset statistics)
     */
    public void resetKeystrokeTimings() {
        keystrokeTimings.clear();
        lastKeystrokeTime = 0;
        System.out.println("Completely reset all keystroke timings");
    }

    /**
     * Records a completed word
     *
     * @param word The completed word
     * @param waveNumber The current wave number
     */
    public void recordCompletedWord(String word, int waveNumber) {
        // Add to total words completed
        totalWords++;

        // Calculate score based on word length and current wave
        int wordScore = word.length() * 10 * waveNumber;
        addScore(wordScore);

        // Play enemy defeat sound
        SoundManager.getInstance().playEnemyDefeat();

        // Force update stats after completing a word
        updateTypingStats(true);
    }

    /**
     * Updates the typing statistics
     */
    private void updateTypingStats() {
        updateTypingStats(false);
    }

    /**
     * Updates the typing statistics
     *
     * @param force Whether to force update regardless of time interval
     */
    private void updateTypingStats(boolean force) {
        long currentTime = System.currentTimeMillis();

        // Only update stats periodically to avoid overhead, unless forced
        if (!force && currentTime - lastStatsUpdate < STATS_UPDATE_INTERVAL) {
            return;
        }

        // Update total typing time
        totalTypingTime = currentTime - typingStartTime;

        // Calculate current WPM and accuracy
        double currentWPM = calculateWPM();
        double currentAccuracy = calculateAccuracy();

        // Store in history for graph
        wpmOverTime.add(currentWPM);
        accuracyOverTime.add(currentAccuracy);

        // Update last stats update time
        lastStatsUpdate = currentTime;
    }

    /**
     * Calculates the words per minute (WPM)
     *
     * @return The calculated WPM
     */
    public double calculateWPM() {
        // If no time has elapsed, return 0
        if (totalTypingTime <= 0) return 0;

        // WPM = (characters typed / 5) / (time in minutes)
        // 5 characters is the standard word length
        double minutes = totalTypingTime / 60000.0;
        return (totalCharactersTyped / 5.0) / minutes;
    }

    /**
     * Calculates the raw words per minute (includes all keystrokes)
     *
     * @return The calculated raw WPM
     */
    public double calculateRawWPM() {
        // If no time has elapsed, return 0
        if (totalTypingTime <= 0) return 0;

        // Raw WPM = (total keystrokes / 5) / (time in minutes)
        double minutes = totalTypingTime / 60000.0;
        return (totalKeystrokes / 5.0) / minutes;
    }

    /**
     * Calculates the typing accuracy as a percentage
     *
     * @return The calculated accuracy percentage
     */
    public double calculateAccuracy() {
        // If no keystrokes, return 0
        if (totalKeystrokes <= 0) return 0;

        return (double) correctKeystrokes / totalKeystrokes * 100.0;
    }

    /**
     * Calculates the typing consistency as a percentage
     *
     * @return The calculated consistency percentage
     */
    public double calculateConsistency() {
        // Debug the content of keystroke timings
        System.out.println("==== CONSISTENCY CALCULATION ====");
        System.out.println("Total keystroke timings: " + keystrokeTimings.size());

        // If less than 2 keystroke timings, return 0
        if (keystrokeTimings.size() < 2) {
            System.out.println("Not enough keystroke timings, need at least 2");
            return 0;
        }

        // Filter out any extreme outliers (e.g., pauses, breaks)
        List<Long> filteredTimings = new ArrayList<>(keystrokeTimings);
        // If there are more than 10 timings, remove the top 10% (likely outliers)
        if (filteredTimings.size() > 10) {
            filteredTimings.sort(Long::compare);
            int removeCount = filteredTimings.size() / 10;
            if (removeCount > 0) {
                // Remove the highest values (likely pauses or breaks)
                filteredTimings = filteredTimings.subList(0, filteredTimings.size() - removeCount);
                System.out.println("Removed " + removeCount + " outliers, remaining: " + filteredTimings.size());
            }
        }

        // Calculate standard deviation of keystroke timings
        double mean = filteredTimings.stream().mapToLong(Long::valueOf).average().getAsDouble();
        System.out.println("Mean keystroke timing: " + mean + " ms");

        // Check for extremely small or zero mean - could cause division by zero
        if (mean < 1.0) {
            System.out.println("Mean is too small, can't calculate consistency accurately");
            return 0.0;
        }

        double variance = filteredTimings.stream()
                .mapToDouble(timing -> Math.pow(timing - mean, 2))
                .average()
                .getAsDouble();
        System.out.println("Variance: " + variance);

        double stdDev = Math.sqrt(variance);
        System.out.println("Standard deviation: " + stdDev);

        // Calculate coefficient of variation (lower is more consistent)
        double cv = stdDev / mean;
        System.out.println("Coefficient of variation: " + cv);

        // Convert to a percentage (100% = perfect consistency, 0% = terrible)
        // Cap at 100% for very consistent typing
        // Ensure we don't return exactly 0.0 if we have valid data
        double consistencyPercentage = Math.max(0.1, Math.min(100, (1 - cv) * 100));
        if (consistencyPercentage < 1.0 && filteredTimings.size() >= 5) {
            // If we have enough data but still get a very low value, set a minimum
            consistencyPercentage = 1.0;
        }
        System.out.println("Consistency percentage: " + consistencyPercentage + "%");
        System.out.println("==== END CONSISTENCY CALCULATION ====");

        return consistencyPercentage;
    }

    /**
     * Gets the WPM over time history
     *
     * @return List of WPM measurements over time
     */
    public List<Double> getWpmOverTime() {
        return new ArrayList<>(wpmOverTime);
    }

    /**
     * Gets the accuracy over time history
     *
     * @return List of accuracy measurements over time
     */
    public List<Double> getAccuracyOverTime() {
        return new ArrayList<>(accuracyOverTime);
    }

    /**
     * Gets the total number of words completed
     *
     * @return The total words completed
     */
    public int getTotalWords() {
        return totalWords;
    }

    /**
     * Gets the total number of characters typed
     *
     * @return The total characters typed
     */
    public int getTotalCharactersTyped() {
        return totalCharactersTyped;
    }

    /**
     * Gets the size of the keystroke timings list (for debugging)
     *
     * @return The number of keystroke timings recorded
     */
    public int getKeystrokeTimingsSize() {
        return keystrokeTimings.size();
    }

    /**
     * Dumps the current keystroke timings for debugging purposes
     */
    public void dumpKeystrokeTimings() {
        System.out.println("DEBUG - All keystroke timings:");
        for (int i = 0; i < keystrokeTimings.size(); i++) {
            System.out.println("  Timing " + i + ": " + keystrokeTimings.get(i) + " ms");
        }
    }

    /**
     * Resets the player manager for a new game
     */
    public void reset() {
        // Reset health and score
        playerHealth = MAX_HEALTH;
        score = 0;

        // Update the UI
        updateScoreText();
        if (healthText != null) {
            healthText.setText(playerHealth + "/" + MAX_HEALTH);
            healthText.setFill(Color.WHITE);
        }
        if (healthDisplay != null) {
            UIFactory.updateHealthBar(healthDisplay, playerHealth);
        }

        // Reset typing stats, BUT preserve keystroke timings for consistency calculation
        totalKeystrokes = 0;
        correctKeystrokes = 0;
        incorrectKeystrokes = 0;
        totalCharactersTyped = 0;
        totalWords = 0;
        typingStartTime = System.currentTimeMillis();
        lastStatsUpdate = typingStartTime;
        wpmOverTime.clear();
        accuracyOverTime.clear();

        // We intentionally DON'T clear keystroke timings to keep consistency data
        // but we DO reset the last keystroke time to avoid inaccurate timing on the first
        // keystroke after restart
        lastKeystrokeTime = 0;

        System.out.println("PlayerManager reset complete - health: " + playerHealth + ", score: " + score);
        System.out.println("Kept " + keystrokeTimings.size() + " keystroke timings for consistency calculation");
    }
} 