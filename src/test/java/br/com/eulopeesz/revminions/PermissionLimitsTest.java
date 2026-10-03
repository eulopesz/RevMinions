package br.com.eulopeesz.revminions;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PermissionLimitsTest {
    private PermissionLimits parse(String yaml)throws Exception{
        YamlConfiguration config=new YamlConfiguration();config.loadFromString(yaml);
        return PermissionLimits.read(config.getConfigurationSection("minions.limites-por-permissao"));
    }
    @Test void bundledDefaultReproducesSectionAndNowResolvesDottedPermission()throws Exception{
        YamlConfiguration config=new YamlConfiguration();
        try(Reader reader=new InputStreamReader(getClass().getResourceAsStream("/config.yml"),StandardCharsets.UTF_8)){config.load(reader);}
        ConfigurationSection section=config.getConfigurationSection("minions.limites-por-permissao");
        assertTrue(section.getValues(false).get("revminions") instanceof ConfigurationSection);
        List<String> checked=new ArrayList<>();
        int limit=PermissionLimits.read(section).resolve(5,key->{checked.add(key);return key.equals("revminions.limit.10");});
        assertEquals(10,limit);assertEquals(Collections.singletonList("revminions.limit.10"),checked);
    }
    @Test void literalAndExplicitNestedConfigurationsProduceSamePermissions()throws Exception{
        String literal="minions:\n  limites-por-permissao:\n    revminions.limit.10: 10\n    revminions.limit.20: 20\n";
        String nested="minions:\n  limites-por-permissao:\n    revminions:\n      limit:\n        '10': 10\n        '20': 20\n";
        assertEquals(20,parse(literal).resolve(5,key->true));assertEquals(20,parse(nested).resolve(5,key->true));
        assertEquals(5,parse(literal).resolve(5,key->false));
    }
    @Test void missingOverridesAndLowerPermissionDoNotReduceBaseLimit()throws Exception{
        assertEquals(5,PermissionLimits.read(null).resolve(5,key->true));
        assertEquals(30,parse("minions:\n  limites-por-permissao:\n    revminions.limit.10: 10\n").resolve(30,key->true));
    }
    @Test void quotedIntegerIsAcceptedButInvalidLimitsFailBeforePlacement()throws Exception{
        assertEquals(10,parse("minions:\n  limites-por-permissao:\n    revminions.limit.10: '10'\n").resolve(5,key->true));
        for(String invalid:Arrays.asList("abc","0","-1","10.5","true","2147483648")){
            assertThrows(IllegalArgumentException.class,()->parse("minions:\n  limites-por-permissao:\n    revminions.limit.test: "+invalid+"\n"));
        }
    }
}
