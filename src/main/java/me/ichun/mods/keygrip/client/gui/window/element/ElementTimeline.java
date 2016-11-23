package me.ichun.mods.keygrip.client.gui.window.element;

import me.ichun.mods.ichunutil.client.gui.Theme;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.keygrip.client.gui.GuiWorkspace;
import me.ichun.mods.keygrip.common.scene.Scene;
import me.ichun.mods.keygrip.common.scene.action.Action;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Mouse;

public class ElementTimeline extends Element
{
    public int mX;
    public int mY;

    public int tickWidth = 5;

    private int currentPos;

    public String selectedIdentifier;

    public double sliderProgVert;
    public double sliderProgHori;

    public static final int buttonsWidth = 121; //including the 1 pix border.

    public ElementTimeline(Window window, int x, int y, int w, int h, int ID)
    {
        super(window, x, y, w, h, ID, false);

        selectedIdentifier = "";
    }

    @Override
    public void draw(int mouseX, int mouseY, boolean hover)
    {
        this.mX = mouseX;
        this.mY = mouseY;
        //        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeBorder[0], parent.workspace.currentTheme.elementTreeBorder[1], parent.workspace.currentTheme.elementTreeBorder[2], 255, getPosX() + 1, getPosY() + 1, width - 2, height - 2, 0);
        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeBorder[0], parent.workspace.currentTheme.elementTreeBorder[1], parent.workspace.currentTheme.elementTreeBorder[2], 255, getPosX() + buttonsWidth - 1, getPosY(), 1, height, 0);

        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, getPosX() + buttonsWidth, getPosY() + height - 19, width - buttonsWidth, 20, 0); //timeline bg

        RendererHelper.startGlScissor(getPosX(), getPosY(), buttonsWidth - 1, height - 20);

        //get total animation element height
        int size = 0;
        boolean hasScrollVert = false;

        int timeWidth = 0;

        Scene currentScene = null;

        if(!((GuiWorkspace)parent.workspace).sceneManager.scenes.isEmpty())
        {
            currentScene = ((GuiWorkspace)parent.workspace).sceneManager.scenes.get(((GuiWorkspace)parent.workspace).sceneManager.selectedScene);
        }

        if(currentScene != null)
        {
            final int spacingY = 13;
            int offY = 0;
            for(Action action : currentScene.actions)
            {
                if(action.startKey + action.getLength() > timeWidth)
                {
                    timeWidth = action.startKey + action.getLength();
                }
                offY += spacingY;
            }

            size = offY;
            hasScrollVert = size > height - 20;
        }

        boolean hasScrollHori = timeWidth + 20 > Math.floor((float)(width - buttonsWidth) / (float)tickWidth);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0D, (double)-((size - (height - 20)) * sliderProgVert), 0D);

        //draw animation elements
        if(currentScene != null)
        {
            final int spacingY = 13;

            int idClicked = -1;
            if(mouseX < posX + buttonsWidth - 1 && mouseX >= posX && mouseY >= posY && mouseY < posY + height)
            {
                idClicked = (int)(mouseY - posY + ((size - (height - 20)) * sliderProgVert)) / 13; //spacing = 13
            }

            int idHovered = 0;
            int offY = 0;
            for(Action action : currentScene.actions)
            {
                drawCompElement(action.name, offY, action.identifier.equals(selectedIdentifier), action.hidden == 1, idClicked == idHovered);

                idHovered++;
                offY += spacingY;
            }
        }
        GlStateManager.popMatrix();

        //Timeline
        RendererHelper.startGlScissor(getPosX() + buttonsWidth, getPosY() - 1, width - (hasScrollVert ? buttonsWidth + 10 : buttonsWidth), height + 3);

        if(Mouse.isButtonDown(0) && mouseInBoundary(mouseX, mouseY) && parent.workspace.elementHovered == this && parent.workspace.hoverTime > 5)
        {
            if(mouseX > posX + buttonsWidth - 1 && mouseX < posX + (hasScrollVert ? width - 10 : width) && mouseY < posY + height - 10)
            {
                double tickPos = (int)(mouseX - (posX + buttonsWidth - 1 - 1) + (hasScrollHori ? ((double)(((timeWidth + 20) * tickWidth) - (width - (hasScrollVert ? buttonsWidth + 10 : buttonsWidth))) * sliderProgHori) : 0));
                setCurrentPos((int)Math.max(0, tickPos / (double)tickWidth));
                if(currentScene != null && currentScene.playing)
                {
                    currentScene.playTime = currentPos;
                }
            }
        }

        if(hasScrollHori)
        {
            int x2 = getPosY() + height - 10;

            int timelineWidth = hasScrollVert ? (width - 10) : width;

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, getPosX() + buttonsWidth + ((timelineWidth - buttonsWidth) / 40), x2 + 4, (timelineWidth - buttonsWidth) - (((timelineWidth - buttonsWidth) / 40) * 2), 2, 0);

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, getPosX() + buttonsWidth + (((timelineWidth - buttonsWidth) - ((timelineWidth - buttonsWidth) / 11)) * sliderProgHori), x2, Math.floor((float)(timelineWidth - buttonsWidth) / 11D), 10, 0);
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBar[0], parent.workspace.currentTheme.elementTreeScrollBar[1], parent.workspace.currentTheme.elementTreeScrollBar[2], 255, getPosX() + buttonsWidth + 1 + (((timelineWidth - buttonsWidth) - ((timelineWidth - buttonsWidth) / 11)) * sliderProgHori), x2 + 1, Math.floor(((float)(timelineWidth - buttonsWidth) / 11D) - 2), 8, 0);

            int sbx1 = getPosX() + buttonsWidth - parent.posX;
            int sbx2 = getPosX() + 1 + timelineWidth - parent.posX;
            int sby1 = x2 + 1 - parent.posY;
            ;
            int sby2 = sby1 + 10;

            if(Mouse.isButtonDown(0) && mouseX >= sbx1 && mouseX <= sbx2 && mouseY >= sby1 && mouseY <= sby2)
            {
                sbx1 += 10;
                sbx2 -= 10;
                sliderProgHori = 1.0F - MathHelper.clamp_double((double)(sbx2 - mouseX) / (double)(sbx2 - sbx1), 0.0D, 1.0D);
            }
        }
        else
        {
            sliderProgHori = 0.0D;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(-((((timeWidth + 20) * tickWidth) - (hasScrollVert ? width - (buttonsWidth + 10) : width - buttonsWidth)) * sliderProgHori), 0D, 0D);

        int tick = 0;
        int timeOffX = 0;
        while(getPosX() + buttonsWidth - 1 + timeOffX < parent.posX + parent.width || timeOffX < (timeWidth + 20) * tickWidth)
        {
            if(tick % 5 == 0)
            {
                RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, getPosX() + buttonsWidth - 0.5D + timeOffX, getPosY(), 1, height - 19, 0);
                RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, getPosX() + buttonsWidth - 1 + timeOffX, getPosY() + height - 19, 2, 7, 0);
                GlStateManager.pushMatrix();
                float scale = 0.5F;
                GlStateManager.scale(scale, scale, scale);
                parent.workspace.getFontRenderer().drawString(Integer.toString(tick), (int)((getPosX() + buttonsWidth + 2 + timeOffX) / scale), (int)((getPosY() + height - 16) / scale), Theme.getAsHex(tick == currentPos ? parent.workspace.currentTheme.font : parent.workspace.currentTheme.fontDim), false);
                GlStateManager.popMatrix();
            }
            else
            {
                RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, getPosX() + buttonsWidth - 1 + timeOffX, getPosY() + height - 19, 2, 2, 0);
            }
            tick++;
            timeOffX += tickWidth;
        }

        RendererHelper.startGlScissor(getPosX() + buttonsWidth, getPosY(), width - (hasScrollVert ? buttonsWidth + 10 : buttonsWidth), height - 20);

        GlStateManager.pushMatrix();
        GlStateManager.translate(0D, (double)-((size - (height - 20)) * sliderProgVert), 0D);

        //Animation Component areas
        if(currentScene != null)
        {
            final int spacingY = 13;

            int idClicked = -1;
            if(mouseX < posX + buttonsWidth - 1 && mouseX >= posX && mouseY >= posY && mouseY < posY + height)
            {
                idClicked = (int)(mouseY - posY + ((size - (height - 20)) * sliderProgVert)) / 13; //spacing = 13
            }

            int idHovered = 0;
            int offY = 0;
            for(Action action : currentScene.actions)
            {
                //draw stuff
                int[] lineClr = parent.workspace.currentTheme.elementTreeItemBg;
                if(action.identifier.equals(selectedIdentifier))
                {
                    lineClr = parent.workspace.currentTheme.elementTreeItemBgSelect;
                }
                else if(idClicked == idHovered)
                {
                    lineClr = parent.workspace.currentTheme.elementTreeItemBgHover;
                }

                RendererHelper.drawColourOnScreen(lineClr[0], lineClr[1], lineClr[2], 255, getPosX() + buttonsWidth - 2D + (action.startKey * tickWidth), getPosY() + offY + 4.5D, 4, 4, 0);
                for(Integer i : action.actionComponents.keySet())
                {
                    RendererHelper.drawColourOnScreen(lineClr[0], lineClr[1], lineClr[2], 255, getPosX() + buttonsWidth - 2D + ((action.startKey + i) * tickWidth), getPosY() + offY + 4.5D, 4, 4, 0);
                }

                RendererHelper.drawColourOnScreen(lineClr[0], lineClr[1], lineClr[2], 255, getPosX() + buttonsWidth - 2D + (action.startKey * tickWidth), getPosY() + offY + 5.5D, ((action.getLength()) * tickWidth), 2, 0);

                idHovered++;
                offY += spacingY;
            }
        }
        GlStateManager.popMatrix();

        RendererHelper.startGlScissor(getPosX() + buttonsWidth, getPosY() - 1, width - (hasScrollVert ? buttonsWidth + 10 : buttonsWidth), height + 3);

        GlStateManager.pushMatrix();
        if(currentScene != null)
        {
            if(currentScene.playing)
            {
                setCurrentPos(currentScene.playTime);
                focusOnTicker();
                if(currentPos < currentScene.getLength())
                {
                    GlStateManager.translate(tickWidth + ((GuiWorkspace)parent.workspace).renderTick, 0F, 0F);
                }
            }
            else
            {
                currentScene.playTime = currentPos;
            }
        }
        //Timeline cursor
        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.tabBorder[0], parent.workspace.currentTheme.tabBorder[1], parent.workspace.currentTheme.tabBorder[2], 255, getPosX() + buttonsWidth - 0.5D + (currentPos * tickWidth), getPosY(), 1, height - 19, 0);
        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.tabBorder[0], parent.workspace.currentTheme.tabBorder[1], parent.workspace.currentTheme.tabBorder[2], 255, getPosX() + buttonsWidth - 2D + (currentPos * tickWidth), getPosY() + height - 19, 4, 1.5D, 0);

        if(currentPos % 5 != 0)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, (getPosX() + buttonsWidth - 1 + (currentPos * tickWidth)) - (parent.workspace.getFontRenderer().getStringWidth(Integer.toString(currentPos)) / 2) * 0.5F, getPosY() + height - 17, parent.workspace.getFontRenderer().getStringWidth(Integer.toString(currentPos)) / 2 + 2, 5, 0);//blocks underlying number
            GlStateManager.pushMatrix();
            float scale = 0.5F;
            GlStateManager.scale(scale, scale, scale);
            parent.workspace.getFontRenderer().drawString(Integer.toString(currentPos), (int)((getPosX() + buttonsWidth + (currentPos * tickWidth)) / scale) - parent.workspace.getFontRenderer().getStringWidth(Integer.toString(currentPos)) / 2, (int)((getPosY() + height - 16) / scale), Theme.getAsHex(parent.workspace.currentTheme.font), false);
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();

        GlStateManager.popMatrix();

        RendererHelper.startGlScissor(getPosX() + buttonsWidth, getPosY() - 1, width - buttonsWidth, height + 3); //vert scroll bar

        if(hasScrollVert)
        {
            int x2 = getPosX() + width - 10;

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, x2, getPosY(), 10, height - 19, 0);

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, x2 + 4, getPosY() + ((height - 20) / 40), 2, (height - 20) - (((height - 20) / 40) * 2), 0);

            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBarBorder[0], parent.workspace.currentTheme.elementTreeScrollBarBorder[1], parent.workspace.currentTheme.elementTreeScrollBarBorder[2], 255, x2, getPosY() + (((height - 20) - ((height - 20) / 11)) * sliderProgVert), 10, Math.ceil((float)(height - 20) / 10D), 0);
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeScrollBar[0], parent.workspace.currentTheme.elementTreeScrollBar[1], parent.workspace.currentTheme.elementTreeScrollBar[2], 255, x2 + 1, getPosY() + 1 + (((height - 20) - ((height - 20) / 11)) * sliderProgVert), 8, Math.ceil(((float)(height - 20) / 10D) - 2), 0);

            int sbx1 = x2 + 1 - parent.posX;
            int sbx2 = sbx1 + 10;
            int sby1 = getPosY() - parent.posY;
            int sby2 = getPosY() + 1 + (height - 20) - parent.posY;

            if(Mouse.isButtonDown(0) && mouseX >= sbx1 && mouseX <= sbx2 && mouseY >= sby1 && mouseY <= sby2)
            {
                sby1 += 10;
                sby2 -= 10;
                sliderProgVert = 1.0F - MathHelper.clamp_double((double)(sby2 - mouseY) / (double)(sby2 - sby1), 0.0D, 1.0D);
            }
        }
        else
        {
            sliderProgVert = 0.0D;
        }

        //reset current scissor
        if(parent.isTab)
        {
            RendererHelper.startGlScissor(parent.posX + 1, parent.posY + 1 + 12, parent.getWidth() - 2, parent.getHeight() - 2 - 12);
        }
        else
        {
            RendererHelper.startGlScissor(parent.posX + 1, parent.posY + 1, parent.getWidth() - 2, parent.getHeight() - 2);
        }
    }

    @Override
    public boolean onClick(int mouseX, int mouseY, int id)
    {
        if(mouseX < posX + 100)
        {
            //get total animation element height
            int size = 0;
            Scene currentScene = null;

            if(((GuiWorkspace)parent.workspace).hasOpenScene())
            {
                currentScene = ((GuiWorkspace)parent.workspace).getOpenScene();
            }

            if(currentScene != null)
            {
                final int spacingY = 13;
                size = currentScene.actions.size() * spacingY;

                int idClicked = (int)(mouseY - posY + ((size - (height - 20)) * sliderProgVert)) / 13; //spacing = 13


                for(int i = 0; i < currentScene.actions.size(); i++)
                {
                    if(idClicked == i)
                    {
                        selectedIdentifier = currentScene.actions.get(i).identifier;
                        if(id == 1)
                        {
                            currentScene.actions.get(i).hidden = currentScene.actions.get(i).hidden == 1 ? 0 : 1;
                        }
                        break;
                    }
                }
            }
        }
        return false;//return true for elements that has input eg typing
    }

    @Override
    public boolean mouseScroll(int mouseX, int mouseY, int k)
    {
        //get total animation element height
        int size = 0;
        int timeWidth = 0;
        Scene currentScene = null;

        if(!((GuiWorkspace)parent.workspace).sceneManager.scenes.isEmpty())
        {
            currentScene = ((GuiWorkspace)parent.workspace).sceneManager.scenes.get(((GuiWorkspace)parent.workspace).sceneManager.selectedScene);
        }

        if(currentScene != null)
        {
            final int spacingY = 13;
            int offY = 0;
            for(Action action : currentScene.actions)
            {
                if(action.startKey + action.getLength() > timeWidth)
                {
                    timeWidth = action.startKey + action.getLength();
                }
                offY += spacingY;
            }

            size = offY;
        }
        if(GuiScreen.isShiftKeyDown())
        {
            if(timeWidth + 20 > Math.floor((float)(width - 101) / (float)tickWidth))
            {
                sliderProgHori += 0.05D * -k;
                sliderProgHori = MathHelper.clamp_double(sliderProgHori, 0.0D, 1.0D);
            }
        }
        else
        {
            if(size > height - 20)
            {
                sliderProgVert += 0.05D * -k;
                sliderProgVert = MathHelper.clamp_double(sliderProgVert, 0.0D, 1.0D);
            }
        }

        return false;
    }

    public void focusOnTicker()
    {
        int totalWidth = 0;
        boolean hasScrollVert = false;

        Scene currentScene = null;

        if(!((GuiWorkspace)parent.workspace).sceneManager.scenes.isEmpty())
        {
            currentScene = ((GuiWorkspace)parent.workspace).sceneManager.scenes.get(((GuiWorkspace)parent.workspace).sceneManager.selectedScene);

            final int spacingY = 13;
            int offY = 0;

            for(Action a : currentScene.actions)
            {
                offY += spacingY;
            }

            totalWidth = (currentScene.getLength() + 20) * tickWidth;
            hasScrollVert = offY > height - 20;
        }

        int elementWidth = width - (hasScrollVert ? buttonsWidth + 10 : buttonsWidth);
        int tickerPos = currentPos * tickWidth;

        if(tickerPos < elementWidth)
        {
            sliderProgHori = 0.0D;
        }
        else
        {
            int hiddenWidth = totalWidth - elementWidth;
            if(tickerPos > elementWidth + sliderProgHori * hiddenWidth || tickerPos < hiddenWidth * sliderProgHori)
            {
                sliderProgHori = MathHelper.clamp_double((tickerPos - (elementWidth / 3D)) / (double)hiddenWidth, 0.0D, 1.0D);
            }
        }
    }

    public void setCurrentPos(int i)
    {
        currentPos = i;
    }

    public int getCurrentPos()
    {
        return currentPos;
    }

    @Override
    public boolean mouseInBoundary(int mouseX, int mouseY)
    {
        return mouseX >= this.posX && mouseX <= this.posX + this.width && mouseY >= this.posY && mouseY <= this.posY + this.height && !(mouseX < this.posX + buttonsWidth - 1 && mouseY > this.posY + this.height - 20);
    }

    public void drawCompElement(String name, int offY, boolean isSelected, boolean isHidden, boolean hover)
    {
        RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBorder[0], parent.workspace.currentTheme.elementTreeItemBorder[1], parent.workspace.currentTheme.elementTreeItemBorder[2], 255, getPosX(), getPosY() + offY, buttonsWidth - 1, 13, 0);

        if(isSelected)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBgSelect[0], parent.workspace.currentTheme.elementTreeItemBgSelect[1], parent.workspace.currentTheme.elementTreeItemBgSelect[2], 255, getPosX() + 1, getPosY() + offY + 1, buttonsWidth - 1 - 2, 13 - 2, 0);
        }
        else if(hover)
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBgHover[0], parent.workspace.currentTheme.elementTreeItemBgHover[1], parent.workspace.currentTheme.elementTreeItemBgHover[2], 255, getPosX() + 1, getPosY() + offY + 1, buttonsWidth - 1 - 2, 13 - 2, 0);
        }
        else
        {
            RendererHelper.drawColourOnScreen(parent.workspace.currentTheme.elementTreeItemBg[0], parent.workspace.currentTheme.elementTreeItemBg[1], parent.workspace.currentTheme.elementTreeItemBg[2], 255, getPosX() + 1, getPosY() + offY + 1, buttonsWidth - 1 - 2, 13 - 2, 0);
        }
        if(isHidden)
        {
            parent.workspace.getFontRenderer().drawString(parent.workspace.reString(name, buttonsWidth - 1), getPosX() + 4, getPosY() + offY + 2, Theme.getAsHex(parent.workspace.currentTheme.fontDim), false);
        }
        else
        {
            parent.workspace.getFontRenderer().drawString(parent.workspace.reString(name, buttonsWidth - 1), getPosX() + 4, getPosY() + offY + 2, Theme.getAsHex(parent.workspace.currentTheme.font), false);
        }
    }

    @Override
    public void resized()
    {
        posX = 1;
        width = parent.width - 2;
        posY = 13;
        height = parent.height - 14;
    }
}
