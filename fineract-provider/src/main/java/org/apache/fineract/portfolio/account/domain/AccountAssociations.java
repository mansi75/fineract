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
package org.apache.fineract.portfolio.account.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_portfolio_account_associations")
@Getter
public class AccountAssociations extends AbstractPersistableCustom<Long> {

    @Column(name = "loan_account_id")
    private Long loanAccountId;

    @Column(name = "savings_account_id")
    private Long savingsAccountId;

    @Column(name = "linked_loan_account_id")
    private Long linkedLoanAccountId;

    @Column(name = "linked_savings_account_id")
    private Long linkedSavingsAccountId;

    @Column(name = "association_type_enum", nullable = false)
    private Integer associationType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected AccountAssociations() {}

    private AccountAssociations(final Long loanAccountId, final Long savingsAccountId, final Long linkedLoanAccountId,
            final Long linkedSavingsAccountId, final Integer associationType, boolean active) {
        this.loanAccountId = loanAccountId;
        this.savingsAccountId = savingsAccountId;
        this.linkedLoanAccountId = linkedLoanAccountId;
        this.linkedSavingsAccountId = linkedSavingsAccountId;
        this.associationType = associationType;
        this.active = active;
    }

    public static AccountAssociations associateSavingsAccountToLoan(final Long loanId, final Long linkedSavingsAccountId,
            final Integer associationType, boolean isActive) {
        return new AccountAssociations(loanId, null, null, linkedSavingsAccountId, associationType, isActive);
    }

    public static AccountAssociations associateSavingsAccountToSavings(final Long savingsAccountId, final Long linkedSavingsAccountId,
            final Integer associationType, boolean isActive) {
        return new AccountAssociations(null, savingsAccountId, null, linkedSavingsAccountId, associationType, isActive);
    }

    public void updateLinkedSavingsAccount(final Long linkedSavingsAccountId) {
        this.linkedSavingsAccountId = linkedSavingsAccountId;
    }
}
