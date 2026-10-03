package br.com.eulopeesz.revminions;
import org.bukkit.*;import org.bukkit.entity.*;
import java.lang.reflect.*;import java.util.*;

/** Packets 1.8 enviados sem dependência direta de NMS. */
public final class Display {
    private static Object component(String text)throws Exception{String v=Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];Class<?> c=Class.forName("net.minecraft.server."+v+".ChatComponentText");return c.getConstructor(String.class).newInstance(text);}
    private static void send(Player player,Object packet)throws Exception{Object handle=player.getClass().getMethod("getHandle").invoke(player);Object connection=handle.getClass().getField("playerConnection").get(handle);for(Method m:connection.getClass().getMethods())if(m.getName().equals("sendPacket")&&m.getParameterCount()==1){m.invoke(connection,packet);return;}}
    public static void action(Player p,String text)throws Exception{String v=Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];Class<?> comp=Class.forName("net.minecraft.server."+v+".IChatBaseComponent"),packet=Class.forName("net.minecraft.server."+v+".PacketPlayOutChat");send(p,packet.getConstructor(comp,byte.class).newInstance(component(text),(byte)2));}
    @SuppressWarnings({"unchecked","rawtypes"})public static void title(Player p,String title,String subtitle)throws Exception{String v=Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];Class<?> packet=Class.forName("net.minecraft.server."+v+".PacketPlayOutTitle"),action=Class.forName(packet.getName()+"$EnumTitleAction"),comp=Class.forName("net.minecraft.server."+v+".IChatBaseComponent");send(p,packet.getConstructor(int.class,int.class,int.class).newInstance(10,50,10));for(String a:Arrays.asList("TITLE","SUBTITLE"))send(p,packet.getConstructor(action,comp).newInstance(Enum.valueOf((Class)action,a),component(a.equals("TITLE")?title:subtitle)));}
}
