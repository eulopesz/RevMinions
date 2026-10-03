package br.com.eulopeesz.revminions;
import org.bukkit.*;import org.bukkit.command.*;import org.bukkit.entity.Player;import org.bukkit.inventory.ItemStack;import java.util.*;

public final class Commands implements CommandExecutor,TabCompleter {
    private final RevMinionsPlugin p;public Commands(RevMinionsPlugin p){this.p=p;}
    public boolean onCommand(CommandSender sender,Command command,String label,String[] a){
        if(a.length>0&&(a[0].equalsIgnoreCase("ajuda")||a[0].equalsIgnoreCase("help"))){p.msg(sender,"ajuda");return true;}
        if(!p.ready){p.msg(sender,"indisponivel");return true;}
        if(a.length==0){if(!(sender instanceof Player)){p.msg(sender,"apenas-jogadores");return true;}if(!sender.hasPermission("revminions.use")){p.msg(sender,"sem-permissao");return true;}p.menus.open((Player)sender,null,"lista");return true;}
        if(a[0].equalsIgnoreCase("reload")&&a.length==1){if(admin(sender)){p.msg(sender,"reload-inicio");p.reload(sender);}return true;}
        if(a[0].equalsIgnoreCase("gemas")){
            if(a.length==1){if(sender instanceof Player)p.msg(sender,"gemas-saldo","gemas",""+p.profile((Player)sender).gems);else p.msg(sender,"apenas-jogadores");return true;}
            if(a.length==2&&a[1].equalsIgnoreCase("top")){List<Model.Profile> list=new ArrayList<>(p.state.players.values());list.sort(Comparator.comparingLong((Model.Profile x)->x.gems).reversed().thenComparing(x->x.name.toLowerCase(Locale.ROOT)));p.msg(sender,"top-titulo");if(list.isEmpty())p.msg(sender,"top-vazio");for(int i=0;i<Math.min(list.size(),p.cfg.c().getInt("gemas.top-limite"));i++)p.msg(sender,"top-linha","posicao",""+(i+1),"player",list.get(i).name,"gemas",""+list.get(i).gems);return true;}
            if(a.length==4&&(a[1].equalsIgnoreCase("give")||a[1].equalsIgnoreCase("set"))){if(!admin(sender))return true;Model.Profile target=p.find(a[2]);if(target==null){p.msg(sender,"jogador-ausente");return true;}try{long amount=Long.parseLong(a[3]);if(amount<0)throw new IllegalArgumentException();target.gems=a[1].equalsIgnoreCase("set")?amount:Model.add(target.gems,amount);p.dirty();p.msg(sender,"gemas-alteradas","player",target.name,"gemas",""+target.gems);}catch(IllegalArgumentException|ArithmeticException e){p.msg(sender,"invalido");}return true;}
        }
        if(a[0].equalsIgnoreCase("give")&&a.length>=3&&a.length<=5){
            if(!admin(sender))return true;Player target=Bukkit.getPlayerExact(a[2]);if(target==null){p.msg(sender,"jogador-ausente");return true;}
            String type=a[1].toLowerCase(Locale.ROOT);try{
                String id;int amount;
                if(type.equals("rgb")){if(a.length>4)throw new IllegalArgumentException();id="rgb";amount=a.length==4?Integer.parseInt(a[3]):1;}
                else{if(a.length<4)throw new IllegalArgumentException();id=a[3].toLowerCase(Locale.ROOT);amount=a.length==5?Integer.parseInt(a[4]):1;}
                if(type.equals("minion")){if(!p.cfg.ids("minions","minions").contains(id))throw new IllegalArgumentException();}
                else if(type.equals("cor")||type.equals("particula")){if(!p.cfg.ids("config","personalizacao."+(type.equals("cor")?"cores":"particulas")).contains(id))throw new IllegalArgumentException();}
                else if(type.equals("gemas")){if(Long.parseLong(id)<=0)throw new IllegalArgumentException();}
                else if(!type.equals("rgb"))throw new IllegalArgumentException();
                ItemStack i=p.signed.give(type,id);if(amount<1||amount>i.getMaxStackSize())throw new IllegalArgumentException();i.setAmount(amount);int slot=target.getInventory().firstEmpty();if(slot<0){p.msg(sender,"inventario-cheio");return true;}target.getInventory().setItem(slot,i);p.msg(sender,"item-entregue","player",target.getName());
            }catch(IllegalArgumentException e){p.msg(sender,"invalido");}return true;
        }
        p.msg(sender,"invalido");return true;
    }
    private boolean admin(CommandSender s){if(s.hasPermission("revminions.admin"))return true;p.msg(s,"sem-permissao");return false;}
    public List<String> onTabComplete(CommandSender s,Command c,String label,String[] a){List<String> list=new ArrayList<>();if(a.length==1){list.addAll(Arrays.asList("ajuda","gemas"));if(s.hasPermission("revminions.admin"))list.addAll(Arrays.asList("reload","give"));}
        if(a[0].equalsIgnoreCase("gemas")&&a.length==2){list.add("top");if(s.hasPermission("revminions.admin"))list.addAll(Arrays.asList("give","set"));}
        if(s.hasPermission("revminions.admin")){if(a[0].equalsIgnoreCase("give")){if(a.length==2)list.addAll(Arrays.asList("minion","cor","particula","rgb","gemas"));if(a.length==3)for(Player x:Bukkit.getOnlinePlayers())list.add(x.getName());if(a.length==4){if(a[1].equalsIgnoreCase("minion"))list.addAll(p.cfg.ids("minions","minions"));if(a[1].equalsIgnoreCase("cor"))list.addAll(p.cfg.ids("config","personalizacao.cores"));if(a[1].equalsIgnoreCase("particula"))list.addAll(p.cfg.ids("config","personalizacao.particulas"));}}if(a.length==3&&a[0].equalsIgnoreCase("gemas"))for(Model.Profile x:p.state.players.values())list.add(x.name);}
        String prefix=a[a.length-1].toLowerCase(Locale.ROOT);list.removeIf(v->!v.toLowerCase(Locale.ROOT).startsWith(prefix));return list;}
}
