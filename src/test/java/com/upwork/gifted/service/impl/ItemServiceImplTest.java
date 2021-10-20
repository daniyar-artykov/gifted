package com.upwork.gifted.service.impl;

import com.upwork.gifted.dto.ItemDto;
import com.upwork.gifted.entity.ItemEntity;
import com.upwork.gifted.repository.ItemRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author Daniyar Artykov
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    @Captor
    private ArgumentCaptor<List<ItemEntity>> itemEntitiesArgumentCaptor;

    @BeforeAll
    static void setUpBefore() {
        org.apache.catalina.webresources.TomcatURLStreamHandlerFactory.getInstance();
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(itemService, "rssFeedUrl", "classpath:input_data/rss_feed.xml");
    }

    @Test
    void testGetItems_order_with_underscore_desc() {
        // given
        int page = 0;
        int size = 10;
        String direction = "desc";
        String sort = "publication_date";

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("publicationDate").descending());
        when(itemRepository.findAllByDeleted(false, pageRequest)).thenReturn(preparePageItemEntity(pageRequest));

        // when
        Page<ItemDto> actualPage = itemService.getItems(page, size, direction, sort);

        // then
        Page<ItemDto> expectedPage = preparePageItemDto(pageRequest);

        assertEquals(expectedPage, actualPage);
    }

    @Test
    void testGetItems_order_without_underscore_asc() {
        // given
        int page = 0;
        int size = 10;
        String direction = "asc";
        String sort = "publicationDate";

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sort).ascending());
        when(itemRepository.findAllByDeleted(false, pageRequest)).thenReturn(preparePageItemEntity(pageRequest));

        // when
        Page<ItemDto> actualPage = itemService.getItems(page, size, direction, sort);

        // then
        Page<ItemDto> expectedPage = preparePageItemDto(pageRequest);

        assertEquals(expectedPage, actualPage);
    }

    @Test
    void testSyncRssFeed_success() {
        // given

        // when
        itemService.syncRssFeed();

        // then
        verify(itemRepository).updateAllDeleted(true);
        verify(itemRepository, never()).updateAllDeleted(false);
        verify(itemRepository).hardDelete();

        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;

        List<ItemEntity> expectedList = new ArrayList<>();
        ItemEntity entity = ItemEntity.builder()
                .author("")
                .publicationDate(LocalDateTime.parse("Wed, 20 Oct 2021 16:10:02 GMT", formatter))
                .title("\n                Bomb attack on Syrian military bus in Damascus kills 14 \n            ")
                .description("At least 14 people have been killed and three others injured after a Syrian military bus was targeted with two explosive devices in the capital Damascus on Wednesday, the SANA state news agency reported.<img src=\"http://feeds.feedburner.com/~r/rss/edition_world/~4/jee9xViLqnM\" height=\"1\" width=\"1\" alt=\"\"/>")
                .deleted(false)
                .build();
        expectedList.add(entity);

        ItemEntity entity1 = ItemEntity.builder()
                .author("")
                .publicationDate(LocalDateTime.parse("Wed, 20 Oct 2021 14:18:26 GMT", formatter))
                .title("\n                Brazilian senators abandon call for homicide charges against President Bolsonaro\n            ")
                .description("Brazilian senators investigating President Jair Bolsonaro's handling of the pandemic decided late on Tuesday to withdraw recommendations for charges of mass homicide and genocide against him, CNN affiliate CNN Brasil reported.<img src=\"http://feeds.feedburner.com/~r/rss/edition_world/~4/MnKUFanTKBI\" height=\"1\" width=\"1\" alt=\"\"/>")
                .deleted(false)
                .build();
        expectedList.add(entity1);

        verify(itemRepository).saveAll(itemEntitiesArgumentCaptor.capture());
        List<ItemEntity> actualList = itemEntitiesArgumentCaptor.getValue();
        assertEquals(expectedList, actualList);
    }

    @Test
    void testSyncRssFeed_error() {
        // given
        doThrow(new RuntimeException("unable to update")).when(itemRepository).updateAllDeleted(true);

        // when
        itemService.syncRssFeed();

        // then
        verify(itemRepository).updateAllDeleted(true);
        verify(itemRepository).updateAllDeleted(false);
        verify(itemRepository, never()).hardDelete();
        verify(itemRepository, never()).saveAll(anyCollection());
    }

    private Page<ItemEntity> preparePageItemEntity(PageRequest pageRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        ItemEntity entity = ItemEntity.builder()
                .author("")
                .publicationDate(LocalDateTime.parse("Wed, 20 Oct 2021 16:10:02 GMT", formatter))
                .title("\n                Bomb attack on Syrian military bus in Damascus kills 14 \n            ")
                .description("At least 14 people have been killed and three others injured after a Syrian military bus was targeted with two explosive devices in the capital Damascus on Wednesday, the SANA state news agency reported.<img src=\"http://feeds.feedburner.com/~r/rss/edition_world/~4/jee9xViLqnM\" height=\"1\" width=\"1\" alt=\"\"/>")
                .deleted(false)
                .build();

        ItemEntity entity1 = ItemEntity.builder()
                .author("")
                .publicationDate(LocalDateTime.parse("Wed, 20 Oct 2021 14:18:26 GMT", formatter))
                .title("\n                Brazilian senators abandon call for homicide charges against President Bolsonaro\n            ")
                .description("Brazilian senators investigating President Jair Bolsonaro's handling of the pandemic decided late on Tuesday to withdraw recommendations for charges of mass homicide and genocide against him, CNN affiliate CNN Brasil reported.<img src=\"http://feeds.feedburner.com/~r/rss/edition_world/~4/MnKUFanTKBI\" height=\"1\" width=\"1\" alt=\"\"/>")
                .deleted(false)
                .build();


        return new PageImpl<>(Arrays.asList(entity, entity1), pageRequest, 15);
    }

    private Page<ItemDto> preparePageItemDto(PageRequest pageRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
        ItemDto dto = ItemDto.builder()
                .author("")
                .publicationDate(LocalDateTime.parse("Wed, 20 Oct 2021 16:10:02 GMT", formatter))
                .title("\n                Bomb attack on Syrian military bus in Damascus kills 14 \n            ")
                .description("At least 14 people have been killed and three others injured after a Syrian military bus was targeted with two explosive devices in the capital Damascus on Wednesday, the SANA state news agency reported.<img src=\"http://feeds.feedburner.com/~r/rss/edition_world/~4/jee9xViLqnM\" height=\"1\" width=\"1\" alt=\"\"/>")
                .build();

        ItemDto dto1 = ItemDto.builder()
                .author("")
                .publicationDate(LocalDateTime.parse("Wed, 20 Oct 2021 14:18:26 GMT", formatter))
                .title("\n                Brazilian senators abandon call for homicide charges against President Bolsonaro\n            ")
                .description("Brazilian senators investigating President Jair Bolsonaro's handling of the pandemic decided late on Tuesday to withdraw recommendations for charges of mass homicide and genocide against him, CNN affiliate CNN Brasil reported.<img src=\"http://feeds.feedburner.com/~r/rss/edition_world/~4/MnKUFanTKBI\" height=\"1\" width=\"1\" alt=\"\"/>")
                .build();


        return new PageImpl<>(Arrays.asList(dto, dto1), pageRequest, 15);
    }

}
