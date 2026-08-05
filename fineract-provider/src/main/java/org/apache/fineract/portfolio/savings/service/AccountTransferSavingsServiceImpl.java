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
package org.apache.fineract.portfolio.savings.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.account.contract.AccountTransferAccountDetail;
import org.apache.fineract.portfolio.account.contract.AccountTransferSavingsService;
import org.apache.fineract.portfolio.account.contract.SavingsTransferDeposit;
import org.apache.fineract.portfolio.account.contract.SavingsTransferWithdrawal;
import org.apache.fineract.portfolio.savings.SavingsTransactionBooleanValues;
import org.apache.fineract.portfolio.savings.domain.GSIMRepositoy;
import org.apache.fineract.portfolio.savings.domain.GroupSavingsIndividualMonitoring;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.springframework.stereotype.Service;

/**
 * Savings side of the account transfer boundary: turns the account-transfer requests into savings domain operations.
 */
@Service
@RequiredArgsConstructor
public class AccountTransferSavingsServiceImpl implements AccountTransferSavingsService {

    private static final boolean IS_ACCOUNT_TRANSFER = true;
    private static final boolean BACKDATED_TXNS_ALLOWED_TILL = false;

    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final SavingsAccountDomainService savingsAccountDomainService;
    private final SavingsAccountWritePlatformService savingsAccountWritePlatformService;
    private final GSIMRepositoy gsimRepository;

    @Override
    public AccountTransferAccountDetail retrieveAccountDetail(final Long savingsAccountId) {
        final SavingsAccount account = this.savingsAccountRepository.findSavingsWithNotFoundDetection(savingsAccountId,
                BACKDATED_TXNS_ALLOWED_TILL);
        return new AccountTransferAccountDetail(account.getId(), account.getClient() == null ? null : account.getClient().getId(),
                account.office() == null ? null : account.office().getId(), account.getCurrency());
    }

    @Override
    public Long withdraw(final SavingsTransferWithdrawal request) {
        final SavingsAccount account = this.savingsAccountAssembler.assembleFrom(request.savingsAccountId(), BACKDATED_TXNS_ALLOWED_TILL);

        final SavingsTransactionBooleanValues transactionBooleanValues = new SavingsTransactionBooleanValues(IS_ACCOUNT_TRANSFER,
                request.regularTransaction(), account.isWithdrawalFeeApplicableForTransfer(), request.interestTransfer(),
                request.exceptionForBalanceCheck());

        return this.savingsAccountDomainService.handleWithdrawal(account, request.formatter(), request.transactionDate(),
                request.transactionAmount(), request.paymentDetail(), transactionBooleanValues, BACKDATED_TXNS_ALLOWED_TILL).getId();
    }

    @Override
    public Long deposit(final SavingsTransferDeposit request) {
        final SavingsAccount account = this.savingsAccountAssembler.assembleFrom(request.savingsAccountId(), BACKDATED_TXNS_ALLOWED_TILL);

        return this.savingsAccountDomainService
                .handleDeposit(account, request.formatter(), request.transactionDate(), request.transactionAmount(),
                        request.paymentDetail(), IS_ACCOUNT_TRANSFER, request.regularTransaction(), BACKDATED_TXNS_ALLOWED_TILL)
                .getId();
    }

    @Override
    public void undoTransferTransaction(final Long savingsAccountId, final Long savingsTransactionId) {
        this.savingsAccountWritePlatformService.undoTransaction(savingsAccountId, savingsTransactionId, true);
    }

    @Override
    public void addToGsimParentDeposit(final Long savingsAccountId, final BigDecimal amount) {
        final SavingsAccount account = this.savingsAccountRepository.findSavingsWithNotFoundDetection(savingsAccountId,
                BACKDATED_TXNS_ALLOWED_TILL);
        if (account.getGsim() == null) {
            return;
        }
        final GroupSavingsIndividualMonitoring gsim = this.gsimRepository.findById(account.getGsim().getId()).orElseThrow();
        gsim.setParentDeposit(gsim.getParentDeposit().add(amount));
        this.gsimRepository.save(gsim);
    }
}
