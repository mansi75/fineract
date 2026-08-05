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
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;

/**
 * The share product state a share account needs to validate, price and book a share purchase. Lets share accounts keep
 * a by-id reference to the product instead of holding the share product aggregate itself.
 */
public record ShareProductDetailsData(Long id, String shortName, Integer accountingType, CurrencyData currency, Long nominalShares,
        Long minimumClientShares, Long maximumClientShares, Long subscribedShares, Long sharesIssued, Long totalShares,
        BigDecimal unitPrice, List<ShareMarketPriceData> marketPrices) {

    public MonetaryCurrency toMonetaryCurrency() {
        return new MonetaryCurrency(currency);
    }

    /**
     * @return the latest price that had already started on the given date, falling back to the product unit price
     */
    public BigDecimal deriveMarketPrice(final LocalDate onDate) {
        BigDecimal marketValue = unitPrice;
        for (ShareMarketPriceData marketPrice : marketPrices) {
            if (!DateUtils.isAfter(marketPrice.fromDate(), onDate)) {
                marketValue = marketPrice.shareValue();
            }
        }
        return marketValue;
    }
}
