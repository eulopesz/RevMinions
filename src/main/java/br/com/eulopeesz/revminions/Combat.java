package br.com.eulopeesz.revminions;
import org.bukkit.*;import org.bukkit.entity.*;import org.bukkit.event.entity.*;import org.bukkit.metadata.FixedMetadataValue;import java.util.*;

/** Combate controlado: uma recompensa por unidade abatida, sem recursão da Horda. */
public final class Combat {
    public static final class Summon {public final Zombie zombie;public final UUID minion,target;public final long expires;public final LivingEntity targetEntity;long next;Summon(Zombie z,UUID m,LivingEntity t,long expires){zombie=z;minion=m;target=t.getUniqueId();targetEntity=t;this.expires=expires;}}
    public final Map<UUID,Summon> summons=new HashMap<>();private final Map<UUID,UUID> kills=new HashMap<>();private final Map<UUID,Long> nextAttack=new HashMap<>(),nextHorde=new HashMap<>();private final RevMinionsPlugin p;private final Random random=new Random();
    public Combat(RevMinionsPlugin p){this.p=p;}
    public void cancel(UUID minion){for(Summon s:new ArrayList<>(summons.values()))if(s.minion.equals(minion)){summons.remove(s.zombie.getUniqueId());s.zombie.remove();}nextHorde.remove(minion);}
    public void clear(){for(Summon s:summons.values())s.zombie.remove();summons.clear();kills.clear();nextAttack.clear();nextHorde.clear();}
    private boolean online(Model.Minion m){return p.cfg.c().getBoolean("minions.trabalhar-com-dono-offline")||Bukkit.getPlayer(m.owner)!=null;}
    public void tick(){
        long now=System.currentTimeMillis();int budget=p.cfg.c().getInt("minions.maximo-ataques-por-ciclo");p.entities.tickHue();
        for(Model.Minion m:p.state.minions.values()){
            if(!m.placed||!p.cfg.ids("minions","minions").contains(m.type))continue;Location loc=p.entities.location(m);if(loc==null||!loc.getWorld().isChunkLoaded(loc.getBlockX()>>4,loc.getBlockZ()>>4)){p.entities.remove(m.id);cancel(m.id);continue;}
            ArmorStand body=p.entities.bodies.get(m.id);if(body==null||body.isDead()){p.entities.spawn(m);body=p.entities.bodies.get(m.id);}if(body==null)continue;
            String status="parado";
            if(!p.integration.owns(m.owner,loc)){status="pausado";cancel(m.id);}else if(!online(m)){status="offline";cancel(m.id);}else if(m.stored()>=p.cfg.capacity(m.type,m.level)){status="cheio";cancel(m.id);}else if(now>=nextAttack.getOrDefault(m.id,0L)&&budget>0){
                double range=p.cfg.f("minions").getDouble("minions."+m.type+".evolucao.alcance-inicial")+(m.level-1)*p.cfg.f("minions").getDouble("minions."+m.type+".evolucao.alcance-por-nivel");
                int max=1+m.enchant("varredura");int count=0;
                for(Entity e:body.getNearbyEntities(range,range,range)){
                    if(count>=max||budget<=0)break;if(!(e instanceof LivingEntity)||e instanceof Player||e instanceof ArmorStand||summons.containsKey(e.getUniqueId())||e.isDead())continue;LivingEntity target=(LivingEntity)e;
                    if(!p.cfg.f("minions").getStringList("minions."+m.type+".mobs").contains(target.getType().name())||!p.integration.samePlot(m.owner,loc,target.getLocation())||target.getLocation().distanceSquared(loc)>range*range)continue;
                    if(!m.room(p.cfg.capacity(m.type,m.level),worstLoot(m,target))){status="cheio";continue;}
                    try{double amount=p.integration.amount(target);if(!Double.isFinite(amount)||amount<1)continue;}catch(Exception ex){p.error(ex);continue;}
                    status="trabalhando";double damage=p.cfg.f("sword").getDouble("espada.dano-inicial")+(m.sword-1)*p.cfg.f("sword").getDouble("espada.dano-por-nivel")+m.enchant("dano")*p.cfg.f("sword").getDouble("encantamentos.dano.valor-por-nivel");
                    hit(m,target,damage,body);horde(m,target,now);count++;budget--;
                }
                double speed=Math.max(0.1,1-m.enchant("velocidade")*p.cfg.f("sword").getDouble("encantamentos.velocidade.valor-por-nivel"));nextAttack.put(m.id,now+(long)(p.cfg.f("sword").getDouble("espada.intervalo-segundos")*speed*1000));
            }
            p.entities.update(m,status);if(p.cfg.c().getBoolean("actionbar.ativada")){Player owner=Bukkit.getPlayer(m.owner);if(owner!=null&&owner.getWorld().equals(loc.getWorld())&&owner.getLocation().distanceSquared(loc)<Math.pow(p.cfg.c().getDouble("actionbar.distancia"),2)){try{Display.action(owner,p.cfg.render(p.cfg.c().getString("actionbar.mensagem"),p.cfg.vars(m)));}catch(Exception ignored){}}}
        }
        for(Summon s:new ArrayList<>(summons.values())){
            Model.Minion m=p.state.minions.get(s.minion);LivingEntity target=s.targetEntity;Location base=m==null?null:p.entities.location(m);
            if(m==null||!m.placed||base==null||!online(m)||!p.integration.owns(m.owner,base)||target==null||target.isDead()||s.zombie.isDead()||now>=s.expires||!p.integration.samePlot(m.owner,base,target.getLocation())){summons.remove(s.zombie.getUniqueId());s.zombie.remove();continue;}
            s.zombie.setTarget(target);
            if(now>=s.next&&budget>0&&m.room(p.cfg.capacity(m.type,m.level),worstLoot(m,target))){if(s.zombie.getLocation().distanceSquared(target.getLocation())>9)s.zombie.teleport(target.getLocation().clone().add(0.5,0,0.5));hit(m,target,p.cfg.f("sword").getDouble("encantamentos.horda-zumbi.dano"),p.entities.bodies.get(m.id));s.next=now+p.cfg.f("sword").getLong("encantamentos.horda-zumbi.intervalo-ataque-ticks")*50;budget--;}
        }
    }
    private long baseGems(LivingEntity e){return Math.max(0,p.cfg.c().getLong("gemas.mobs."+e.getType().name(),p.cfg.c().getLong("gemas.por-mob")));}
    private int baseItems(Model.Minion m,LivingEntity e){String path="minions."+m.type+".drops-base."+e.getType().name();int count=p.cfg.f("minions").getInt(path+".quantidade",0);double multi=1+m.enchant("pilhagem")*p.cfg.f("sword").getDouble("encantamentos.pilhagem.valor-por-nivel");return (int)Math.ceil(count*multi);}
    private int xp(Model.Minion m){return (int)Math.ceil(1+m.enchant("experiencia")*p.cfg.f("sword").getDouble("encantamentos.experiencia.valor-por-nivel"));}
    private boolean rewardMatches(String id,LivingEntity e){String r="recompensas."+id+".";List<String> mobs=p.cfg.f("recompensas").getStringList(r+"mobs");return p.cfg.f("recompensas").getBoolean(r+"ativada",true)&&(mobs.contains("ALL")||mobs.contains(e.getType().name()));}
    private long worstLoot(Model.Minion m,LivingEntity e){long n=baseGems(e)+baseItems(m,e)+xp(m);for(String id:p.cfg.ids("recompensas","recompensas"))if(rewardMatches(id,e)){String r="recompensas."+id+".";n=Math.addExact(n,p.cfg.f("recompensas").getString(r+"tipo").equals("COMANDO")?1:p.cfg.f("recompensas").getLong(r+"quantidade"));}return n;}
    private void hit(Model.Minion m,LivingEntity target,double damage,ArmorStand source){
        if(source==null||target.isDead()||damage<=0)return;
        try{
            double stack=p.integration.amount(target);
            if(stack>1&&damage>=target.getHealth()){
                EntityDamageByEntityEvent check=new EntityDamageByEntityEvent(source,target,EntityDamageEvent.DamageCause.ENTITY_ATTACK,damage);Bukkit.getPluginManager().callEvent(check);if(check.isCancelled())return;
                if(check.getFinalDamage()<target.getHealth()){kills.put(target.getUniqueId(),m.id);target.damage(damage,source);kills.remove(target.getUniqueId());return;}
                p.integration.amount(target,stack-1);target.setHealth(target.getMaxHealth());reward(m,target);return;
            }
            kills.put(target.getUniqueId(),m.id);target.damage(damage,source);kills.remove(target.getUniqueId());
        }catch(Exception e){p.error(e);}
    }
    public void death(EntityDeathEvent e){if(summons.containsKey(e.getEntity().getUniqueId())){e.getDrops().clear();e.setDroppedExp(0);return;}UUID id=kills.remove(e.getEntity().getUniqueId());if(id==null)return;Model.Minion m=p.state.minions.get(id);if(m==null)return;e.getDrops().clear();e.setDroppedExp(0);reward(m,e.getEntity());}
    private void addItems(Model.Minion m,org.bukkit.inventory.ItemStack template,int amount){while(amount>0){org.bukkit.inventory.ItemStack i=template.clone();int size=Math.min(i.getMaxStackSize(),amount);i.setAmount(size);boolean merged=false;for(org.bukkit.inventory.ItemStack old:m.items)if(old.isSimilar(i)&&old.getAmount()+size<=old.getMaxStackSize()){old.setAmount(old.getAmount()+size);merged=true;break;}if(!merged)m.items.add(i);amount-=size;}}
    private void reward(Model.Minion m,LivingEntity target){
        long cap=p.cfg.capacity(m.type,m.level);if(!m.room(cap,worstLoot(m,target)))return;
        m.kills=Math.addExact(m.kills,1);long gems=baseGems(target);m.pendingGems=Model.add(m.pendingGems,gems);m.generated=Model.add(m.generated,gems);m.xp=Math.addExact(m.xp,xp(m));
        String base="minions."+m.type+".drops-base."+target.getType().name();int items=baseItems(m,target);if(items>0)addItems(m,new org.bukkit.inventory.ItemStack(Configs.mat(p.cfg.f("minions").getString(base+".material"))),items);
        for(String id:p.cfg.ids("recompensas","recompensas"))if(rewardMatches(id,target)){
            String r="recompensas."+id+".";double chance=p.cfg.f("recompensas").getDouble(r+"chance");if(p.cfg.f("recompensas").getBoolean(r+"fortuna"))chance=Model.chance(chance,m.enchant("fortuna"),p.cfg.f("sword").getDouble("encantamentos.fortuna.aumento-percentual-por-nivel"));if(random.nextDouble()*100>=chance)continue;
            String type=p.cfg.f("recompensas").getString(r+"tipo");int amount=p.cfg.f("recompensas").getInt(r+"quantidade",1);
            if(type.equals("ITEM"))addItems(m,p.signed.plain(Configs.mat(p.cfg.f("recompensas").getString(r+"material")),p.cfg.f("recompensas").getString(r+"nome"),p.cfg.f("recompensas").getStringList(r+"lore"),p.cfg.vars(m)),amount);
            else if(type.equals("GEMAS")){m.pendingGems=Model.add(m.pendingGems,amount);m.generated=Model.add(m.generated,amount);}
            else m.commands.add(new Model.PendingCommand(p.cfg.f("recompensas").getString(r+"nome"),p.cfg.f("recompensas").getStringList(r+"comandos")));
        }p.dirty();
    }
    private void horde(Model.Minion m,LivingEntity target,long now){
        int level=m.enchant("horda-zumbi");if(level<=0||target.isDead()||now<nextHorde.getOrDefault(m.id,0L))return;String r="encantamentos.horda-zumbi.";
        double chance=p.cfg.f("sword").getDouble(r+"chance-inicial")+(level-1)*p.cfg.f("sword").getDouble(r+"chance-por-nivel");if(random.nextDouble()*100>=Math.min(100,chance))return;
        int count=Math.min(p.cfg.f("sword").getInt(r+"limite-zumbis"),p.cfg.f("sword").getInt(r+"zumbis-iniciais")+(level-1)*p.cfg.f("sword").getInt(r+"zumbis-por-nivel"));count=Math.min(count,p.cfg.f("sword").getInt(r+"limite-global")-summons.size());if(count<=0)return;
        int spawned=0;for(int i=0;i<count;i++){Location l=target.getLocation().clone().add(Math.cos(i*Math.PI*2/count)*0.6,0,Math.sin(i*Math.PI*2/count)*0.6);if(!p.integration.samePlot(m.owner,p.entities.location(m),l))continue;Zombie z=l.getWorld().spawn(l,Zombie.class);z.setBaby(true);z.setCanPickupItems(false);z.setCustomName(Entities.MARK);z.setCustomNameVisible(false);z.setMetadata("rev-horde",new FixedMetadataValue(p,true));z.setTarget(target);spawned++;summons.put(z.getUniqueId(),new Summon(z,m.id,target,now+p.cfg.f("sword").getLong(r+"duracao-segundos")*1000));}
        if(spawned==0)return;nextHorde.put(m.id,now+p.cfg.f("sword").getLong(r+"cooldown-segundos")*1000);Player owner=Bukkit.getPlayer(m.owner);if(owner!=null)try{Map<String,String> vars=new HashMap<>();vars.put("quantidade",""+spawned);Display.title(owner,p.cfg.render(p.cfg.f("messages").getString("horda.titulo"),vars),p.cfg.render(p.cfg.f("messages").getString("horda.subtitulo"),vars));}catch(Exception e){p.log("console.horda-erro","erro",e.toString());}
    }
}
