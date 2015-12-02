/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office365.msgraphsnippetapp.snippet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.microsoft.office365.msgraphsnippetapp.snippet.MeSnippets.getMeSnippets;
import static com.microsoft.office365.msgraphsnippetapp.snippet.MessageSnippets.getMessageSnippets;

public class SnippetContent {


    public static final List<AbstractSnippet<?, ?>> ITEMS = new ArrayList<>();

    static {
        AbstractSnippet<?, ?>[][] baseSnippets = new AbstractSnippet<?, ?>[][]{

                getMeSnippets(),
                getMessageSnippets(),
        };

        for (AbstractSnippet<?, ?>[] snippetArray : baseSnippets) {
            Collections.addAll(ITEMS, snippetArray);
        }
    }

}
