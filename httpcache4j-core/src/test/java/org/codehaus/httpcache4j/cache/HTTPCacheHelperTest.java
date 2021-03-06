/*
 * Copyright (c) 2009. The Codehaus. All Rights Reserved.
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
 */

package org.codehaus.httpcache4j.cache;

import org.junit.Test;
import org.codehaus.httpcache4j.*;

import java.time.LocalDateTime;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:hamnis@codehaus.org">Erlend Hamnaberg</a>
 * @version $Revision: #5 $ $Date: 2008/09/15 $
 */
public class HTTPCacheHelperTest {
    private final HTTPCacheHelper helper = new HTTPCacheHelper(CacheHeaderBuilder.getBuilder());


    @Test
    public void cacheableRequest() {
        assertTrue(helper.isCacheableRequest(new HTTPRequest("fooo")));
        assertTrue(helper.isCacheableRequest(new HTTPRequest("fooo").withCacheControl(CacheControl.empty())));
        assertFalse(helper.isCacheableRequest(new HTTPRequest("fooo", HTTPMethod.POST)));
        assertFalse(helper.isCacheableRequest(new HTTPRequest("fooo").withCacheControl(new CacheControl.Builder().noStore().noCache().build())));
        assertFalse(helper.isCacheableRequest(new HTTPRequest("fooo").withCacheControl(new CacheControl.Builder().noCache().build())));
        assertFalse(helper.isCacheableRequest(new HTTPRequest("fooo").withCacheControl(new CacheControl.Builder().noStore().build())));
    }

    @Test
    public void testCacheableResponses() {
        Headers headers = new Headers();
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "private, max-age=39");
        headers = headers.add(HeaderConstants.ETAG, "\"123\"");
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, createDateTime(10)));
        assertCacheableHeaders(headers);

        headers = new Headers();
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "private, max-age=39");
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, createDateTime(40)));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, createDateTime(0)));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, createDateTime(0)));
        assertCacheableHeaders(headers);

        headers = new Headers();
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "private, max-age=39");
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, createDateTime(0)));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.DATE, createDateTime(0)));
        assertCacheableHeaders(headers);
    }

    private void assertCacheableHeaders(Headers headers) {
        assertTrue("Response was not cacheable", helper.isCacheableResponse(new HTTPResponse(Status.OK, headers)));
        assertTrue("Response was not cacheable", helper.isCacheableResponse(new HTTPResponse(Status.NON_AUTHORITATIVE_INFORMATION, headers)));
        assertTrue("Response was not cacheable", helper.isCacheableResponse(new HTTPResponse(Status.MULTIPLE_CHOICES, headers)));
        assertTrue("Response was not cacheable", helper.isCacheableResponse(new HTTPResponse(Status.MOVED_PERMANENTLY, headers)));
        assertTrue("Response was not cacheable", helper.isCacheableResponse(new HTTPResponse(Status.GONE, headers)));
    }

    @Test
    public void testNotCacheableResponses() {
        Headers headers = new Headers();
        headers = headers.add(HeaderUtils.toHttpDate("date", createDateTime(10)));
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "private, max-age=39");
        assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(Status.NOT_MODIFIED, headers)));
        assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(Status.ACCEPTED, headers)));
        assertFalse("Response was cacheable", helper.isCacheableResponse(new HTTPResponse(Status.CREATED, headers)));
    }

    @Test
    public void responseWithExpiryShouldBeStored() {
        Headers headers = new Headers();
        headers = headers.add(HeaderUtils.toHttpDate("date", createDateTime(10)));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, createDateTime(11)));
        assertTrue(helper.shouldBeStored(new HTTPResponse(Status.OK, headers)));
    }

    @Test
    public void responseWithETagShouldBeStored() {
        Headers headers = new Headers().add("etag", new Tag("foo").format());
        assertTrue(helper.shouldBeStored(new HTTPResponse(Status.OK, headers)));
    }

    @Test
    public void responseWithLastModifiedShouldBeStored() {
        Headers headers = new Headers().add(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, createDateTime(11)));
        assertTrue(helper.shouldBeStored(new HTTPResponse(Status.OK, headers)));
    }

    @Test
    public void responseWithMaxAgeShouldBeStored() {
        Headers headers = new Headers().add(HeaderConstants.CACHE_CONTROL, "max-age=10");
        assertTrue(helper.shouldBeStored(new HTTPResponse(Status.OK, headers)));
    }

    @Test
    public void responseWithEverythingShouldBeStored() {
        Headers headers = new Headers();
        headers = headers.add(HeaderUtils.toHttpDate("date", createDateTime(10)));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.EXPIRES, createDateTime(20)));
        headers = headers.add(HeaderUtils.toHttpDate(HeaderConstants.LAST_MODIFIED, createDateTime(9)));
        headers = headers.add(HeaderConstants.CACHE_CONTROL, "max-age=10");
        headers = headers.add("etag", new Tag("foo").format());
        assertTrue(helper.shouldBeStored(new HTTPResponse(Status.OK, headers)));
    }

    @Test
    public void responseWithNotHeadersShouldNotBeStored() {
        Headers headers = new Headers();
        assertFalse(helper.shouldBeStored(new HTTPResponse(Status.OK, headers)));
    }

    @Test
    public void responseWithOnlyDateShouldNotBeStored() {
        Headers headers = new Headers();
        headers = headers.add(HeaderUtils.toHttpDate("date", createDateTime(10)));
        assertFalse(helper.shouldBeStored(new HTTPResponse(Status.OK, headers)));
    }

    @Test
    public void makesureWeAreRemovingUncachableHeaders() {
        Headers headers = new Headers().add("Connection", "close").add("Transfer-Encoding", "compress");

        Headers modified = helper.removeUnmodifiableHeaders(headers);

        assertEquals(new Headers(), modified);
    }

    private LocalDateTime createDateTime(int seconds) {
        return LocalDateTime.of(2009, 4, 22, 10, 10, seconds, 0);
    }
}
