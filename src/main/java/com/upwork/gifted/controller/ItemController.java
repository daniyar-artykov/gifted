package com.upwork.gifted.controller;

import com.upwork.gifted.dto.ItemDto;
import com.upwork.gifted.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Daniyar Artykov
 */
@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items")
    public Page<ItemDto> getItems(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "desc") String direction,
                                  @RequestParam(defaultValue = "publicationDate") String sort) {
        return itemService.getItems(page, size, direction, sort);
    }

}
