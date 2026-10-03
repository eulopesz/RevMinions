package br.com.eulopeesz.revminions;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import java.io.File;
import java.util.*;

public final class Configs {
    public final Map<String,YamlConfiguration> files=new HashMap<>();
    private final PermissionLimits permissionLimits;
    public Configs(RevMinionsPlugin p) throws Exception {
        for(String name:Arrays.asList("config","menus","messages","minions","sword","holograms","recompensas")) {
            File file=new File(p.getDataFolder(),name+".yml");if(!file.exists())p.saveResource(name+".yml",false);
            YamlConfiguration y=new YamlConfiguration();y.load(file);files.put(name,y);
        }
        if(!Arrays.asList("LOCAL","MYSQL").contains(c().getString("armazenamento.tipo")))throw new IllegalArgumentException("storage");
        Object rawLimits=c().get("minions.limites-por-permissao");
        if(rawLimits!=null && !(rawLimits instanceof ConfigurationSection))throw new IllegalArgumentException("minions.limites-por-permissao deve ser uma seção");
        permissionLimits=PermissionLimits.read(c().getConfigurationSection("minions.limites-por-permissao"));
        positive(c().getInt("armazenamento.autosave-segundos"));positive(c().getInt("minions.intervalo-ticks"));positive(c().getInt("minions.maximo-ataques-por-ciclo"));positive(c().getInt("minions.limite-por-jogador"));
        for(String id:ids("config","personalizacao.cores"))color(id);
        for(String id:ids("config","personalizacao.particulas")) { String effect=c().getString("personalizacao.particulas."+id);if(!effect.equals("NONE")&&Effect.valueOf(effect).getType()!=Effect.Type.PARTICLE)throw new IllegalArgumentException("efeito"); }
        if(!ids("config","personalizacao.cores").contains("aqua")||!ids("config","personalizacao.particulas").contains("nenhuma"))throw new IllegalArgumentException("defaults de cosméticos");
        for(String type:Arrays.asList("cor","particula","rgb","gemas"))mat(c().getString("ativadores."+type+".material"));
        if(ids("minions","minions").isEmpty())throw new IllegalArgumentException("minions vazios");
        for(String id:ids("minions","minions")) {
            validId(id);String root="minions."+id;YamlConfiguration y=f("minions");mat(y.getString(root+".material"));
            for(String part:Arrays.asList("capacete","peitoral","calca","botas"))mat(y.getString(root+".roupa."+part));
            color(y.getString(root+".roupa.cor"));
            int max=y.getInt(root+".evolucao.nivel-maximo");positive(max);if(max>10000)throw new IllegalArgumentException("nível máximo");
            finite(y.getDouble(root+".evolucao.alcance-inicial"),true);finite(y.getDouble(root+".evolucao.alcance-por-nivel"),false);
            if(y.getDouble(root+".evolucao.alcance-inicial")+max*y.getDouble(root+".evolucao.alcance-por-nivel")>32)throw new IllegalArgumentException("alcance > 32");
            for(int n=1;n<=max;n++){cost(id,n);if(capacity(id,n)<=0)throw new IllegalArgumentException("capacidade");}
            for(String mob:y.getStringList(root+".mobs"))org.bukkit.entity.EntityType.valueOf(mob);
            for(String mob:ids("minions",root+".drops-base")){mat(y.getString(root+".drops-base."+mob+".material"));positive(y.getInt(root+".drops-base."+mob+".quantidade"));}
        }
        mat(f("sword").getString("espada.material"));int max=f("sword").getInt("espada.nivel-maximo");positive(max);if(max>10000)throw new IllegalArgumentException("espada limite");
        for(int n=1;n<=max;n++)Model.cost(f("sword").getDouble("espada.custo-inicial"),f("sword").getDouble("espada.custo-multiplicador"),n-1);
        finite(f("sword").getDouble("espada.dano-inicial"),true);finite(f("sword").getDouble("espada.dano-por-nivel"),false);finite(f("sword").getDouble("espada.intervalo-segundos"),true);
        for(String id:ids("sword","encantamentos")) {
            if(f("sword").contains("encantamentos."+id+".valor-por-nivel"))finite(f("sword").getDouble("encantamentos."+id+".valor-por-nivel"),false);
            if(!Arrays.asList("dano","velocidade","pilhagem","experiencia","varredura","fortuna","horda-zumbi").contains(id))throw new IllegalArgumentException("encantamento não implementado");
            String path="encantamentos."+id;int levels=f("sword").getInt(path+".nivel-maximo");positive(levels);if(levels>10000)throw new IllegalArgumentException("encantamento limite");
            for(int n=0;n<levels;n++)Model.cost(f("sword").getDouble(path+".custo-inicial"),f("sword").getDouble(path+".custo-multiplicador"),n);
        }
        for(String id:Arrays.asList("dano","velocidade","pilhagem","experiencia","varredura","fortuna","horda-zumbi"))if(!f("sword").isConfigurationSection("encantamentos."+id))throw new IllegalArgumentException("encantamento ausente");
        for(String id:ids("recompensas","recompensas")) {
            validId(id);String path="recompensas."+id;YamlConfiguration y=f("recompensas");finite(y.getDouble(path+".chance"),false);if(y.getDouble(path+".chance")>100)throw new IllegalArgumentException("chance");
            String type=y.getString(path+".tipo");if(!Arrays.asList("ITEM","GEMAS","COMANDO").contains(type))throw new IllegalArgumentException("recompensa tipo");
            if(!type.equals("COMANDO")){positive(y.getInt(path+".quantidade"));if(type.equals("ITEM"))mat(y.getString(path+".material"));}
        }
        Set<Integer> slots=new HashSet<>();for(String id:ids("menus","principal.itens")){String path="principal.itens."+id;int slot=f("menus").getInt(path+".slot",-1);if(slot<0||slot>=27||!slots.add(slot))throw new IllegalArgumentException("slots");mat(f("menus").getString(path+".material"));}
        for(String menu:Arrays.asList("principal","evolucao","espada","personalizacao","armazem","amigos","cores","particulas"))if(text(f("menus").getString(menu+".titulo","")).length()>32)throw new IllegalArgumentException("titulo longo");
        String h="encantamentos.horda-zumbi.";for(String k:Arrays.asList("limite-zumbis","limite-global","duracao-segundos","cooldown-segundos","intervalo-ataque-ticks"))positive(f("sword").getInt(h+k));
        for(String key:Arrays.asList("chance-inicial","chance-por-nivel","zumbis-iniciais","zumbis-por-nivel","dano"))finite(f("sword").getDouble(h+key),false);
        finite(f("sword").getDouble("encantamentos.fortuna.aumento-percentual-por-nivel"),false);
        if(f("sword").getInt(h+"limite-global")>500)throw new IllegalArgumentException("horda limite global");
    }
    public int placementLimit(java.util.function.Predicate<String> hasPermission){return permissionLimits.resolve(c().getInt("minions.limite-por-jogador"),hasPermission);}
    private void positive(long n){if(n<=0)throw new IllegalArgumentException("positivo");}
    private void finite(double n,boolean positive){if(!Double.isFinite(n)||(positive?n<=0:n<0))throw new IllegalArgumentException("valor finito");}
    private void validId(String id){if(!id.matches("[a-z0-9_-]+"))throw new IllegalArgumentException("id");}
    public static Material mat(String s){Material m=Material.matchMaterial(s==null?"":s);if(m==null||m==Material.AIR)throw new IllegalArgumentException("material "+s);return m;}
    public YamlConfiguration f(String name){return files.get(name);}public YamlConfiguration c(){return f("config");}
    public List<String> ids(String file,String path){ConfigurationSection s=f(file).getConfigurationSection(path);return s==null?Collections.emptyList():new ArrayList<>(s.getKeys(false));}
    public Color color(String id){String[] rgb=c().getString("personalizacao.cores."+id,"").split(",");if(rgb.length!=3)throw new IllegalArgumentException("cor "+id);return Color.fromRGB(Integer.parseInt(rgb[0]),Integer.parseInt(rgb[1]),Integer.parseInt(rgb[2]));}
    public long capacity(String type,int level){String p="minions."+type+".evolucao.";return Math.addExact(f("minions").getLong(p+"capacidade-inicial"),Math.multiplyExact(level-1L,f("minions").getLong(p+"capacidade-por-nivel")));}
    public long cost(String type,int current){String p="minions."+type+".evolucao.";return Model.cost(f("minions").getDouble(p+"custo-inicial"),f("minions").getDouble(p+"custo-multiplicador"),current-1);}
    public long swordCost(Model.Minion m,String enchant){String path=enchant==null?"espada":"encantamentos."+enchant;return Model.cost(f("sword").getDouble(path+".custo-inicial"),f("sword").getDouble(path+".custo-multiplicador"),enchant==null?m.sword-1:m.enchant(enchant));}
    public Map<String,String> vars(Model.Minion m){Map<String,String> v=new HashMap<>();v.put("nome",m.name);v.put("nivel",""+m.level);v.put("espada",""+m.sword);v.put("abates",""+m.kills);v.put("gemas_geradas",""+m.generated);v.put("armazenado",""+m.stored());v.put("capacidade",""+capacity(m.type,m.level));v.put("custo",""+cost(m.type,m.level));v.put("gemas_pendentes",""+m.pendingGems);v.put("comandos_pendentes",""+m.commands.size());v.put("xp",""+m.xp);return v;}
    public static String text(String s){return ChatColor.translateAlternateColorCodes('&',s==null?"":s);}
    public String render(String s,Map<String,String> vars){s=(s==null?"":s).replace("{prefix}",f("messages").getString("prefix",""));for(Map.Entry<String,String> e:vars.entrySet())s=s.replace("{"+e.getKey()+"}",e.getValue());return text(s);}
}
