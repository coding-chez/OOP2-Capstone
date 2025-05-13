package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.oop2.typewiz.util.SoundManager;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.List;
import java.util.function.Consumer;

/**
 * Manages keyboard input and typing functionality.
 * Implements the Command pattern for key actions.
 */
public class InputManager {

    private StringBuilder currentInput;
    private Entity selectedWordBlock;
    private final EntityManager entityManager;
    private final PlayerManager playerManager;
    private final GameStateManager stateManager;

    // Callback for when a game should be restarted
    private Consumer<Void> restartGameCallback;

    /**
     * Creates a new InputManager
     *
     * @param entityManager The entity manager
     * @param playerManager The player manager
     * @param stateManager The game state manager
     */
    public InputManager(EntityManager entityManager, PlayerManager playerManager, GameStateManager stateManager) {
        this.entityManager = entityManager;
        this.playerManager = playerManager;
        this.stateManager = stateManager;
        this.currentInput = new StringBuilder();
    }

    /**
     * Sets up event handlers for keyboard input
     */
    public void setupInput() {
        System.out.println("Setting up input handlers...");
        // Handle key typing (letters, digits, etc.)
        FXGL.getInput().addEventHandler(KeyEvent.KEY_TYPED, this::handleKeyTyped);

        // Handle key presses (backspace, shift, space, enter)
        FXGL.getInput().addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPressed);
        System.out.println("Input handlers set up successfully");
    }

    /**
     * Handles key typed events (letters, digits, etc.)
     *
     * @param event The key event
     */
    private void handleKeyTyped(KeyEvent event) {
        // Check if game is active
        if (!stateManager.isInState(GameStateManager.GameState.PLAYING)) {
            System.out.println("Not processing typing - game is not in PLAYING state: " + stateManager.getCurrentState());
            return;
        }

        // Skip empty characters or non-printable control characters
        if (event.getCharacter().isEmpty() || event.getCharacter().length() == 0 ||
                event.getCharacter().charAt(0) < ' ') { // ASCII 32 is space, the first printable character
            return;
        }

        char typedChar = event.getCharacter().charAt(0);
        System.out.println("Received typed character: " + typedChar);

        // Only process letter, digit, hyphen, or apostrophe
        if (Character.isLetterOrDigit(typedChar) || typedChar == '-' || typedChar == '\'') {
            System.out.println("Processing typed character: " + typedChar);

            // If no word is selected, try to select one first
            if (selectedWordBlock == null) {
                List<Entity> enemies = entityManager.getActiveEnemies();
                System.out.println("No word selected. Found " + enemies.size() + " active enemies");
                if (!enemies.isEmpty()) {
                    Entity target = findMostUrgentEnemy(enemies);
                    if (target != null) {
                        selectWordBlock(target);
                        System.out.println("Auto-selected target for typing: " + target.getString("word"));
                    } else {
                        System.out.println("No urgent enemy found to select");
                    }
                } else {
                    System.out.println("No active enemies to select from");
                }
            }

            // Now process the character
            processTypedCharacter(typedChar);

            // Consume the event to prevent it from bubbling up
            event.consume();
        }
    }

    /**
     * Handles key pressed events (backspace, shift, space, enter)
     *
     * @param event The key event
     */
    private void handleKeyPressed(KeyEvent event) {
        // Handle retry on game over screen
        if (stateManager.isInState(GameStateManager.GameState.GAME_OVER) ||
                stateManager.isInState(GameStateManager.GameState.VICTORY)) {
            if (event.getCode() == KeyCode.ENTER && restartGameCallback != null) {
                System.out.println("Restarting game via keyboard...");
                // Play button click sound for restart
                SoundManager.getInstance().playDamage();
                restartGameCallback.accept(null);
                event.consume();
                return;
            }
            return;
        }

        // Don't process keys if not in playing state
        if (!stateManager.isInState(GameStateManager.GameState.PLAYING)) {
            return;
        }

        if (event.getCode() == KeyCode.BACK_SPACE && currentInput.length() > 0) {
            // Play error sound for backspace
            SoundManager.getInstance().playTypingSound(false);
            // Delete last character
            currentInput.deleteCharAt(currentInput.length() - 1);
            updateLetterColors();
            event.consume(); // Consume backspace to prevent it from triggering browser back navigation
        }
        else if (event.getCode() == KeyCode.SPACE) {
            // Play spacebar complete sound
            SoundManager.getInstance().playSpacebarComplete();
            // Complete word with Space
            checkWordCompletion();
            event.consume(); // Consume space to prevent default scrolling behavior
        }
    }

    /**
     * Processes a typed character
     *
     * @param typedChar The character that was typed
     */
    private void processTypedCharacter(char typedChar) {
        if (selectedWordBlock == null) {
            System.out.println("No word block selected, attempting to find one...");
            // Select a word block if none is selected
            List<Entity> enemies = entityManager.getActiveEnemies();
            System.out.println("Found " + enemies.size() + " active enemies");

            if (!enemies.isEmpty()) {
                // Find the most urgent enemy to target
                Entity mostUrgent = findMostUrgentEnemy(enemies);
                if (mostUrgent != null) {
                    System.out.println("Selected most urgent enemy with word: " + mostUrgent.getString("word"));
                    selectWordBlock(mostUrgent);
                    // Now that we've selected an enemy, continue processing the character
                    // instead of returning
                }
                else {
                    System.out.println("No valid enemy found after searching");
                    return; // No valid enemies found
                }
            }
            else {
                System.out.println("No enemies available to select");
                return; // No enemies available
            }
        }

        try {
            String targetWord = selectedWordBlock.getString("word");
            System.out.println("Processing character '" + typedChar + "' for word '" + targetWord + "', current input: '" + currentInput.toString() + "'");

            // Make sure we're not exceeding the word length
            if (currentInput.length() >= targetWord.length()) {
                System.out.println("Input length exceeded target word length");
                return;
            }

            // Check if this would be a valid next character
            boolean isCorrect = typedChar == targetWord.charAt(currentInput.length());
            System.out.println("Character is " + (isCorrect ? "correct" : "incorrect"));

            // Play typing sound based on correctness
            SoundManager.getInstance().playTypingSound(isCorrect);

            // Record the keystroke in player stats
            playerManager.recordKeystroke(typedChar, isCorrect);

            if (isCorrect) {
                // Only add if it's correct (part of error trapping)
                currentInput.append(typedChar);
                updateLetterColors();
                System.out.println("Updated input to: '" + currentInput.toString() + "'");

                // Check if we've completed the word
                if (currentInput.length() == targetWord.length()) {
                    // Word is fully typed - make all letters blue for visual feedback
                    System.out.println("Word fully typed, marking as complete");
                    markWordAsComplete();
                }
            } else {
                // Wrong character - clear input but still count keystroke for consistency
                currentInput.setLength(0);
                resetToYellowHighlight();
                System.out.println("Incorrect character, reset input");
            }
        } catch (IllegalArgumentException e) {
            // If word property doesn't exist, ignore and wait for next update
            System.out.println("Error processing character: " + e.getMessage());
            currentInput.setLength(0);
        }
    }

    /**
     * Selects a word block for typing
     *
     * @param wordBlock The word block to select
     */
    public void selectWordBlock(Entity wordBlock) {
        if (wordBlock == null) return;

        // Deselect the previous block if there is one
        if (selectedWordBlock != null) {
            try {
                // Reset the previous block's word color to default white
                if (selectedWordBlock.isType(Game.EntityType.GARGOYLE)) {
                    GargoyleFactory.resetBlockToDefaultColor(selectedWordBlock);
                } else if (selectedWordBlock.isType(Game.EntityType.GRIMOUGE)) {
                    GrimougeFactory.resetBlockToDefaultColor(selectedWordBlock);
                } else if (selectedWordBlock.isType(Game.EntityType.VYLEYE)) {
                    VyleyeFactory.resetBlockToDefaultColor(selectedWordBlock);
                }
            } catch (Exception e) {
                // Ignore errors when resetting colors
            }
        }

        // Select the new block
        selectedWordBlock = wordBlock;

        // Always reset input when switching blocks
        currentInput.setLength(0);

        // Set initial yellow highlight for the selected block
        try {
            if (selectedWordBlock.isType(Game.EntityType.GARGOYLE)) {
                GargoyleFactory.selectWordBlock(selectedWordBlock);
            } else if (selectedWordBlock.isType(Game.EntityType.GRIMOUGE)) {
                GrimougeFactory.selectWordBlock(selectedWordBlock);
            } else if (selectedWordBlock.isType(Game.EntityType.VYLEYE)) {
                VyleyeFactory.selectWordBlock(selectedWordBlock);
            }

            // Immediately show that the block is selected and ready for input
            System.out.println("Word block selected and ready for input: " + selectedWordBlock.getString("word"));

            // Ensure the word is properly initialized and has the required properties
            if (!selectedWordBlock.getProperties().exists("processed")) {
                selectedWordBlock.setProperty("processed", true);
            }
        } catch (Exception e) {
            // If we can't highlight, just continue
            System.out.println("Error highlighting word block: " + e.getMessage());
        }
    }

    /**
     * Updates the letter colors based on current input
     */
    private void updateLetterColors() {
        if (selectedWordBlock == null) return;

        try {
            if (selectedWordBlock.isType(Game.EntityType.GARGOYLE)) {
                GargoyleFactory.updateLetterColors(selectedWordBlock, currentInput.toString());
            } else if (selectedWordBlock.isType(Game.EntityType.GRIMOUGE)) {
                GrimougeFactory.updateLetterColors(selectedWordBlock, currentInput.toString());
            } else if (selectedWordBlock.isType(Game.EntityType.VYLEYE)) {
                VyleyeFactory.updateLetterColors(selectedWordBlock, currentInput.toString());
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
    }

    /**
     * Marks the current word as complete
     */
    private void markWordAsComplete() {
        if (selectedWordBlock == null) return;

        try {
            if (selectedWordBlock.isType(Game.EntityType.GARGOYLE)) {
                GargoyleFactory.markWordAsComplete(selectedWordBlock);
            } else if (selectedWordBlock.isType(Game.EntityType.GRIMOUGE)) {
                GrimougeFactory.markWordAsComplete(selectedWordBlock);
            } else if (selectedWordBlock.isType(Game.EntityType.VYLEYE)) {
                VyleyeFactory.markWordAsComplete(selectedWordBlock);
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }

        // The actual removal and selection of a new entity will happen in checkWordCompletion()
    }

    /**
     * Resets the selected block to yellow highlight
     */
    private void resetToYellowHighlight() {
        if (selectedWordBlock == null) return;

        try {
            if (selectedWordBlock.isType(Game.EntityType.GARGOYLE)) {
                GargoyleFactory.selectWordBlock(selectedWordBlock);
            } else if (selectedWordBlock.isType(Game.EntityType.GRIMOUGE)) {
                GrimougeFactory.selectWordBlock(selectedWordBlock);
            } else if (selectedWordBlock.isType(Game.EntityType.VYLEYE)) {
                VyleyeFactory.selectWordBlock(selectedWordBlock);
            }
        } catch (Exception e) {
            // Property may not exist yet, ignore the error
        }
    }

    /**
     * Cycles to the next word block in the game
     */
    private void selectNextWordBlock() {
        List<Entity> enemies = entityManager.getActiveEnemies();
        if (enemies.isEmpty()) {
            System.out.println("No enemies available to cycle through");
            return;
        }

        System.out.println("Cycling through " + enemies.size() + " available enemies");

        // First, sort enemies by proximity to the center of the screen (left to right)
        double centerX = FXGL.getAppWidth() / 2.0;
        enemies.sort((e1, e2) -> {
            double dist1 = Math.abs(e1.getX() - centerX);
            double dist2 = Math.abs(e2.getX() - centerX);
            return Double.compare(dist1, dist2);
        });

        // If no block is currently selected, select the closest one to the center
        if (selectedWordBlock == null) {
            if (!enemies.isEmpty()) {
                Entity closestToCenter = enemies.get(0);
                selectWordBlock(closestToCenter);
                System.out.println("Selected enemy closest to center: " + closestToCenter.getString("word"));
            }
            return;
        }

        // Find the index of the currently selected enemy
        int currentIndex = -1;
        for (int i = 0; i < enemies.size(); i++) {
            if (enemies.get(i) == selectedWordBlock) {
                currentIndex = i;
                break;
            }
        }

        // Move to the next enemy in the list
        if (currentIndex != -1) {
            int nextIndex = (currentIndex + 1) % enemies.size();
            Entity nextEnemy = enemies.get(nextIndex);
            selectWordBlock(nextEnemy);
            System.out.println("Cycled from enemy #" + currentIndex + " to #" + nextIndex +
                    ": " + nextEnemy.getString("word"));
        } else {
            // Current selection not found in list, select the first one
            if (!enemies.isEmpty()) {
                Entity firstEnemy = enemies.get(0);
                selectWordBlock(firstEnemy);
                System.out.println("Selected first enemy: " + firstEnemy.getString("word"));
            }
        }
    }

    /**
     * Checks if the current word is completed and processes it
     */
    private void checkWordCompletion() {
        if (selectedWordBlock == null) {
            System.out.println("Cannot check word completion - no block selected");
            return;
        }

        try {
            String targetWord = selectedWordBlock.getString("word");
            String typed = currentInput.toString();

            System.out.println("Checking word completion: typed='" + typed + "', target='" + targetWord + "'");

            // Check if the typed text matches the target word
            if (typed.equals(targetWord)) {
                System.out.println("Word completed successfully!");

                // Word completed successfully
                Entity completedBlock = selectedWordBlock;
                boolean wasGargoyle = completedBlock.isType(Game.EntityType.GARGOYLE);
                boolean wasGrimouge = completedBlock.isType(Game.EntityType.GRIMOUGE);
                boolean wasVyleye = completedBlock.isType(Game.EntityType.VYLEYE);

                // Record the completed word
                int waveNumber = stateManager.isInState(GameStateManager.GameState.PLAYING) ? 1 : 0;
                playerManager.recordCompletedWord(targetWord, waveNumber);

                // Clear the selection before we remove the entity
                selectedWordBlock = null;

                // Get all active enemies BEFORE removing the completed one
                List<Entity> enemies = entityManager.getActiveEnemies();
                enemies.remove(completedBlock);

                // Remove the completed block
                entityManager.removeEntity(completedBlock);
                System.out.println("Removed completed enemy, remaining enemies: " + enemies.size());

                // Select a new enemy if any available
                if (!enemies.isEmpty()) {
                    // Find the closest one to the right edge of the screen (most urgent)
                    Entity mostUrgent = findMostUrgentEnemy(enemies);
                    if (mostUrgent != null) {
                        System.out.println("Selected new most urgent enemy: " + mostUrgent.getString("word"));
                        selectWordBlock(mostUrgent);
                    }
                } else {
                    System.out.println("No more enemies to select");
                }
            } else {
                // Word not completed - reset input and maintain yellow highlight
                System.out.println("Word does not match target, resetting input");
                currentInput.setLength(0);
                resetToYellowHighlight();
            }
        } catch (IllegalArgumentException e) {
            // Handle missing property gracefully
            System.out.println("Error checking word completion: " + e.getMessage());
            currentInput.setLength(0);

            // Try to select another entity if available
            List<Entity> enemies = entityManager.getActiveEnemies();
            if (!enemies.isEmpty() && selectedWordBlock != null) {
                System.out.println("Trying to select another valid enemy");
                // Try to find any valid enemy to select
                for (Entity enemy : enemies) {
                    try {
                        // Verify this enemy has the word property
                        enemy.getString("word");
                        selectWordBlock(enemy);
                        System.out.println("Selected alternative enemy: " + enemy.getString("word"));
                        break;
                    } catch (Exception ex) {
                        // Skip this one if it has errors
                        continue;
                    }
                }
            }
        }
    }

    /**
     * Finds the most urgent enemy (closest to exiting the screen)
     *
     * @param enemies List of active enemies
     * @return The most urgent enemy to target
     */
    private Entity findMostUrgentEnemy(List<Entity> enemies) {
        if (enemies.isEmpty()) return null;

        Entity mostUrgent = null;
        double screenWidth = FXGL.getAppWidth();
        double minDistance = Double.MAX_VALUE;

        for (Entity enemy : enemies) {
            try {
                // Make sure it has required properties
                enemy.getString("word");
                boolean movingRight = enemy.getBoolean("movingRight");

                // Calculate how close it is to the edge it's moving toward
                double distance;
                if (movingRight) {
                    distance = screenWidth - enemy.getX();
                } else {
                    distance = enemy.getX();
                }

                // Keep the one with the smallest distance to edge
                if (distance < minDistance) {
                    minDistance = distance;
                    mostUrgent = enemy;
                }
            } catch (Exception e) {
                // Skip entities with issues
                continue;
            }
        }

        return mostUrgent;
    }

    /**
     * Gets the current typed input
     *
     * @return The current input string
     */
    public String getCurrentInput() {
        return currentInput.toString();
    }

    /**
     * Gets the currently selected word block
     *
     * @return The selected word block
     */
    public Entity getSelectedWordBlock() {
        return selectedWordBlock;
    }

    /**
     * Sets the callback for game restart
     *
     * @param callback The restart game callback
     */
    public void setRestartGameCallback(Consumer<Void> callback) {
        this.restartGameCallback = callback;
    }

    /**
     * Resets input state
     */
    public void reset() {
        System.out.println("Resetting input manager...");
        currentInput.setLength(0);
        selectedWordBlock = null;
        System.out.println("Input manager reset complete");
    }

    /**
     * Public method to cycle to the next word block (for use by other components)
     */
    public void cycleToNextWordBlock() {
        // Only select next word if we're in playing state
        if (!stateManager.isInState(GameStateManager.GameState.PLAYING)) {
            System.out.println("Not cycling - game is not in PLAYING state");
            return;
        }

        // Get active enemies and check if there are any to cycle through
        List<Entity> enemies = entityManager.getActiveEnemies();
        if (enemies.isEmpty()) {
            System.out.println("No enemies available to cycle through");
            return;
        }

        // Log the attempt to cycle
        System.out.println("Shift key pressed - cycling to next word block, " + enemies.size() + " enemies available");

        // Play shift cycle sound
        SoundManager.getInstance().playShiftCycle();

        // Perform the cycling action
        selectNextWordBlock();

        // Immediately after cycling, ensure the word is ready for typing
        if (selectedWordBlock != null) {
            try {
                System.out.println("Ready to type word: " + selectedWordBlock.getString("word"));
            } catch (Exception e) {
                System.out.println("Selected word block has issues: " + e.getMessage());
            }
        }
    }
} 