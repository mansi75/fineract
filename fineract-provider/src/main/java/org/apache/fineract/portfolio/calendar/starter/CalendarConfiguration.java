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
package org.apache.fineract.portfolio.calendar.starter;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.portfolio.calendar.contract.CalendarClientReadService;
import org.apache.fineract.portfolio.calendar.contract.CalendarGroupReadService;
import org.apache.fineract.portfolio.calendar.contract.CalendarLoanReadService;
import org.apache.fineract.portfolio.calendar.contract.CalendarLoanWriteService;
import org.apache.fineract.portfolio.calendar.domain.CalendarHistoryRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarInstanceRepository;
import org.apache.fineract.portfolio.calendar.domain.CalendarRepository;
import org.apache.fineract.portfolio.calendar.serialization.CalendarCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.calendar.service.CalendarDropdownReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarDropdownReadPlatformServiceImpl;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformServiceImpl;
import org.apache.fineract.portfolio.calendar.service.CalendarWritePlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarWritePlatformServiceJpaRepositoryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class CalendarConfiguration {

    @Bean
    @ConditionalOnMissingBean(CalendarDropdownReadPlatformService.class)
    public CalendarDropdownReadPlatformService calendarDropdownReadPlatformService() {
        return new CalendarDropdownReadPlatformServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(CalendarReadPlatformService.class)
    public CalendarReadPlatformService calendarReadPlatformService(JdbcTemplate jdbcTemplate,
            ConfigurationDomainService configurationDomainService) {
        return new CalendarReadPlatformServiceImpl(jdbcTemplate, configurationDomainService);
    }

    @Bean
    @ConditionalOnMissingBean(CalendarWritePlatformService.class)
    public CalendarWritePlatformService calendarWritePlatformService(CalendarRepository calendarRepository,
            CalendarHistoryRepository calendarHistoryRepository, CalendarCommandFromApiJsonDeserializer fromApiJsonDeserializer,
            CalendarInstanceRepository calendarInstanceRepository, CalendarLoanWriteService calendarLoanWriteService,
            ConfigurationDomainService configurationDomainService, CalendarGroupReadService calendarGroupReadService,
            CalendarLoanReadService calendarLoanReadService, CalendarClientReadService calendarClientReadService) {
        return new CalendarWritePlatformServiceJpaRepositoryImpl(calendarRepository, calendarHistoryRepository, fromApiJsonDeserializer,
                calendarInstanceRepository, calendarLoanWriteService, configurationDomainService, calendarGroupReadService,
                calendarLoanReadService, calendarClientReadService);
    }
}
