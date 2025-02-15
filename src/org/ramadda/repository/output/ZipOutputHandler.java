/**
Copyright (c) 2008-2023 Geode Systems LLC
SPDX-License-Identifier: Apache-2.0
*/

package org.ramadda.repository.output;



import org.ramadda.repository.*;
import org.ramadda.repository.metadata.*;
import org.ramadda.repository.util.SelectInfo;
import org.ramadda.repository.auth.*;


import org.ramadda.repository.util.FileWriter;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.sql.SqlUtil;

import org.w3c.dom.*;

import ucar.unidata.util.IOUtil;
import ucar.unidata.xml.XmlUtil;

import java.io.*;

import java.net.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;



import java.util.regex.*;

import java.util.zip.*;


/**
 *
 *
 *
 * @author RAMADDA Development Team
 * @version $Revision: 1.3 $
 */
@SuppressWarnings("unchecked")
public class ZipOutputHandler extends OutputHandler {

    private boolean debug = false;

    /** _more_ */
    private static final String ARG_WRITETODISK = "writetodisk";

    /** _more_ */
    private final LogManager.LogId LOGID =
        new LogManager.LogId(
            "org.ramadda.repository.output.ZipOutputHandler");



    /** _more_ */
    public static final OutputType OUTPUT_ZIP =
        new OutputType("Zip and Download File", "zip.zip",
                       OutputType.TYPE_OTHER, "", ICON_ZIP);


    /** _more_ */
    public static final OutputType OUTPUT_THUMBNAILS =
        new OutputType("Zip thumbnails", "zip.thumbnails",
                       OutputType.TYPE_OTHER, "", ICON_ZIP);


    /** _more_ */
    public static final OutputType OUTPUT_ZIPTREE =
        new OutputType("Zip and Download Tree", "zip.tree",
                       OutputType.TYPE_ACTION | OutputType.TYPE_OTHER, "",
                       ICON_ZIP);


    /** _more_ */
    public static final OutputType OUTPUT_ZIPGROUP =
        new OutputType("Zip and Download Files", "zip.zipgroup",
                       OutputType.TYPE_OTHER, "", ICON_ZIP);

    /** _more_ */
    public static final OutputType OUTPUT_EXPORT =
        new OutputType("Export Entries", "zip.export",
                       OutputType.TYPE_FILE | OutputType.TYPE_ACTION, "",
                       "fa-file-export");


    /**
     * _more_
     *
     * @param repository _more_
     * @param element _more_
     * @throws Exception _more_
     */
    public ZipOutputHandler(Repository repository, Element element)
            throws Exception {
        super(repository, element);
        addType(OUTPUT_ZIP);
        addType(OUTPUT_ZIPGROUP);
        addType(OUTPUT_ZIPTREE);
        addType(OUTPUT_THUMBNAILS);
        addType(OUTPUT_EXPORT);
    }


