package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.time.LocalTimer;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

/**
 * Manages game waves including spawning, progression, and difficulty scaling.
 * Implements the Strategy pattern for different wave behaviors.
 */
public class WaveManager {

    // Wave settings
    private static final int MAX_WAVES = 10;
    private static final double WAVE_SPAWN_DELAY = 7.0; // seconds between spawn groups
    private static final double SPAWN_SPEED_INCREASE = 0.9; // each group spawns 10% faster than the last
    private static final double SCREEN_MARGIN = 150;

    // Wave difficulty progression parameters
    private static final int[] WAVE_SPAWNS_PER_WAVE = {
            10, 12, 15, 18, 20, 22, 25, 28, 30, 35 // Increasing spawns per wave
    };
    private static final double[] WAVE_SPEED_MULTIPLIERS = {
            1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.8, 2.0, 2.2 // Increasing speed per wave
    };
    private static final int[] MIN_SPAWNS_PER_GROUP_BY_WAVE = {
            1, 1, 2, 2, 2, 3, 3, 3, 4, 4 // Increasing minimum spawns per group
    };
    private static final int[] MAX_SPAWNS_PER_GROUP_BY_WAVE = {
            2, 3, 3, 4, 4, 5, 5, 6, 6, 7 // Increasing maximum spawns per group
    };
    private static final double[] SPAWN_DELAY_MULTIPLIERS = {
            1.0, 0.95, 0.9, 0.85, 0.8, 0.75, 0.7, 0.65, 0.6, 0.5 // Decreasing delay between groups
    };

    private int currentWave;
    private boolean waveInProgress;
    private boolean isSpawningWave;
    private int totalWaveSpawns;
    private int currentGroupSize;
    private double currentSpawnDelay;
    private boolean spawnFromRight;
    private int nextEnemyType; // 0 = Gargoyle, 1 = Grimouge, 2 = Vyleye

    private LocalTimer waveSpawnTimer;
    private final Random random;
    private final EntityManager entityManager;
    private final GameStateManager stateManager;

    // Function to get random words based on wave difficulty
    private Supplier<String> wordSupplier;

    private double minY;
    private double maxY;
    private double spawnPerimeterRight;

    private int maxWaves;
    private int[] waveSpawnsPerWave;
    private double[] waveSpeedMultipliers;
    private int[] minSpawnsPerGroupByWave;
    private int[] maxSpawnsPerGroupByWave;
    private double[] spawnDelayMultipliers;

    /**
     * Creates a new WaveManager
     *
     * @param entityManager The entity manager
     * @param stateManager The game state manager
     * @param wordSupplier Function that supplies random words
     * @param screenHeight Height of the game screen
     * @param maxWaves Maximum number of waves
     * @param waveSpawnsPerWave Array of wave spawn counts
     * @param waveSpeedMultipliers Array of wave speed multipliers
     * @param minSpawnsPerGroupByWave Array of minimum spawns per group
     * @param maxSpawnsPerGroupByWave Array of maximum spawns per group
     * @param spawnDelayMultipliers Array of spawn delay multipliers
     */
    public WaveManager(EntityManager entityManager, GameStateManager stateManager, Supplier<String> wordSupplier, double screenHeight,
                       int maxWaves, int[] waveSpawnsPerWave, double[] waveSpeedMultipliers, int[] minSpawnsPerGroupByWave,
                       int[] maxSpawnsPerGroupByWave, double[] spawnDelayMultipliers) {
        this.entityManager = entityManager;
        this.stateManager = stateManager;
        this.wordSupplier = wordSupplier;
        this.random = new Random();
        this.maxWaves = maxWaves;
        this.waveSpawnsPerWave = waveSpawnsPerWave;
        this.waveSpeedMultipliers = waveSpeedMultipliers;
        this.minSpawnsPerGroupByWave = minSpawnsPerGroupByWave;
        this.maxSpawnsPerGroupByWave = maxSpawnsPerGroupByWave;
        this.spawnDelayMultipliers = spawnDelayMultipliers;

        // Initialize wave state
        this.currentWave = 1;
        this.waveInProgress = false;
        this.isSpawningWave = false;
        this.nextEnemyType = 0; // Start with Gargoyles

        // Initialize spawn area with reduced top margin to allow spawning higher
        this.spawnPerimeterRight = 100; // Distance from right edge where entities spawn
        this.minY = SCREEN_MARGIN * 0.6; // Reduced top margin to allow more top spawning
        this.maxY = screenHeight - (SCREEN_MARGIN * 1.5); // Increased bottom margin

        // Initialize timers
        this.waveSpawnTimer = FXGL.newLocalTimer();
        this.waveSpawnTimer.capture();
    }

