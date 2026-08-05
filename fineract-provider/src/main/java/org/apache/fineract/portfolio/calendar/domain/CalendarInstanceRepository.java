/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.calendar.domain;

import java.util.List;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@CacheConfig(cacheNames = "calendarInstances")
public interface CalendarInstanceRepository extends JpaRepository<CalendarInstance, Long>, JpaSpecificationExecutor<CalendarInstance> {

    @Cacheable(key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('calId_' + #calendarId + '_entityId_' + #entityId + '_entityTypeId_' + #entityTypeId)")
    CalendarInstance findByCalendarIdAndEntityIdAndEntityTypeId(Long calendarId, Long entityId, Integer entityTypeId);

    @Cacheable(key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('entityId_' + #entityId + '_entityTypeId_' + #entityTypeId)")
    List<CalendarInstance> findByEntityIdAndEntityTypeId(Long entityId, Integer entityTypeId);

    /**
     * @param entityId
     *            : Id of the client, group, loan or savings account the calendar is attached to.
     * @param entityTypeId:
     *            {@link CalendarEntityType}
     * @param calendarTypeId:
     *            {@link CalendarType}
     * @return
     */
    @Cacheable(key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('entityId_' + #entityId + '_entityTypeId_' + #entityTypeId + '_calendarTypeId_' + #calendarTypeId)")
    CalendarInstance findByEntityIdAndEntityTypeIdAndCalendarTypeId(Long entityId, Integer entityTypeId, Integer calendarTypeId);

    @Cacheable(key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('findCalendarInstanceByEntityId_entityId_' + #entityId + '_entityTypeId_' + #entityTypeId)")
    @Query("select ci from CalendarInstance ci where ci.entityId = :entityId and ci.entityTypeId = :entityTypeId")
    CalendarInstance findCalendarInstanceByEntityId(@Param("entityId") Long entityId, @Param("entityTypeId") Integer entityTypeId);

    @Cacheable(key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('calendarId_' + #calendarId + '_entityTypeId_' + #entityTypeId)")
    List<CalendarInstance> findByCalendarIdAndEntityTypeId(Long calendarId, Integer entityTypeId);

    // Override JpaRepository methods to add cache eviction
    @Override
    @CacheEvict(allEntries = true)
    <S extends CalendarInstance> S save(S entity);

    @Override
    @CacheEvict(allEntries = true)
    <S extends CalendarInstance> List<S> saveAll(Iterable<S> entities);

    @Override
    @CacheEvict(allEntries = true)
    void delete(CalendarInstance entity);

    @Override
    @CacheEvict(allEntries = true)
    void deleteById(Long id);

    @Override
    @CacheEvict(allEntries = true)
    void deleteAll();

    @Override
    @CacheEvict(allEntries = true)
    void deleteAll(Iterable<? extends CalendarInstance> entities);

    @Override
    @CacheEvict(allEntries = true)
    void deleteAllById(Iterable<? extends Long> ids);

    @Override
    @CacheEvict(allEntries = true)
    <S extends CalendarInstance> S saveAndFlush(S entity);
}
