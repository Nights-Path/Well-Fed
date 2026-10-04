package com.nightspath.wellfed.client.screen;

import com.nightspath.wellfed.WellFed;
import com.nightspath.wellfed.menu.FoodBowlMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class FoodBowlScreen extends AbstractContainerScreen<FoodBowlMenu> {
    private static final Identifier TEXTURE =
            WellFed.id("textures/gui/container/food_bowl.png");
    private static final int TEXTURE_WIDTH = 176;
    private static final int TEXTURE_HEIGHT = 132;

    public FoodBowlScreen(FoodBowlMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 5;
        this.inventoryLabelY = 39;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos,
                this.topPos,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }
}
