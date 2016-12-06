/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.restcomm.cache.util;

import java.util.*;

/**
 * General utility methods used throughout the Infinispan code base.
 *
 * @author <a href="brian.stansberry@jboss.com">Brian Stansberry</a>
 * @author Galder Zamarreño
 * @since 4.0
 */
public final class Util {

    /**
     * Prevent instantiation
     */
    private Util() {
    }

    /**
     * Null-safe equality test.
     *
     * @param a first object to compare
     * @param b second object to compare
     * @return true if the objects are equals or both null, false otherwise.
     */
    public static boolean safeEquals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static <T> Set<T> asSet(T... a) {
        if (a.length > 1)
            return new HashSet<T>(Arrays.<T>asList(a));
        else
            return Collections.singleton(a[0]);
    }

}
