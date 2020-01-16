package me.crafter.mc.lockettepro;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class DependencyProtocolLib {

    public static void setUpProtocolLib(Plugin plugin){
        if (Config.protocollib) {
            addTileEntityDataListener(plugin);
            addMapChunkListener(plugin);
        }
    }
    
    public static void cleanUpProtocolLib(Plugin plugin){
        try {
            if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")){
                ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void addTileEntityDataListener(Plugin plugin){
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.LOW, PacketType.Play.Server.TILE_ENTITY_DATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                if (packet.getIntegers().read(0) != 9) return;
                NbtCompound nbtcompound = (NbtCompound) packet.getNbtModifier().read(0);
                onSignSend(event.getPlayer(), nbtcompound);
            }
        });
    }
    
    public static void addMapChunkListener(Plugin plugin){
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.LOW, PacketType.Play.Server.MAP_CHUNK) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();
                List<?> tileentitydatas = packet.getSpecificModifier(List.class).read(0);
                for (Object tileentitydata : tileentitydatas) {
                    NbtCompound nbtcompound = NbtFactory.fromNMSCompound(tileentitydata);
                    if (!"minecraft:sign".equals(nbtcompound.getString("id"))) continue;
                    onSignSend(event.getPlayer(), nbtcompound);
                }
            }
        });
    }

    public static void onSignSend(Player player, NbtCompound nbtcompound) {
        String raw_line1 = nbtcompound.getString("Text1");
        if (LocketteProAPI.isLockStringOrAdditionalString(Utils.getSignLineFromUnknown(raw_line1))) {
            // Private line
            String line1 = Utils.getSignLineFromUnknown(nbtcompound.getString("Text1"));
            if (LocketteProAPI.isLineExpired(line1)) {
                nbtcompound.put("Text1", WrappedChatComponent.fromText(Config.getLockExpireString()).getJson());
            } else {
                nbtcompound.put("Text1", WrappedChatComponent.fromText(Utils.StripSharpSign(line1)).getJson());
            }
            // Other line
            for (int i = 2; i <= 4; i++) {
                String line = Utils.getSignLineFromUnknown(nbtcompound.getString("Text" + i));
                if (Utils.isUsernameUuidLine(line)) {
                    nbtcompound.put("Text" + i, WrappedChatComponent.fromText(Utils.getUsernameFromLine(line)).getJson());
                }
            }
        }
    }
    
}