    /**
     * Gets the current wave number
     *
     * @return The current wave number (1-based)
     */
    public int getCurrentWave() {
        return currentWave;
    }

    /**
     * Gets the maximum number of waves
     *
     * @return The maximum number of waves
     */
    public int getMaxWaves() {
        return maxWaves;
    }

    /**
     * Checks if all waves have been completed
     *
     * @return true if all waves are completed
     */
    public boolean areAllWavesCompleted() {
        return currentWave > maxWaves;
    }

    /**
     * Starts a new wave
     */
    public void startWave() {
        if (areAllWavesCompleted()) {
            stateManager.victory(null);
            return;
        }
        entityManager.removeAllEntitiesOfType(Game.EntityType.GARGOYLE);
        entityManager.removeAllEntitiesOfType(Game.EntityType.GRIMOUGE);
        entityManager.removeAllEntitiesOfType(Game.EntityType.VYLEYE);
        int waveIndex = currentWave - 1;
        isSpawningWave = false;
        spawnFromRight = true;
        nextEnemyType = 0;
        int minSpawns = minSpawnsPerGroupByWave[waveIndex];
        int maxSpawns = maxSpawnsPerGroupByWave[waveIndex];
        System.out.println("Wave " + currentWave + " settings: minSpawns=" + minSpawns + ", maxSpawns=" + maxSpawns);
        double availableHeight = maxY - minY;
        int maxPossibleSpawns = Math.max(1, (int)(availableHeight / (SCREEN_MARGIN * 0.3)));
        maxSpawns = Math.min(maxSpawns, maxPossibleSpawns);
        minSpawns = Math.min(minSpawns, maxSpawns);
        int newGroupSize;
        if (minSpawns == maxSpawns) {
            newGroupSize = minSpawns;
        } else {
            newGroupSize = Math.max(1, random.nextInt(maxSpawns - minSpawns + 1) + minSpawns);
        }
        currentGroupSize = newGroupSize;
        System.out.println("Set currentGroupSize to " + currentGroupSize);
        totalWaveSpawns = waveSpawnsPerWave[waveIndex];
        currentSpawnDelay = WAVE_SPAWN_DELAY * spawnDelayMultipliers[waveIndex];
        waveSpawnTimer.capture();
        waveInProgress = true;
        System.out.println("Starting wave " + currentWave + " with " + totalWaveSpawns +
                " total spawns, speed multiplier " + waveSpeedMultipliers[waveIndex] + ", group size " + currentGroupSize);
    }

    /**
     * Updates the wave spawning logic
     *
     * @return true if wave is completed
     */
    public boolean update() {
        // Don't do anything if the wave is not in progress or not spawning
        if (!waveInProgress || !isSpawningWave) {
            return false;
        }

        // Check if we need to spawn a new group
        List<Entity> activeEnemies = entityManager.getActiveEnemies();
        if (activeEnemies.isEmpty() || waveSpawnTimer.elapsed(Duration.seconds(currentSpawnDelay))) {
            if (totalWaveSpawns > 0) {
                spawnGroup();
            } else if (activeEnemies.isEmpty()) {
                // Wave completed
                System.out.println("[DEBUG] Wave " + currentWave + " completed. Calling waveCompleted()");
                waveCompleted();
                return true;
            }
        }

        return false;
    }

