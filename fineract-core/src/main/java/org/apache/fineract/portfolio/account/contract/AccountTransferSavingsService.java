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
package org.apache.fineract.portfolio.account.contract;

import java.math.BigDecimal;

/**
 * Savings operations the account transfer feature needs. Implemented by the savings feature so that neither side
 * imports the other's domain.
 */
public interface AccountTransferSavingsService {

    AccountTransferAccountDetail retrieveAccountDetail(Long savingsAccountId);

    /**
     * @return the id of the created savings withdrawal transaction
     */
    Long withdraw(SavingsTransferWithdrawal request);

    /**
     * @return the id of the created savings deposit transaction
     */
    Long deposit(SavingsTransferDeposit request);

    void undoTransferTransaction(Long savingsAccountId, Long savingsTransactionId);

    /**
     * Adds the transferred amount to the parent GSIM deposit of the given account. Does nothing when the account is not
     * part of a GSIM.
     */
    void addToGsimParentDeposit(Long savingsAccountId, BigDecimal amount);
}
