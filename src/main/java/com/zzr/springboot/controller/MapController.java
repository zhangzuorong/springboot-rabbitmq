package com.zzr.springboot.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 开发公司：山东海豚数据技术有限公司
 * 版权：山东海豚数据技术有限公司
 * <p>
 * MapController
 *
 * @author zzr
 * @created Create Time: 2019/5/23
 */
@RestController
@RequestMapping("/map")
public class MapController {

    @Value("${tengxun.mpKey}")
    public String key;

    @Autowired
    public RestTemplate restTemplate;

    /**
     * 逆地址解析（坐标位置描述）
     * @param lat 纬度
     * @param lng 经度
     */
    @GetMapping("/geocoder")
    public String geocoder(String lat,String lng){
        String location = lat+","+lng;
        String url = "https://apis.map.qq.com/ws/geocoder/v1/?location="+location+"&key="+key;
        ResponseEntity<String> results = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        JSONObject jsonObject = JSONObject.parseObject(results.getBody());
        if(Integer.parseInt(jsonObject.get("status").toString()) != 0){
            String resutl = jsonObject.get("message").toString();
            System.out.println(resutl);
            return resutl;
        }
        Map<String,Object> map = (Map<String, Object>) jsonObject.get("result");
        System.out.println(map.get("address"));
        String resutl = jsonObject.get("message").toString()+"地址="+map.get("address");
        return resutl;
    }

    /**
     * 关键词输入提示
     * @param keyword 用户输入的关键词（希望获取后续提示的关键词）
     * @param region 限制城市范围：根据城市名称限制地域范围， 如，仅获取“广州市”范围内的提示内容
     */
    @GetMapping("/suggestion")
    public List<Map<String,Object>>  suggestion(String keyword,String region){
        String url = "https://apis.map.qq.com/ws/place/v1/suggestion?region="+region+"&keyword="+keyword+"&key="+key;
        ResponseEntity<String> results = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        JSONObject jsonObject = JSONObject.parseObject(results.getBody());
        if(Integer.parseInt(jsonObject.get("status").toString()) != 0){
            String resutl = jsonObject.get("message").toString();
            System.out.println(resutl);
            return new ArrayList<>();
        }
        List<Map<String,Object>> list = (List<Map<String, Object>>) jsonObject.get("data");
        return list;
    }

}
