package com.upwork.gifted.service;

import com.upwork.gifted.dto.ItemDto;
import org.springframework.data.domain.Page;

/**
 * @author Daniyar Artykov
 */
public interface ItemService {

    Page<ItemDto> getItems(int page, int size, String direction, String sort);

}
