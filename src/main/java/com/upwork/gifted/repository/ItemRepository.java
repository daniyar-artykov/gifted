package com.upwork.gifted.repository;

import com.upwork.gifted.entity.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @author Daniyar Artykov
 */
public interface ItemRepository extends JpaRepository<ItemEntity, Long> {

    Page<ItemEntity> findAllByDeleted(boolean deleted, Pageable pageable);

    @Modifying
    @Query("update ItemEntity i set i.deleted=:deleted")
    void updateAllDeleted(@Param("deleted") boolean deleted);

    @Modifying
    @Query("delete from ItemEntity i where i.deleted=true")
    void hardDelete();

}
