import java.util.HashMap;
import java.util.Map;

public class TimeWheel {

    public static void main(String[] args){

        Map<String,String> map=new HashMap<>();
        map.put("1","1");
        map.put("2","2");
        map.put("3","");

        String s = map.get("15");
        System.out.println(s);
        String s1 = map.get("3");
        System.out.println(s1);

        String email="hudong@qq.com";
        String result = email.substring(email.lastIndexOf("@")+1);
        System.out.println(result);
    }
}
