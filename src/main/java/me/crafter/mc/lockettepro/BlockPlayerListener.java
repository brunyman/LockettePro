package me.crafter.mc.lockettepro;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class BlockPlayerListener implements Listener {

    // Quick protect for chests
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerQuickLockChest(PlayerInteractEvent event){
        // Check quick lock enabled
        if (Config.getQuickProtectAction() == (byte)0) return;
        // Get player and action info
        Action action = event.getAction();
        Player player = event.getPlayer();
        // Check action correctness
        if (action == Action.RIGHT_CLICK_BLOCK && Tag.SIGNS.isTagged(player.getInventory().getItemInMainHand().getType())) {
            if (player.getGameMode().equals(GameMode.SPECTATOR)) {
                return;
            }
            // Check quick lock action correctness
            if (!((event.getPlayer().isSneaking() && Config.getQuickProtectAction() == (byte)2) ||
                    (!event.getPlayer().isSneaking() && Config.getQuickProtectAction() == (byte)1))) return;
            // Check permission 
            if (!player.hasPermission("lockettepro.lock")) return;
            // Get target block to lock
            BlockFace blockface = event.getBlockFace();
            if (blockface == BlockFace.NORTH || blockface == BlockFace.WEST || blockface == BlockFace.EAST || blockface == BlockFace.SOUTH){
                Block block = event.getClickedBlock();
                if (block == null) return;
                // Check permission with external plugin
                if (Dependency.isProtectedFrom(block, player)) return; // blockwise
                if (Dependency.isProtectedFrom(block.getRelative(event.getBlockFace()), player)) return; // signwise
                // Check whether locking location is obstructed
                Block signLoc = block.getRelative(blockface);
                if (!signLoc.isEmpty()) return;
                // Check whether this block is lockable
                if (LocketteProAPI.isLockable(block)){
                    // Is this block already locked?
                    boolean locked = LocketteProAPI.isLocked(block);
                    // Cancel event here
                    event.setCancelled(true);
                    // Check lock info info
                    if (!locked && !LocketteProAPI.isUpDownLockedDoor(block)){
                    	Material signType = player.getInventory().getItemInMainHand().getType();
                        // Not locked, not a locked door nearby
                        Utils.removeASign(player);
                        // Send message
                        Utils.sendMessages(player, Config.getLang("locked-quick"));
                        // Put sign on
                        Block newsign = Utils.putSignOn(block, blockface, Config.getDefaultPrivateString(), player.getName(), signType);
                        Utils.resetCache(block);
                        // Cleanups - UUID
                        if (Config.isUuidEnabled()){
                            Utils.updateLineByPlayer(newsign, 1, player);
                        }
                        // Cleanups - Expiracy
                        if (Config.isLockExpire()) {
                            if (player.hasPermission("lockettepro.noexpire")) {
                                Utils.updateLineWithTime(newsign, true); // set created to -1 (no expire)
                            } else {
                                Utils.updateLineWithTime(newsign, false); // set created to now
                            }
                        }
                        Dependency.logPlacement(player, newsign);
                    } else if (!locked && LocketteProAPI.isOwnerUpDownLockedDoor(block, player)){
                        // Not locked, (is locked door nearby), is owner of locked door nearby
                        Material signType = player.getInventory().getItemInMainHand().getType();
                        Utils.removeASign(player);
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-quick"));
                        Utils.putSignOn(block, blockface, Config.getDefaultAdditionalString(), "", signType);
                        Dependency.logPlacement(player, block.getRelative(blockface));
                    } else if (LocketteProAPI.isOwner(block, player)) {
                        // Locked, (not locked door nearby), is owner of locked block
                        Material signType = player.getInventory().getItemInMainHand().getType();
                        Utils.removeASign(player);
                        Utils.putSignOn(block, blockface, Config.getDefaultAdditionalString(), "", signType);
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-quick"));
                        Dependency.logPlacement(player, block.getRelative(blockface));
                    } else {
                        // Cannot lock this block
                        Utils.sendMessages(player, Config.getLang("cannot-lock-quick"));
                    }
                }
            }
        }
    }
    
    // Manual protection
    @EventHandler(priority = EventPriority.NORMAL)
    public void onManualLock(SignChangeEvent event){
        if (!Tag.WALL_SIGNS.isTagged(event.getBlock().getType())) return;
        String topline = event.getLine(0);
        if (topline == null) topline = "";
        Player player = event.getPlayer();
        /*  Issue #46 - Old version of Minecraft trim signs in unexpected way.
         *  This is caused by Minecraft was doing: (unconfirmed but seemingly)
         *  Place Sign -> Event Fire -> Trim Sign
         *  The event.getLine() will be inaccurate if the line has white space to trim
         * 
         *  This will cause player without permission will be able to lock chests by
         *  adding a white space after the [private] word.
         *  Currently this is fixed by using trimmed line in checking permission. Trimmed
         *  line should not be used anywhere else.  
         */
        if (!player.hasPermission("lockettepro.lock")){
            String toplinetrimmed = topline.trim();
            if (LocketteProAPI.isLockString(toplinetrimmed) || LocketteProAPI.isAdditionalString(toplinetrimmed)){
                event.setLine(0, Config.getLang("sign-error"));
                Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
                return;
            }
        }
        if (LocketteProAPI.isLockString(topline) || LocketteProAPI.isAdditionalString(topline)){
            Block block = LocketteProAPI.getAttachedBlock(event.getBlock());
            if (LocketteProAPI.isLockable(block)){
                if (Dependency.isProtectedFrom(block, player)){ // External check here
                    event.setLine(0, Config.getLang("sign-error"));
                    Utils.sendMessages(player, Config.getLang("cannot-lock-manual"));
                    return; 
                }
                boolean locked = LocketteProAPI.isLocked(block);
                if (!locked && !LocketteProAPI.isUpDownLockedDoor(block)){
                    if (LocketteProAPI.isLockString(topline)){
                        Utils.sendMessages(player, Config.getLang("locked-manual"));
                        if (!player.hasPermission("lockettepro.lockothers")){ // Player with permission can lock with another name
                            event.setLine(1, player.getName());
                        }
                        Utils.resetCache(block);
                    } else {
                        Utils.sendMessages(player, Config.getLang("not-locked-yet-manual"));
                        event.setLine(0, Config.getLang("sign-error"));
                    }
                } else if (!locked && LocketteProAPI.isOwnerUpDownLockedDoor(block, player)){
                    if (LocketteProAPI.isLockString(topline)){
                        Utils.sendMessages(player, Config.getLang("cannot-lock-door-nearby-manual"));
                        event.setLine(0, Config.getLang("sign-error"));
                    } else {
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-manual"));
                    }
                } else if (LocketteProAPI.isOwner(block, player)){
                    if (LocketteProAPI.isLockString(topline)){
                        Utils.sendMessages(player, Config.getLang("block-already-locked-manual"));
                        event.setLine(0, Config.getLang("sign-error"));
                    } else {
                        Utils.sendMessages(player, Config.getLang("additional-sign-added-manual"));
                    }
                } else { // Not possible to fall here except override
                    Utils.sendMessages(player, Config.getLang("block-already-locked-manual"));
                    event.getBlock().breakNaturally();
                    Utils.playAccessDenyEffect(player, block);
                }
            } else {
                Utils.sendMessages(player, Config.getLang("block-is-not-lockable"));
                event.setLine(0, Config.getLang("sign-error"));
                Utils.playAccessDenyEffect(player, block);
            }
        }
    }
    
    // Player select sign
    @EventHandler(priority = EventPriority.LOW)
    public void playerSelectSign(PlayerInteractEvent event){
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.hasBlock() && Tag.WALL_SIGNS.isTagged(block.getType())) {
            Player player = event.getPlayer();
            if (!player.hasPermission("lockettepro.edit")) return;
            if (LocketteProAPI.isOwnerOfSign(block, player) || (LocketteProAPI.isLockSignOrAdditionalSign(block) && player.hasPermission("lockettepro.admin.edit"))){
                Utils.selectSign(player, block);
                Utils.sendMessages(player, Config.getLang("sign-selected"));
                Utils.playLockEffect(player, block);
            }
        }
    }
    
    // Player break sign
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptBreakSign(BlockBreakEvent event){
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (player.hasPermission("lockettepro.admin.break")) return;
        if (LocketteProAPI.isLockSign(block)){
            if (LocketteProAPI.isOwnerOfSign(block, player)){
                Utils.sendMessages(player, Config.getLang("break-own-lock-sign"));
                Utils.resetCache(LocketteProAPI.getAttachedBlock(block));
                // Remove additional signs?
            } else {
                Utils.sendMessages(player, Config.getLang("cannot-break-this-lock-sign"));
                event.setCancelled(true);
                Utils.playAccessDenyEffect(player, block);
            }
        } else if (LocketteProAPI.isAdditionalSign(block)){
            // TODO the next line is spaghetti
            if (!LocketteProAPI.isLocked(LocketteProAPI.getAttachedBlock(block))){
                // phew, the locked block is expired!
                // nothing
            } else if (LocketteProAPI.isOwnerOfSign(block, player)){
                Utils.sendMessages(player, Config.getLang("break-own-additional-sign"));
            } else if (!LocketteProAPI.isProtected(LocketteProAPI.getAttachedBlock(block))){
                Utils.sendMessages(player, Config.getLang("break-redundant-additional-sign"));
            } else {
                Utils.sendMessages(player, Config.getLang("cannot-break-this-additional-sign"));
                event.setCancelled(true);
                Utils.playAccessDenyEffect(player, block);
            }
        }
    }
    
    // Protect block from being destroyed
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptBreakLockedBlocks(BlockBreakEvent event){
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (LocketteProAPI.isLocked(block) || LocketteProAPI.isUpDownLockedDoor(block)){
            Utils.sendMessages(player, Config.getLang("block-is-locked"));
            event.setCancelled(true);
            Utils.playAccessDenyEffect(player, block);
        }
    }

    // Protect block from being used & handle double doors
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptInteractLockedBlocks(PlayerInteractEvent event) {
    	if (event.hasBlock() == false) return;
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        if (LockettePro.needCheckHand() && LocketteProAPI.isChest(block)){
            if (event.getHand() != EquipmentSlot.HAND){
                if (action == Action.RIGHT_CLICK_BLOCK){
                    /*if (LocketteProAPI.isChest(block)){
                        // something not right
                        event.setCancelled(true);
                    }*/
                    event.setCancelled(true);
                    return;
                }
            }
        }
        switch (action){
        case LEFT_CLICK_BLOCK:
        case RIGHT_CLICK_BLOCK:
            Player player = event.getPlayer();
            if (((LocketteProAPI.isLocked(block) && !LocketteProAPI.isUser(block, player)) || (LocketteProAPI.isUpDownLockedDoor(block) && !LocketteProAPI.isUserUpDownLockedDoor(block, player))) && !player.hasPermission("lockettepro.admin.use")){
                Utils.sendMessages(player, Config.getLang("block-is-locked"));
                event.setCancelled(true);
                Utils.playAccessDenyEffect(player, block);
            } else { // Handle double doors
                if (action == Action.RIGHT_CLICK_BLOCK) {
                    if ((LocketteProAPI.isDoubleDoorBlock(block) || LocketteProAPI.isSingleDoorBlock(block)) && LocketteProAPI.isLocked(block)){
                        Block doorblock = LocketteProAPI.getBottomDoorBlock(block);
                        org.bukkit.block.data.Openable openablestate = (org.bukkit.block.data.Openable ) doorblock.getBlockData();
                        boolean shouldopen = !openablestate.isOpen(); // Move to here
                        int closetime = LocketteProAPI.getTimerDoor(doorblock);
                        List<Block> doors = new ArrayList<Block>();
                        doors.add(doorblock);
                        if (doorblock.getType() == Material.IRON_DOOR || doorblock.getType() == Material.IRON_TRAPDOOR){
                            LocketteProAPI.toggleDoor(doorblock, shouldopen);
                        }
                        for (BlockFace blockface : LocketteProAPI.newsfaces){
                            Block relative = doorblock.getRelative(blockface);
                            if (relative.getType() == doorblock.getType()){
                                doors.add(relative);
                                LocketteProAPI.toggleDoor(relative, shouldopen);
                            }
                        }
                        if (closetime > 0) {
                            for (Block door : doors) {
                                if (door.hasMetadata("lockettepro.toggle")) {
                                    return;
                                }
                            }
                            for (Block door : doors) {
                                door.setMetadata("lockettepro.toggle", new FixedMetadataValue(LockettePro.getPlugin(), true));
                            }
                            Bukkit.getScheduler().runTaskLater(LockettePro.getPlugin(), new DoorToggleTask(doors), closetime*20);
                        }
                    }
                }
            }
            break;
        default:
            break;
        }
    }
    
    // Protect block from interfere block
    @EventHandler(priority = EventPriority.HIGH)
    public void onAttemptPlaceInterfereBlocks(BlockPlaceEvent event){
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (player.hasPermission("lockettepro.admin.interfere")) return;
        if (LocketteProAPI.mayInterfere(block, player)){
            Utils.sendMessages(player, Config.getLang("cannot-interfere-with-others"));
            event.setCancelled(true);
            Utils.playAccessDenyEffect(player, block);		
        }
    }
    
    // Tell player about lockettepro
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlaceFirstBlockNotify(BlockPlaceEvent event){
        if (event.isCancelled()) return;
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (!player.hasPermission("lockettepro.lock")) return;
        if (Utils.shouldNotify(player) && Config.isLockable(block.getType())){
            switch (Config.getQuickProtectAction()){
            case (byte)0:
                Utils.sendMessages(player, Config.getLang("you-can-manual-lock-it"));	
                break;
            case (byte)1:
            case (byte)2:
                Utils.sendMessages(player, Config.getLang("you-can-quick-lock-it"));	
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        if (LocketteProAPI.isProtected(block) && !(LocketteProAPI.isOwner(block, player) || LocketteProAPI.isOwnerOfSign(block, player))) {
            event.setCancelled(true);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isDead()) {
                        player.updateInventory();
                    }
                }
            }.runTaskLater(LockettePro.getPlugin(), 1L);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBucketUse(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        if (LocketteProAPI.isProtected(block) && !(LocketteProAPI.isOwner(block, player) || LocketteProAPI.isOwnerOfSign(block, player))) {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onLecternTake(PlayerTakeLecternBookEvent event){
        Player player = event.getPlayer();
        Block block = event.getLectern().getBlock();
        if(LocketteProAPI.isProtected(block) && !(LocketteProAPI.isOwner(block, player) || LocketteProAPI.isOwnerOfSign(block, player))){
            event.setCancelled(true);
        }
    }
}
