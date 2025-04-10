package cn.org.alan.exam.utils;

import cn.org.alan.exam.constant.enums.ReEnum;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 校验正则
 */
public class ReUtil {

    public static boolean verifyRe(String re, String value)
    {
        if(StringUtil.isEmpty(value)) return false;
        Pattern pattern = Pattern.compile(re);
        Matcher matcher = pattern.matcher(value);
        return matcher.matches();
    }

    public static boolean verifyRe(ReEnum re, String value)
    {
        return verifyRe(re.getRegex(),value);
    }
}
