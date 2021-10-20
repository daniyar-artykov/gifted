package com.upwork.gifted.repository;

import com.upwork.gifted.GiftedApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Daniyar Artykov
 */
@SpringBootTest(classes = GiftedApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    public void testFindAllByDeleted() {
        itemRepository.findAllByDeleted(false, Pageable.ofSize(1));
    }

    @Test
    @Transactional
    public void testUpdateAllDeleted() {
        itemRepository.updateAllDeleted(true);
    }

    @Test
    @Transactional
    public void testHardDelete() {
        itemRepository.hardDelete();
    }

}
