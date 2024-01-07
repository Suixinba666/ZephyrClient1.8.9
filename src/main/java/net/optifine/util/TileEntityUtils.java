package net.optifine.util;

import net.minecraft.src.Config;
import net.minecraft.tileentity.*;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;


public class TileEntityUtils {
    public static String getTileEntityName(IBlockAccess blockAccess, BlockPos blockPos) {
        TileEntity tileentity = blockAccess.getTileEntity(blockPos);
        return getTileEntityName(tileentity);
    }

    public static String getTileEntityName(TileEntity te) {
        if (!(te instanceof IWorldNameable)) {
            return null;
        } else {
            IWorldNameable iworldnameable = (IWorldNameable) te;
            updateTileEntityName(te);
            return !iworldnameable.hasCustomName() ? null : iworldnameable.getName();
        }
    }

    public static void updateTileEntityName(TileEntity te) {
        BlockPos blockpos = te.getPos();
        String s = getTileEntityRawName(te);

        if (s == null) {
            String s1 = getServerTileEntityRawName(blockpos);
            s1 = Config.normalize(s1);
            setTileEntityRawName(te, s1);
        }
    }

    public static String getServerTileEntityRawName(BlockPos blockPos) {
        TileEntity tileentity = IntegratedServerUtils.getTileEntity(blockPos);
        return tileentity == null ? null : getTileEntityRawName(tileentity);
    }

    public static String getTileEntityRawName(TileEntity te) {
        if (te instanceof TileEntityBeacon) {
            return ((TileEntityBeacon) te).customName;
        } else if (te instanceof TileEntityBrewingStand) {
            return ((TileEntityBrewingStand) te).customName;
        } else if (te instanceof TileEntityEnchantmentTable) {
            return ((TileEntityEnchantmentTable) te).customName;
        } else if (te instanceof TileEntityFurnace) {
            return ((TileEntityFurnace) te).furnaceCustomName;
        } else {
            if (te instanceof IWorldNameable) {
                IWorldNameable iworldnameable = (IWorldNameable) te;

                if (iworldnameable.hasCustomName()) {
                    return iworldnameable.getName();
                }
            }

            return null;
        }
    }

    public static void setTileEntityRawName(TileEntity te, String name) {
        if (te instanceof TileEntityBeacon) {
            ((TileEntityBeacon) te).customName = name;
        } else if (te instanceof TileEntityBrewingStand) {
            ((TileEntityBrewingStand) te).customName = name;
        } else if (te instanceof TileEntityEnchantmentTable) {
            ((TileEntityEnchantmentTable) te).customName = name;
        } else if (te instanceof TileEntityFurnace) {
            ((TileEntityFurnace) te).furnaceCustomName = name;
        } else if (te instanceof TileEntityChest) {
            ((TileEntityChest) te).setCustomName(name);
        } else if (te instanceof TileEntityDispenser) {
            ((TileEntityDispenser) te).setCustomName(name);
        } else if (te instanceof TileEntityHopper) {
            ((TileEntityHopper) te).setCustomName(name);
        } else {
        }
    }
}
