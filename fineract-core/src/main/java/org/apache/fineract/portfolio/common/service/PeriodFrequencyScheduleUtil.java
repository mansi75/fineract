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
package org.apache.fineract.portfolio.common.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

public final class PeriodFrequencyScheduleUtil {

    private PeriodFrequencyScheduleUtil() {}

    /**
     * Tells whether {@code date} is one of the recurrences of a schedule that starts on {@code startDate} and repeats
     * every {@code repaidEvery} periods of the given frequency.
     */
    public static boolean isDateFallsInSchedule(final PeriodFrequencyType frequency, final int repaidEvery, final LocalDate startDate,
            final LocalDate date) {
        return switch (frequency) {
            case DAYS -> DateUtils.getExactDifferenceInDays(startDate, date) % repaidEvery == 0;
            case WEEKS -> matchesRecurrence(ChronoUnit.WEEKS, repaidEvery, startDate, date);
            case MONTHS -> matchesRecurrence(ChronoUnit.MONTHS, repaidEvery, startDate, date);
            case YEARS -> matchesRecurrence(ChronoUnit.YEARS, repaidEvery, startDate, date);
            case INVALID, WHOLE_TERM -> false;
        };
    }

    private static boolean matchesRecurrence(final ChronoUnit unit, final int repaidEvery, final LocalDate startDate,
            final LocalDate date) {
        final int difference = DateUtils.getExactDifference(startDate, date, unit);
        return difference % repaidEvery == 0 && DateUtils.isEqual(startDate.plus(difference, unit), date);
    }
}
