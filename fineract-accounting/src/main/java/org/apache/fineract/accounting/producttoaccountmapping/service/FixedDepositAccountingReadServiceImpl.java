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
package org.apache.fineract.accounting.producttoaccountmapping.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.fixeddeposit.contract.FixedDepositAccountingReadService;
import org.springframework.stereotype.Service;

/**
 * Accounting module implementation of the contract declared by fineract-core for the fixed deposit feature. Keeping the
 * implementation here means fixed deposit depends only on the core interface, and accounting supplies the details at
 * runtime. Fixed deposit products are mapped exactly like savings products.
 */
@Service
@RequiredArgsConstructor
public class FixedDepositAccountingReadServiceImpl implements FixedDepositAccountingReadService {

    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService;

    @Override
    public List<EnumOptionData> retrieveAccountingRuleTypeOptions() {
        return this.accountingDropdownReadPlatformService.retrieveAccountingRuleTypeOptions();
    }

    @Override
    public Map<String, List<GLAccountData>> retrieveAccountMappingOptions() {
        return this.accountingDropdownReadPlatformService.retrieveAccountMappingOptionsForSavingsProducts();
    }

    @Override
    public Map<String, Object> fetchAccountMappingDetails(final Long productId, final Integer accountingType) {
        return this.accountMappingReadPlatformService.fetchAccountMappingDetailsForSavingsProduct(productId, accountingType);
    }

    @Override
    public List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappings(final Long productId) {
        return this.accountMappingReadPlatformService.fetchPaymentTypeToFundSourceMappingsForSavingsProduct(productId);
    }

    @Override
    public List<ChargeToGLAccountMapper> fetchFeeToIncomeAccountMappings(final Long productId) {
        return this.accountMappingReadPlatformService.fetchFeeToIncomeAccountMappingsForSavingsProduct(productId);
    }

    @Override
    public List<ChargeToGLAccountMapper> fetchPenaltyToIncomeAccountMappings(final Long productId) {
        return this.accountMappingReadPlatformService.fetchPenaltyToIncomeAccountMappingsForSavingsProduct(productId);
    }
}
