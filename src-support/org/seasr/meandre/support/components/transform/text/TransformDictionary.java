package org.seasr.meandre.support.components.transform.text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class TransformDictionary {

    public static Map<String,List<String>> buildFrom(String rules) throws TransformDictionaryException
    {
        rules = rules.replaceAll("\n","");

        Map<String,List<String>> map = new HashMap<String,List<String>>();
        StringTokenizer tokens = new StringTokenizer(rules, ";");

        while (tokens.hasMoreTokens()) {
            String line = tokens.nextToken();
            String[] parts = line.split("=");

            if (parts.length != 2) {            // lots of = signs
                parts = line.split(":=");
            }

            if (parts.length != 2)
                throw new TransformDictionaryException("Unable to build dictionary! Parse error.");

            String key    = parts[0].trim();
            String values = parts[1].trim();

            values = values.replace("{","");
            values = values.replace("}","");

            StringTokenizer vTokens = new StringTokenizer(values, ",");
            while (vTokens.hasMoreTokens()) {
                String value = vTokens.nextToken().trim();

                List<String> replacements = map.get(value);
                if (replacements == null) {
                    replacements = new ArrayList<String>();
                    map.put(value, replacements);
                }

                replacements.add(key);
            }
        }
        return map;
    }

}
