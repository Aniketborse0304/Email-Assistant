package com.email.writer;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


@Service
public class EmailGeneratorService {

    private final WebClient webClient;
    private final String apikey;

    public EmailGeneratorService(WebClient.Builder WebClientBuilder ,
                                 @Value("${gemini.api.url}") String baseUrl,
                                 @Value("${gemini.api.key}") String geminiApikey
                                                        ) {
        this.apikey = geminiApikey;
        this.webClient = WebClientBuilder.baseUrl(baseUrl)
                .build();
    }

    public String generateEmailReply(EmailRequest emailRequest) {
        //build prompt

        String prompt = buildPrompt(emailRequest);
        // prepare raw JSON body
        String requestBody = String.format("""
                {
                    "contents": [
                      {
                        "parts": [
                          {
                            "text": "%s"
                          }
                        ]
                      }
                    ]
                  }
                """ , prompt);
        //send request

        String response= webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1beta/models/gemini-2.5-flash:generateContent")
                        .build())
                .header("x-goog-api-key" , apikey)
                .header("Content-Type" , "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        //Extract Response
        return extractResponseContent(response);
    }

    private String extractResponseContent(String response) {  // ✅ only ONE definition, inside class
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            // ✅ Navigate the full Gemini response path and return as String
            return root.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

        } catch (Exception e) {  // ✅ catches JsonProcessingException + NullPointerException
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }

//    private String buildPrompt(EmailRequest emailRequest) {
//        StringBuilder prompt = new StringBuilder();
//        prompt.append("Generate a professional reply for the following email:\n");
//
//        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
//            prompt.append("Use a ").append(emailRequest.getTone()).append(" tone.\n");
//            // use a professional tone
//        }
//
//        // ✅ Always appended, regardless of tone
//        prompt.append("Original Email: \n").append(emailRequest.getEmailcontent());
//
//        return prompt.toString();  // ✅ Always returns
//    }

    private String buildPrompt(EmailRequest emailRequest) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are an AI-powered Smart Email Assistant. Your job is to write highly professional, context-aware, and polished email replies.\n\n");
        prompt.append("Follow these rules strictly:\n");
        prompt.append("- Write a complete, ready-to-send email reply\n");
        prompt.append("- Use proper email structure: greeting, body, closing\n");
        prompt.append("- Be concise yet thorough\n");
        prompt.append("- Never include placeholders like [Your Name] or include if needed\n");
        prompt.append("- Match the context and intent of the original email\n\n");

        if (emailRequest.getTone() != null && !emailRequest.getTone().isEmpty()) {
            prompt.append("Tone: Write the reply in a ")
                    .append(emailRequest.getTone())
                    .append(" tone.\n\n");
        }

        prompt.append("Original Email to Reply To:\n");
        prompt.append("---\n");
        prompt.append(emailRequest.getEmailContent());
        prompt.append("\n---\n\n");
        prompt.append("Now write a smart, professional reply to the above email:");

        return prompt.toString();
    }
}
