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
package org.apache.fineract.portfolio.shareaccounts.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;

/**
 * The savings operations the share account feature depends on: a share account settles purchases and dividends against
 * the client's savings account. Declared here so share accounts never compile against the savings aggregate; the
 * savings feature owns the implementation.
 */
public interface ShareAccountSavingsService {

    /**
     * Checks that the savings account exists and is a savings deposit of the given client held in the given currency.
     *
     * @throws org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException
     *             when it is not
     */
    void validateSavingsDepositOfClient(Long clientId, Long savingsAccountId, String currencyCode);

    /**
     * @return the active savings deposits of the client in the given currency, empty when there are none
     */
    List<SavingsAccountData> retrieveActiveSavingsDepositsForLookup(Long clientId, String currencyCode);

    /**
     * Pays a share dividend into the savings account.
     *
     * @return the id of the resulting savings transaction
     */
    Long payDividend(Long savingsAccountId, LocalDate paymentDate, BigDecimal amount);
}
