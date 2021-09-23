package logicfix;

import arc.util.Log;
import arc.util.io.Reads;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Itemsc;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.net.Packet;
import mindustry.net.Packets;
import mindustry.type.Item;
import mindustry.world.Tile;
import pluginutil.PluginUtil;

import java.util.*;

import static pluginutil.GHParse.colorRemove;

public class PacketReader {
    public static Object[] readInvokePacket(Reads read, int id) {
        for (Packet packet : packets)
            if (id == packet.id){
                ArrayList<Object> objs = new ArrayList<>();
                packet.map.forEach((k, v) -> objs.add(read(read, v)));
                return objs.toArray(new Object[0]);
            }
        return null;
    }

    public static String readInvokePacketToString(Reads read, int id) {
        for (Packet packet : packets) {
            if (id != packet.id)
                continue;

            StringBuilder sb = new StringBuilder();
            int lineLength = 0;
            Map.Entry<String, Class<?>>[] map = packet.map.entrySet().toArray(new Map.Entry[0]);
            for (int i = 0; i < map.length; i++) {
                StringBuilder sb2 = new StringBuilder();
                sb2.append("[red]").append(map[i].getKey()).append("[]=[green]");
                try {
                    sb2.append(readToStr(read, map[i].getValue()));
                } catch (NullPointerException ee){
                    sb2.append("null");
                } catch (Exception eee) {
                    eee.printStackTrace();
                }

                sb2.append("[]");

                if (i + 1 < map.length)
                    sb2.append(", ");

                int strLength = colorRemove(sb2.toString()).length();
                if (lineLength != 0 && lineLength + strLength > 150){
                    lineLength = 0;
                    sb.append("\n");
                }else
                    lineLength += strLength;

                sb.append(sb2);
            }
            String str = sb.toString();
//            str = str.replaceAll("(.{150})", "$1\n");
            return str;
        }
        return null;
    }

    public static class Packet{
        int id;
        LinkedHashMap<String, Class<?>> map;
        String name;

        public Packet(int id, Object... entries) {
            this.id = id;
            map = new LinkedHashMap<>();

            if(entries.length % 2 != 1)
                throw new RuntimeException("Packet Objects cannot have odd amount.");

            if (entries.length > 1)
                for (int i = 0; i < entries.length-1; i += 2)
                    map.put((String) entries[i + 1], (Class<?>) entries[i]);

            name = (String)entries[entries.length-1];
        }
    }

    public static LinkedHashSet<Packet> packets;

    static {
        packets = new LinkedHashSet<>();
        packets.add(new Packet(6,
                Unit.class, "unit",
        "unitControl"));
        packets.add(new Packet(7,
        "unitCommand"));
        packets.add(new Packet(8,
        "unitClear"));
        packets.add(new Packet(14,
                Building.class, "build",
        "transferInventory"));
        packets.add(new Packet(16,
                Tile.class, "tile",
                "tileTap"));
        packets.add(new Packet(19,
                Building.class, "build",
                Object.class, "value",
        "tileConfig"));
        packets.add(new Packet(27,
                Team.class, "team",
        "setPlayerTeamEditor"));
        packets.add(new Packet(32,
                String.class, "type",
                String.class, "contents",
                "serverPacketUnreliable"));
        packets.add(new Packet(33,
                String.class, "type",
                String.class, "contents",
                "serverPacketReliable"));
        packets.add(new Packet(36,
                String.class, "message",
                "sendChatMessage"));
        packets.add(new Packet(39,
                Building.class, "build",
                boolean.class, "direction",
        "rotateBlock"));
        packets.add(new Packet(41,
                Unit.class, "target",
                "requestUnitPayload"));
        packets.add(new Packet(42,
                Building.class, "build",
                Item.class, "item",
                int.class, "amount",
        "requestItem"));
        packets.add(new Packet(43,
                float.class, "x",
                float.class, "y",
                "requestDropPayload"));
        packets.add(new Packet(44,
                Building.class, "build",
                "requestBuildPayload"));
        packets.add(new Packet(50,
                long.class, "time",
                "ping"));
        packets.add(new Packet(65,
                float.class, "angle",
                "dropItem"));
        packets.add(new Packet(70,
                "connectConfirm"));
        packets.add(new Packet(72,
                int.class, "snapshotID",
                int.class, "unitID",
                boolean.class, "dead",
                float.class, "x",
                float.class, "y",
                float.class, "pointerX",
                float.class, "pointerY",
                float.class, "rotation",
                float.class, "baseRotation",
                float.class, "xVelocity",
                float.class, "yVelocity",
                Tile.class, "mining",
                boolean.class, "boosting",
                boolean.class, "shooting",
                boolean.class, "chatting",
                boolean.class, "building",
                BuildPlan[].class, "requests",
                float.class, "viewX",
                float.class, "viewY",
                float.class, "viewWidth",
                float.class, "viewHeight",
                "clientSnapshot"));
        packets.add(new Packet(80,
                Player.class, "other",
                Packets.AdminAction.class, "action",
                "adminRequest"));
    }

