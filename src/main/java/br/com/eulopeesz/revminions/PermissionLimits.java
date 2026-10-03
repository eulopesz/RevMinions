package br.com.eulopeesz.revminions;

import org.bukkit.configuration.ConfigurationSection;
import java.util.*;
import java.util.function.Predicate;

/** Chaves de permissão pontuadas podem virar MemorySections ao carregar YAML. */
public final class PermissionLimits {
    private final Map<String,Integer> limits;
    private PermissionLimits(Map<String,Integer> limits) { this.limits=Collections.unmodifiableMap(limits); }
    public static PermissionLimits read(ConfigurationSection section) {
        Map<String,Integer> limits=new LinkedHashMap<>();
        if(section!=null)flatten(section,"",limits);
        return new PermissionLimits(limits);
    }
    private static void flatten(ConfigurationSection section,String prefix,Map<String,Integer> result) {
        for(Map.Entry<String,Object> entry:section.getValues(false).entrySet()) {
            String permission=prefix.isEmpty()?entry.getKey():prefix+"."+entry.getKey();
            Object value=entry.getValue();
            if(value instanceof ConfigurationSection) {
                flatten((ConfigurationSection)value,permission,result);
                continue;
            }
            if(permission.isEmpty()||!permission.matches("[a-zA-Z0-9_.*-]+"))throw new IllegalArgumentException("Permissão inválida: "+permission);
            double number;
            if(value instanceof Number)number=((Number)value).doubleValue();
            else if(value instanceof String) {
                try{number=Double.parseDouble((String)value);}catch(NumberFormatException e){throw invalid(permission);}
            } else throw invalid(permission);
            if(!Double.isFinite(number)||number<1||number>Integer.MAX_VALUE||number!=Math.floor(number))throw invalid(permission);
            result.put(permission,(int)number);
        }
    }
    private static IllegalArgumentException invalid(String permission) {
        return new IllegalArgumentException("minions.limites-por-permissao."+permission+" exige inteiro positivo");
    }
    public int resolve(int base,Predicate<String> hasPermission) {
        int limit=base;
        for(Map.Entry<String,Integer> entry:limits.entrySet())if(hasPermission.test(entry.getKey()))limit=Math.max(limit,entry.getValue());
        return limit;
    }
}
