package com.godzhang.waiyutong;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Waiyutong {
    private String m_Cookie;
    private boolean m_isLogined;
    private String m_HomeworkJson;
    private List<List<String>> m_Answer;

    public static String httpPost(String urlStr, Map<String,String> params, StringBuffer saveCookie){
        URL connect;
        StringBuffer data = new StringBuffer();
        try {
            connect = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection)connect.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);//post不能使用缓存
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            OutputStreamWriter paramout = new OutputStreamWriter(connection.getOutputStream(),"UTF-8");
            String paramsStr = "";   //拼接Post 请求的参数
            for(String param : params.keySet()){
                paramsStr += "&" + param + "=" + params.get(param);
            }

            if(!paramsStr.isEmpty()){
                paramsStr = paramsStr.substring(1);
            }
            paramout.write(paramsStr);
            paramout.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
            String SERVERID = "";
            String PHPSESSID = "";
            List<String> list = connection.getHeaderFields().get("Set-Cookie");
            for (int i = list.size() - 1; i >= 0; i--)
            {
                String str = list.get(i);
                if (str.contains("SERVERID") && SERVERID.isEmpty())
                {
                    SERVERID = str.substring(0, str.indexOf(';'));
                    // System.out.println(SERVERID);
                }
                else if(str.contains("PHPSESSID") && PHPSESSID.isEmpty())
                {
                    PHPSESSID = str.substring(0, str.indexOf(';'));
                    //System.out.println(PHPSESSID);
                }
                //Log.d("WAIYUTONG_DEBUG", str);
            }
            saveCookie.append(SERVERID + ';' + PHPSESSID);
            paramout.close();
            reader.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data.toString();
    }
    public static String httpPost(String urlStr, String cookie, Map<String,String> params){
        URL connect;
        StringBuffer data = new StringBuffer();
        try {
            connect = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection)connect.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);//post不能使用缓存
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
            connection.setRequestProperty("Cookie", cookie);
            OutputStreamWriter paramout = new OutputStreamWriter(
                    connection.getOutputStream(),"UTF-8");
            String paramsStr = "";   //拼接Post 请求的参数
            for(String param : params.keySet()){
                paramsStr += "&" + param + "=" + params.get(param);
            }

            if(!paramsStr.isEmpty()){
                paramsStr = paramsStr.substring(1);
            }
            paramout.write(paramsStr);
            paramout.flush();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                data.append(line);
            }
            paramout.close();
            reader.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return data.toString();
    }
    public static String MD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public Waiyutong() {
        m_Cookie = "";
        m_HomeworkJson = "";
        m_isLogined = false;
        m_Answer = new ArrayList<>();
    }

    public boolean Login(String username, String password) {
        final String LoginUrl = "http://www.waiyutong.org/User/login.html";
        String usernameEncoded = URLEncoder.encode(username);
        String passwordMD5 = MD5(password);
        if (null == usernameEncoded || passwordMD5 == null)
        {
            return false;
        }

        Map<String, String> param = new HashMap<>();
        param.put("username", usernameEncoded);
        param.put("password", passwordMD5);
        StringBuffer cookie = new StringBuffer();
        String result = httpPost(LoginUrl, param, cookie);
        //Log.d("WAIYUTONG_DEBUG", cookie.toString());
        JSONObject root = JSON.parseObject(result);
        if (root.getIntValue("status") == 1)
        {
            m_Cookie = cookie.toString();
            m_isLogined = true;
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean GetHomework(int hid) {
        if (!m_isLogined)
        {
            return false;
        }

        final String homeworkUrl = "http://student.waiyutong.org/Practice/getHomeworkTests.html";
        Map<String, String> param = new HashMap<>();
        param.put("hid", String.valueOf(hid));
        String result = httpPost(homeworkUrl, m_Cookie, param);

        JSONObject root = JSON.parseObject(result);
        if (root.getIntValue("status") == 1)
        {
            m_HomeworkJson = result;
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean GetHomeworkByHid(int hid) {
        if (!GetHomework(hid))
        {
            return false;
        }
        this.ParseHomework();
        return true;
    }

    private void ParseHomework() {
        if (!m_isLogined)
        {
            return;
        }

        List<String> answer0 = new ArrayList<>();
        List<String> answer1 = new ArrayList<>();
        List<String> answer2 = new ArrayList<>();
        List<String> answer3 = new ArrayList<>();
        List<String> answer4 = new ArrayList<>();
        m_Answer.add(answer0);
        m_Answer.add(answer1);
        m_Answer.add(answer2);
        m_Answer.add(answer3);
        m_Answer.add(answer4);

        int i = 0;

        while (i < 10)
        {
            String all = JSON.parseObject(m_HomeworkJson).getJSONObject("info").getJSONArray("parseTests").get(i).toString();

            if (i >= 0 && i < 5)
            {
                Pattern pattern = Pattern.compile("<p class=\\\\\"right_answer_class\\\\\" data-right-answer=\\\\\"(.)\\\\\">");
                Matcher m = pattern.matcher(all);
                m.find();
                //System.out.println(m.group(1));
                answer0.add(m.group(1));
            }
            else if (i == 5)
            {
                Pattern pattern = Pattern.compile("<p class=\\\\\"right_answer_class\\\\\" data-right-answer=\\\\\"(.)\\\\\">");
                Matcher m = pattern.matcher(all);
                while (m.find())
                {
                    //System.out.println(m.group(1));
                    answer1.add(m.group(1));
                }
            }
            else if (i == 6)
            {
                Pattern pattern = Pattern.compile("<p class=\\\\\"right_answer_class\\\\\" data-right-answer=\\\\\"(.)\\\\\">");
                Matcher m = pattern.matcher(all);
                while (m.find())
                {
                    //System.out.println(m.group(1));
                    answer2.add(m.group(1));
                }
            }
            else if (i == 7)
            {
                i++;
                continue;
            }
            else if (i == 8)
            {
                Pattern pattern = Pattern.compile("<div class=\\\\\"speak_sentence no_audio answer enable\\\\\" data-mp3=\\\\\".{0,20}.mp3\\\\\" data-starttime=[0-9] data-endtime=[0-9] data-text=\\\\\"(.*?)\\\\\"> answer:&nbsp;&nbsp;(.*?)</div>");
                Matcher m = pattern.matcher(all);
                while (m.find())
                {
                    //System.out.println(m.group(1));
                    answer3.add(m.group(1));
                }
            }
            else if (i == 9)
            {
                Pattern pattern = Pattern.compile("<span class=\\\\\"speak_sentence enable\\\\\" data-mp3=\\\\\".[0-9]*.mp3\\\\\" data-starttime=.[0-9]* data-endtime=.[0-9]*>(.*?)</span>");
                Matcher m = pattern.matcher(all);
                while (m.find())
                {
                    //System.out.println(m.group(1));
                    answer4.add(m.group(1));
                }
            }

            i++;
        }
    }

    public List<List<String>> GetAnswer() {
        return m_Answer;
    }

    private List<Integer> GetHomeworkHidByDate(String date) {
        String pageUrl = "http://student.waiyutong.org/Homework/listDetail.html";
        List<Integer> ret = new ArrayList<>();
        int page = 1;
        int pageNum = -1;
        Map<String, String> param = new HashMap<>();
        param.put("p", String.valueOf(page));

        String result = httpPost(pageUrl, m_Cookie, new HashMap<>());
        JSONObject root = JSON.parseObject(result);
        if (root.getIntValue("status") == 1)
        {
            String tmp = root.getJSONObject("info").getString("page");
            int pos = tmp.indexOf("共");
            int ePos = tmp.indexOf("条");
            String num = tmp.substring(pos + 2, ePos - 1);
            pageNum = Integer.valueOf(num).intValue();
        }
        else
        {
            return null;
        }

        if (pageNum % 4 == 0)
        {
            pageNum %= 4;
        }
        else
        {
            pageNum = pageNum / 4 + 1;
        }

        while (page <= pageNum)
        {
            System.out.println("page=" + page);
            JSONObject oJson = JSON.parseObject(httpPost(pageUrl, m_Cookie, param));
            if (oJson.getIntValue("status") != 1)
            {
                break;
            }

            JSONArray all = oJson.getJSONObject("info").getJSONArray("homework");
            for (int i = 0; i < oJson.size() - 1; i++)
            {
                if (all.getJSONObject(i).get("start_time").toString().contains(date))
                {
                    String hidTmp = all.getJSONObject(i).getString("id");
                    int hid = Integer.valueOf(hidTmp.substring(0, hidTmp.length()));
                    ret.add(hid);
                }
            }

            ++page;
            param.replace("p", String.valueOf(page));
        }

        return ret;
    }

    public boolean GetHomeworkByDate(String date)
    {
        List<Integer> list = GetHomeworkHidByDate(date);
        if (list != null)
        {
            for (int i = 0; i < list.size(); i++)
            {
                if (GetHomework(list.get(i)))
                {
                    ParseHomework();
                }
            }
        }
        return false;
    }

    private int GetLastestHomeworkHid() {
        if (!m_isLogined)
        {
            //Log.d("WAIYUTONG_DEBUG", "HID NOT LOGINED");
            return -1;
        }
        final String hkListUrl = "http://student.waiyutong.org/Homework/listDetail.html";
        String result = httpPost(hkListUrl, m_Cookie, new HashMap<>());

        JSONObject root = JSON.parseObject(result);
        //Log.d("WAIYUTONG_DEBUG", result);
        if (root.getIntValue("status") == 1)
        {
            String tmp = root.getJSONObject("info").getJSONArray("homework").getJSONObject(0).getString("id");
            int hid = Integer.valueOf(tmp).intValue();
            //System.out.println(hid);
            return hid;
        }
        else
        {
            //Log.d("WAIYUTONG_DEBUG", "HID NO FOUND");
            return -1;
        }
    }
    public boolean GetLastestHomework() {
        if (!m_isLogined)
        {
            //Log.d("WAIYUTONG_DEBUG", "Not LOGIN RETURN FALSE");
            return false;
        }
        int hid = GetLastestHomeworkHid();
        if (hid < 0)
        {
            //Log.d("WAIYUTONG_DEBUG", "HID ERR RETURN FALSE");
            return false;
        }

        if (GetHomework(hid))
        {
            ParseHomework();
            return true;
        }
        //Log.d("WAIYUTONG_DEBUG", "NORMALLY RETURN FALSE");
        return false;
    }
}
