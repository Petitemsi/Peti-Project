package com.peti.service;

import com.peti.model.ClothingItem;
import com.peti.model.OutfitSuggestionRequestDto;
import com.peti.model.Category;
import com.peti.constants.Occasion;
import com.peti.constants.Season;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutfitSuggestionServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec responseSpec;

    @InjectMocks
    private OutfitSuggestionService outfitSuggestionService;

    private OutfitSuggestionRequestDto request;
    private List<ClothingItem> clothingItems;
    private ClothingItem clothingItem;

    @BeforeEach
    void setUp() {
        // Initialize test data
        request = new OutfitSuggestionRequestDto();
        OutfitSuggestionRequestDto.WeatherDetails weatherDetails = new OutfitSuggestionRequestDto.WeatherDetails();
        weatherDetails.setTemperature(20.0);
        weatherDetails.setWeatherCondition("Sunny");
        request.setWeatherDetails(weatherDetails);
        request.setOccasion(Occasion.CASUAL);
        request.setSeason(Season.SUMMER);

        clothingItem = new ClothingItem();
        clothingItem.setId(1L);
        clothingItem.setCategory(new Category(1L, "Shirts", "Casual shirts", true));
        clothingItem.setImageUrl("http://example.com/shirt.png");
        clothingItems = Arrays.asList(clothingItem);
    }

    @Test
    void suggestOutfits_WithValidItems_ShouldReturnOutfitIds() {
        // Arrange
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("\"1\"\n\n**Reason:**\n- **Shirt (ID 1):** Suitable for casual occasion and warm weather.");

        // Act
        String result = outfitSuggestionService.suggestOutfits(request, clothingItems);

        // Assert
        assertNotNull(result);
        assertEquals("\"1\"\n\n**Reason:**\n- **Shirt (ID 1):** Suitable for casual occasion and warm weather.", result);
        verify(chatClient, times(1)).prompt(any(Prompt.class));
        verify(requestSpec, times(1)).call();
        verify(responseSpec, times(1)).content();
    }

    @Test
    void suggestOutfits_WithNoValidImages_ShouldReturnNone() {
        // Arrange
        ClothingItem itemWithoutImage = new ClothingItem();
        itemWithoutImage.setId(2L);
        itemWithoutImage.setCategory(new Category(2L, "Pants", "Casual pants", true));
        itemWithoutImage.setImageUrl(""); // Empty image URL
        List<ClothingItem> itemsWithoutImages = Arrays.asList(itemWithoutImage);
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("none\n\n**Reason:**\n- No valid images provided.");

        // Act
        String result = outfitSuggestionService.suggestOutfits(request, itemsWithoutImages);

        // Assert
        assertEquals("none\n\n**Reason:**\n- No valid images provided.", result);
        verify(chatClient, times(1)).prompt(any(Prompt.class));
        verify(requestSpec, times(1)).call();
        verify(responseSpec, times(1)).content();
    }
    @Test
    void suggestOutfits_WithEmptyClothingItems_ShouldReturnNone() {
        // Arrange
        List<ClothingItem> emptyItems = Arrays.asList();
        when(chatClient.prompt(any(Prompt.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("none\n\n**Reason:**\n- No clothing items provided.");

        // Act
        String result = outfitSuggestionService.suggestOutfits(request, emptyItems);

        // Assert
        assertEquals("none\n\n**Reason:**\n- No clothing items provided.", result);
        verify(chatClient, times(1)).prompt(any(Prompt.class));
        verify(requestSpec, times(1)).call();
        verify(responseSpec, times(1)).content();
    }
}