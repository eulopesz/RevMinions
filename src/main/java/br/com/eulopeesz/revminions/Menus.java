package br.com.eulopeesz.revminions;
import org.bukkit.*;import org.bukkit.entity.Player;import org.bukkit.inventory.*;import org.bukkit.inventory.meta.SkullMeta;import java.util.*;

public final class Menus {
    public static final class Holder implements InventoryHolder {public final UUID minion;public final String type;public final int page;public final Map<Integer,String> actions=new HashMap<>();private Inventory inventory;Holder(UUID id,String type,int page){minion=id;this.type=type;this.page=page;}public Inventory getInventory(){return inventory;}}
    private final RevMinionsPlugin p;public Menus(RevMinionsPlugin p){this.p=p;}
    public void open(Player player,Model.Minion m,String type){open(player,m,type,0);}
    private void button(Holder h,int slot,Material material,String name,List<String> lore,Map<String,String> vars,String action){h.inventory.setItem(slot,p.signed.plain(material,name,lore,vars));h.actions.put(slot,action);}
    private String label(String key){return p.cfg.f("menus").getString("botoes."+key,key);}
    public void open(Player player,Model.Minion m,String type,int page){
        if(!p.ready)return;if(m!=null&&!p.access(player,m)){p.msg(player,"sem-permissao");return;}
        Holder h=new Holder(m==null?null:m.id,type,page);boolean large=Arrays.asList("lista","armazem","amigos","cores","particulas","espada").contains(type);
        String title=type.equals("lista")?p.cfg.f("menus").getString("principal.titulo"):p.cfg.f("menus").getString(type+".titulo");Map<String,String> vars=m==null?new HashMap<>():p.cfg.vars(m);
        h.inventory=Bukkit.createInventory(h,large?54:27,p.cfg.render(title,vars));
        if(type.equals("lista")){
            List<Model.Minion> list=new ArrayList<>();for(Model.Minion one:p.state.minions.values())if(one.placed&&one.owner.equals(player.getUniqueId()))list.add(one);
            for(int i=page*45;i<Math.min(list.size(),page*45+45);i++){Model.Minion one=list.get(i);button(h,i-page*45,Material.ARMOR_STAND,one.name,p.cfg.f("menus").getStringList("principal.itens.estatisticas.lore"),p.cfg.vars(one),"open:"+one.id);}pagination(h,list.size());
        }else if(type.equals("principal")){
            for(String id:p.cfg.ids("menus","principal.itens")){String path="principal.itens."+id;button(h,p.cfg.f("menus").getInt(path+".slot"),Configs.mat(p.cfg.f("menus").getString(path+".material")),p.cfg.f("menus").getString(path+".nome"),p.cfg.f("menus").getStringList(path+".lore"),vars,id);}
        }else if(type.equals("evolucao")){button(h,13,Material.EXP_BOTTLE,label("confirmar"),Collections.emptyList(),vars,"upgrade");}
        else if(type.equals("espada")){
            Map<String,String> v=new HashMap<>(vars);v.put("custo",""+p.cfg.swordCost(m,null));button(h,4,Material.IRON_SWORD,label("confirmar"),Collections.emptyList(),v,"sword");int slot=10;
            for(String id:p.cfg.ids("sword","encantamentos")){v=new HashMap<>(vars);v.put("nome",p.cfg.f("sword").getString("encantamentos."+id+".nome"));v.put("nivel",""+m.enchant(id));v.put("custo",""+p.cfg.swordCost(m,id));button(h,slot++,Material.ENCHANTED_BOOK,label("encantamento"),p.cfg.f("menus").getStringList("botoes.encantamento-lore"),v,"enchant:"+id);}
        }else if(type.equals("personalizacao")){
            button(h,10,Material.LEATHER_CHESTPLATE,label("cores"),Collections.emptyList(),vars,"cores");button(h,12,Material.NETHER_STAR,label("particulas"),Collections.emptyList(),vars,"particulas");button(h,14,Material.NAME_TAG,label("nome"),Collections.emptyList(),vars,"name");button(h,16,Material.LEATHER_CHESTPLATE,label("rgb"),Collections.emptyList(),vars,"rgb");
        }else if(type.equals("cores")||type.equals("particulas")){
            List<String> ids=p.cfg.ids("config","personalizacao."+type);
            for(int i=page*45;i<Math.min(ids.size(),page*45+45);i++){String id=ids.get(i);ItemStack item=p.signed.plain(type.equals("cores")?Material.LEATHER_CHESTPLATE:Material.NETHER_STAR,id,Collections.emptyList(),vars);if(type.equals("cores")){item=p.signed.clothes(Material.LEATHER_CHESTPLATE,p.cfg.color(id));org.bukkit.inventory.meta.ItemMeta meta=item.getItemMeta();meta.setDisplayName(id);item.setItemMeta(meta);}h.inventory.setItem(i-page*45,item);h.actions.put(i-page*45,"select:"+id);}pagination(h,ids.size());
        }else if(type.equals("armazem")){
            for(int i=page*45;i<Math.min(m.items.size(),page*45+45);i++)h.inventory.setItem(i-page*45,m.items.get(i).clone());
            button(h,45,Material.CHEST,label("coletar-itens"),Collections.emptyList(),vars,"collect-items");button(h,46,Material.EMERALD,label("coletar-gemas"),Collections.emptyList(),vars,"collect-gems");button(h,47,Material.PAPER,label("coletar-comandos"),Collections.emptyList(),vars,"collect-commands");button(h,48,Material.EXP_BOTTLE,label("coletar-xp"),Collections.emptyList(),vars,"collect-xp");pagination(h,m.items.size());
        }else if(type.equals("amigos")){
            List<UUID> list=new ArrayList<>(m.friends);for(int i=page*45;i<Math.min(list.size(),page*45+45);i++){UUID id=list.get(i);Model.Profile friend=p.state.players.get(id);String name=friend==null?id.toString():friend.name;Map<String,String> v=new HashMap<>();v.put("player",name);ItemStack head=p.signed.plain(Material.SKULL_ITEM,label("amigo"),p.cfg.f("menus").getStringList("botoes.amigo-lore"),v);head.setDurability((short)3);SkullMeta meta=(SkullMeta)head.getItemMeta();meta.setOwner(name);head.setItemMeta(meta);h.inventory.setItem(i-page*45,head);h.actions.put(i-page*45,"remove-friend:"+id);}button(h,46,Material.EMERALD,label("adicionar-amigo"),Collections.emptyList(),vars,"add-friend");pagination(h,list.size());
        }
        if(!type.equals("lista"))button(h,large?49:22,Material.ARROW,label("voltar"),Collections.emptyList(),vars,"back");player.openInventory(h.inventory);
    }
    private void pagination(Holder h,int count){if(h.page>0)button(h,50,Material.ARROW,label("anterior"),Collections.emptyList(),Collections.emptyMap(),"prev");if((h.page+1)*45<count)button(h,53,Material.ARROW,label("proxima"),Collections.emptyList(),Collections.emptyMap(),"next");}
    public void click(Player player,Holder h,int slot){
        String action=h.actions.get(slot);if(action==null||!p.ready)return;Model.Minion m=h.minion==null?null:p.state.minions.get(h.minion);
        if(action.startsWith("open:")){m=p.state.minions.get(UUID.fromString(action.substring(5)));if(m!=null)open(player,m,"principal");return;}
        if(action.equals("next")||action.equals("prev")){open(player,m,h.type,h.page+(action.equals("next")?1:-1));return;}
        if(m==null||!m.placed||!p.access(player,m)){p.msg(player,"minion-ausente");player.closeInventory();return;}
        if(action.equals("back")){open(player,m,"principal");return;}
        if(Arrays.asList("evolucao","espada","personalizacao","armazem","amigos","cores","particulas").contains(action)){open(player,m,action);return;}
        boolean owner=m.owner.equals(player.getUniqueId());
        if(action.equals("collect-items")){if(!owner&&!p.cfg.c().getBoolean("amigos.coletar-itens")){p.msg(player,"apenas-dono");return;}boolean any=false;for(Iterator<ItemStack> it=m.items.iterator();it.hasNext();){ItemStack original=it.next();Map<Integer,ItemStack> left=player.getInventory().addItem(original.clone());if(left.isEmpty()){it.remove();any=true;}else{ItemStack remain=left.values().iterator().next();if(remain.getAmount()<original.getAmount()){original.setAmount(remain.getAmount());any=true;}break;}}p.msg(player,any?"coletado":"inventario-cheio");p.dirty();open(player,m,"armazem");return;}
        if(action.startsWith("collect-")){
            if(!owner){p.msg(player,"apenas-dono");return;}Model.Profile profile=p.profile(player);
            if(action.equals("collect-gems")){if(m.pendingGems==0){p.msg(player,"armazem-vazio");return;}try{profile.gems=Model.add(profile.gems,m.pendingGems);}catch(ArithmeticException e){p.msg(player,"invalido");return;}m.pendingGems=0;}
            if(action.equals("collect-xp")){if(m.xp==0){p.msg(player,"armazem-vazio");return;}player.giveExp(m.xp);m.xp=0;}
            if(action.equals("collect-commands")){if(m.commands.isEmpty()){p.msg(player,"armazem-vazio");return;}Model.PendingCommand reward=m.commands.remove(0);p.dirty();for(String command:reward.commands)Bukkit.dispatchCommand(Bukkit.getConsoleSender(),command.replace("{player}",player.getName()).replace("{uuid}",player.getUniqueId().toString()));}
            p.dirty();p.msg(player,"coletado");open(player,m,"armazem");return;
        }
        if(action.equals("name")||action.equals("rgb")||action.startsWith("select:")){
            if(!owner&&!p.cfg.c().getBoolean("amigos.personalizar")){p.msg(player,"apenas-dono");return;}Model.Profile profile=p.state.players.get(m.owner);
            if(action.equals("name")){p.ask(player,m,"name");return;}
            if(action.equals("rgb")){if(!profile.rgb){p.msg(player,"bloqueado");return;}m.rgb=!m.rgb;}
            if(action.startsWith("select:")){String id=action.substring(7);boolean color=h.type.equals("cores");if(!(color?profile.colors:profile.particles).contains(id)){p.msg(player,"bloqueado");return;}if(color){m.color=id;m.rgb=false;}else m.particle=id;}
            p.dirty();p.msg(player,"personalizado");open(player,m,"personalizacao");return;
        }
        if(!owner){p.msg(player,"apenas-dono");return;}
        if(action.equals("add-friend")){p.ask(player,m,"friend");return;}
        if(action.startsWith("remove-friend:")){m.friends.remove(UUID.fromString(action.substring(14)));p.dirty();p.msg(player,"amigo-removido");open(player,m,"amigos");return;}
        if(action.equals("recolher")){
            if(m.stored()>0){p.msg(player,"armazem-cheio");return;}if(player.getInventory().firstEmpty()<0){p.msg(player,"inventario-cheio");return;}
            p.combat.cancel(m.id);m.placed=false;p.entities.remove(m.id);player.getInventory().addItem(p.signed.returned(m));p.dirty();p.msg(player,"recolhido");player.closeInventory();return;
        }
        if(action.equals("upgrade")||action.equals("sword")||action.startsWith("enchant:")){
            boolean minion=action.equals("upgrade"),sword=action.equals("sword");String enchant=action.startsWith("enchant:")?action.substring(8):null;
            int current=minion?m.level:sword?m.sword:m.enchant(enchant);int max=minion?p.cfg.f("minions").getInt("minions."+m.type+".evolucao.nivel-maximo"):p.cfg.f("sword").getInt(sword?"espada.nivel-maximo":"encantamentos."+enchant+".nivel-maximo");
            if(current>=max){p.msg(player,"nivel-maximo");return;}long cost=minion?p.cfg.cost(m.type,current):p.cfg.swordCost(m,enchant);Model.Profile profile=p.profile(player);if(profile.gems<cost){p.msg(player,"gemas-insuficientes","custo",""+cost);return;}profile.gems-=cost;if(minion)m.level++;else if(sword)m.sword++;else m.enchants.put(enchant,current+1);p.dirty();p.msg(player,"evoluiu");open(player,m,h.type);
        }
    }
}
