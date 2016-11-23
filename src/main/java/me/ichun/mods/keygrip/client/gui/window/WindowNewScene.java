package me.ichun.mods.keygrip.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.Theme;
import me.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import me.ichun.mods.ichunutil.client.gui.window.Window;
import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import me.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import me.ichun.mods.keygrip.client.gui.GuiWorkspace;
import me.ichun.mods.keygrip.common.scene.Scene;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.translation.I18n;

public class WindowNewScene extends Window
{
    public WindowNewScene(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.newScene.title", true);

        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 1, "window.newScene.name"));

        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(I18n.translateToLocal("window.newScene.name"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
        }
    }

    @Override
    public void elementTriggered(Element element)
    {
        if(element.id == 0)
        {
            workspace.removeWindow(this, true);
        }
        if(element.id > 0)
        {
            String projName = "";
            for(int i = 0; i < elements.size(); i++)
            {
                if(elements.get(i) instanceof ElementTextInput)
                {
                    ElementTextInput text = (ElementTextInput)elements.get(i);
                    if(text.id == 1)
                    {
                        projName = text.textField.getText();
                    }
                }
            }
            if(projName.isEmpty())
            {
                projName = "NewScene";
            }
            Scene scene = new Scene(projName);
            ((GuiWorkspace)workspace).sceneManager.addScene(scene);
            Minecraft mc = Minecraft.getMinecraft();
            scene.startPos = new int[] { (int)Math.round(mc.thePlayer.posX * Scene.PRECISION), (int)Math.round(mc.thePlayer.posY * Scene.PRECISION), (int)Math.round(mc.thePlayer.posZ * Scene.PRECISION) };
            workspace.removeWindow(this, true);
        }
    }
}
