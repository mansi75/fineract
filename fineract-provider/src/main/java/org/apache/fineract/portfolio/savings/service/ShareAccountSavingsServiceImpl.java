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
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.apache.fineract.portfolio.shareaccounts.contract.ShareAccountSavingsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShareAccountSavingsServiceImpl implements ShareAccountSavingsService {

    private static final DepositAccountType SHARE_SETTLEMENT_ACCOUNT_TYPE = DepositAccountType.SAVINGS_DEPOSIT;

    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final SavingsAccountRepositoryWrapper savingsAccountRepository;
    private final SavingsAccountAssembler savingsAccountAssembler;
    private final SavingsAccountDomainService savingsAccountDomainService;

    @Override
    public void validateSavingsDepositOfClient(final Long clientId, final Long savingsAccountId, final String currencyCode) {
        if (!this.savingsAccountReadPlatformService.isAccountBelongsToClient(clientId, savingsAccountId, SHARE_SETTLEMENT_ACCOUNT_TYPE,
                currencyCode)) {
            throw new SavingsAccountNotFoundException(savingsAccountId);
        }
        this.savingsAccountRepository.findOneWithNotFoundDetection(savingsAccountId, SHARE_SETTLEMENT_ACCOUNT_TYPE);
    }

    @Override
    public List<SavingsAccountData> retrieveActiveSavingsDepositsForLookup(final Long clientId, final String currencyCode) {
        return List.copyOf(
                this.savingsAccountReadPlatformService.retrieveActiveForLookup(clientId, SHARE_SETTLEMENT_ACCOUNT_TYPE, currencyCode));
    }

    @Override
    @Transactional
    public Long payDividend(final Long savingsAccountId, final LocalDate paymentDate, final BigDecimal amount) {
        final SavingsAccount savingsAccount = this.savingsAccountAssembler.assembleFrom(savingsAccountId, false);
        final SavingsAccountTransaction transaction = this.savingsAccountDomainService.handleDividendPayout(savingsAccount, paymentDate,
                amount, false);
        return transaction.getId();
    }
}
