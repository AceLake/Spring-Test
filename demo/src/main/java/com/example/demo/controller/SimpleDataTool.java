package com.example.demo.controller;

import java.util.List;
import com.example.demo.io.SimpleModel;
import com.example.demo.io.JsonHelper;

public class SimpleDataTool {

    private static final String JSON_FILENAME_SIMPLE = "simple.json";
    private static final JsonHelper<SimpleModel> simpleModelLoader = new JsonHelper<>();

    private List<SimpleModel> simpleModels;

    public List<SimpleModel> loadSimpleModels() {
        simpleModels = simpleModelLoader.loadJson(JSON_FILENAME_SIMPLE, SimpleModel.class);

        return simpleModels;
    }

}
