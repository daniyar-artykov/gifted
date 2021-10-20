package com.upwork.gifted.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Daniyar Artykov
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {

    private Long id;
    private String title;
    private String description;
    private LocalDateTime publicationDate;
    private String author;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;

}
