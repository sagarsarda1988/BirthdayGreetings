/*
 * Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license.
 * See LICENSE in the project root for license information.
 */
package com.microsoft.office365.msgraphsnippetapp.snippet;

import com.microsoft.office365.msgraphapiservices.MSGraphMeService;
import com.microsoft.office365.msgraphsnippetapp.R;
import com.microsoft.office365.msgraphsnippetapp.application.SnippetApp;

import retrofit.Callback;
import retrofit.client.Response;

import static com.microsoft.office365.msgraphsnippetapp.R.array.get_me_contacts;

public abstract class MeSnippets<Result> extends AbstractSnippet<MSGraphMeService, Result> {
    /**
     * Snippet constructor
     *
     * @param descriptionArray The String array for the specified snippet
     */
    public MeSnippets(Integer descriptionArray) {
        super(SnippetCategory.meSnippetCategory, descriptionArray);
    }

    static MeSnippets[] getMeSnippets() {
        return new MeSnippets[]{
                // Marker element
                new MeSnippets(null) {
                    @Override
                    public void request(MSGraphMeService service, Callback callback) {
                        // Not implemented
                    }
                },
                // Snippets


                /* Get information about signed in user
                 * HTTP GET https://graph.microsoft.com/{version}/me
                 * @see https://graph.microsoft.io/docs/api-reference/v1.0/api/user_get
                 */
                new MeSnippets<Response>(get_me_contacts) {
                    @Override
                    public void request(MSGraphMeService service, Callback<Response> callback) {
                        service.getMeContacts(
                                getVersion(),
                                callback);
                    }
                }
        };
    }

    @Override
    public abstract void request(MSGraphMeService service, Callback<Result> callback);

}
