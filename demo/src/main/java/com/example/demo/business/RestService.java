package com.example.demo.business;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.controller.SimpleDataTool;
import com.example.demo.io.SimpleModel;

@RestController
@RequestMapping("/service")
public class RestService {
        
    SimpleDataTool tool = new SimpleDataTool();
    List<SimpleModel> strings = tool.loadSimpleModels();


    @GetMapping (path="/getjson", produces={MediaType.APPLICATION_JSON_VALUE})
    public List<SimpleModel> getProductsAsJSON()
    {
        return strings;
    }

}
