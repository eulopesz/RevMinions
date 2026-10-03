package br.com.eulopeesz.revminions;
import org.bukkit.*;import org.bukkit.entity.*;import org.bukkit.metadata.FixedMetadataValue;import org.bukkit.util.EulerAngle;import java.util.*;

public final class Entities {
    public static final String MARK="§0§0§1§7§2§0";
    public final Map<UUID,ArmorStand> bodies=new HashMap<>();private final Map<UUID,List<ArmorStand>> holograms=new HashMap<>();
    private final RevMinionsPlugin p;private double hue;
    public Entities(RevMinionsPlugin p){this.p=p;}
    public Location location(Model.Minion m){World w=Bukkit.getWorld(m.world);return w==null?null:new Location(w,m.x,m.y,m.z,(float)m.yaw,0);}
    public void clearChunk(Chunk c){for(Entity e:c.getEntities())if((e instanceof ArmorStand&&e.getCustomName()!=null&&e.getCustomName().endsWith(MARK))||(e instanceof Zombie&&MARK.equals(e.getCustomName())))e.remove();}
    public void remove(UUID id){ArmorStand b=bodies.remove(id);if(b!=null)b.remove();List<ArmorStand> h=holograms.remove(id);if(h!=null)for(ArmorStand a:h)a.remove();}
    public void clear(){for(UUID id:new ArrayList<>(bodies.keySet()))remove(id);}
    private ArmorStand stand(Location l,UUID id,boolean hologram){ArmorStand a=l.getWorld().spawn(l,ArmorStand.class);a.setGravity(false);a.setVisible(!hologram);a.setSmall(true);a.setBasePlate(false);a.setCustomName(MARK);a.setMetadata("revminion",new FixedMetadataValue(p,id.toString()));if(hologram)a.setMarker(true);return a;}
    public void spawn(Model.Minion m){Location l=location(m);if(l==null||!l.getWorld().isChunkLoaded(l.getBlockX()>>4,l.getBlockZ()>>4))return;remove(m.id);ArmorStand b=stand(l,m.id,false);b.setArms(true);b.setRightArmPose(new EulerAngle(-0.5,0,0));bodies.put(m.id,b);List<ArmorStand> h=new ArrayList<>();List<String> lines=p.cfg.f("holograms").getStringList("linhas");for(int i=0;i<lines.size();i++)h.add(stand(l.clone().add(0,p.cfg.f("holograms").getDouble("altura")+(lines.size()-i)*p.cfg.f("holograms").getDouble("espacamento"),0),m.id,true));holograms.put(m.id,h);update(m,"parado");}
    public void update(Model.Minion m,String status){ArmorStand body=bodies.get(m.id);if(body==null||body.isDead())return;Map<String,String> vars=p.cfg.vars(m);vars.put("estado",p.cfg.f("messages").getString("estado."+status,status));body.setCustomName(Configs.text(m.name)+MARK);body.setCustomNameVisible(false);
        Color color=m.rgb?Color.fromRGB(java.awt.Color.HSBtoRGB((float)hue,1,1)&0xffffff):p.cfg.color(m.color);String r="minions."+m.type+".roupa.";body.setHelmet(p.signed.clothes(Configs.mat(p.cfg.f("minions").getString(r+"capacete")),color));body.setChestplate(p.signed.clothes(Configs.mat(p.cfg.f("minions").getString(r+"peitoral")),color));body.setLeggings(p.signed.clothes(Configs.mat(p.cfg.f("minions").getString(r+"calca")),color));body.setBoots(p.signed.clothes(Configs.mat(p.cfg.f("minions").getString(r+"botas")),color));body.setItemInHand(p.signed.plain(Configs.mat(p.cfg.f("sword").getString("espada.material")),p.cfg.f("sword").getString("espada.nome"),Collections.emptyList(),vars));
        List<ArmorStand> h=holograms.get(m.id);List<String> lines=p.cfg.f("holograms").getStringList("linhas");if(h!=null)for(int i=0;i<Math.min(h.size(),lines.size());i++){h.get(i).setCustomName(p.cfg.render(lines.get(i),vars)+MARK);h.get(i).setCustomNameVisible(true);}
        String effect=p.cfg.c().getString("personalizacao.particulas."+m.particle,"NONE");if(!effect.equals("NONE"))body.getWorld().spigot().playEffect(body.getLocation().add(0,0.8,0),Effect.valueOf(effect),0,0,0.3f,0.2f,0.3f,0,2,24);
    }
    public void tickHue(){hue=(hue+p.cfg.c().getDouble("personalizacao.rgb.velocidade",0.02))%1.0;}
    public UUID id(Entity e){if(!e.hasMetadata("revminion"))return null;try{return UUID.fromString(e.getMetadata("revminion").get(0).asString());}catch(Exception x){return null;}}
}
