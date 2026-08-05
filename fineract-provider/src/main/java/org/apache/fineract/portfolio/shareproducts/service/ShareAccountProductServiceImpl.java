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
package org.apache.fineract.portfolio.shareproducts.service;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.products.data.ProductData;
import org.apache.fineract.portfolio.products.service.ShareProductReadPlatformService;
import org.apache.fineract.portfolio.shareaccounts.contract.ShareAccountProductService;
import org.apache.fineract.portfolio.shareaccounts.contract.ShareMarketPriceData;
import org.apache.fineract.portfolio.shareaccounts.contract.ShareProductDetailsData;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProduct;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductMarketPrice;
import org.apache.fineract.portfolio.shareproducts.domain.ShareProductRepositoryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShareAccountProductServiceImpl implements ShareAccountProductService {

    private final ShareProductRepositoryWrapper shareProductRepository;
    private final ShareProductReadPlatformService shareProductReadPlatformService;
    private final ShareProductDropdownReadPlatformService shareProductDropdownReadPlatformService;
    private final ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;

    @Override
    public ShareProductDetailsData findProduct(final Long shareProductId) {
        final ShareProduct product = this.shareProductRepository.findOneWithNotFoundDetection(shareProductId);
        return new ShareProductDetailsData(product.getId(), product.getShortName(), product.getAccountingType(), toCurrencyData(product),
                product.getDefaultClientShares(), product.getMinimumClientShares(), product.getMaximumClientShares(),
                product.getSubscribedShares(), product.getSharesIssued(), product.getTotalShares(), product.getUnitPrice(),
                toMarketPrices(product));
    }

    @Override
    @Transactional
    public void addSubscribedShares(final Long shareProductId, final Long shares) {
        final ShareProduct product = this.shareProductRepository.findOneWithNotFoundDetection(shareProductId);
        product.addSubscribedShares(shares);
        this.shareProductRepository.save(product);
    }

    @Override
    @Transactional
    public void removeSubscribedShares(final Long shareProductId, final Long shares) {
        final ShareProduct product = this.shareProductRepository.findOneWithNotFoundDetection(shareProductId);
        product.removeSubscribedShares(shares);
        this.shareProductRepository.saveAndFlush(product);
    }

    @Override
    public List<ProductData> retrieveAllProductsForLookup() {
        return List.copyOf(this.shareProductReadPlatformService.retrieveAllForLookup());
    }

    @Override
    public List<EnumOptionData> retrieveLockinPeriodFrequencyTypeOptions() {
        return List.copyOf(this.shareProductDropdownReadPlatformService.retrieveLockinPeriodFrequencyTypeOptions());
    }

    @Override
    public List<EnumOptionData> retrieveMinimumActivePeriodFrequencyTypeOptions() {
        return List.copyOf(this.shareProductDropdownReadPlatformService.retrieveMinimumActivePeriodFrequencyTypeOptions());
    }

    private CurrencyData toCurrencyData(final ShareProduct product) {
        final MonetaryCurrency currency = product.getCurrency();
        final ApplicationCurrency applicationCurrency = this.applicationCurrencyRepository.findOneWithNotFoundDetection(currency);
        return new CurrencyData(currency.getCode(), applicationCurrency.getName(), currency.getDigitsAfterDecimal(),
                currency.getInMultiplesOf(), applicationCurrency.getDisplaySymbol(), applicationCurrency.getNameCode());
    }

    private List<ShareMarketPriceData> toMarketPrices(final ShareProduct product) {
        if (product.getMarketPrice() == null) {
            return List.of();
        }
        return product.getMarketPrice().stream() //
                .sorted(Comparator.comparing(ShareProductMarketPrice::getStartDate)) //
                .map(price -> new ShareMarketPriceData(price.getStartDate(), price.getPrice())) //
                .toList();
    }
}
