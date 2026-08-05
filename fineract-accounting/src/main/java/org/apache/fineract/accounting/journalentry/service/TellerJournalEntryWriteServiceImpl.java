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
package org.apache.fineract.accounting.journalentry.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingConstants.FinancialActivity;
import org.apache.fineract.accounting.financialactivityaccount.domain.FinancialActivityAccountRepositoryWrapper;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.journalentry.domain.JournalEntry;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryRepository;
import org.apache.fineract.accounting.journalentry.domain.JournalEntryType;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.organisation.office.domain.OfficeRepositoryWrapper;
import org.apache.fineract.organisation.teller.contract.CashierTransactionJournalEntryData;
import org.apache.fineract.organisation.teller.contract.TellerJournalEntryWriteService;
import org.springframework.stereotype.Service;

/**
 * Accounting implementation of the contract declared by the organisation module. Keeping the implementation here means
 * the organisation module depends only on the interface it owns, and the accounting module keeps ownership of which GL
 * accounts a cash movement between the main vault and a cashier hits.
 */
@Service
@RequiredArgsConstructor
public class TellerJournalEntryWriteServiceImpl implements TellerJournalEntryWriteService {

    private final JournalEntryRepository journalEntryRepository;
    private final FinancialActivityAccountRepositoryWrapper financialActivityAccountRepositoryWrapper;
    private final OfficeRepositoryWrapper officeRepositoryWrapper;

    @Override
    public void createCashierAllocationJournalEntries(final CashierTransactionJournalEntryData transaction) {
        createJournalEntries(transaction, FinancialActivity.CASH_AT_TELLER, FinancialActivity.CASH_AT_MAINVAULT);
    }

    @Override
    public void createCashierSettlementJournalEntries(final CashierTransactionJournalEntryData transaction) {
        createJournalEntries(transaction, FinancialActivity.CASH_AT_MAINVAULT, FinancialActivity.CASH_AT_TELLER);
    }

    private void createJournalEntries(final CashierTransactionJournalEntryData transaction, final FinancialActivity debitActivity,
            final FinancialActivity creditActivity) {
        final GLAccount debitAccount = glAccountOf(debitActivity);
        final GLAccount creditAccount = glAccountOf(creditActivity);
        final Office office = this.officeRepositoryWrapper.findOneWithNotFoundDetection(transaction.officeId());

        this.journalEntryRepository.saveAndFlush(journalEntry(office, debitAccount, transaction, JournalEntryType.DEBIT));
        this.journalEntryRepository.saveAndFlush(journalEntry(office, creditAccount, transaction, JournalEntryType.CREDIT));
    }

    private GLAccount glAccountOf(final FinancialActivity financialActivity) {
        return this.financialActivityAccountRepositoryWrapper.findByFinancialActivityTypeWithNotFoundDetection(financialActivity.getValue())
                .getGlAccount();
    }

    private static JournalEntry journalEntry(final Office office, final GLAccount glAccount,
            final CashierTransactionJournalEntryData transaction, final JournalEntryType journalEntryType) {
        return JournalEntry.createNew(office, null, glAccount, transaction.currencyCode(), transaction.transactionId(), false,
                transaction.transactionDate(), journalEntryType, transaction.amount(), transaction.note(), null, null, null, null, null,
                null, null);
    }
}
