package br.com.eulopeesz.revminions;
import java.util.*;
import org.bukkit.inventory.ItemStack;

/** Estado do RevMinions. Autor: eulopeesz. Acesso de jogo na thread principal. */
public final class Model {
    public final Map<UUID,Profile> players=new LinkedHashMap<>();
    public final Map<UUID,Minion> minions=new LinkedHashMap<>();
    public static final class Profile {
        public final UUID id; public String name; public long gems;
        public final Set<String> colors=new LinkedHashSet<>(),particles=new LinkedHashSet<>(); public boolean rgb;
        public Profile(UUID id,String name) { this.id=id;this.name=name;colors.add("aqua");particles.add("nenhuma"); }
    }
    public static final class Minion {
        public final UUID id; public UUID owner; public String type,name,world,color="aqua",particle="nenhuma";
        public double x,y,z,yaw; public boolean placed=true,rgb; public int level=1,sword=1;
        public long kills,generated,pendingGems; public int xp;
        public final Map<String,Integer> enchants=new LinkedHashMap<>();
        public final Set<UUID> friends=new LinkedHashSet<>();
        public final List<ItemStack> items=new ArrayList<>(); public final List<PendingCommand> commands=new ArrayList<>();
        public Minion(UUID id) { this.id=id; }
        public int enchant(String key) { return enchants.getOrDefault(key,0); }
        public long stored() { long n=pendingGems+xp+commands.size(); for(ItemStack i:items) n=Math.addExact(n,i.getAmount()); return n; }
        public boolean room(long capacity,long amount) { return amount>=0 && stored()<=capacity && amount<=capacity-stored(); }
    }
    public static final class PendingCommand {
        public final String name; public final List<String> commands;
        public PendingCommand(String name,List<String> commands) { this.name=name;this.commands=new ArrayList<>(commands); }
    }
    public static long add(long value,long increment) { if(increment<0) throw new IllegalArgumentException(); return Math.addExact(value,increment); }
    public static double chance(double base,int level,double perLevel) { return Math.min(100,Math.max(0,base*(1+level*perLevel/100))); }
    public static long cost(double initial,double multiplier,int current) { double v=initial*Math.pow(multiplier,current); if(!Double.isFinite(v)||v<0||v>Long.MAX_VALUE/2) throw new IllegalArgumentException("custo inválido"); return (long)Math.ceil(v); }
}
