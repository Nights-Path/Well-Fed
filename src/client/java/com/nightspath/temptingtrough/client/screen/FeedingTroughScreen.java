package com.nightspath.temptingtrough.client.screen;

import com.nightspath.temptingtrough.menu.FeedingTroughMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class FeedingTroughScreen extends AbstractContainerScreen<FeedingTroughMenu> {
    private static final Identifier HOPPER_TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/container/hopper.png");

    public FeedingTroughScreen(
            FeedingTroughMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title, 176, 133);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                HOPPER_TEXTURE,
                this.leftPos,
                this.topPos,
                0.0F,
                0.0F,
                this.imageWidth,
                this.imageHeight,
                256,
                256
        );
    }
}
