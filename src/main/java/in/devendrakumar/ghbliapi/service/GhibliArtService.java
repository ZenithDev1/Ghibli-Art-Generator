//package in.devendrakumar.ghbliapi.service;
//
//import in.devendrakumar.ghbliapi.client.StabilityAIClient;
//import in.devendrakumar.ghbliapi.dto.TextToImageRequest;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//@Service
//public class GhibliArtService {
//
//    private final StabilityAIClient stabilityAIClient;
//    private final String apiKey;
//
//    public GhibliArtService(StabilityAIClient stabilityAIClient, @Value("${stability.api.key}") String apiKey) {
//        this.stabilityAIClient = stabilityAIClient;
//        this.apiKey = apiKey;
//    }
//
//    public byte[] createGhibliArt(MultipartFile image, String prompt) {
//        String finalPrompt = prompt+", in the beautiful, detailed anime style of studio ghibli.";
//        String engineId = "stable-diffusion-v1-6";
//        String stylePreset = "anime";
//
//        return stabilityAIClient.generateImageFromImage(
//                "Bearer " + apiKey,
//                engineId,
//                image,
//                finalPrompt,
//                stylePreset
//        );
//    }
//
//    public byte[] createGhibliArtFromText(String prompt, String style) {
//        String finalPrompt = prompt+", in the beautiful, detailed anime style of studio ghibli.";
//        String engineId = "stable-diffusion-v1-6";
//        String stylePreset = style.equals("general") ? "anime" : style.replace("_", "-");
//
//        TextToImageRequest requestPayload = new TextToImageRequest(finalPrompt, stylePreset);
//
//        return stabilityAIClient.generateImageFromText(
//                "Bearer " + apiKey,
//                engineId,
//                requestPayload
//        );
//    }
//}

package in.devendrakumar.ghbliapi.service;

import in.devendrakumar.ghbliapi.client.StabilityAIClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class GhibliArtService {

    // OLD: Stability Dependencies
    private final StabilityAIClient stabilityAIClient;
    private final String stabilityApiKey;

    // NEW: Hugging Face Dependencies
    private final String hfUrl;
    private final String hfToken;
    private final HttpClient httpClient;

    public GhibliArtService(
            StabilityAIClient stabilityAIClient,
            @Value("${stability.api.key}") String stabilityApiKey,
            @Value("${huggingface.api.url}") String hfUrl,
            @Value("${huggingface.api.token}") String hfToken) {

        // Initialize Old
        this.stabilityAIClient = stabilityAIClient;
        this.stabilityApiKey = stabilityApiKey;

        // Initialize New
        this.hfUrl = hfUrl;
        this.hfToken = hfToken;
        this.httpClient = HttpClient.newHttpClient();
    }


    // Note: API keys nahi hai kyunki paise bhi to nahi h!!!!!!!!!!, This will fail with "401 Unauthorized" or "Insufficient Credits"
    public byte[] createGhibliArt(MultipartFile image, String prompt) {
        String finalPrompt = prompt + ", in the beautiful, detailed anime style of studio ghibli.";
        String engineId = "stable-diffusion-v1-6";
        String stylePreset = "anime";

        return stabilityAIClient.generateImageFromImage(
                "Bearer " + stabilityApiKey,
                engineId,
                image,
                finalPrompt,
                stylePreset
        );
    }

    // ---------------------------------------------------------
    // (Updated to use Free Hugging Face)
    // ---------------------------------------------------------
    public byte[] createGhibliArtFromText(String prompt, String style) {
        try {
            // 1. Prepare the Prompt
            String finalPrompt = "studio ghibli style, " + style + ", " + prompt + ", highly detailed, 8k resolution, anime masterpiece";

            // 2. Create Simple JSON Payload (No DTO needed for this simple call)
            // We use String.format to safely insert the prompt
            String jsonBody = String.format("{\"inputs\": \"%s\"}", finalPrompt);

            // 3. Build the Request using standard Java HTTP Client
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(hfUrl))
                    .header("Authorization", "Bearer " + hfToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            // 4. Send Request
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            // 5. Error Handling
            if (response.statusCode() != 200) {
                String errorBody = new String(response.body(), StandardCharsets.UTF_8);
                System.err.println("Hugging Face API Error: " + response.statusCode() + " - " + errorBody);
                throw new RuntimeException("API Error " + response.statusCode());
            }

            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to generate art: " + e.getMessage());
        }
    }
}