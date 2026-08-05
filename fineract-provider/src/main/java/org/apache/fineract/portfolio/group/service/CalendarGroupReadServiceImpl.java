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
package org.apache.fineract.portfolio.group.service;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.calendar.contract.CalendarGroupData;
import org.apache.fineract.portfolio.calendar.contract.CalendarGroupReadService;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.group.domain.GroupRepositoryWrapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalendarGroupReadServiceImpl implements CalendarGroupReadService {

    private final GroupRepositoryWrapper groupRepositoryWrapper;

    @Override
    public CalendarGroupData retrieveGroupWithNotFoundDetection(final Long groupId) {
        final Group group = this.groupRepositoryWrapper.findOneWithNotFoundDetection(groupId);
        final Group parent = group.getParent();
        return new CalendarGroupData(group.getId(), group.getActivationDate(), group.isCenter(), group.isGroup(),
                parent == null ? null : parent.getId());
    }
}
