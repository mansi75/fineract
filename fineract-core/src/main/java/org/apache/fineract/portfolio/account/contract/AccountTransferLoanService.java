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
import org.apache.fineract.infrastructure.core.domain.ExternalId;

/**
 * Loan operations the account transfer feature needs. Implemented by the loan feature so that neither side imports the
 * other's domain.
 */
public interface AccountTransferLoanService {

    AccountTransferAccountDetail retrieveAccountDetail(Long loanId);

    /**
     * @return the id of the created loan repayment transaction
     */
    Long repay(LoanTransferRepayment request);

    /**
     * @return the id of the created loan charge payment transaction
     */
    Long payCharge(LoanTransferChargePayment request);

    /**
     * @return the id of the created loan refund transaction
     */
    Long refund(LoanTransferPayout request);

    /**
     * @return the id of the created refund transaction on an active loan
     */
    Long refundForActiveLoan(LoanTransferPayout request);

    /**
     * @return the id of the created loan disbursement transaction
     */
    Long disburse(LoanTransferPayout request);

    void reverseTransfer(Long loanTransactionId);

    /**
     * Adjusts the loan transaction created by a transfer down to zero so that the transfer can be undone.
     */
    void adjustTransferTransaction(Long loanTransactionId, ExternalId reversalTxnExternalId);

    /**
     * @return the amount paid in advance on the loan, never {@code null}
     */
    BigDecimal retrievePaidInAdvance(Long loanId);
}