    /**
     * _more_
     *
     * @param request _more_
     *
     * @return _more_
     */
    public AuthorizationMethod getAuthorizationMethod(Request request) {
        return AuthorizationMethod.AUTH_HTTP;
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param state _more_
     * @param links _more_
     *
     * @throws Exception _more_
     */
    public void getEntryLinks(Request request, State state, List<Link> links)
            throws Exception {



        if (state.entry != null) {
            if (getAccessManager().canDownload(request, state.entry)
                    && getAccessManager().canDoExport(request, state.entry)) {
                links.add(makeLink(request, state.entry, OUTPUT_ZIP));
            }

            //      if (getAccessManager().canDoExport(request, state.entry)) {
            //          links.add(makeLink(request, state.entry, OUTPUT_EXPORT));
            //      }
            return;
        }

        if ((state.group != null) && state.group.isDummy()) {
            if ( !request.isAnonymous()) {
                links.add(makeLink(request, state.entry, OUTPUT_EXPORT));
            }
        }

        boolean hasFile  = false;
        boolean hasGroup = false;
        for (Entry child : state.getAllEntries()) {
            if (getAccessManager().canDownload(request, child)) {
                hasFile = true;

                break;
            }
            if (child.isGroup()) {
                hasGroup = true;
            }
        }


        if (hasFile) {
            if (state.group != null) {
                links.add(makeLink(request, state.group, OUTPUT_ZIPGROUP));
                links.add(makeLink(request, state.group, OUTPUT_THUMBNAILS));
            } else {
                links.add(makeLink(request, state.group, OUTPUT_ZIP));
            }
        }

        if ((state.group != null) && (hasGroup || hasFile)
                && ( !state.group.isTopEntry() || state.group.isDummy())) {
            links.add(makeLink(request, state.group, OUTPUT_ZIPTREE));
        }

    }




    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param entry _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result outputEntry(Request request, OutputType outputType,
                              Entry entry)
            throws Exception {
        List<Entry> entries = new ArrayList<Entry>();
        entries.add(entry);

        return toZip(request, entry.getName(), entries, false, false,false);
    }


    /**
     * _more_
     *
     * @param request _more_
     * @param outputType _more_
     * @param group _more_
     * @param children _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public Result outputGroup(Request request, OutputType outputType,
                              Entry group, List<Entry> children)
            throws Exception {

        OutputType output = request.getOutput();
        if (output.equals(OUTPUT_ZIPTREE)) {
            getLogManager().logInfo("Doing zip tree");
            return toZip(request, group.getName(), children, true, false,false);
        }
        if (output.equals(OUTPUT_THUMBNAILS)) {
            getLogManager().logInfo("Doing zip thumbnails");
            return toZip(request, group.getName(), children, false, false,true);
        }	
        if (output.equals(OUTPUT_EXPORT)) {
            return toZip(request, group.getName(), children, true, true,false);
        } else {
            return toZip(request, group.getName(), children, false, false,false);
        }
    }



    /**
     * _more_
     *
     * @param output _more_
     *
     * @return _more_
     */
    public String getMimeType(OutputType output) {
        if (output.equals(OUTPUT_ZIP) || output.equals(OUTPUT_ZIPGROUP) || output.equals(OUTPUT_THUMBNAILS)) {
            return repository.getMimeTypeFromSuffix(".zip");
        } else {
            return super.getMimeType(output);
        }
    }





    /**
     * _more_
     *
     * @param request _more_
     * @param prefix _more_
     * @param entries _more_
     * @param recurse _more_
     * @param forExport _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public Result toZip(Request request, String prefix, List<Entry> entries,
                        boolean recurse, boolean forExport,boolean thumbnails)
            throws Exception {


        OutputStream os         = null;
        boolean      doingFile  = false;
        File         tmpFile    = null;
        boolean      isInternal = false;
        Element      root       = null;
        boolean      ok         = true;
        //First recurse down without a zos to check the size
        try {
            processZip(request, entries, recurse, 0, null, prefix, 0,
                       new int[] { 0 }, forExport, thumbnails,null);
        } catch (IllegalArgumentException iae) {
            ok = false;
        }
        if ( !ok) {
            return new Result(
                "Error",
                new StringBuffer(
                    getPageHandler().showDialogError(
                        "Size of request has exceeded maximum size")));
        }

        //Now set the return file name
        if (prefix.length() == 0) {
            request.setReturnFilename("entry.zip");
        } else {
            request.setReturnFilename(prefix + ".zip");
        }

        Result     result         = new Result();
        FileWriter fileWriter     = null;


        boolean    writeToDisk    = request.get(ARG_WRITETODISK, false);
        File       writeToDiskDir = null;
        if (writeToDisk) {
            //IMPORTANT: Make sure that the user is an admin when handling the write to disk 
            request.ensureAdmin();
            forExport = true;
            writeToDiskDir =
                getStorageManager().makeTempDir(getRepository().getGUID(),
                    false).getDir();
            fileWriter = new FileWriter(writeToDiskDir);
        } else {
            tmpFile = (File) request.getExtraProperty("zipfile");
            if ((tmpFile == null)
                    && (request.getHttpServletResponse() != null)) {
                os = request.getHttpServletResponse().getOutputStream();
                request.getHttpServletResponse().setContentType(
                    getMimeType(OUTPUT_ZIP));
            } else {
                if (tmpFile == null) {
                    tmpFile = getRepository().getStorageManager().getTmpFile(
                        request, ".zip");
                } else {
                    isInternal = true;
                }
                os = getStorageManager().getUncheckedFileOutputStream(
                    tmpFile);
                doingFile = true;
            }
            fileWriter = new FileWriter(new ZipOutputStream(os));
            result.setNeedToWrite(false);
            if (request.get(ARG_COMPRESS, true) == false) {
                //You would think that setting the method to stored would work
                //but it throws an error wanting the crc to be set on the ZipEntry
                //            zos.setMethod(ZipOutputStream.STORED);
                fileWriter.setCompressionOn();
            }
        }

        Hashtable seen = new Hashtable();
        try {
            if (forExport) {
                Document doc = XmlUtil.makeDocument();
                root = XmlUtil.create(doc, TAG_ENTRIES, null,
                                      new String[] {});

            }
            processZip(request, entries, recurse, 0, fileWriter, prefix, 0,
                       new int[] { 0 }, forExport, thumbnails,root);

            if (root != null) {
                String xml = XmlUtil.toString(root);
                fileWriter.writeFile("entries.xml", xml.getBytes());
            }
        } finally {
            fileWriter.close();
        }
        if (doingFile) {
            IOUtil.close(os);

            return new Result(
                "", getStorageManager().getFileInputStream(tmpFile),
                getMimeType(OUTPUT_ZIP));

        }
        getLogManager().logInfo("Zip File ended");

        if (writeToDisk) {
            return new Result("Export",
                              new StringBuffer("<p>Exported to:<br>"
                                  + writeToDiskDir));
        }

        return result;


    }


    /**
     * _more_
     *
     * @param request _more_
     * @param entries _more_
     * @param recurse _more_
     * @param level _more_
     * @param fileWriter _more_
     * @param prefix _more_
     * @param sizeSoFar _more_
     * @param counter _more_
     * @param forExport _more_
     * @param entriesRoot _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    protected long processZip(Request request, List<Entry> entries,
                              boolean recurse, int level,
                              FileWriter fileWriter, String prefix,
                              long sizeSoFar, int[] counter,
                              boolean forExport, boolean thumbnails,Element entriesRoot)
            throws Exception {

        long      sizeProcessed = 0;
        Hashtable seen          = new Hashtable();
        long      sizeLimit;
        if (request.isAnonymous()) {
            sizeLimit = MEGA
                        * getRepository().getProperty(
                            request.PROP_ZIPOUTPUT_ANONYMOUS_MAXSIZEMB, 4000);
        } else {
            sizeLimit = MEGA
                        * getRepository().getProperty(
                            request.PROP_ZIPOUTPUT_REGISTERED_MAXSIZEMB,
                            8000);
        }
	if(debug)
	    System.err.println("toZip:");
        for (Entry entry : entries) {
	    if(debug)
		System.err.println("entry:" + entry);
            //Check for access
            if (forExport) {
                if ( !getAccessManager().canDoExport(request, entry)) {
                    continue;
                }
            }
	    //Don't export synthetic entries beyond the top most level
	    if(getEntryManager().isSynthEntry(entry.getId())) {
		if(level>0) continue;
	    }
            counter[0]++;
            //Don't get big files
	    
            if (!thumbnails && request.defined(ARG_MAXFILESIZE) && entry.isFile()) {
		long length = getStorageManager().getEntryFileLength(entry);
                if (length   >= request.get(ARG_MAXFILESIZE, 0)) {
                    continue;
                }
            }

            Request tmpRequest = getRepository().getTmpRequest();
            Element entryNode  = null;
            if (forExport && (entriesRoot != null)) {
                entryNode = getRepository().getXmlOutputHandler().getEntryTag(
                    tmpRequest, entry, fileWriter,
                    entriesRoot.getOwnerDocument(), entriesRoot, true,
                    level != 0);
                //                System.err.println ("exporting:" + XmlUtil.toString(entryNode));
            }

            if (entry.isGroup() && recurse) {
                Entry group = (Entry) entry;
		SelectInfo info = new SelectInfo(request, entry,false);
                List<Entry> children = getEntryManager().getChildren(request,
								     group,info);
                String path = group.getName();
                if (prefix.length() > 0) {
                    path = prefix + "/" + path;
                }
                sizeProcessed += processZip(request, children, recurse,
                                            level + 1, fileWriter, path,
                                            sizeProcessed + sizeSoFar,
                                            counter, forExport, thumbnails,entriesRoot);
            }


            if (!thumbnails &&  !getAccessManager().canDownload(request, entry)) {
		if(debug)
		    System.err.println("No download:" + entry);
                continue;
            }

	    String path=null;
            String name=null;

	    if(thumbnails) {
		List<Metadata> metadataList =
		    getMetadataManager().findMetadata(request, entry,
                         new String[] { ContentMetadataHandler.TYPE_THUMBNAIL},
						      false);

		if(metadataList!=null && metadataList.size()>0) {
		    String[]tuple=  getMetadataManager().getFileUrl(request, entry, metadataList.get(0));
		    if(tuple!=null) {
			name = getStorageManager().getOriginalFilename(tuple[0]);
			path = tuple[2];
			if(debug)
			    System.err.println("File:" + entry +" " + name);
		    } else {
			if(debug)
			    System.err.println("No tuple:" + entry);
		    }
		} else {
		    if(debug)
			System.err.println("No thumbnail:" + entry);
		}
	    } else {
		path = getStorageManager().getEntryResourcePath(entry);
		name = getStorageManager().getFileTail(entry);
	    }
	    if(path==null) continue;

            int    cnt  = 1;
            if ( !forExport) {
                while (seen.get(name) != null) {
                    name = (cnt++) + "_" + name;
                }
                seen.put(name, name);
                if (!thumbnails && prefix.length() > 0) {
                    name = prefix + "/" + name;
                }
            }
            File f = new File(path);
            sizeProcessed += f.length();

            //check for size limit
            if (sizeSoFar + sizeProcessed > sizeLimit) {
                throw new IllegalArgumentException(
                    "Size of request has exceeded maximum size");
            }

            if (fileWriter != null) {
                InputStream fis =
                    getStorageManager().getFileInputStream(path);
                if ((entryNode != null) && forExport) {
                    fileWriter.writeFile(entry.getId(), fis);
                    XmlUtil.setAttributes(entryNode, new String[] { ATTR_FILE,
                            entry.getId(), ATTR_FILENAME, name });

                } else {
                    fileWriter.writeFile(name, fis);
                }
            }
        }

        return sizeProcessed;

    }

}
