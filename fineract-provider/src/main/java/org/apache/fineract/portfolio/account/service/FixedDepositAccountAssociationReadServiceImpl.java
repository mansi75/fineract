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
package org.apache.fineract.portfolio.account.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.fixeddeposit.contract.FixedDepositAccountAssociationReadService;

/**
 * Account transfers module implementation of the contract declared by fineract-core for the fixed deposit feature.
 * Keeping the implementation here means fixed deposit depends only on the core interface, and the account transfers
 * module supplies the details at runtime.
 */
@RequiredArgsConstructor
public class FixedDepositAccountAssociationReadServiceImpl implements FixedDepositAccountAssociationReadService {

    private final AccountAssociationsReadPlatformService accountAssociationsReadPlatformService;

    @Override
    public PortfolioAccountData retrieveLinkedSavingsAccount(final Long savingsId) {
        return this.accountAssociationsReadPlatformService.retriveSavingsLinkedAssociation(savingsId);
    }

    @Override
    public PortfolioAccountData retrieveSavingsAccount(final Long savingsId) {
        return this.accountAssociationsReadPlatformService.retriveSavingsAccount(savingsId);
    }
}
