package com.upwork.gifted.service.impl;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.upwork.gifted.dto.ItemDto;
import com.upwork.gifted.entity.ItemEntity;
import com.upwork.gifted.repository.ItemRepository;
import com.upwork.gifted.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Daniyar Artykov
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    @Value("${rss-feed.url:http://rss.cnn.com/rss/edition_world.rss}")
    private String rssFeedUrl;
    private final ItemRepository itemRepository;
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Override
    public Page<ItemDto> getItems(int page, int size, String direction, String sortingProperty) {
        PageRequest pageRequest = buildPageRequest(page, size, direction, sortingProperty);

        Page<ItemEntity> itemEntities = itemRepository.findAllByDeleted(false, pageRequest);

        return itemEntities.map(this::toDto);
    }

    private PageRequest buildPageRequest(int page, int size, String direction, String sortingProperty) {
        // START of fixing underscore issue in sort param
        // The issue is described there https://github.com/spring-projects/spring-data-rest/issues/1276
        // here we are replacing underscore ( _ ) with empty string and replacing next letter to its upper case
        // because Spring Data / JPA doesn't support underscore in sort field,
        // and underscore is a reserved field
        while (sortingProperty.contains("_")) {
            int idx = sortingProperty.indexOf('_');
            String nextLetter = idx + 2 <= sortingProperty.length() - 1
                    ? sortingProperty.substring(idx + 1, idx + 2) : "";
            sortingProperty = sortingProperty.replace("_" + nextLetter, nextLetter.toUpperCase());
        }
        // END of fixing underscore issue in sort param

        Sort sort = Sort.by(sortingProperty);
        if ("desc".equals(direction.toLowerCase())) {
            sort = sort.descending();
        }

        return PageRequest.of(page, size, sort);
    }

    private ItemDto toDto(ItemEntity entity) {
        return ItemDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .publicationDate(entity.getPublicationDate())
                .author(entity.getAuthor())
                .createdDate(entity.getCreatedDate())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }

    @Transactional
    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void syncRssFeed() {
        SyndFeedInput input = new SyndFeedInput();
        try {
            URL feedSource = new URL(rssFeedUrl);
            SyndFeed feed = input.build(new XmlReader(feedSource));
            itemRepository.updateAllDeleted(true);
            if (feed != null && feed.getEntries() != null) {
                saveSyndEntries(feed);
            }
        } catch (Exception ex) {
            // rollback changes in db
            itemRepository.updateAllDeleted(false);
            log.error("unable get feed. Error: ", ex);
        }
    }

    private void saveSyndEntries(SyndFeed feed) {
        List<ItemEntity> entities = new ArrayList<>();
        for (SyndEntry entry : feed.getEntries()) {
            if (entry == null) {
                continue;
            }
            ItemEntity entity = ItemEntity.builder()
                    .author(entry.getAuthor())
                    .publicationDate(entry.getPublishedDate() != null
                            ? entry.getPublishedDate().toInstant().atZone(ZONE_ID).toLocalDateTime()
                            : null)
                    .title(entry.getTitle())
                    .description(entry.getDescription() != null ? entry.getDescription().getValue() : null)
                    .updatedDate(entry.getUpdatedDate() != null
                            ? entry.getUpdatedDate().toInstant().atZone(ZONE_ID).toLocalDateTime()
                            : null)
                    .deleted(false)
                    .build();
            entities.add(entity);
        }
        itemRepository.hardDelete();
        itemRepository.saveAll(entities);
    }

}
