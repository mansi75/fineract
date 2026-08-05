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
package org.apache.fineract.portfolio.shareaccounts.service;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.shareaccounts.contract.ShareAccountSavingsService;
import org.apache.fineract.portfolio.shareaccounts.contract.ShareDividendDetailsService;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class ShareAccountSchedularServiceImpl implements ShareAccountSchedularService {

    private final ShareDividendDetailsService shareDividendDetailsService;
    private final ShareAccountSavingsService shareAccountSavingsService;

    @Override
    @Transactional
    public void postDividend(final Long dividendDetailId, final Long savingsId) {
        final BigDecimal dividendAmount = this.shareDividendDetailsService.retrieveDividendAmount(dividendDetailId);
        final Long savingsTransactionId = this.shareAccountSavingsService.payDividend(savingsId, DateUtils.getBusinessLocalDate(),
                dividendAmount);
        this.shareDividendDetailsService.markDividendPosted(dividendDetailId, savingsTransactionId);
    }
}
