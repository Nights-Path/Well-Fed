package com.nightspath.wellfed.client.config;

import com.nightspath.wellfed.config.WellFedConfig;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class WellFedConfigScreen extends Screen {
    private final Screen parent;
    private WellFedConfig working;

    public WellFedConfigScreen(Screen parent) {
        super(Component.translatable("well_fed.config.title"));
        this.parent = parent;
        this.working = WellFedConfig.copyCurrent();
    }

    @Override
    protected void init() {
        boolean editable = editable();
        int gap = 6;
        int columnWidth = Math.min(190, Math.max(130, (this.width - 30 - gap) / 2));
        int totalWidth = columnWidth * 2 + gap;
        int left = (this.width - totalWidth) / 2;
        int right = left + columnWidth + gap;
        int top = 32;
        int row = 24;

        Button title = addRenderableWidget(Button.builder(
                Component.translatable("well_fed.config.title"),
                button -> {}
        ).bounds((this.width - Math.min(300, this.width - 20)) / 2, 8,
                Math.min(300, this.width - 20), 20).build());
        title.active = false;

        addIntRow(
                left,
                top,
                columnWidth,
                editable,
                () -> {
                    int size = this.working.troughInfluenceRadius() * 2 + 1;
                    return Component.translatable(
                            "well_fed.config.trough_influence",
                            this.working.troughInfluenceRadius(),
                            size,
                            size,
                            size
                    );
                },
                this.working::troughInfluenceRadius,
                this.working::setTroughInfluenceRadius,
                2,
                8
        );

        addIntRow(
                left,
                top + row,
                columnWidth,
                editable,
                () -> {
                    int size = this.working.troughInteractionRadius() * 2 + 1;
                    return Component.translatable(
                            "well_fed.config.trough_interaction",
                            this.working.troughInteractionRadius(),
                            size,
                            size,
                            size
                    );
                },
                this.working::troughInteractionRadius,
                this.working::setTroughInteractionRadius,
                1,
                this.working.troughInfluenceRadius()
        );

        addIntRow(
                left,
                top + row * 2,
                columnWidth,
                editable,
                () -> Component.translatable(
                        "well_fed.config.max_babies",
                        this.working.maxNearbyBabies()
                ),
                this.working::maxNearbyBabies,
                this.working::setMaxNearbyBabies,
                1,
                64
        );

        addIntRow(
                left,
                top + row * 3,
                columnWidth,
                editable,
                () -> Component.translatable(
                        "well_fed.config.max_adults",
                        this.working.maxNearbyAdults()
                ),
                this.working::maxNearbyAdults,
                this.working::setMaxNearbyAdults,
                1,
                128
        );

        Button populationMode = addRenderableWidget(Button.builder(
                populationModeLabel(),
                button -> {
                    this.working.setPopulationCountingMode(
                            this.working.populationCountingMode()
                                            == WellFedConfig.PopulationCountingMode.ALL_NEARBY_LIVESTOCK
                                    ? WellFedConfig.PopulationCountingMode.SAME_SPECIES_ONLY
                                    : WellFedConfig.PopulationCountingMode.ALL_NEARBY_LIVESTOCK
                    );
                    button.setMessage(populationModeLabel());
                }
        ).bounds(left, top + row * 4, columnWidth, 20).build());
        populationMode.active = editable;

        Button breeding = addRenderableWidget(Button.builder(
                toggleLabel(
                        "well_fed.config.auto_breeding",
                        this.working.automaticTroughBreeding()
                ),
                button -> {
                    this.working.setAutomaticTroughBreeding(
                            !this.working.automaticTroughBreeding()
                    );
                    button.setMessage(toggleLabel(
                            "well_fed.config.auto_breeding",
                            this.working.automaticTroughBreeding()
                    ));
                }
        ).bounds(left, top + row * 5, columnWidth, 20).build());
        breeding.active = editable;

        addIntRow(
                right,
                top,
                columnWidth,
                editable,
                () -> {
                    int size = this.working.foodBowlHealingRadius() * 2 + 1;
                    return Component.translatable(
                            "well_fed.config.bowl_radius",
                            this.working.foodBowlHealingRadius(),
                            size,
                            size,
                            size
                    );
                },
                this.working::foodBowlHealingRadius,
                this.working::setFoodBowlHealingRadius,
                1,
                8
        );

        Button village = addRenderableWidget(Button.builder(
                toggleLabel(
                        "well_fed.config.village_decorations",
                        this.working.villageDecorationsEnabled()
                ),
                button -> {
                    this.working.setVillageDecorationsEnabled(
                            !this.working.villageDecorationsEnabled()
                    );
                    button.setMessage(toggleLabel(
                            "well_fed.config.village_decorations",
                            this.working.villageDecorationsEnabled()
                    ));
                }
        ).bounds(right, top + row, columnWidth, 20).build());
        village.active = editable;

        addIntRow(
                right,
                top + row * 2,
                columnWidth,
                editable,
                () -> Component.translatable(
                        "well_fed.config.bowl_village_chance",
                        this.working.foodBowlVillageChancePercent()
                ),
                this.working::foodBowlVillageChancePercent,
                this.working::setFoodBowlVillageChancePercent,
                0,
                100
        );

        addIntRow(
                right,
                top + row * 3,
                columnWidth,
                editable,
                () -> Component.translatable(
                        "well_fed.config.trough_village_chance",
                        this.working.feedingTroughVillageChancePercent()
                ),
                this.working::feedingTroughVillageChancePercent,
                this.working::setFeedingTroughVillageChancePercent,
                0,
                100
        );

        Button chestLoot = addRenderableWidget(Button.builder(
                toggleLabel(
                        "well_fed.config.village_chest_loot",
                        this.working.villageChestLootEnabled()
                ),
                button -> {
                    this.working.setVillageChestLootEnabled(
                            !this.working.villageChestLootEnabled()
                    );
                    button.setMessage(toggleLabel(
                            "well_fed.config.village_chest_loot",
                            this.working.villageChestLootEnabled()
                    ));
                }
        ).bounds(right, top + row * 4, columnWidth, 20).build());
        chestLoot.active = editable;

        addIntRow(
                right,
                top + row * 5,
                columnWidth,
                editable,
                () -> Component.translatable(
                        "well_fed.config.chest_loot_chance",
                        this.working.villageChestLootChancePercent()
                ),
                this.working::villageChestLootChancePercent,
                this.working::setVillageChestLootChancePercent,
                0,
                100
        );

        int bottom = top + row * 6 + 8;

        if (!editable) {
            Button notice = addRenderableWidget(Button.builder(
                    Component.translatable("well_fed.config.remote_notice")
                            .withStyle(ChatFormatting.GRAY),
                    button -> {}
            ).bounds((this.width - Math.min(360, this.width - 20)) / 2,
                    bottom, Math.min(360, this.width - 20), 20).build());
            notice.active = false;
            bottom += 24;
        }

        Button done = addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> saveAndClose()
        ).bounds(this.width / 2 - 102, bottom, 100, 20).build());
        done.active = editable;

        addRenderableWidget(Button.builder(
                Component.translatable("gui.cancel"),
                button -> closeWithoutSaving()
        ).bounds(this.width / 2 + 2, bottom, 100, 20).build());
    }

    private void addIntRow(
            int x,
            int y,
            int width,
            boolean editable,
            Supplier<Component> label,
            IntSupplier getter,
            IntConsumer setter,
            int min,
            int max
    ) {
        int side = 20;
        int centerWidth = Math.max(40, width - side * 2 - 4);

        Button value = addRenderableWidget(Button.builder(
                label.get(),
                button -> {}
        ).bounds(x + side + 2, y, centerWidth, 20).build());
        value.active = false;

        Button minus = addRenderableWidget(Button.builder(
                Component.literal("-"),
                button -> {
                    setter.accept(Math.max(min, getter.getAsInt() - 1));
                    value.setMessage(label.get());
                }
        ).bounds(x, y, side, 20).build());

        Button plus = addRenderableWidget(Button.builder(
                Component.literal("+"),
                button -> {
                    setter.accept(Math.min(max, getter.getAsInt() + 1));
                    value.setMessage(label.get());
                }
        ).bounds(x + side + 2 + centerWidth + 2, y, side, 20).build());

        minus.active = editable;
        plus.active = editable;
    }

    private Component populationModeLabel() {
        String key = this.working.populationCountingMode()
                        == WellFedConfig.PopulationCountingMode.SAME_SPECIES_ONLY
                ? "well_fed.config.population_mode.same_species"
                : "well_fed.config.population_mode.all";
        return Component.translatable(
                "well_fed.config.population_mode",
                Component.translatable(key)
        );
    }

    private static Component toggleLabel(String key, boolean enabled) {
        return Component.translatable(
                key,
                Component.translatable(enabled ? "options.on" : "options.off")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)
        );
    }

    private boolean editable() {
        if (this.minecraft == null) {
            return false;
        }
        return this.minecraft.level == null || this.minecraft.hasSingleplayerServer();
    }

    private void saveAndClose() {
        WellFedConfig.applyAndSave(this.working);
        closeWithoutSaving();
    }

    private void closeWithoutSaving() {
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(this.parent);
        }
    }

    @Override
    public void onClose() {
        closeWithoutSaving();
    }
}
