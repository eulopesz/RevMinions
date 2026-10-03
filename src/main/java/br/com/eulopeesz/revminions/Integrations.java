package br.com.eulopeesz.revminions;
import org.bukkit.*;
import org.bukkit.entity.*;
import java.lang.reflect.*;
import java.util.*;

/** Assinaturas conferidas nos jars fornecidos, sem redistribuí-los. */
public final class Integrations {
    private final Class<?> plot,location;private final Method toLocation,getPlot,isOwner;private Object spawners;private Method getStack,setStack;
    @SuppressWarnings({"unchecked","rawtypes"})public Integrations(RevMinionsPlugin p)throws Exception{
        ClassLoader cl=Bukkit.getPluginManager().getPlugin("PlotSquared").getClass().getClassLoader();
        plot=Class.forName("com.intellectualcrafters.plot.object.Plot",true,cl);location=Class.forName("com.intellectualcrafters.plot.object.Location",true,cl);
        Class<?> util=Class.forName("com.plotsquared.bukkit.util.BukkitUtil",true,cl);toLocation=util.getMethod("getLocation",Location.class);getPlot=plot.getMethod("getPlot",location);isOwner=plot.getMethod("isOwner",UUID.class);
        for(Class<?> service:Bukkit.getServicesManager().getKnownServices())if(service.getName().equals("br.com.ystoreplugins.product.yspawnersv2.SpawnerV2APIHolder")){
            spawners=Bukkit.getServicesManager().load((Class)service);getStack=service.getMethod("getEntityStackAmount",Entity.class);setStack=service.getMethod("setEntityStackAmount",Entity.class,double.class);
        }
        if(spawners==null&&p.cfg.c().getBoolean("integracoes.yspawners-obrigatorio"))throw new IllegalStateException("Serviço SpawnerV2APIHolder não registrado. Ative ySpawnersV2 no yPlugins.");
    }
    public Object plot(Location l)throws Exception{return getPlot.invoke(null,toLocation.invoke(null,l));}
    public boolean owns(UUID uuid,Location l){try{Object p=plot(l);return p!=null&&(Boolean)isOwner.invoke(p,uuid);}catch(Exception e){return false;}}
    public boolean samePlot(UUID owner,Location a,Location b){try{Object p=plot(a),q=plot(b);return p!=null&&q!=null&&p.equals(q)&&(Boolean)isOwner.invoke(p,owner);}catch(Exception e){return false;}}
    public double amount(Entity e)throws Exception{return spawners==null?1:((Number)getStack.invoke(spawners,e)).doubleValue();}
    public void amount(Entity e,double n)throws Exception{if(spawners==null)throw new IllegalStateException("sem API");setStack.invoke(spawners,e,n);}
}
