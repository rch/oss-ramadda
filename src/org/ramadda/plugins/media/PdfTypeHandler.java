/**
Copyright (c) 2008-2023 Geode Systems LLC
SPDX-License-Identifier: Apache-2.0
*/

package org.ramadda.plugins.media;


import org.apache.tika.metadata.Office;

import org.apache.tika.metadata.TikaCoreProperties;


import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.type.*;


import org.ramadda.service.*;


import org.ramadda.util.HtmlUtils;
import org.ramadda.util.Utils;

import org.w3c.dom.*;

import ucar.unidata.util.DateUtil;

import ucar.unidata.util.IOUtil;

import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;


import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.Date;

import java.util.Date;
import java.util.List;


/**
 *
 *
 */
public class PdfTypeHandler extends GenericTypeHandler {


    /**
     * _more_
     *
     * @param repository _more_
     * @param entryNode _more_
     *
     * @throws Exception _more_
     */
    public PdfTypeHandler(Repository repository, Element entryNode)
            throws Exception {
        super(repository, entryNode);
    }

    /**
     * _more_
     *
     *
     * @param request _more_
     * @param entry _more_
     * @param service _more_
     * @param output _more_
     *
     * @throws Exception _more_
     */
    @Override
    public void handleServiceResults(Request request, Entry entry,
                                     Service service, ServiceOutput output)
            throws Exception {
        super.handleServiceResults(request, entry, service, output);
        if ((request != null) && !request.get(ARG_METADATA_ADD, false)) {
            return;
        }
        List<Entry> entries = output.getEntries();
        if (entries.size() == 0) {
            return;
        }

        String created = (String) entry.getTransientProperty(
                             Office.CREATION_DATE.getName());
        if (created == null) {
            created = (String) entry.getTransientProperty(
                TikaCoreProperties.CREATED.getName());
        }
        if (created != null) {
            Date dttm = Utils.parseDate(created);
            entry.setStartDate(dttm.getTime());
            entry.setEndDate(dttm.getTime());
        }

        String saved =
            (String) entry.getTransientProperty(Office.SAVE_DATE.getName());
        if (saved != null) {
            Date dttm = Utils.parseDate(saved);
            entry.setEndDate(dttm.getTime());
        }

        String author =
            (String) entry.getTransientProperty(Office.AUTHOR.getName());
        if (Utils.stringDefined(author)) {
            getMetadataManager().addMetadata(request,
                entry,
                new Metadata(
                    getRepository().getGUID(), entry.getId(),
                    "metadata_author", false, author, null, null, null,
                    null));
        }

        Entry serviceEntry = entries.get(0);
        if ( !serviceEntry.getResource().getPath().endsWith(".txt")) {
            return;
        }

        String results =
            IOUtil.readContents(serviceEntry.getFile().toString(),
                                getClass());
        if ( !Utils.stringDefined(results)) {
            return;
        }

        if (results.length() > Entry.MAX_DESCRIPTION_LENGTH - 100) {
            results = results.substring(0, Entry.MAX_DESCRIPTION_LENGTH
                                        - 100);
        }

        List<String> headerLines = new ArrayList<String>();
        String       firstLine   = null;
        for (String line : StringUtil.split(results, "\n", true, true)) {
            line = clean(line);
            if ((line.length() == 0) || line.startsWith("!")) {
                continue;
            }
            headerLines.add(line);
            if (headerLines.size() >= 100) {
                break;
            }
        }


        if ((headerLines.size() > 0)
                && !Utils.stringDefined(entry.getDescription())) {
            String desc = "<pre class=\"ramadda-pre\">"
                          + StringUtil.join("\n", headerLines);
            entry.setDescription(desc + "</pre>");
        }
    }


    /**
     * _more_
     *
     * @param s _more_
     *
     * @return _more_
     */
    private String clean(String s) {
        if (s == null) {
            return s;
        }
        s = Utils.removeNonAscii(s);
        s = s.trim();
        s = s.replaceAll("\n", " ");
        s = s.replaceAll("(_-)+", "");

        return s;
    }


}
