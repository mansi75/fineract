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
package org.apache.fineract.portfolio.loanaccount.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.calendar.contract.CalendarLoanReadService;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstance;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCalendarInstanceRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalendarLoanReadServiceImpl implements CalendarLoanReadService {

    private static final List<LoanStatus> ACTIVE_LOAN_STATUSES = List.of(LoanStatus.SUBMITTED_AND_PENDING_APPROVAL, LoanStatus.APPROVED,
            LoanStatus.ACTIVE);

    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanCalendarInstanceRepository loanCalendarInstanceRepository;

    @Override
    public LocalDate retrieveLoanActivationDate(final Long loanId) {
        final Loan loan = this.loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        return loan.getApprovedOnDate() == null ? loan.getSubmittedOnDate() : loan.getApprovedOnDate();
    }

    @Override
    public boolean hasActiveLoansSyncedWithCalendar(final Long calendarId) {
        final Integer count = this.loanCalendarInstanceRepository.countOfLoansSyncedWithCalendar(calendarId, ACTIVE_LOAN_STATUSES);
        return count != null && count > 0;
    }

    @Override
    public List<CalendarInstance> retrieveCalendarInstancesForActiveGroupLoans(final Long groupId, final Long clientId) {
        return this.loanCalendarInstanceRepository.findCalendarInstancesForLoansByGroupIdAndClientIdAndStatuses(groupId, clientId,
                ACTIVE_LOAN_STATUSES);
    }
}
