package me.crafter.mc.lockettepro;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class LocketteProAPI {

    public static BlockFace[] newsfaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    public static BlockFace[] allfaces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    public static boolean isLocked(Block block){
        if (block == null) return false;
        switch (block.getType()){
        // Double Doors
        case OAK_DOOR:
        case SPRUCE_DOOR:
        case BIRCH_DOOR:
        case JUNGLE_DOOR:
        case ACACIA_DOOR:
        case DARK_OAK_DOOR:
        case CRIMSON_DOOR:
        case WARPED_DOOR:
        case IRON_DOOR:
            Block[] doors = getDoors(block);
            if (doors == null) return false;
            for (BlockFace doorface : newsfaces){
                Block relative0 = doors[0].getRelative(doorface), relative1 = doors[1].getRelative(doorface);
                if (relative0.getType() == doors[0].getType() && relative1.getType() == doors[1].getType()){
                    if (isLockedSingleBlock(relative1.getRelative(BlockFace.UP), doorface.getOppositeFace())) return true;
                    if (isLockedSingleBlock(relative1, doorface.getOppositeFace())) return true;
                    if (isLockedSingleBlock(relative0, doorface.getOppositeFace())) return true;
                    if (isLockedSingleBlock(relative0.getRelative(BlockFace.DOWN), doorface.getOppositeFace())) return true;
                }
            }
            if (isLockedSingleBlock(doors[1].getRelative(BlockFace.UP), null)) return true;
            if (isLockedSingleBlock(doors[1], null)) return true;
            if (isLockedSingleBlock(doors[0], null)) return true;
            if (isLockedSingleBlock(doors[0].getRelative(BlockFace.DOWN), null)) return true;
            break;
        case LECTERN:
        	return isLockedSingleBlock(block, null);
        // Chests (Second block only)
        case CHEST:
        case TRAPPED_CHEST:
            // Check second chest sign
            BlockFace chestface = getRelativeChestFace(block);
            if (chestface != null) {
                Block relativechest = block.getRelative(chestface);
                if (isLockedSingleBlock(relativechest, chestface.getOppositeFace())) return true;
            }
            // Don't break here
        // Everything else (First block of container check goes here)
        default:
            if (isLockedSingleBlock(block, null)) return true;
            break;
        }
        return false;
    }

    public static String getOwner(Block block){
        switch (block.getType()){
        // Double Doors
        case OAK_DOOR:
        case SPRUCE_DOOR:
        case BIRCH_DOOR:
        case JUNGLE_DOOR:
        case ACACIA_DOOR:
        case DARK_OAK_DOOR:
        case CRIMSON_DOOR:
        case WARPED_DOOR:
        case IRON_DOOR:
            Block[] doors = getDoors(block);
            if (doors == null) return null;
            for (BlockFace doorface : newsfaces){
                Block relative0 = doors[0].getRelative(doorface), relative1 = doors[1].getRelative(doorface);
                if (relative0.getType() == doors[0].getType() && relative1.getType() == doors[1].getType()){
                    String f1 = getLockOwnerSingleBlock(relative1.getRelative(BlockFace.UP), doorface.getOppositeFace());
                    String f2 = getLockOwnerSingleBlock(relative1, doorface.getOppositeFace());
                    String f3 = getLockOwnerSingleBlock(relative0, doorface.getOppositeFace());
                    String f4 = getLockOwnerSingleBlock(relative0.getRelative(BlockFace.DOWN), doorface.getOppositeFace());
                    if (f1 != null) {
                    	return f1;
                    } else if (f2 != null) {
                    	return f2;
                    } else if (f3 != null) {
                    	return f3;
                    } else if (f4 != null) {
                    	return f4;
                    }
                }
            }
            String f1 = getLockOwnerSingleBlock(doors[1].getRelative(BlockFace.UP), null);
            String f2 = getLockOwnerSingleBlock(doors[1], null);
            String f3 = getLockOwnerSingleBlock(doors[0], null);
            String f4 = getLockOwnerSingleBlock(doors[0].getRelative(BlockFace.DOWN), null);
            if (f1 != null) {
            	return f1;
            } else if (f2 != null) {
            	return f2;
            } else if (f3 != null) {
            	return f3;
            } else if (f4 != null) {
            	return f4;
            }
            break;
        case LECTERN:
            return getLockOwnerSingleBlock(block, null);
        case CHEST:
        case TRAPPED_CHEST:
            // Check second chest sign
            BlockFace chestface = getRelativeChestFace(block);
            if (chestface != null) {
                Block relativechest = block.getRelative(chestface);
                return getLockOwnerSingleBlock(relativechest, chestface.getOppositeFace());
            }
            // Don't break here
        // Everything else (First block of container check goes here)
        default:
            return getLockOwnerSingleBlock(block, null);
        }
        return null;
    }

    public static boolean isOwner(Block block, Player player){
        switch (block.getType()){
        // Double Doors
        case OAK_DOOR:
        case SPRUCE_DOOR:
        case BIRCH_DOOR:
        case JUNGLE_DOOR:
        case ACACIA_DOOR:
        case DARK_OAK_DOOR:
        case CRIMSON_DOOR:
        case WARPED_DOOR:
        case IRON_DOOR:
            Block[] doors = getDoors(block);
            if (doors == null) return false;
            for (BlockFace doorface : newsfaces){
                Block relative0 = doors[0].getRelative(doorface), relative1 = doors[1].getRelative(doorface);
                if (relative0.getType() == doors[0].getType() && relative1.getType() == doors[1].getType()){
                    if (isOwnerSingleBlock(relative1.getRelative(BlockFace.UP), doorface.getOppositeFace(), player)) return true;
                    if (isOwnerSingleBlock(relative1, doorface.getOppositeFace(), player)) return true;
                    if (isOwnerSingleBlock(relative0, doorface.getOppositeFace(), player)) return true;
                    if (isOwnerSingleBlock(relative0.getRelative(BlockFace.DOWN), doorface.getOppositeFace(), player)) return true;
                }
            }
            if (isOwnerSingleBlock(doors[1].getRelative(BlockFace.UP), null, player)) return true;
            if (isOwnerSingleBlock(doors[1], null, player)) return true;
            if (isOwnerSingleBlock(doors[0], null, player)) return true;
            if (isOwnerSingleBlock(doors[0].getRelative(BlockFace.DOWN), null, player)) return true;
            break;
        // Chests (Second block only)
        case LECTERN:
        	return isOwnerSingleBlock(block, null, player);
        case CHEST:
        case TRAPPED_CHEST:
            // Check second chest sign
            BlockFace chestface = getRelativeChestFace(block);
            if (chestface != null) {
                Block relativechest = block.getRelative(chestface);
                if (isOwnerSingleBlock(relativechest, chestface.getOppositeFace(), player)) return true;
            }
            // Don't break here
        // Everything else (First block of container check goes here)
        default:
            if (isOwnerSingleBlock(block, null, player)) return true;
            break;
        }
        return false;
    }

    public static boolean isUser(Block block, Player player){
        switch (block.getType()){
        // Double Doors
        case OAK_DOOR:
        case SPRUCE_DOOR:
        case BIRCH_DOOR:
        case JUNGLE_DOOR:
        case ACACIA_DOOR:
        case DARK_OAK_DOOR:
        case CRIMSON_DOOR:
        case WARPED_DOOR:
        case IRON_DOOR:
            Block[] doors = getDoors(block);
            if (doors == null) return false;
            for (BlockFace doorface : newsfaces){
                Block relative0 = doors[0].getRelative(doorface), relative1 = doors[1].getRelative(doorface);
                if (relative0.getType() == doors[0].getType() && relative1.getType() == doors[1].getType()){
                    if (isUserSingleBlock(relative1.getRelative(BlockFace.UP), doorface.getOppositeFace(), player)) return true;
                    if (isUserSingleBlock(relative1, doorface.getOppositeFace(), player)) return true;
                    if (isUserSingleBlock(relative0, doorface.getOppositeFace(), player)) return true;
                    if (isUserSingleBlock(relative0.getRelative(BlockFace.DOWN), doorface.getOppositeFace(), player)) return true;
                }
            }
            if (isUserSingleBlock(doors[1].getRelative(BlockFace.UP), null, player)) return true;
            if (isUserSingleBlock(doors[1], null, player)) return true;
            if (isUserSingleBlock(doors[0], null, player)) return true;
            if (isUserSingleBlock(doors[0].getRelative(BlockFace.DOWN), null, player)) return true;
            break;
        // Lecterns
        case LECTERN:
        	return isUserSingleBlock(block, null, player);
        // Chests (Second block only)
        case CHEST:
        case TRAPPED_CHEST:
            // Check second chest sign
            BlockFace chestface = getRelativeChestFace(block);
            if (chestface != null) {
                Block relativechest = block.getRelative(chestface);
                if (isUserSingleBlock(relativechest, chestface.getOppositeFace(), player)) return true;
            }
            // Don't break here
        // Everything else (First block of container check goes here)
        default:
            if (isUserSingleBlock(block, null, player)) return true;
            break;
        }
        return false;
    }

    public static boolean isProtected(Block block){
        return (isLockSign(block) || isLocked(block) || isUpDownLockedDoor(block));
    }

    public static boolean isLockedSingleBlock(Block block, BlockFace exempt){
        for (BlockFace blockface : newsfaces){
            if (blockface == exempt) continue;
            Block relativeblock = block.getRelative(blockface);
            // Find [Private] sign?
            if (isLockSign(relativeblock) && getFacing(relativeblock) == blockface){
                // Found [Private] sign, is expire turned on and expired? (relativeblock is now sign)
                if (Config.isLockExpire() && LocketteProAPI.isSignExpired(relativeblock)) {
                    continue; // Private sign but expired... But impossible to have 2 [Private] signs anyway?
                }
                return true;
            }
        }
        return false;
    }

    public static boolean isOwnerSingleBlock(Block block, BlockFace exempt, Player player){ // Requires isLocked
        for (BlockFace blockface : newsfaces){
            if (blockface == exempt) continue;
            Block relativeblock = block.getRelative(blockface);
            if (isLockSign(relativeblock) && getFacing(relativeblock) == blockface){
                if (isOwnerOnSign(relativeblock, player)){
                    return true;
                }
            }
        }
        return false;
    }

    public static String getLockOwnerSingleBlock(Block block, BlockFace exempt) {
    	for (BlockFace blockface : newsfaces){
            if (blockface == exempt) continue;
            Block relativeblock = block.getRelative(blockface);
            if (isLockSign(relativeblock) && getFacing(relativeblock) == blockface){
                return getOwnerOnSign(relativeblock);
            }
        }
    	return null;
    }

    public static boolean isUserSingleBlock(Block block, BlockFace exempt, Player player){ // Requires isLocked
        for (BlockFace blockface : newsfaces){
            if (blockface == exempt) continue;
            Block relativeblock = block.getRelative(blockface);
            if (isLockSignOrAdditionalSign(relativeblock) && getFacing(relativeblock) == blockface){
                if (isUserOnSign(relativeblock, player)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isOwnerOfSign(Block block, Player player){ // Requires isSign
        Block protectedblock = getAttachedBlock(block);
        // Normal situation, that block is just locked by an adjacent sign
        if (isOwner(protectedblock, player)) return true;
        // Situation where double door's block
        if (isUpDownLockedDoor(protectedblock) && isOwnerUpDownLockedDoor(protectedblock, player)) return true;
        // Otherwise...
        return false;
    }

    public static boolean isLockable(Block block){
        Material material = block.getType();
        //Bad blocks
        if(Tag.SIGNS.isTagged(material)){
            return false;
        }
        if (Config.isLockable(material)){ // Directly lockable
            return true;
        } else { // Indirectly lockable
            Block blockup = block.getRelative(BlockFace.UP);
            if (blockup != null && isUpDownAlsoLockableBlock(blockup)) return true;
            Block blockdown = block.getRelative(BlockFace.DOWN);
            if (blockdown != null && isUpDownAlsoLockableBlock(blockdown)) return true;
            return false;
        }
    }


    public static boolean isChest(@Nullable Block block){
        if (block == null) {
            return false;
        }
        switch (block.getType()){
        case CHEST:
        case TRAPPED_CHEST:
            return true;
        default:
            return false;
        }
    }

    public static boolean isUpDownAlsoLockableBlock(Block block){
        if (Config.isLockable(block.getType())){
            switch (block.getType()){
            case OAK_DOOR:
            case SPRUCE_DOOR:
            case BIRCH_DOOR:
            case JUNGLE_DOOR:
            case ACACIA_DOOR:
            case DARK_OAK_DOOR:
            case CRIMSON_DOOR:
            case WARPED_DOOR:
            case IRON_DOOR:
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    public static boolean mayInterfere(Block block, Player player){
        if (block.getState() instanceof Container) {
            for (BlockFace blockface : allfaces) {
                Block newblock = block.getRelative(blockface);
                if (isLocked(newblock) && !isOwner(newblock, player)) {
                    return true;
                }
            }
        }
        // if LEFT may interfere RIGHT
        switch (block.getType()){
        case OAK_DOOR:
        case SPRUCE_DOOR:
        case BIRCH_DOOR:
        case JUNGLE_DOOR:
        case ACACIA_DOOR:
        case DARK_OAK_DOOR:
        case CRIMSON_DOOR:
        case WARPED_DOOR:
        case IRON_DOOR:
            for (BlockFace blockface : newsfaces){
                Block newblock = block.getRelative(blockface);
                switch (newblock.getType()){
                case OAK_DOOR:
                case SPRUCE_DOOR:
                case BIRCH_DOOR:
                case JUNGLE_DOOR:
                case ACACIA_DOOR:
                case DARK_OAK_DOOR:
                case CRIMSON_DOOR:
                case WARPED_DOOR:
                case IRON_DOOR:
                    if (isLocked(newblock) && !isOwner(newblock, player)){
                        return true;
                    }
                default:
                    break;
                }
            }
            // Temp workaround bad code for checking up and down signs
            Block newblock2 = block.getRelative(BlockFace.UP, 2);
            switch (newblock2.getType()){
            default:
                if (isLocked(newblock2) && !isOwner(newblock2, player)){
                    return true;
                }
                break;
            }
            Block newblock3 = block.getRelative(BlockFace.DOWN, 1);
            switch (newblock3.getType()){
            default:
                if (isLocked(newblock3) && !isOwner(newblock3, player)){
                    return true;
                }
                break;
            }
            break;
            // End temp workaround bad code for checking up and down signs
        case CHEST:
        case TRAPPED_CHEST:
        case OAK_WALL_SIGN:
        case SPRUCE_SIGN:
        case BIRCH_SIGN:
        case JUNGLE_SIGN:
        case ACACIA_SIGN:
        case DARK_OAK_SIGN:
            for (BlockFace blockface : allfaces){
                Block newblock = block.getRelative(blockface);
                switch (newblock.getType()){
                case CHEST:
                case TRAPPED_CHEST:
                    if (isLockedSingleBlock(newblock, null) && !isOwnerSingleBlock(newblock, null, player)){
                        return true;
                    }
                default:
                    break;
                }
            }
            break;
        // This is extra interfere block
        case HOPPER:
        case DISPENSER:
        case DROPPER:
            if (!Config.isInterferePlacementBlocked()) return false;
            for (BlockFace blockface : allfaces){
                Block newblock = block.getRelative(blockface);
                switch (newblock.getType()){
                case CHEST:
                case TRAPPED_CHEST:
                case HOPPER:
                case DISPENSER:
                case DROPPER:
                    if (isLocked(newblock) && !isOwner(newblock, player)){
                        return true;
                    }
                default:
                    break;
                }
            }
            break;
        default:
            break;
        }
        return false;
    }

    public static boolean isSign(Block block){
        return Tag.WALL_SIGNS.isTagged(block.getType());
    }

    public static boolean isLockSign(Block block){
        return isSign(block) && isLockString(((Sign)block.getState()).getLine(0));
    }

    public static boolean isAdditionalSign(Block block){
        return isSign(block) && isAdditionalString(((Sign)block.getState()).getLine(0));
    }

    public static boolean isLockSignOrAdditionalSign(Block block){
        if (isSign(block)){
            String line = ((Sign)block.getState()).getLine(0);
            return isLockStringOrAdditionalString(line);
        } else {
            return false;
        }
    }

    public static boolean isOwnerOnSign(Block block, Player player){ // Requires isLockSign
        String[] lines = ((Sign)block.getState()).getLines();
        if (Utils.isPlayerOnLine(player, lines[1])){
            if (Config.isUuidEnabled()){
                Utils.updateLineByPlayer(block, 1, player);
            }
            return true;
        }
        return false;
    }

    public static String getOwnerOnSign(Block block){ // Requires isLockSign
        String[] lines = ((Sign)block.getState()).getLines();
        return lines[1];
    }

    public static boolean isUserOnSign(Block block, Player player){ // Requires (isLockSign or isAdditionalSign)
        String[] lines = ((Sign)block.getState()).getLines();
        // Normal
        for (int i = 1; i < 4; i ++){
            if (Utils.isPlayerOnLine(player, lines[i])){
                if (Config.isUuidEnabled()){
                    Utils.updateLineByPlayer(block, i, player);
                }
                return true;
            } else if (Config.isEveryoneSignString(lines[i])) {
                return true;
            }
        }
        // For Vault & Scoreboard
        for (int i = 1; i < 4; i++) {
            if (Dependency.isPermissionGroupOf(lines[i], player)) return true;
            if (Dependency.isScoreboardTeamOf(lines[i], player)) return true;
        }
        return false;
    }

    public static boolean isSignExpired(Block block){
        if (!isSign(block) || !isLockSign(block)) return false;
        return isLineExpired(((Sign)block.getState()).getLine(0));
    }

    public static boolean isLineExpired(String line){
        long createdtime = Utils.getCreatedFromLine(line);
        if (createdtime == -1L) return false; // No expire
        long currenttime = (int)(System.currentTimeMillis()/1000);
        return createdtime + Config.getLockExpireDays() * 86400L < currenttime;
    }

    public static boolean isUpDownLockedDoor(@Nullable Block block){
        if (block == null) {
            return false;
        }
        Block blockup = block.getRelative(BlockFace.UP);
        if (blockup != null && isUpDownAlsoLockableBlock(blockup) && isLocked(blockup)) return true;
        Block blockdown = block.getRelative(BlockFace.DOWN);
        if (blockdown != null && isUpDownAlsoLockableBlock(blockdown) && isLocked(blockdown)) return true;
        return false;
    }

    public static boolean isOwnerUpDownLockedDoor(Block block, Player player){
        Block blockup = block.getRelative(BlockFace.UP);
        if (blockup != null && isUpDownAlsoLockableBlock(blockup) && isOwner(blockup, player)) return true;
        Block blockdown = block.getRelative(BlockFace.DOWN);
        if (blockdown != null && isUpDownAlsoLockableBlock(blockdown) && isOwner(blockdown, player)) return true;
        return false;
    }

    public static boolean isUserUpDownLockedDoor(Block block, Player player){
        Block blockup = block.getRelative(BlockFace.UP);
        if (blockup != null && isUpDownAlsoLockableBlock(blockup) && isUser(blockup, player)) return true;
        Block blockdown = block.getRelative(BlockFace.DOWN);
        if (blockdown != null && isUpDownAlsoLockableBlock(blockdown) && isUser(blockdown, player)) return true;
        return false;
    }

    public static boolean isLockString(String line){
        if (line.contains("#")) line = line.split("#", 2)[0];
        return Config.isPrivateSignString(line);
    }

    public static boolean isAdditionalString(String line){
        if (line.contains("#")) line = line.split("#", 2)[0];
        return Config.isAdditionalSignString(line);
    }

    public static boolean isLockStringOrAdditionalString(String line){
        return isLockString(line) || isAdditionalString(line);
    }

    public static Block getAttachedBlock(Block sign){ // Requires isSign
        BlockFace facing = getFacing(sign);
        return sign.getRelative(facing.getOppositeFace());
    }

    public static int getTimerOnSigns(Block block){
        for (BlockFace blockface : newsfaces){
            Block relative = block.getRelative(blockface);
            if (isSign(relative)){
                Sign sign = (Sign)relative.getState();
                for (String line : sign.getLines()){
                    int linetime = Config.getTimer(line);
                    if (linetime > 0) return linetime;
                }
            }
        }
        return 0;
    }

    public static int getTimerDoor(Block block){
        int timersingle = getTimerSingleDoor(block);
        if (timersingle > 0) return timersingle;
        for (BlockFace blockface : newsfaces){
            Block relative = block.getRelative(blockface);
            timersingle = getTimerSingleDoor(relative);
            if (timersingle > 0) return timersingle;
        }
        return 0;
    }

    public static int getTimerSingleDoor(Block block){
        Block[] doors = getDoors(block);
        if (doors == null) return 0;
        Block relativeup = doors[1].getRelative(BlockFace.UP);
        int relativeuptimer = getTimerOnSigns(relativeup);
        if (relativeuptimer > 0) return relativeuptimer;
        int doors0 = getTimerOnSigns(doors[0]);
        if (doors0 > 0) return doors0;
        int doors1 = getTimerOnSigns(doors[1]);
        if (doors1 > 0) return doors1;
        Block relativedown = doors[0].getRelative(BlockFace.DOWN);
        int relativedowntimer = getTimerOnSigns(relativedown);
        if (relativedowntimer > 0) return relativedowntimer;
        return 0;
    }

    public static Block[] getDoors(Block block){
        Block[] doors = new Block[2];
        boolean found = false;
        Block up = block.getRelative(BlockFace.UP), down = block.getRelative(BlockFace.DOWN);
        if (up.getType() == block.getType()){
            found = true;
            doors[0] = block; doors[1] = up;
        }
        if (down.getType() == block.getType()){
            if (found == true){ // error 3 doors
                return null;
            }
            doors[1] = block; doors[0] = down;
            found = true;
        }
        if (!found){ // error 1 door
            return null;
        }
        return doors;
    }

    public static boolean isDoubleDoorBlock(@Nullable Block block){
        if (block == null) {
            return false;
        }
        return Tag.DOORS.isTagged(block.getType());
    }

    public static boolean isSingleDoorBlock(@Nullable Block block){
        if (block == null) {
            return false;
        }
        switch (block.getType()){
        case OAK_FENCE_GATE:
        case SPRUCE_FENCE_GATE:
        case BIRCH_FENCE_GATE:
        case CRIMSON_FENCE_GATE:
        case WARPED_FENCE_GATE:
        case ACACIA_FENCE_GATE:
        case JUNGLE_FENCE_GATE:
        case ACACIA_TRAPDOOR:
        case DARK_OAK_TRAPDOOR:
        case CRIMSON_TRAPDOOR:
        case WARPED_TRAPDOOR:
        case BIRCH_TRAPDOOR:
        case OAK_TRAPDOOR:
        case SPRUCE_TRAPDOOR:
        case IRON_TRAPDOOR:
            return true;
        default:
            return false;
        }
    }

    public static Block getBottomDoorBlock(Block block){ // Requires isDoubleDoorBlock || isSingleDoorBlock
        if (isDoubleDoorBlock(block)){
            Block relative = block.getRelative(BlockFace.DOWN);
            if (relative.getType() == block.getType()){
                return relative;
            } else {
                return block;
            }
        } else {
            return block;
        }
    }

    public static void toggleDoor(Block block, boolean open) {
        org.bukkit.block.data.Openable openablestate = (org.bukkit.block.data.Openable) block.getBlockData();
        openablestate.setOpen(open);
        block.setBlockData(openablestate);
        block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
    }

    public static void toggleDoor(Block block) {
        org.bukkit.block.data.Openable openablestate = (org.bukkit.block.data.Openable) block.getBlockData();
        openablestate.setOpen(!openablestate.isOpen());
        block.setBlockData(openablestate);
        block.getWorld().playEffect(block.getLocation(), Effect.DOOR_TOGGLE, 0);
    }

    public static BlockFace getRelativeChestFace(Block block) {
        Chest chest = (Chest) block.getBlockData();
        BlockFace face = getFacing(block);
        BlockFace relativeFace = null;
        if (chest.getType() == Chest.Type.LEFT) {
            if (face == BlockFace.NORTH) {
                relativeFace = BlockFace.EAST;
            } else if (face == BlockFace.SOUTH) {
                relativeFace = BlockFace.WEST;
            } else if (face == BlockFace.WEST) {
                relativeFace = BlockFace.NORTH;
            } else if (face == BlockFace.EAST) {
                relativeFace = BlockFace.SOUTH;
            }
        } else if (chest.getType() == Chest.Type.RIGHT) {
            if (face == BlockFace.NORTH) {
                relativeFace = BlockFace.WEST;
            } else if (face == BlockFace.SOUTH) {
                relativeFace = BlockFace.EAST;
            } else if (face == BlockFace.WEST) {
                relativeFace = BlockFace.SOUTH;
            } else if (face == BlockFace.EAST) {
                relativeFace = BlockFace.NORTH;
            }
        }
        return relativeFace;
    }

    public static BlockFace getFacing(Block block) {
        BlockData data = block.getBlockData();
        BlockFace f = null;
        if (data instanceof Directional && data instanceof Waterlogged && ((Waterlogged) data).isWaterlogged()) {
            String str = ((Directional) data).toString();
            if (str.contains("facing=west")) {
                f = BlockFace.WEST;
            } else if (str.contains("facing=east")) {
                f = BlockFace.EAST;
            } else if (str.contains("facing=south")) {
                f = BlockFace.SOUTH;
            } else if (str.contains("facing=north")) {
                f = BlockFace.NORTH;
            }
        } else if (data instanceof Directional) {
            f = ((Directional) data).getFacing();
        }
        return f;
    }
}
