package br.com.eulopeesz.revminions;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import javax.crypto.*;import javax.crypto.spec.SecretKeySpec;
import java.nio.file.*;import java.nio.charset.StandardCharsets;import java.security.*;import java.util.*;

public final class SignedItems {
    private final RevMinionsPlugin p;private final byte[] key;
    public SignedItems(RevMinionsPlugin p)throws Exception{this.p=p;Path path=p.getDataFolder().toPath().resolve("item-secret.key");if(!Files.exists(path)){byte[] k=new byte[32];new SecureRandom().nextBytes(k);Files.write(path,k,StandardOpenOption.CREATE_NEW);}key=Files.readAllBytes(path);if(key.length!=32)throw new IllegalStateException("chave inválida");}
    private String mac(String value){try{Mac m=Mac.getInstance("HmacSHA256");m.init(new SecretKeySpec(key,"HmacSHA256"));return Base64.getEncoder().encodeToString(m.doFinal(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
    public ItemStack plain(Material material,String name,List<String> lore,Map<String,String> vars){ItemStack i=new ItemStack(material);ItemMeta m=i.getItemMeta();m.setDisplayName(p.cfg.render(name,vars));List<String> l=new ArrayList<>();for(String s:lore)l.add(p.cfg.render(s,vars));m.setLore(l);i.setItemMeta(m);return i;}
    public ItemStack give(String type,String id){
        Map<String,String> v=new HashMap<>();v.put("id",id);v.put("valor",id);String path=type.equals("minion")?"minions."+id:"ativadores."+type;org.bukkit.configuration.file.YamlConfiguration y=type.equals("minion")?p.cfg.f("minions"):p.cfg.c();
        ItemStack i=plain(Configs.mat(y.getString(path+".material")),y.getString(path+".nome"),y.getStringList(path+".lore"),v);tag(i,type,id);return i;
    }
    public ItemStack returned(Model.Minion m){ItemStack i=give("minion",m.type);tag(i,"placed",m.id.toString());return i;}
    private void tag(ItemStack i,String type,String id){ItemMeta meta=i.getItemMeta();List<String> lore=meta.hasLore()?new ArrayList<>(meta.getLore()):new ArrayList<>();if(!lore.isEmpty()&&lore.get(lore.size()-1).replace("§","").startsWith("REV|"))lore.remove(lore.size()-1);
        String payload=type+":"+id+":"+i.getType().name(),raw="REV|"+payload+"|"+mac(payload);StringBuilder hidden=new StringBuilder();for(char c:raw.toCharArray())hidden.append('§').append(c);lore.add(hidden.toString());meta.setLore(lore);i.setItemMeta(meta);
    }
    public String[] read(ItemStack i){if(i==null||!i.hasItemMeta()||!i.getItemMeta().hasLore())return null;List<String> lore=i.getItemMeta().getLore();if(lore.isEmpty())return null;String[] raw=lore.get(lore.size()-1).replace("§","").split("\\|",-1);if(raw.length!=3||!raw[0].equals("REV")||!MessageDigest.isEqual(mac(raw[1]).getBytes(StandardCharsets.UTF_8),raw[2].getBytes(StandardCharsets.UTF_8)))return null;String[] values=raw[1].split(":",-1);return values.length==3&&values[2].equals(i.getType().name())?new String[]{values[0],values[1]}:null;}
    public void consume(Player player){ItemStack i=player.getItemInHand();if(i.getAmount()==1)player.setItemInHand(null);else{i.setAmount(i.getAmount()-1);player.setItemInHand(i);}}
    public ItemStack clothes(Material mat,Color color){ItemStack i=new ItemStack(mat);if(i.getItemMeta() instanceof LeatherArmorMeta){LeatherArmorMeta meta=(LeatherArmorMeta)i.getItemMeta();meta.setColor(color);i.setItemMeta(meta);}return i;}
}
