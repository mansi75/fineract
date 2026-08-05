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
package org.apache.fineract.portfolio.fixeddeposit.contract;

import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;

/**
 * Accounting reads the fixed deposit feature needs. Declared in fineract-core and implemented by the accounting module,
 * so fixed deposit never imports an accounting service and accounting never imports a fixed deposit type.
 */
public interface FixedDepositAccountingReadService {

    List<EnumOptionData> retrieveAccountingRuleTypeOptions();

    Map<String, List<GLAccountData>> retrieveAccountMappingOptions();

    Map<String, Object> fetchAccountMappingDetails(Long productId, Integer accountingType);

    List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappings(Long productId);

    List<ChargeToGLAccountMapper> fetchFeeToIncomeAccountMappings(Long productId);

    List<ChargeToGLAccountMapper> fetchPenaltyToIncomeAccountMappings(Long productId);
}
