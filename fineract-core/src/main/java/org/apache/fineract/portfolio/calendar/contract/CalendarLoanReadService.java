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
package org.apache.fineract.portfolio.calendar.contract;

import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;

/**
 * Core read-contract owned by the calendar feature and implemented by the loan module. The loan module owns which
 * statuses count as active, so callers never need the loan status types.
 */
public interface CalendarLoanReadService {

    /**
     * Approved-on date when the loan has one, otherwise the submitted-on date.
     */
    LocalDate retrieveLoanActivationDate(Long loanId);

    boolean hasActiveLoansSyncedWithCalendar(Long calendarId);

    List<CalendarInstance> retrieveCalendarInstancesForActiveGroupLoans(Long groupId, Long clientId);
}
