package br.com.eulopeesz.revminions;
import org.junit.jupiter.api.Test;import java.util.*;import org.bukkit.Material;import org.bukkit.inventory.ItemStack;import static org.junit.jupiter.api.Assertions.*;

class ModelPersistenceTest {
    @Test void fortunaUsesRelativeIncrease(){assertEquals(12,Model.chance(10,4,5),0.0001);assertEquals(2,Model.chance(2,0,5),0.0001);}
    @Test void chanceNeverExceedsOneHundred(){assertEquals(100,Model.chance(90,20,5));}
    @Test void gemsCannotOverflowIntoNegativeValues(){assertThrows(ArithmeticException.class,()->Model.add(Long.MAX_VALUE,1));assertThrows(IllegalArgumentException.class,()->Model.add(10,-1));assertEquals(15,Model.add(10,5));}
    @Test void costsAreRoundedUpAndNonFiniteGrowthIsRejected(){assertEquals(115,Model.cost(100,1.15,1));assertThrows(IllegalArgumentException.class,()->Model.cost(100,Double.POSITIVE_INFINITY,2));}
    @Test void warehouseCountsEveryPendingReward(){Model.Minion m=new Model.Minion(UUID.randomUUID());m.pendingGems=10;m.xp=3;m.items.add(new ItemStack(Material.DIAMOND,2));m.commands.add(new Model.PendingCommand("test",Arrays.asList("give {player} diamond 1")));assertEquals(16,m.stored());assertTrue(m.room(20,4));assertFalse(m.room(20,5));assertFalse(m.room(10,0));}
    @Test void combinedSnapshotPreservesOfflineGemsAndUnplacedMinion()throws Exception{
        Model s=new Model();Model.Profile profile=new Model.Profile(UUID.randomUUID(),"Lopeeszz");profile.gems=900;profile.rgb=true;profile.colors.add("roxo");s.players.put(profile.id,profile);
        Model.Minion m=new Model.Minion(UUID.randomUUID());m.owner=profile.id;m.name="Caçador";m.world="plotworld";m.type="zumbi";m.placed=false;m.level=7;m.sword=4;m.enchants.put("horda-zumbi",2);m.pendingGems=50;m.commands.add(new Model.PendingCommand("Rara",Arrays.asList("give {player} emerald 1")));s.minions.put(m.id,m);
        Model round=Persistence.decode(Persistence.encode(s));assertEquals(900,round.players.get(profile.id).gems);assertTrue(round.players.get(profile.id).rgb);assertTrue(round.players.get(profile.id).colors.contains("roxo"));Model.Minion got=round.minions.get(m.id);assertFalse(got.placed);assertEquals(2,got.enchant("horda-zumbi"));assertEquals(50,got.pendingGems);assertEquals(1,got.commands.size());assertEquals("give {player} emerald 1",got.commands.get(0).commands.get(0));
    }
    @Test void corruptionDoesNotCreateBlankProgress(){assertThrows(Exception.class,()->Persistence.decode("players: [unterminated"));}
    @Test void negativeStoredBalanceFailsLoad(){assertThrows(Exception.class,()->Persistence.decode("players:\n  "+UUID.randomUUID()+":\n    name: test\n    gems: -1\n"));}
}
