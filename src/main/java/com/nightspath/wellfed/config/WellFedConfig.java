package com.nightspath.wellfed.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.nightspath.wellfed.WellFed;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class WellFedConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("well-fed.json");

    private static volatile WellFedConfig INSTANCE = new WellFedConfig();

    private int troughInfluenceRadius = 4;
    private int troughInteractionRadius = 2;
    private int maxNearbyBabies = 5;
    private int maxNearbyAdults = 10;
    private PopulationCountingMode populationCountingMode =
            PopulationCountingMode.ALL_NEARBY_LIVESTOCK;
    private boolean automaticTroughBreeding = true;

    private int foodBowlHealingRadius = 2;

    private boolean villageDecorationsEnabled = true;
    private int foodBowlVillageChancePercent = 40;
    private int feedingTroughVillageChancePercent = 25;

    private boolean villageChestLootEnabled = true;
    private int villageChestLootChancePercent = 1;

    // Advanced diagnostic setting; intentionally not shown in the normal GUI.
    private boolean worldgenDiagnostics = false;

    public enum PopulationCountingMode {
        ALL_NEARBY_LIVESTOCK,
        SAME_SPECIES_ONLY
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            INSTANCE = new WellFedConfig();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            WellFedConfig loaded = GSON.fromJson(reader, WellFedConfig.class);
            if (loaded == null) {
                loaded = new WellFedConfig();
            }
            loaded.validate();
            INSTANCE = loaded;
        } catch (Exception exception) {
            WellFed.LOGGER.warn(
                    "Could not read {}. Using Well Fed defaults.",
                    CONFIG_PATH,
                    exception
            );
            INSTANCE = new WellFedConfig();
        }
    }

    public static WellFedConfig get() {
        return INSTANCE;
    }

    public static WellFedConfig copyCurrent() {
        return new WellFedConfig(INSTANCE);
    }

    public static WellFedConfig defaults() {
        return new WellFedConfig();
    }

    public static void applyAndSave(WellFedConfig config) {
        WellFedConfig validated = new WellFedConfig(config);
        validated.validate();
        INSTANCE = validated;
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException exception) {
            WellFed.LOGGER.error(
                    "Could not save Well Fed config to {}.",
                    CONFIG_PATH,
                    exception
            );
        }
    }

    private WellFedConfig() {
    }

    private WellFedConfig(WellFedConfig other) {
        this.troughInfluenceRadius = other.troughInfluenceRadius;
        this.troughInteractionRadius = other.troughInteractionRadius;
        this.maxNearbyBabies = other.maxNearbyBabies;
        this.maxNearbyAdults = other.maxNearbyAdults;
        this.populationCountingMode = other.populationCountingMode;
        this.automaticTroughBreeding = other.automaticTroughBreeding;
        this.foodBowlHealingRadius = other.foodBowlHealingRadius;
        this.villageDecorationsEnabled = other.villageDecorationsEnabled;
        this.foodBowlVillageChancePercent = other.foodBowlVillageChancePercent;
        this.feedingTroughVillageChancePercent = other.feedingTroughVillageChancePercent;
        this.villageChestLootEnabled = other.villageChestLootEnabled;
        this.villageChestLootChancePercent = other.villageChestLootChancePercent;
        this.worldgenDiagnostics = other.worldgenDiagnostics;
    }

    private void validate() {
        this.troughInfluenceRadius = clamp(this.troughInfluenceRadius, 2, 8);
        this.troughInteractionRadius = clamp(
                this.troughInteractionRadius,
                1,
                this.troughInfluenceRadius
        );
        this.maxNearbyBabies = clamp(this.maxNearbyBabies, 1, 64);
        this.maxNearbyAdults = clamp(this.maxNearbyAdults, 1, 128);
        if (this.populationCountingMode == null) {
            this.populationCountingMode = PopulationCountingMode.ALL_NEARBY_LIVESTOCK;
        }
        this.foodBowlHealingRadius = clamp(this.foodBowlHealingRadius, 1, 8);
        this.foodBowlVillageChancePercent =
                clamp(this.foodBowlVillageChancePercent, 0, 100);
        this.feedingTroughVillageChancePercent =
                clamp(this.feedingTroughVillageChancePercent, 0, 100);
        this.villageChestLootChancePercent =
                clamp(this.villageChestLootChancePercent, 0, 100);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public int troughInfluenceRadius() {
        return this.troughInfluenceRadius;
    }

    public void setTroughInfluenceRadius(int value) {
        this.troughInfluenceRadius = clamp(value, 2, 8);
        if (this.troughInteractionRadius > this.troughInfluenceRadius) {
            this.troughInteractionRadius = this.troughInfluenceRadius;
        }
    }

    public int troughInteractionRadius() {
        return this.troughInteractionRadius;
    }

    public void setTroughInteractionRadius(int value) {
        this.troughInteractionRadius =
                clamp(value, 1, this.troughInfluenceRadius);
    }

    public int maxNearbyBabies() {
        return this.maxNearbyBabies;
    }

    public void setMaxNearbyBabies(int value) {
        this.maxNearbyBabies = clamp(value, 1, 64);
    }

    public int maxNearbyAdults() {
        return this.maxNearbyAdults;
    }

    public void setMaxNearbyAdults(int value) {
        this.maxNearbyAdults = clamp(value, 1, 128);
    }

    public PopulationCountingMode populationCountingMode() {
        return this.populationCountingMode;
    }

    public void setPopulationCountingMode(PopulationCountingMode value) {
        this.populationCountingMode = value == null
                ? PopulationCountingMode.ALL_NEARBY_LIVESTOCK
                : value;
    }

    public boolean automaticTroughBreeding() {
        return this.automaticTroughBreeding;
    }

    public void setAutomaticTroughBreeding(boolean value) {
        this.automaticTroughBreeding = value;
    }

    public int foodBowlHealingRadius() {
        return this.foodBowlHealingRadius;
    }

    public void setFoodBowlHealingRadius(int value) {
        this.foodBowlHealingRadius = clamp(value, 1, 8);
    }

    public boolean villageDecorationsEnabled() {
        return this.villageDecorationsEnabled;
    }

    public void setVillageDecorationsEnabled(boolean value) {
        this.villageDecorationsEnabled = value;
    }

    public int foodBowlVillageChancePercent() {
        return this.foodBowlVillageChancePercent;
    }

    public void setFoodBowlVillageChancePercent(int value) {
        this.foodBowlVillageChancePercent = clamp(value, 0, 100);
    }

    public int feedingTroughVillageChancePercent() {
        return this.feedingTroughVillageChancePercent;
    }

    public void setFeedingTroughVillageChancePercent(int value) {
        this.feedingTroughVillageChancePercent = clamp(value, 0, 100);
    }

    public boolean villageChestLootEnabled() {
        return this.villageChestLootEnabled;
    }

    public void setVillageChestLootEnabled(boolean value) {
        this.villageChestLootEnabled = value;
    }

    public int villageChestLootChancePercent() {
        return this.villageChestLootChancePercent;
    }

    public void setVillageChestLootChancePercent(int value) {
        this.villageChestLootChancePercent = clamp(value, 0, 100);
    }

    public boolean worldgenDiagnostics() {
        return this.worldgenDiagnostics;
    }

    public void setWorldgenDiagnostics(boolean value) {
        this.worldgenDiagnostics = value;
    }
}
