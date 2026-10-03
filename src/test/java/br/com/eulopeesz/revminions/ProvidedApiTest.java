package br.com.eulopeesz.revminions;
import org.junit.jupiter.api.Test;import org.junit.jupiter.api.Assumptions;import java.net.*;import java.nio.file.*;import java.util.UUID;import org.bukkit.Location;import org.bukkit.entity.Entity;import static org.junit.jupiter.api.Assertions.*;

class ProvidedApiTest {
    @Test void providedJarsExposeRequiredSignatures()throws Exception{
        String directory=System.getProperty("rev.test.jars");Assumptions.assumeTrue(directory!=null,"Jars comerciais não fazem parte do repositório");
        try(URLClassLoader cl=new URLClassLoader(new URL[]{Paths.get(directory,"yplugins-3.7.0.jar").toUri().toURL(),Paths.get(directory,"PlotSquared.jar").toUri().toURL()},getClass().getClassLoader())){
            Class<?> api=Class.forName("br.com.ystoreplugins.product.yspawnersv2.SpawnerV2APIHolder",false,cl);assertEquals(double.class,api.getMethod("getEntityStackAmount",Entity.class).getReturnType());assertNotNull(api.getMethod("setEntityStackAmount",Entity.class,double.class));
            Class<?> plot=Class.forName("com.intellectualcrafters.plot.object.Plot",false,cl),loc=Class.forName("com.intellectualcrafters.plot.object.Location",false,cl);assertNotNull(plot.getMethod("getPlot",loc));assertEquals(boolean.class,plot.getMethod("isOwner",UUID.class).getReturnType());assertNotNull(Class.forName("com.plotsquared.bukkit.util.BukkitUtil",false,cl).getMethod("getLocation",Location.class));
        }
    }
}