    /**
     * Spawns a group of entities
     */
    private List<Entity> spawnGroup() {
        int waveIndex = Math.min(currentWave - 1, maxWaves - 1);

        // Check how many more entities we can spawn based on the active entity limit
        int availableSlots = entityManager.getAvailableEntitySlots();
        if (availableSlots <= 0) {
            System.out.println("Cannot spawn more entities - at maximum capacity");
            return new ArrayList<>(); // Return empty list
        }

        // Adjust group size to respect available slots
        int adjustedGroupSize = Math.min(currentGroupSize, availableSlots);
        if (adjustedGroupSize != currentGroupSize) {
            System.out.println("Adjusted spawn group size from " + currentGroupSize +
                    " to " + adjustedGroupSize + " due to entity limit");
            currentGroupSize = adjustedGroupSize;
        }

        // Ensure we don't try to spawn 0 entities
        if (currentGroupSize <= 0) {
            int minSpawns = minSpawnsPerGroupByWave[waveIndex];
            int maxSpawns = maxSpawnsPerGroupByWave[waveIndex];
            if (minSpawns == maxSpawns) {
                currentGroupSize = minSpawns;
            } else {
                currentGroupSize = Math.max(1, random.nextInt(maxSpawns - minSpawns + 1) + minSpawns);
            }
            // Adjust again based on available slots
            currentGroupSize = Math.min(currentGroupSize, availableSlots);
            System.out.println("Fixed zero group size to: " + currentGroupSize);
        }

        List<Entity> spawnedEntities;

        // Calculate spawn heights for better distribution
        double availableHeight = maxY - minY;

        // Create position segments that ensure coverage of the entire screen height
        List<Double> yPositions = new ArrayList<>();

        // Define height segments to ensure good distribution
        final int NUM_SEGMENTS = 5; // More segments for better distribution

        // Option 1: Ensure we have entities at various screen heights with bias toward top
        if (currentGroupSize <= NUM_SEGMENTS) {
            // For small groups, distribute evenly with emphasis on top section
            for (int i = 0; i < currentGroupSize; i++) {
                // Bias toward top of screen (weighted distribution)
                double segmentHeight = availableHeight / NUM_SEGMENTS;

                // Select segment with bias toward top segments
                int segmentIndex;
                if (random.nextDouble() < 0.6) { // 60% chance to pick from top half
                    segmentIndex = random.nextInt(NUM_SEGMENTS / 2);
                } else {
                    segmentIndex = random.nextInt(NUM_SEGMENTS);
                }

                // Position within segment with random offset
                double basePos = minY + segmentIndex * segmentHeight;
                double randomOffset = random.nextDouble() * segmentHeight;
                double finalPos = Math.max(minY, Math.min(maxY, basePos + randomOffset));

                yPositions.add(finalPos);
            }
        } else {
            // For larger groups, ensure coverage across all segments

            // First, place at least one entity in each segment to ensure full coverage
            for (int segment = 0; segment < NUM_SEGMENTS && segment < currentGroupSize; segment++) {
                double segmentHeight = availableHeight / NUM_SEGMENTS;
                double basePos = minY + segment * segmentHeight;
                double randomOffset = random.nextDouble() * segmentHeight;
                yPositions.add(basePos + randomOffset);
            }

            // For remaining entities, distribute randomly but with top bias
            for (int i = NUM_SEGMENTS; i < currentGroupSize; i++) {
                double yPos;
                if (random.nextDouble() < 0.5) { // 50% chance to spawn in top half
                    yPos = minY + random.nextDouble() * (availableHeight / 2);
                } else {
                    yPos = minY + random.nextDouble() * availableHeight;
                }
                yPositions.add(yPos);
            }
        }

        // Apply minimum spacing between entities to prevent overlap
        Collections.sort(yPositions);
        List<Double> spacedPositions = new ArrayList<>();
        double minSpacing = 60; // Minimum pixels between entities

        for (double pos : yPositions) {
            // Check if this position would be too close to any existing position
            boolean tooClose = false;
            for (double existingPos : spacedPositions) {
                if (Math.abs(existingPos - pos) < minSpacing) {
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                spacedPositions.add(pos);
            } else {
                // Find an alternative position
                double altPos = pos;
                boolean foundPosition = false;

                // Try positions above and below with increasing distance
                for (int offset = 1; offset < 10 && !foundPosition; offset++) {
                    // Try above
                    altPos = pos - (offset * minSpacing / 2);
                    if (altPos >= minY) {
                        boolean positionValid = true;
                        for (double existingPos : spacedPositions) {
                            if (Math.abs(existingPos - altPos) < minSpacing) {
                                positionValid = false;
                                break;
                            }
                        }
                        if (positionValid) {
                            spacedPositions.add(altPos);
                            foundPosition = true;
                            break;
                        }
                    }

                    // Try below
                    altPos = pos + (offset * minSpacing / 2);
                    if (altPos <= maxY) {
                        boolean positionValid = true;
                        for (double existingPos : spacedPositions) {
                            if (Math.abs(existingPos - altPos) < minSpacing) {
                                positionValid = false;
                                break;
                            }
                        }
                        if (positionValid) {
                            spacedPositions.add(altPos);
                            foundPosition = true;
                            break;
                        }
                    }
                }

                // If we couldn't find a good position, just use original if we have space
                if (!foundPosition && spacedPositions.size() < currentGroupSize) {
                    spacedPositions.add(pos);
                }
            }
        }

        // If we don't have enough positions, add some more
        while (spacedPositions.size() < currentGroupSize && spacedPositions.size() < 12) {
            double randomY = minY + random.nextDouble() * availableHeight;
            boolean tooClose = false;
            for (double existingPos : spacedPositions) {
                if (Math.abs(existingPos - randomY) < minSpacing) {
                    tooClose = true;
                    break;
                }
            }

            if (!tooClose) {
                spacedPositions.add(randomY);
            }
        }

        // Use these spaced positions instead of the original ones
        yPositions = spacedPositions;

        // Spawn different enemy types in sequence
        if (nextEnemyType == 0) {
            // Spawn Gargoyles
            System.out.println("Spawning group of " + currentGroupSize + " gargoyles from right side");

            // Use GargoyleFactory to spawn a group of gargoyles with improved positioning
            spawnedEntities = GargoyleFactory.spawnGargoyleGroup(
                    currentGroupSize,
                    minY,
                    maxY,
                    spawnFromRight, // Always spawn from right
                    spawnPerimeterRight,
                    wordSupplier, // Method reference to get random words
                    Game.EntityType.GARGOYLE
            );

            System.out.println("GargoyleFactory returned " + spawnedEntities.size() + " entities");

            // Adjust Y positions for better distribution
            for (int i = 0; i < spawnedEntities.size(); i++) {
                if (i < yPositions.size()) {
                    spawnedEntities.get(i).setY(yPositions.get(i));
                }
            }
        }
        else if (nextEnemyType == 1) {
            // Spawn Grimouges
            System.out.println("Spawning group of " + currentGroupSize + " grimouges from right side");

            // Use GrimougeFactory to spawn a group of grimouges
            spawnedEntities = GrimougeFactory.spawnGrimougeGroup(
                    currentGroupSize,
                    minY,
                    maxY,
                    spawnFromRight, // Always spawn from right
                    spawnPerimeterRight,
                    wordSupplier, // Method reference to get random words
                    Game.EntityType.GRIMOUGE
            );

            System.out.println("GrimougeFactory returned " + spawnedEntities.size() + " entities");

            // Adjust Y positions for better distribution
            for (int i = 0; i < spawnedEntities.size(); i++) {
                if (i < yPositions.size()) {
                    spawnedEntities.get(i).setY(yPositions.get(i));
                }
            }
        }
        else {
            // Spawn Vyleyes
            System.out.println("Spawning group of " + currentGroupSize + " vyleyes from right side");

            // Use VyleyeFactory to spawn a group of vyleyes
            spawnedEntities = VyleyeFactory.spawnVyleyeGroup(
                    currentGroupSize,
                    minY,
                    maxY,
                    spawnFromRight, // Always spawn from right
                    spawnPerimeterRight,
                    wordSupplier, // Method reference to get random words
                    Game.EntityType.VYLEYE
            );

            System.out.println("VyleyeFactory returned " + spawnedEntities.size() + " entities");

            // Adjust Y positions for better distribution
            for (int i = 0; i < spawnedEntities.size(); i++) {
                if (i < yPositions.size()) {
                    spawnedEntities.get(i).setY(yPositions.get(i));
                }
            }
        }

        // Update nextEnemyType for the next spawn (cycle through 0, 1, 2)
        nextEnemyType = (nextEnemyType + 1) % 3;

        // Add spawned entities to entity manager and ensure they're attached to the world
        List<Entity> successfullyAddedEntities = new ArrayList<>();
        for (Entity entity : spawnedEntities) {
            String entityType = "unknown";
            if (entity.isType(Game.EntityType.GARGOYLE)) {
                entityType = "gargoyle";
            } else if (entity.isType(Game.EntityType.GRIMOUGE)) {
                entityType = "grimouge";
            } else if (entity.isType(Game.EntityType.VYLEYE)) {
                entityType = "vyleye";
            }

            System.out.println("Adding " + entityType + " to entity manager with word: " +
                    (entity.getProperties().exists("word") ? entity.getString("word") : "unknown"));

            boolean added = entityManager.addActiveEntity(entity);
            if (added) {
                successfullyAddedEntities.add(entity);

                // Ensure the entity is attached to the world
                if (!entity.isActive()) {
                    System.out.println("Attaching " + entityType + " to world");
                    FXGL.getGameWorld().addEntity(entity);
                }
            } else {
                // Entity wasn't added due to limit, so we can remove it
                if (entity.isActive()) {
                    entity.removeFromWorld();
                }
            }
        }

        // Update our reference to only the entities that were actually added
        spawnedEntities = successfullyAddedEntities;

        // Check if we need to select a new entity automatically
        InputManager inputManager = FXGL.getWorldProperties().getObject("inputManager");
        if (inputManager != null && inputManager.getSelectedWordBlock() == null && !spawnedEntities.isEmpty()) {
            // We only want to select one if there are no other active selections
            Entity closestEntity = null;

            if (spawnedEntities.get(0).isType(Game.EntityType.GARGOYLE)) {
                closestEntity = GargoyleFactory.findClosestGargoyleToCenter(spawnedEntities);
            } else if (spawnedEntities.get(0).isType(Game.EntityType.GRIMOUGE)) {
                closestEntity = GrimougeFactory.findClosestGrimougeToCenter(spawnedEntities);
            } else if (spawnedEntities.get(0).isType(Game.EntityType.VYLEYE)) {
                closestEntity = VyleyeFactory.findClosestVyleyeToCenter(spawnedEntities);
            }

            if (closestEntity != null) {
                System.out.println("WaveManager: Automatically selecting new entity after spawn");
                inputManager.selectWordBlock(closestEntity);
            }
        }

        totalWaveSpawns -= spawnedEntities.size();

        // Get name of next entity type to spawn
        String nextEntityType = "unknown";
        if (nextEnemyType == 0) {
            nextEntityType = "gargoyles";
        } else if (nextEnemyType == 1) {
            nextEntityType = "grimouges";
        } else {
            nextEntityType = "vyleyes";
        }

        System.out.println("Successfully spawned " + spawnedEntities.size() +
                " entities, next spawn: " + nextEntityType + ", " + totalWaveSpawns + " remaining in wave");

        // Prepare for next group with wave-specific parameters
        int waveMinSpawns = minSpawnsPerGroupByWave[waveIndex];
        int waveMaxSpawns = maxSpawnsPerGroupByWave[waveIndex];

        // Calculate next group size
        if (waveMinSpawns == waveMaxSpawns) {
            currentGroupSize = waveMinSpawns;
        } else {
            currentGroupSize = random.nextInt(waveMaxSpawns - waveMinSpawns + 1) + waveMinSpawns;
        }

        // Apply wave-specific delay reduction for next spawn
        currentSpawnDelay *= SPAWN_SPEED_INCREASE * spawnDelayMultipliers[waveIndex];

        waveSpawnTimer.capture();

        return spawnedEntities;
    }

    /**
     * Handles wave completion
     */
    private void waveCompleted() {
        System.out.println("[DEBUG] In waveCompleted() - Current wave before completion: " + currentWave);
        waveInProgress = false;
        isSpawningWave = false;

        // Notify game state manager of wave completion first
        stateManager.completeWave(null);

        // Then increment wave number
        currentWave++;
        System.out.println("[DEBUG] Wave number incremented to: " + currentWave);
    }

    /**
     * Starts spawning for the current wave
     */
    public void startSpawning() {
        System.out.println("WaveManager: startSpawning called");

        // Make sure the wave is properly started first
        if (!waveInProgress) {
            System.out.println("WaveManager: Wave not in progress, starting wave first");
            startWave();
        }

        // Ensure group size is not zero
        if (currentGroupSize <= 0) {
            int waveIndex = Math.min(currentWave - 1, maxWaves - 1);
            int minSpawns = minSpawnsPerGroupByWave[waveIndex];
            int maxSpawns = maxSpawnsPerGroupByWave[waveIndex];
            currentGroupSize = Math.max(1, random.nextInt(maxSpawns - minSpawns + 1) + minSpawns);
            System.out.println("WaveManager: Corrected group size to: " + currentGroupSize);
        }

        isSpawningWave = true;
        waveSpawnTimer.capture();

        // Reset next enemy type to start with Gargoyles
        nextEnemyType = 0;

        System.out.println("WaveManager: Spawning first group of enemies, size=" + currentGroupSize);

        // Spawn first group right away
        List<Entity> spawned = spawnGroup();

        System.out.println("WaveManager: Spawned first group with " + spawned.size() +
                " enemies, total remaining: " + totalWaveSpawns);

        // Automatically select the first entity (closest to center) for targeting
        if (!spawned.isEmpty()) {
            // Find the closest entity to the center of the screen
            Entity closestEntity = null;

            if (spawned.get(0).isType(Game.EntityType.GARGOYLE)) {
                closestEntity = GargoyleFactory.findClosestGargoyleToCenter(spawned);
            } else if (spawned.get(0).isType(Game.EntityType.GRIMOUGE)) {
                closestEntity = GrimougeFactory.findClosestGrimougeToCenter(spawned);
            } else if (spawned.get(0).isType(Game.EntityType.VYLEYE)) {
                closestEntity = VyleyeFactory.findClosestVyleyeToCenter(spawned);
            }

            // Get input manager from world properties and select the entity
            InputManager inputManager = FXGL.getWorldProperties().getObject("inputManager");
            if (inputManager != null && closestEntity != null) {
                System.out.println("WaveManager: Automatically selecting first enemy");
                inputManager.selectWordBlock(closestEntity);
            }
        }
    }

    /**
     * Gets the speed multiplier for the current wave
     *
     * @return The speed multiplier
     */
    public double getCurrentWaveSpeedMultiplier() {
        int waveIndex = Math.min(currentWave - 1, maxWaves - 1);
        return waveSpeedMultipliers[waveIndex];
    }

    /**
     * Resets the wave manager for a new game
     */
    public void reset() {
        currentWave = 1;
        waveInProgress = false;
        isSpawningWave = false;
        nextEnemyType = 0;
    }
} 