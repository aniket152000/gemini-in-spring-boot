package com.example.gemini_and_springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.gemini_and_springboot.service.GeminiAIService;

@RestController
@RequestMapping("/api")
public class GeminiAIController {

    private final GeminiAIService geminiAIService;

    @Autowired
    public GeminiAIController(GeminiAIService geminiAIService) {
        this.geminiAIService = geminiAIService;
    }

    @PostMapping("/generate")
    public String generateContent(@RequestParam("file") MultipartFile file) {
        return geminiAIService.predictImage(file);

    }

}
