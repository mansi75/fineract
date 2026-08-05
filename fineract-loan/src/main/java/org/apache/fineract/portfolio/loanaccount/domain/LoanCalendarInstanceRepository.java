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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.util.List;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Calendar instance queries that join the loan entity. They live in the loan module so that the calendar feature does
 * not query loan tables. Shares the {@code calendarInstances} cache with
 * {@code org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository}, which evicts it on write.
 */
@Repository
@CacheConfig(cacheNames = "calendarInstances")
public interface LoanCalendarInstanceRepository extends JpaRepository<CalendarInstance, Long> {

    @Cacheable(key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('groupId_' + #groupId + '_clientId_' + #clientId + '_statuses_' + T(org.springframework.util.StringUtils).collectionToCommaDelimitedString(#loanStatuses))")
    @Query("select ci from CalendarInstance ci where ci.entityId in (select loan.id from Loan loan where loan.client.id = :clientId and loan.group.id = :groupId and loan.loanStatus in :loanStatuses) and ci.entityTypeId = 3")
    List<CalendarInstance> findCalendarInstancesForLoansByGroupIdAndClientIdAndStatuses(@Param("groupId") Long groupId,
            @Param("clientId") Long clientId, @Param("loanStatuses") List<LoanStatus> loanStatuses);

    /**
     * EntityType = 3 is for loan
     */
    @Cacheable(key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('countLoans_calendarId_' + #calendarId + '_statuses_' + T(org.springframework.util.StringUtils).collectionToCommaDelimitedString(#loanStatuses))")
    @Query("SELECT COUNT(ci.id) FROM CalendarInstance ci, Loan loan WHERE loan.id = ci.entityId AND ci.entityTypeId = 3 AND ci.calendar.id = :calendarId AND loan.loanStatus IN :loanStatuses ")
    Integer countOfLoansSyncedWithCalendar(@Param("calendarId") Long calendarId, @Param("loanStatuses") List<LoanStatus> loanStatuses);
}
