/*
 * Copyright (c) 2008, The Codehaus. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.codehaus.httpcache4j.cache;

import org.codehaus.httpcache4j.*;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Assert;
import org.junit.Test;

public class CacheItemTest {
    private CacheItem item;
    private DateTime storageTime = new DateTime(2008, 10, 12, 15, 0, 0, 0);
    private DateTime now = new DateTime(2008, 10, 12, 15, 10, 0, 0);

    public void setupItem(Headers headers) {
        DateTimeUtils.setCurrentMillisFixed(storageTime.getMillis());
        item = new CacheItem(new HTTPResponse(null, Status.OK, headers));
        DateTimeUtils.setCurrentMillisFixed(now.getMillis());
    }

    @Test
    public void testIsNotStale() {
        Headers headers = new Headers();
        headers.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=3600"));
        setupItem(headers);
        Assert.assertTrue("Item was stale", !item.isStale());
    }

    @Test
    public void testIsStale() {
        Headers headers = new Headers();
        headers.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale());
    }

    @Test
    public void testIsStaleExpiresHeader() {
        Headers headers = new Headers();
        headers.add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, now));
        setupItem(headers);
        Assert.assertTrue("Item was not stale", item.isStale());
    }

    @Test
    public void testIsNotStaleExpiresHeader() {
        Headers headers = new Headers();
        DateTime future = new DateTime(now);
        future = future.plusHours(1);
        headers.add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, future));
        setupItem(headers);
        Assert.assertTrue("Item was stale", !item.isStale());
    }

    @Test
    public void testIsStaleEqualToDateHeader() {
        Headers headers = new Headers();
        DateTime future = new DateTime(now);
        future = future.plusHours(1);
        headers.add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, future));
        headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, future));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale());
    }

    @Test
    public void testIsStaleExpiresHeaderWithInvalidDate() {
        Headers headers = new Headers();
        headers.add(new Header(HeaderConstants.EXPIRES, "foo"));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale());
    }

    @Test
    public void testIsStaleExpiresHeaderWithCacheControl() {
        Headers headers = new Headers();
        //This should be preferred by the isStale method.
        headers.add(new Header(HeaderConstants.CACHE_CONTROL, "private, max-age=60"));
        DateTime future = new DateTime(now);
        future = future.plusHours(1); //We now say that the expires is not stale.
        headers.add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, future));
        setupItem(headers);
        Assert.assertTrue("Item was stale", item.isStale());
    }
}