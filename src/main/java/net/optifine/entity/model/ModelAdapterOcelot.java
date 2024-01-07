package net.optifine.entity.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelOcelot;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderOcelot;
import net.minecraft.entity.passive.EntityOcelot;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelAdapterOcelot extends ModelAdapter {
    private static Map<String, Integer> mapPartFields = null;

    public ModelAdapterOcelot() {
        super(EntityOcelot.class, "ocelot", 0.4F);
    }

    public ModelBase makeModel() {
        return new ModelOcelot();
    }

    public ModelRenderer getModelRenderer(ModelBase model, String modelPart) {
        if (!(model instanceof ModelOcelot)) {
            return null;
        } else {
            ModelOcelot modelocelot = (ModelOcelot) model;
            Map<String, Integer> map = getMapPartFields();

            if (map.containsKey(modelPart)) {
                int i = map.get(modelPart).intValue();

                List<ModelRenderer> list = Arrays.asList(
                        modelocelot.ocelotBackLeftLeg,
                        modelocelot.ocelotBackRightLeg,
                        modelocelot.ocelotFrontLeftLeg,
                        modelocelot.ocelotFrontRightLeg,
                        modelocelot.ocelotTail,
                        modelocelot.ocelotTail2,
                        modelocelot.ocelotHead,
                        modelocelot.ocelotBody
                );

                return list.get(i);
            } else {
                return null;
            }
        }
    }

    public String[] getModelRendererNames() {
        return new String[]{"back_left_leg", "back_right_leg", "front_left_leg", "front_right_leg", "tail", "tail2", "head", "body"};
    }

    private static Map<String, Integer> getMapPartFields() {
        if (mapPartFields == null) {
            mapPartFields = new HashMap();
            mapPartFields.put("back_left_leg", Integer.valueOf(0));
            mapPartFields.put("back_right_leg", Integer.valueOf(1));
            mapPartFields.put("front_left_leg", Integer.valueOf(2));
            mapPartFields.put("front_right_leg", Integer.valueOf(3));
            mapPartFields.put("tail", Integer.valueOf(4));
            mapPartFields.put("tail2", Integer.valueOf(5));
            mapPartFields.put("head", Integer.valueOf(6));
            mapPartFields.put("body", Integer.valueOf(7));
        }
        return mapPartFields;
    }

    public IEntityRenderer makeEntityRender(ModelBase modelBase, float shadowSize) {
        RenderManager rendermanager = Minecraft.getMinecraft().getRenderManager();
        return new RenderOcelot(rendermanager, modelBase, shadowSize);
    }
}
