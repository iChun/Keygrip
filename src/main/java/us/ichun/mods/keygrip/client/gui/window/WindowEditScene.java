package us.ichun.mods.keygrip.client.gui.window;

import net.minecraft.util.StatCollector;
import us.ichun.mods.ichunutil.client.gui.Theme;
import us.ichun.mods.ichunutil.client.gui.window.IWorkspace;
import us.ichun.mods.ichunutil.client.gui.window.Window;
import us.ichun.mods.ichunutil.client.gui.window.element.Element;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementButton;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementNumberInput;
import us.ichun.mods.ichunutil.client.gui.window.element.ElementTextInput;
import us.ichun.mods.keygrip.client.gui.GuiWorkspace;
import us.ichun.mods.keygrip.common.scene.Scene;

public class WindowEditScene extends Window
{
    public Scene currentScene;

    public WindowEditScene(IWorkspace parent, int x, int y, int w, int h, int minW, int minH)
    {
        super(parent, x, y, w, h, minW, minH, "window.editScene.title", true);

        elements.add(new ElementTextInput(this, 10, 30, width - 20, 12, 1, "window.newScene.name", ((GuiWorkspace)workspace).getOpenScene().name));
        elements.add(new ElementNumberInput(this, 10, 65, 160, 12, -1, "window.editScene.position", 3, true, -30000000, 30000000, ((GuiWorkspace)workspace).getOpenScene().startPos[0] / (double)Scene.PRECISION, ((GuiWorkspace)workspace).getOpenScene().startPos[1] / (double)Scene.PRECISION, ((GuiWorkspace)workspace).getOpenScene().startPos[2] / (double)Scene.PRECISION));

        elements.add(new ElementButton(this, width - 140, height - 30, 60, 16, 100, false, 1, 1, "element.button.ok"));
        elements.add(new ElementButton(this, width - 70, height - 30, 60, 16, 0, false, 1, 1, "element.button.cancel"));

        currentScene = ((GuiWorkspace)parent).getOpenScene();
    }

    @Override
    public void draw(int mouseX, int mouseY)
    {
        super.draw(mouseX, mouseY);
        if(!minimized)
        {
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.newScene.name"), posX + 11, posY + 20, Theme.getAsHex(workspace.currentTheme.font), false);
            workspace.getFontRenderer().drawString(StatCollector.translateToLocal("window.editScene.position"), posX + 11, posY + 55, Theme.getAsHex(workspace.currentTheme.font), false);
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
            int[] startPos = new int[3];
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
                else if(elements.get(i) instanceof ElementNumberInput)
                {
                    ElementNumberInput nums = (ElementNumberInput)elements.get(i);
                    startPos = new int[] { (int)Math.round(Double.parseDouble(nums.textFields.get(0).getText()) * Scene.PRECISION), (int)Math.round(Double.parseDouble(nums.textFields.get(1).getText()) * Scene.PRECISION), (int)Math.round(Double.parseDouble(nums.textFields.get(2).getText()) * Scene.PRECISION) };
                }
            }
            if(projName.isEmpty())
            {
                return;
            }
            currentScene.name = projName;
            currentScene.startPos = startPos;
            ((GuiWorkspace)workspace).sceneManager.resized();
            workspace.removeWindow(this, true);
        }
    }
}
