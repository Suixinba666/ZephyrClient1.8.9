package tech.imxianyu.utils.skin;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.beans.ConstructorProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayerSkinTextureCache {
    private final Map<UUID, ResourceLocation> loadedSkins = new HashMap<UUID, ResourceLocation>();
    private final Map<String, ResourceLocation> loadedUsernameSkins = new HashMap<String, ResourceLocation>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(3);
    private final SkinManager skinManager;
    private final MinecraftSessionService minecraftSessionService;

    @ConstructorProperties(value = {"skinManager", "minecraftSessionService"})
    public PlayerSkinTextureCache(SkinManager skinManager, MinecraftSessionService minecraftSessionService) {
        this.skinManager = skinManager;
        this.minecraftSessionService = minecraftSessionService;
    }

    public ResourceLocation getSkinTexture(GameProfile gameProfile) {
        if (gameProfile == null) {
            return DefaultPlayerSkin.getDefaultSkinLegacy();
        }
        UUID uuid = gameProfile.getId();
        ResourceLocation loadedSkinResource = this.loadedSkins.get(uuid);
        if (loadedSkinResource == null) {
            loadedSkinResource = DefaultPlayerSkin.getDefaultSkinLegacy();
            this.loadedSkins.put(uuid, loadedSkinResource);
            this.requestTexture(gameProfile);
        }
        return loadedSkinResource;
    }

    public ResourceLocation getSkinTexture(UUID uuid) {
        if (uuid == null) {
            return DefaultPlayerSkin.getDefaultSkinLegacy();
        }
        ResourceLocation loadedSkinResource = this.loadedSkins.get(uuid);
        if (loadedSkinResource == null) {
            loadedSkinResource = DefaultPlayerSkin.getDefaultSkinLegacy();
            this.loadedSkins.put(uuid, loadedSkinResource);
            this.requestTexture(new GameProfile(uuid, "Steve"));
        }
        return loadedSkinResource;
    }

    public ResourceLocation getSkinTexture(String username) {
        if (username == null) {
            return DefaultPlayerSkin.getDefaultSkinLegacy();
        }
        ResourceLocation loadedSkinResource = this.loadedUsernameSkins.get(username);
        if (loadedSkinResource == null) {
            loadedSkinResource = DefaultPlayerSkin.getDefaultSkinLegacy();
            this.loadedUsernameSkins.put(username, loadedSkinResource);
            UUIDFetcher.getUUID(username, new Consumer<UUID>() {

                @Override
                public void accept(UUID uuid) {
                    String username = UUIDFetcher.getName(uuid);
                    GameProfile gameProfile = new GameProfile(uuid, username);
                    PlayerSkinTextureCache.this.requestTexture(gameProfile);
                }
            });
        }
        return loadedSkinResource;
    }

    public ResourceLocation getCachedSkinTexture(GameProfile gameProfile) {
        if (gameProfile != null) {
            Minecraft minecraft = Minecraft.getMinecraft();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(gameProfile);
            if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                return minecraft.getSkinManager().loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            }
            UUID uuid = EntityPlayer.getUUID(gameProfile);
            return DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        return DefaultPlayerSkin.getDefaultSkinLegacy();
    }

    private void requestTexture(final GameProfile gameProfile) {
        MinecraftProfileTexture minecraftProfileTexture = null;
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = this.skinManager.loadSkinFromCache(gameProfile);
        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
            minecraftProfileTexture = map.get(MinecraftProfileTexture.Type.SKIN);
        }
        if (minecraftProfileTexture == null) {
            this.executorService.execute(new Runnable() {

                @Override
                public void run() {
                    MinecraftProfileTexture requestedProfileTexture;
                    if (!gameProfile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId())) {
                        PlayerSkinTextureCache.this.minecraftSessionService.fillProfileProperties(gameProfile, false);
                    }
                    if ((requestedProfileTexture = PlayerSkinTextureCache.this.getMinecraftProfileTexture(gameProfile, MinecraftProfileTexture.Type.SKIN)) != null) {
                        Minecraft.getMinecraft().addScheduledTask(new Runnable() {

                            @Override
                            public void run() {
                                PlayerSkinTextureCache.this.loadSkinTexture(gameProfile, requestedProfileTexture);
                            }
                        });
                    }
                }
            });
        } else {
            this.loadSkinTexture(gameProfile, minecraftProfileTexture);
        }
    }

    private void loadSkinTexture(final GameProfile gameProfile, MinecraftProfileTexture profileTexture) {
        this.skinManager.loadSkin(profileTexture, MinecraftProfileTexture.Type.SKIN, new SkinManager.SkinAvailableCallback() {

            @Override
            public void skinAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
                if (typeIn == MinecraftProfileTexture.Type.SKIN) {
                    PlayerSkinTextureCache.this.loadedSkins.put(gameProfile.getId(), location);
                    PlayerSkinTextureCache.this.loadedUsernameSkins.put(gameProfile.getName(), location);
                }
            }
        });
    }

    private MinecraftProfileTexture getMinecraftProfileTexture(GameProfile gameProfile, MinecraftProfileTexture.Type type) {
        HashMap map = Maps.newHashMap();
        try {
            map.putAll(this.minecraftSessionService.getTextures(gameProfile, false));
        } catch (InsecureTextureException insecureTextureException) {
            // empty catch block
        }
        if (map.isEmpty() && gameProfile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId())) {
            gameProfile.getProperties().clear();
            gameProfile.getProperties().putAll(Minecraft.getMinecraft().getProfileProperties());
            map.putAll(this.minecraftSessionService.getTextures(gameProfile, false));
        }
        return (MinecraftProfileTexture) map.get(type);
    }
}

