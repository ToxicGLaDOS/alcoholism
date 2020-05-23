package com.toxicglados.alcoholism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.container.DistilleryContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.tileentity.FurnaceTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DistilleryScreen extends ContainerScreen<DistilleryContainer> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Alcoholism.MOD_ID, "textures/gui/furnace_gui.png");

    public DistilleryScreen(DistilleryContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.guiLeft = 0;
        this.guiTop = 0;

        this.xSize = 256;
        this.ySize = 240;
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        // 8.0f and 6.0f are the top left of where to start drawing the string
        // 4210752 is the default color
        this.font.drawString(this.title.getFormattedText(), 8.0f, 6.0f, 4210752);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0f, 103.0f, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindTexture(BACKGROUND_TEXTURE);
        // this.width and this.height are the screen width and height
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.blit(x, y, 0, 0, this.xSize, this.ySize);
    }
}