    public static Object read(Reads read, Class<?> cls){
        try {
            if (cls == boolean.class) return read.bool();
            if (cls == byte.class) return read.b();
            if (cls == short.class) return read.s();
            if (cls == int.class) return read.i();
            if (cls == float.class) return read.f();
            if (cls == double.class) return read.d();
            if (cls == long.class) return read.l();

            if (cls == Tile.class) return mindustry.io.TypeIO.readTile(read);
            if (cls == Item.class) return mindustry.io.TypeIO.readItem(read);
            if (cls == Team.class) return mindustry.io.TypeIO.readTeam(read);
            if (cls == Unit.class) return mindustry.io.TypeIO.readUnit(read);
            if (cls == Object.class) return mindustry.io.TypeIO.readObject(read);
            if (cls == Player.class) return mindustry.io.TypeIO.readEntity(read);
            if (cls == String.class) return mindustry.io.TypeIO.readString(read);
            if (cls == Building.class) return mindustry.io.TypeIO.readBuilding(read);
            if (cls == Packets.AdminAction.class) return mindustry.io.TypeIO.readAction(read);
            if (cls == BuildPlan[].class) return mindustry.io.TypeIO.readRequests(read);
        } catch (Exception e){
            Log.info("Failed to read type [" + cls + "]");
        }

        return null;
    }
    
    public static String readToStr(Reads read, Class<?> cls){
        try {
            if (cls == boolean.class) return String.valueOf(read.bool());
            if (cls == byte.class) return String.valueOf(read.b());
            if (cls == short.class) return String.valueOf(read.s());
            if (cls == int.class) return String.valueOf(read.i());
            if (cls == float.class) return String.valueOf(read.f());
            if (cls == double.class) return String.valueOf(read.d());
            if (cls == long.class) return String.valueOf(read.l());

            if (cls == Tile.class) return mindustry.io.TypeIO.readTile(read).toString();
            if (cls == Item.class) return mindustry.io.TypeIO.readItem(read).toString();
            if (cls == Team.class) return mindustry.io.TypeIO.readTeam(read).toString();
            if (cls == Unit.class) return mindustry.io.TypeIO.readUnit(read).toString();
            if (cls == Object.class) return mindustry.io.TypeIO.readObject(read).toString();
            if (cls == Player.class) return mindustry.io.TypeIO.readEntity(read).toString();
            if (cls == String.class) return mindustry.io.TypeIO.readString(read);
            if (cls == Building.class) return mindustry.io.TypeIO.readBuilding(read).toString();
            if (cls == Packets.AdminAction.class) return mindustry.io.TypeIO.readAction(read).toString();
            if (cls == BuildPlan[].class) {
                BuildPlan[] plans = mindustry.io.TypeIO.readRequests(read);
                StringBuilder sb = new StringBuilder("[");
                StringBuilder sb2 = new StringBuilder();
                int lineLength = 0;
                sb.append(":\n");
                for (int i = 0; i < plans.length; i++) {
                    sb2.delete(0, sb2.length());

                    sb2.append("{");
                    sb2.append("[red]x[]=[green]").append(plans[i].x).append(", [red]y[]=[green]").append(plans[i].y);
                    sb2.append(", [red]block[]=[green]").append(plans[i].block).append(", [red]breaking[]=[green]").append(plans[i].breaking);
                    sb2.append(", [red]progress[]=[green]").append(plans[i].progress).append(", [red]config[]=[green]").append(plans[i].config);
                    sb2.append("[]}");

                    if (i + 1 < plans.length)
                        sb2.append(", ");

                    int strLength = colorRemove(sb2.toString()).length();
                    if (lineLength != 0 && lineLength + strLength > 150){
                        lineLength = 0;
                        sb.append("\n");
                    }else
                        lineLength += strLength;

                    sb.append(sb2);
                }
                return sb.toString() + ']';
            }
        } catch (Exception e){
            Log.info("Failed to read type [" + cls + "]");
        }

        return "null";
    }
}
