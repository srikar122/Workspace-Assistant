package com.org.workspace.repository;

import com.org.workspace.domain.entity.CalendarEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, String> {

    List<CalendarEventEntity> findAllByOrderByStartTimeAsc();
}