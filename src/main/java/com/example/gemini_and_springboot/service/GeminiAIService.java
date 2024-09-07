package com.example.gemini_and_springboot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Candidate;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.Part;
import com.google.cloud.vertexai.api.SafetySetting;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


@Service
public class GeminiAIService {

    @Value("${gemini.api.project-id}")
    private String projectId;

    @Value("${gemini.api.location}")
    private String location;

    @Value("${gemini.api.modelName}")
    private String modelName;

    public String predictImage(MultipartFile file) {

        StringBuilder responseBuilder = new StringBuilder();

        try (VertexAI vertexAi = new VertexAI(projectId, location);) {
            GenerationConfig generationConfig = GenerationConfig.newBuilder()
                    .setMaxOutputTokens(8192)
                    .setTemperature(1F)
                    .setTopP(0.95F)
                    .build();

            List<SafetySetting> safetySettings = Arrays.asList(
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT)
                            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                            .build(),
                    SafetySetting.newBuilder()
                            .setCategory(HarmCategory.HARM_CATEGORY_HARASSMENT)
                            .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE)
                            .build());
            
            GenerativeModel model = new GenerativeModel.Builder()
                    .setModelName(modelName)
                    .setVertexAi(vertexAi)
                    .setGenerationConfig(generationConfig)
                    .setSafetySettings(safetySettings)
                    .build();

            // Convert the MultipartFile to byte[]
            byte[] imageBytes = file.getBytes();

            // Create PartMaker from image bytes
            var image = PartMaker.fromMimeTypeAndData(file.getContentType(), imageBytes);

            // Generate content using VertexAI
            var content = ContentMaker.fromMultiModalData(image, "what is this image");
            ResponseStream<GenerateContentResponse> responseStream = model.generateContentStream(content);

            // Do something with the response
            responseStream.stream().forEach(System.out::println);

            // Using stream
            // responseStream.stream().forEach(response -> {
            // response.getCandidatesList().forEach(candidate -> {
            // candidate.getContent().getPartsList().forEach(part -> {
            // String text = part.getText();
            // responseBuilder.append(text).append("\n");
            // });
            // });
            // });

            for (GenerateContentResponse response : responseStream) {
                for (Candidate candidate : response.getCandidatesList()) {
                    for (Part part : candidate.getContent().getPartsList()) {
                        String text = part.getText();
                        responseBuilder.append(text).append("\n");
                    }
                }
            }

        } catch (IOException e) {
            return "Error processing the request: " + e.getMessage();
        }

        return responseBuilder.toString();

    }

}
