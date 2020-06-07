package com.toxicglados.alcoholism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.toxicglados.alcoholism.Alcoholism;
import com.toxicglados.alcoholism.container.FermentingVatContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class FermentingVatScreen extends ContainerScreen<FermentingVatContainer> {

    private static final ResourceLocation BACKGROUND_TEXTURE = new ResourceLocation(Alcoholism.MOD_ID, "textures/gui/tahona_gui.png");

    private final int PROGRESS_ARROW_X = 80;
    private final int PROGRESS_ARROW_Y = 35;
    private final int PROGRESS_ARROW_OFFSET_X = 176;
    private final int PROGRESS_ARROW_OFFSET_Y = 0;
    private final int PROGRESS_ARROW_WIDTH = 22;
    private final int PROGRESS_ARROW_HEIGHT = 15;

    public FermentingVatScreen(FermentingVatContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);

        // Just an FYI: These default to
        // xSize = 176
        // ySize = 166
        // see ContainerScreen implementation for details
        // IMPORTANT: Minecraft is expecting a 256x256 image. Any bigger (and probably smaller) and weird stuff happening
        // These tell the super class how big the image is and it is used to calculate stuff during rendering
        // setting it wrong might not always look wrong, but it can be affect things so be careful i guess?
        this.xSize = 176;
        this.ySize = 166;
        // This sets up the guiLeft and guiTop which are used similarly to xSize and ySize
        this.init();

        //this.tileEntity = screenContainer.getTileEntity();
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
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8.0f, 70.0f, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        this.minecraft.getTextureManager().bindTexture(BACKGROUND_TEXTURE);

        // Blitting actually draws the texture,
        // The first two arguments define where on the screen it goes
        // The next two define which pixels to start reading from on the texture
        // The last two arguments define how big the texture is in the image
        // essentially the last four arguments allow you have multiple pieces in one image (see texture/gui/* for examples)
        this.blit(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        int progressArrowWidth = container.getCookProgressionScaled(PROGRESS_ARROW_WIDTH);


        this.blit(this.guiLeft + PROGRESS_ARROW_X, this.guiTop + PROGRESS_ARROW_Y, PROGRESS_ARROW_OFFSET_X, PROGRESS_ARROW_OFFSET_Y, progressArrowWidth, PROGRESS_ARROW_HEIGHT);

    }
}
