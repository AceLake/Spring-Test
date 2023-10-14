package com.example.demo.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.controller.SimpleDataTool;
import com.example.demo.io.JsonHelper;
import com.example.demo.io.SimpleModel;

@Controller
@RequestMapping("/home")
public class HomeController {
    @GetMapping("/")
	public String display(Model model) {
        // http://localhost:8080/service/getjson
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/service/getjson"))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        JsonHelper jsonHelper = new JsonHelper();
        List<SimpleModel> strings = jsonHelper.loadJson2(response.body(), SimpleModel.class);


        for (SimpleModel simpleModel : strings) {
            System.out.println(simpleModel.getName());
            System.out.print(", "+simpleModel.getInteger());
            System.out.print(", "+simpleModel.getDecimal());
        }
		model.addAttribute("title","Store Page");    
		model.addAttribute("strings", strings);
		
        // this needs to return a storefront html page
		return "home";
	}
}