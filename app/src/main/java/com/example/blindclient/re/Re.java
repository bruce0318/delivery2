package com.example.blindclient.re;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Re {
    private static final Map<String, Operation> pattern = new HashMap<String, Operation>(){{
        put("(.*)导航到(.*)", Operation.Navigation);
        put("(.*)志愿者(.*)", Operation.Volunteer);
        put("(.*)监护人(.*)", Operation.Guardian);
        put("(.*)退出程序(.*)", Operation.Quit);
        put("(.*)退出(.*)导航(.*)", Operation.QuitNavi);
        put("(.*)停止(.*)导航(.*)", Operation.QuitNavi);
    }
    };

    public static Operation match(String content){
        for(Map.Entry<String, Operation> entry : pattern.entrySet()){
            if(Pattern.matches(entry.getKey(), content))
                return entry.getValue();
        }
        return Operation.Error;
    }

    public static String searchPlace(String content){
        String REGEX = "到";
        Pattern pattern = Pattern.compile(REGEX);
        Matcher matcher = pattern.matcher(content);

        if(matcher.find()){
            return content.substring(matcher.start() + 1);
        }
        else{
            return null;
        }
    }
}
