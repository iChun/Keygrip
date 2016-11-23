package me.ichun.mods.keygrip.client.gui.window;

import me.ichun.mods.ichunutil.client.gui.window.element.Element;
import me.ichun.mods.ichunutil.client.render.RendererHelper;
import me.ichun.mods.keygrip.client.gui.GuiWorkspace;
import me.ichun.mods.keygrip.client.gui.window.element.ElementSceneTab;
import me.ichun.mods.keygrip.common.scene.Scene;

import java.util.ArrayList;

public class WindowSceneSelection extends WindowTopDock
{
    public ArrayList<Scene> scenes = new ArrayList<Scene>();
    public int selectedScene;

    public WindowSceneSelection(GuiWorkspace parent, int w, int h)
    {
        super(parent, w, h);

        elements.clear();

        selectedScene = -1;
    }

    @Override
    public void elementTriggered(Element element)
    {
        selectedScene = element.id;
    }

    @Override
    public void draw(int mouseX, int mouseY) //4 pixel border?
    {
        if(scenes.isEmpty() || width <= 0)
        {
            return;
        }
        super.draw(mouseX, mouseY);
        RendererHelper.drawColourOnScreen(workspace.currentTheme.tabSideInactive[0], workspace.currentTheme.tabSideInactive[1], workspace.currentTheme.tabSideInactive[2], 255, posX, posY, width, 1, 0);
    }

    @Override
    public void resized()
    {
        for(Element element : elements)
        {
            element.resized();
        }
        if(!workspace.levels.get(0).isEmpty())
        {
            posX = workspace.levels.get(0).get(0).width - 2;
        }
        else
        {
            posX = 0;
        }
        posY = workspace.TOP_DOCK_HEIGHT + 1;
        if(!workspace.levels.get(1).isEmpty())
        {
            width = workspace.width - posX - workspace.levels.get(1).get(0).width + 2;
        }
        else
        {
            width = workspace.width - posX;
        }
        height = 12;
    }

    @Override
    public void shutdown()
    {
        for(Scene scene : scenes)
        {
            scene.destroy();
        }
    }

    public void addScene(Scene scene)
    {
        scenes.add(scene);
        elements.add(new ElementSceneTab(this, 0, 0, 10, 10, elements.size(), scene));
        if(scenes.size() == 1)
        {
            changeScene(scenes.size() - 1);
        }

        resized();
    }

    public void removeScene(String ident)
    {
        for(int i = scenes.size() - 1; i >= 0; i--)
        {
            Scene project = scenes.get(i);
            if(project.identifier.equals(ident))
            {
                project.destroy();
                scenes.remove(i);
                if(i == selectedScene || selectedScene == scenes.size())
                {
                    selectedScene--;
                    if(selectedScene < 0 && !scenes.isEmpty())
                    {
                        selectedScene = 0;
                    }
                }
                changeScene(selectedScene);
                break;
            }
        }

        ArrayList<Element> els = new ArrayList<Element>(elements);
        for(int i = scenes.size() - 1; i >= 0; i--)
        {
            Scene project = scenes.get(i);
            for(Element e : elements)
            {
                if(e instanceof ElementSceneTab)
                {
                    ElementSceneTab tab = (ElementSceneTab)e;
                    if(tab.info.identifier.equals(project.identifier))
                    {
                        tab.id = i;
                        els.remove(e);
                    }
                }
            }
        }

        for(int i = els.size() - 1; i >= 0; i--)
        {
            if(els.get(i) instanceof ElementSceneTab)
            {
                elements.remove(els.get(i));
            }
        }
        //        if(selectedScene >= 0)
        //        {
        //            updateModelTree(projects.get(selectedProject));
        //        }
        //        else
        //        {
        //            ((GuiWorkspace)workspace).windowModelTree.modelList.trees.clear();
        //            ((GuiWorkspace)workspace).windowAnimate.animList.trees.clear();
        //            ((GuiWorkspace)workspace).windowControls.selectedObject = null;
        //            ((GuiWorkspace)workspace).windowControls.refresh = true;
        //        }

        resized();
    }

    public void changeScene(Scene info)
    {
        for(int i = 0; i < scenes.size(); i++)
        {
            if(scenes.get(i) == info)
            {
                changeScene(i);
                return;
            }
        }
    }

    public void changeScene(int i)
    {
        selectedScene = i;
        parent.timeline.timeline.selectedIdentifier = "";
        parent.timeline.timeline.setCurrentPos(0);
        parent.timeline.timeline.focusOnTicker();
    }

    @Override
    public int getHeight()
    {
        return 12;
    }
}
