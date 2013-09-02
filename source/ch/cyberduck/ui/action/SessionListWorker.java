package ch.cyberduck.ui.action;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.apache.log4j.Logger;

import java.text.MessageFormat;

/**
 * @version $Id$
 */
public class SessionListWorker extends Worker<AttributedList<Path>> {
    private static final Logger log = Logger.getLogger(SessionListWorker.class);

    private Session<?> session;

    private Cache cache;

    private Path directory;

    private ListProgressListener listener;

    protected SessionListWorker(final Session<?> session, final Cache cache, final Path directory,
                                final ListProgressListener listener) {
        this.session = session;
        this.cache = cache;
        this.directory = directory;
        this.listener = listener;
    }

    @Override
    public AttributedList<Path> run() throws BackgroundException {
        if(cache.isCached(directory.getReference())) {
            return cache.get(directory.getReference());
        }
        final AttributedList<Path> children = session.list(directory, new ListProgressListener() {
            @Override
            public void chunk(final AttributedList<Path> list) throws ConnectionCanceledException {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Retrieved chunk of %d items in %s", list.size(), directory));
                }
                if(isCanceled()) {
                    throw new ConnectionCanceledException();
                }
                listener.chunk(list);
            }
        });
        cache.put(directory.getReference(), children);
        return children;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Listing directory {0}", "Status"),
                directory.getName());
    }
}