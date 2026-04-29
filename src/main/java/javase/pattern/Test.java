package javase.pattern;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description:
 * @Author: nianjie.chen
 * @Date: 5/4/2022
 */
public class Test {



//    @org.junit.jupiter.api.Test
    void testSplit(){
        String s = "the quick brown fox jumps over the lazy dog.";
        Pattern p = Pattern.compile("\\wo\\w");
        Matcher m = p.matcher(s);
        while (m.find()) {
            String sub = s.substring(m.start(), m.end());
            System.out.println(sub);
        }
    }


    //自定义简单的模板引擎
    //@org.junit.jupiter.api.Test
    void testTemplate(){
        Map<String, String> map = new HashMap<>();
        map.put("name", "Jay");
        map.put("age","25");

        String message = "my name is ${name} and age is ${age}.";

        for (String key : map.keySet()){
            String regex = "\\$\\{"+key+"\\}";
            message = message.replaceAll(regex, map.get(key));
        }
        System.out.println(message);
    }
}
