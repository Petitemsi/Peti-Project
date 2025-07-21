package com.peti.service;

import com.peti.model.ClothingItem;
import com.peti.model.OutfitSuggestionRequestDto;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OutfitSuggestionService {

    private final ChatClient chatClient;

    public OutfitSuggestionService(@Qualifier("openAiChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String suggestOutfits(OutfitSuggestionRequestDto request, List<ClothingItem> clothingItems) {

        // Build list of Media objects with metadata containing item IDs
        List<Media> mediaList = clothingItems.stream()
                .filter(item -> item.getImageUrl() != null && !item.getImageUrl().isEmpty())
                .map(item -> Media.builder()
                        .mimeType(MimeTypeUtils.IMAGE_PNG)
                        .id(item.getId().toString())
                        .data(URI.create(item.getImageUrl()))
                        .build()
                )
                .collect(Collectors.toList());
        // Construct clothing items description with ID and category
        String clothingItemsDescription = clothingItems.stream()
                .map(item -> String.format("ID: %s, Category: %s", item.getId(),
                        item.getCategory() != null ? item.getCategory().getName() : "Unknown"))
                .collect(Collectors.joining("\n"));
        // Construct user prompt
        String userText = String.format("""
                                            You are a fashion expert assisting users of the PETI Smart Virtual Wardrobe system.
                        
                                            Each provided image represents a clothing item and has a unique "id" in its media.
                                            Below is the list of clothing items with their IDs and categories:
                                            %s
                        
                                            Your task:
                        
                                            I provide you my all wardrobe clothes images, Today Temperature is %s°C and the occasion is %s.
                                             I want you analyze my all wardrobe clothes images and suggest me a suitable outfit for the occasion. that which 
                                             clothes should I wear today. and please return the the complete outfit I dont want a incomplete outfit please give me best suggestion 
                                             I want to look smart for this occasion. and Always gives me unique outfit suggestions.
                        
                                             Return only the clothing item IDs (from the media id), in a comma-separated list. 
                                               For example: "12,27,45"
                        
                                            If you cannot form a suitable outfit with the given images, respond with: none
                                            
                                            and also return the reason with ids why you choose this outfit
                                            
                                            Response always should be in this format:
                                            "2, 3, 6, 8, 10"
                        
                                            **Reason:**
                        
                                            - **Shoes (ID 2):** Formal black leather shoes are appropriate for a formal occasion.
                                            - **Blazer (ID 3):** A classic black blazer adds a formal touch.
                                            - **Shirt (ID 6):** A white shirt is a staple for formal events.
                                            - **Pants (ID 8):** These formal brown pants complement the blazer and shirt.
                                            - **Tie (ID 10):** A blue tie adds a subtle pop of color while maintaining formality.
                        """,
                clothingItemsDescription,
                (request.getWeatherDetails() != null && request.getWeatherDetails().getTemperature() != null) ?
                        request.getWeatherDetails().getTemperature() : "Unknown",
                request.getOccasion() != null ? request.getOccasion() : "Unknown"
        );


        // Build and send prompt to GPT-4o
        Prompt prompt = new Prompt(
                UserMessage.builder()
                        .text(userText)
                        .media(mediaList)
                        .build(),
                OpenAiChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_O.getValue())
                        .build()
        );

        // Get and return AI response
        String response = chatClient.prompt(prompt).call().content().trim();
        return response;
    }
}
