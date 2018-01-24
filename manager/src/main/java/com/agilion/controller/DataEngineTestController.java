package com.agilion.controller;

import com.agilion.services.dataengine.DataEngineClient;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Created by Alex_Lappy_486 on 1/19/18.
 */
@Controller
@RequestMapping("/admin/dataengine")
public class DataEngineTestController
{
    @Autowired
    DataEngineClient client;

    @Autowired
    Gson gson;

    @RequestMapping("/listOperations")
    public @ResponseBody
    String  listOperations()
    {
        return client.listOperations().toString();
    }

    @RequestMapping("/test")
    public String dataEngineTest(Model model)
    {
        model.addAttribute("operations", gson.toJson(client.listOperations()));
        return "admin/dataEngineTest";
    }

    @RequestMapping(value = "/submit", method = RequestMethod.POST)
    public @ResponseBody String dataEngineSubmit(@RequestBody Map<String, Object> data)
    {
        sendDataAsDataEngineRequest(data);
        return "admin/dataEngineTest";
    }

    public void sendDataAsDataEngineRequest(Map<String, Object> data)
    {

    }
}
