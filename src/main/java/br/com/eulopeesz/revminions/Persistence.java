package br.com.eulopeesz.revminions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import com.zaxxer.hikari.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

/** Snapshot único por servidor: saldo, minions e recompensas são gravados juntos. */
public final class Persistence implements AutoCloseable {
    private final Path local;private HikariDataSource pool;
    public Persistence(RevMinionsPlugin p,Configs cfg)throws Exception{
        local=p.getDataFolder().toPath().resolve("data.yml");
        if(cfg.c().getString("armazenamento.tipo").equals("MYSQL")){
            String host=cfg.c().getString("mysql.host"),db=cfg.c().getString("mysql.database");if(!host.matches("[a-zA-Z0-9.:-]+")||!db.matches("[a-zA-Z0-9_]+"))throw new IllegalArgumentException("mysql");
            HikariConfig c=new HikariConfig();c.setJdbcUrl("jdbc:mysql://"+host+":"+cfg.c().getInt("mysql.porta")+"/"+db+"?useSSL="+cfg.c().getBoolean("mysql.ssl")+"&characterEncoding=UTF-8&serverTimezone=UTC&connectTimeout=5000&socketTimeout=10000");
            c.setDriverClassName("com.mysql.cj.jdbc.Driver");c.setUsername(cfg.c().getString("mysql.usuario"));c.setPassword(cfg.c().getString("mysql.senha"));c.setMaximumPoolSize(cfg.c().getInt("mysql.pool-maximo"));c.setMinimumIdle(1);c.setConnectionTimeout(5000);pool=new HikariDataSource(c);
            try(Connection con=pool.getConnection();Statement s=con.createStatement()){s.executeUpdate("CREATE TABLE IF NOT EXISTS revminions_state (id INT PRIMARY KEY,payload LONGTEXT NOT NULL) CHARACTER SET utf8mb4");}catch(Exception e){pool.close();throw e;}
        }
    }
    public String load()throws Exception{
        if(pool==null)return Files.exists(local)?new String(Files.readAllBytes(local),StandardCharsets.UTF_8):"";
        try(Connection c=pool.getConnection();Statement p=c.createStatement();ResultSet r=p.executeQuery("SELECT payload FROM revminions_state WHERE id=1")){return r.next()?r.getString(1):"";}
    }
    public void save(String snapshot)throws Exception{
        if(pool==null){Path tmp=local.resolveSibling("data.tmp");Files.write(tmp,snapshot.getBytes(StandardCharsets.UTF_8));try{Files.move(tmp,local,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);}catch(AtomicMoveNotSupportedException e){Files.move(tmp,local,StandardCopyOption.REPLACE_EXISTING);}return;}
        try(Connection c=pool.getConnection();PreparedStatement p=c.prepareStatement("INSERT INTO revminions_state(id,payload) VALUES(1,?) ON DUPLICATE KEY UPDATE payload=VALUES(payload)")){p.setString(1,snapshot);p.executeUpdate();}
    }
    public void close(){if(pool!=null)pool.close();}
    public static String encode(Model state){
        YamlConfiguration y=new YamlConfiguration();
        for(Model.Profile p:state.players.values()){String r="players."+p.id+".";y.set(r+"name",p.name);y.set(r+"gems",p.gems);y.set(r+"colors",new ArrayList<>(p.colors));y.set(r+"particles",new ArrayList<>(p.particles));y.set(r+"rgb",p.rgb);}
        for(Model.Minion m:state.minions.values()){
            String r="minions."+m.id+".";y.set(r+"owner",m.owner.toString());y.set(r+"type",m.type);y.set(r+"name",m.name);y.set(r+"world",m.world);y.set(r+"x",m.x);y.set(r+"y",m.y);y.set(r+"z",m.z);y.set(r+"yaw",m.yaw);y.set(r+"placed",m.placed);y.set(r+"level",m.level);y.set(r+"sword",m.sword);y.set(r+"enchants",m.enchants);y.set(r+"color",m.color);y.set(r+"particle",m.particle);y.set(r+"rgb",m.rgb);y.set(r+"kills",m.kills);y.set(r+"generated",m.generated);y.set(r+"gems",m.pendingGems);y.set(r+"xp",m.xp);y.set(r+"items",m.items);
            List<String> friends=new ArrayList<>();for(UUID id:m.friends)friends.add(id.toString());y.set(r+"friends",friends);
            List<Map<String,Object>> commands=new ArrayList<>();for(Model.PendingCommand c:m.commands){Map<String,Object> v=new HashMap<>();v.put("name",c.name);v.put("commands",c.commands);commands.add(v);}y.set(r+"commands",commands);
        }
        return y.saveToString();
    }
    @SuppressWarnings("unchecked")public static Model decode(String data)throws Exception{
        YamlConfiguration y=new YamlConfiguration();y.loadFromString(data);Model s=new Model();ConfigurationSection ps=y.getConfigurationSection("players");
        if(ps!=null)for(String id:ps.getKeys(false)){String r="players."+id+".";Model.Profile p=new Model.Profile(UUID.fromString(id),y.getString(r+"name"));p.gems=y.getLong(r+"gems");if(p.gems<0||p.name==null||p.name.isEmpty())throw new IllegalArgumentException("perfil inválido");p.colors.addAll(y.getStringList(r+"colors"));p.particles.addAll(y.getStringList(r+"particles"));p.rgb=y.getBoolean(r+"rgb");s.players.put(p.id,p);}
        ConfigurationSection ms=y.getConfigurationSection("minions");if(ms!=null)for(String id:ms.getKeys(false)){
            String r="minions."+id+".";Model.Minion m=new Model.Minion(UUID.fromString(id));m.owner=UUID.fromString(y.getString(r+"owner"));m.type=y.getString(r+"type");m.name=y.getString(r+"name");m.world=y.getString(r+"world");m.x=y.getDouble(r+"x");m.y=y.getDouble(r+"y");m.z=y.getDouble(r+"z");m.yaw=y.getDouble(r+"yaw");m.placed=y.getBoolean(r+"placed");m.level=y.getInt(r+"level",1);m.sword=y.getInt(r+"sword",1);m.color=y.getString(r+"color","aqua");m.particle=y.getString(r+"particle","nenhuma");m.rgb=y.getBoolean(r+"rgb");m.kills=y.getLong(r+"kills");m.generated=y.getLong(r+"generated");m.pendingGems=y.getLong(r+"gems");m.xp=y.getInt(r+"xp");
            ConfigurationSection es=y.getConfigurationSection(r+"enchants");if(es!=null)for(String k:es.getKeys(false))m.enchants.put(k,es.getInt(k));
            for(String f:y.getStringList(r+"friends"))m.friends.add(UUID.fromString(f));List<?> items=y.getList(r+"items");if(items!=null)for(Object i:items)if(i instanceof ItemStack)m.items.add((ItemStack)i);
            for(Map<?,?> c:y.getMapList(r+"commands"))m.commands.add(new Model.PendingCommand(String.valueOf(c.get("name")),(List<String>)c.get("commands")));
            if(m.level<1||m.sword<1||m.pendingGems<0||m.xp<0||!s.players.containsKey(m.owner))throw new IllegalArgumentException("minion inválido");s.minions.put(m.id,m);
        }return s;
    }
}
