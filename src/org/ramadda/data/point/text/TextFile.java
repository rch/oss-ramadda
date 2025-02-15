/**
Copyright (c) 2008-2021 Geode Systems LLC
SPDX-License-Identifier: Apache-2.0
*/

package org.ramadda.data.point.text;


import org.ramadda.data.point.*;
import org.ramadda.data.record.*;
import org.ramadda.util.HtmlUtils;
import org.ramadda.util.IO;
import org.ramadda.util.Station;
import org.ramadda.util.Utils;


import ucar.unidata.util.IOUtil;
import ucar.unidata.util.Misc;
import ucar.unidata.util.StringUtil;

import java.awt.*;
import java.awt.image.*;

import java.io.*;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


/**
 *
 */
public abstract class TextFile extends PointFile {

    /** _more_ */
    static int cnt = 0;

    /** _more_ */
    int mycnt = cnt++;

    /** _more_ */
    public static final String PROP_FIELDS = "fields";


    /** _more_ */
    public static final String DFLT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm Z";

    /** _more_ */
    public static final String PROP_SKIPLINES = "skiplines";

    /** _more_ */
    public static final String PROP_DATEFORMAT = "dateformat";

    /** _more_ */
    public static final String PROP_HEADER_DELIMITER = "header.delimiter";

    /** _more_ */
    public static final String PROP_HEADER_STANDARD = "header.standard";

    /** _more_ */
    public static final String PROP_DELIMITER = "delimiter";

    /** _more_ */
    protected String firstDataLine = null;



    /** _more_ */
    private List<String> headerLines = new ArrayList<String>();



    /** _more_ */
    private boolean headerStandard = false;

    /**  */
    private boolean firstLineFields = false;

    /** _more_ */
    String commentLineStart = null;


    /**
     * _more_
     */
    public TextFile() {}

    /**
     * ctor
     *
     *
     * @throws IOException _more_
     */
    public TextFile(IO.Path path) throws IOException {
        super(path);
    }

    /**
     * _more_
     *
     * @param properties _more_
     *
     * @throws IOException _more_
     */
    public TextFile(IO.Path path, Hashtable properties)
            throws IOException {
        super(path, properties);
    }


    /**
     * _more_
     *
     * @param context _more_
     * @param properties _more_
     */
    public TextFile(IO.Path path, RecordFileContext context,
                    Hashtable properties) {
       super(path, context, properties);
    }

    /** _more_ */
    private static Hashtable<String, String> fieldsMap =
        new Hashtable<String, String>();

    /**
     * _more_
     *
     * @param path _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public String getFieldsFileContents(String path) throws IOException {
        String fields = fieldsMap.get(path);
        if (fields == null) {
            fields = IOUtil.readContents(path, getClass()).trim();
            fields = fields.replaceAll("\n", " ");
            fieldsMap.put(path, fields);
        }

        return fields;
    }

    /**
     * _more_
     *
     * @return _more_
     *
     * @throws IOException _more_
     */
    public String getFieldsFileContents() throws IOException {
        String path = getClass().getCanonicalName();
        path = path.replaceAll("\\.", "/");
        path = "/" + path + ".fields.txt";

        //        System.err.println ("path:" + path);
        return getFieldsFileContents(path);
    }


    /**
     *  Set the FirstLineFields property.
     *
     *  @param value The new value for FirstLineFields
     */
    public void setFirstLineFields(boolean value) {
        firstLineFields = value;
    }

    /**
     *  Get the FirstLineFields property.
     *
     *  @return The FirstLineFields
     */
    public boolean getFirstLineFields() {
        return getProperty("firstLineDefinesFields", firstLineFields);

    }




    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     */
    public int getSkipLines(VisitInfo visitInfo) {
        int skipLines = Integer.parseInt(getProperty(PROP_SKIPLINES, "0"));
        return skipLines;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public String getHeaderDelimiter() {
        return getProperty(PROP_HEADER_DELIMITER, (String) null);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean isHeaderStandard() {
        return getProperty(PROP_HEADER_STANDARD, headerStandard);
    }

    /**
     * _more_
     *
     * @param v _more_
     */
    public void setIsHeaderStandard(boolean v) {
        headerStandard = v;
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public boolean readHeader() {
        return false;
    }


    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @return _more_
     *
     *
     * @throws Exception _more_
     *
     */
    @Override
    public RecordIO readHeader(RecordIO recordIO) throws Exception {
        return recordIO;
    }

    /**
     * _more_
     *
     * @param recordIO _more_
     *
     * @throws Exception _more_
     */
    public void writeHeader(RecordIO recordIO) throws Exception {
        for (String line : headerLines) {
            recordIO.getPrintWriter().println(line);
        }
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public List<String> getHeaderLines() {
        return headerLines;
    }

    /**
     * _more_
     *
     * @param record _more_
     * @param toks _more_
     * @param header _more_
     *
     * @return _more_
     */
    public List<String> processTokens(TextRecord record, List<String> toks,
                                      boolean header) {
        return toks;
    }

    /**
     * _more_
     *
     * @param record _more_
     * @param field _more_
     * @param tok _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public double parseValue(TextRecord record, RecordField field, String tok)
            throws Exception {
        try {
            return Double.parseDouble(tok);
        } catch (NumberFormatException nfe1) {
            if (tok.startsWith("(") && tok.endsWith(")")) {
                tok = tok.substring(1, tok.length()).substring(0,
                                    tok.length() - 2);

                return -Double.parseDouble(tok);
            }

            if (tok.endsWith("%")) {
                tok = tok.substring(0, tok.length() - 1);
            } else if (tok.startsWith("$")) {
                tok = tok.substring(1);
            }
            tok = tok.replaceAll(",", "");
	    if(tok.endsWith("+"))
		tok = tok.replaceAll("\\+", "");	    
            try {
                return Double.parseDouble(tok);
            } catch (NumberFormatException nfe2) {
                throw new IllegalArgumentException(
                    "Bad number format for field:" + field.getName()
                    + " value=" + tok);
            }
        }
    }



    /**
     * _more_
     *
     * @param siteId _more_
     * @param record _more_
     *
     * @return _more_
     */
    public Station setLocation(String siteId, TextRecord record) {
        Station station = setLocation(siteId);
        if (station != null) {
            record.setLocation(station);
        }

        return station;
    }



    /**
     * _more_
     *
     * @param lines _more_
     */
    public void setHeaderLines(List<String> lines) {
        headerLines = lines;
    }

    /**
     * _more_
     */
    public void initAfterClone() {
        super.initAfterClone();
        headerLines = new ArrayList<String>();
    }

    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    public boolean isHeaderLine(String line) {
        return line.startsWith("#");
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    int xcnt = 0;

    /**
     * _more_
     *
     * @param visitInfo _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public VisitInfo prepareToVisit(VisitInfo visitInfo) throws Exception {

        boolean debug             = false;

        boolean haveReadHeader    = headerLines.size() > 0;
        String  headerDelimiter   = getHeaderDelimiter();
        boolean firstLineFields   = getFirstLineFields();
        String  sfieldRow         = (String) getProperty("fieldRow", null);
        String  lastHeaderPattern = getProperty("lastHeaderPattern", null);
	int skipCnt  = getSkipLines(visitInfo);
	if (debug) {
	    System.err.println(
			       "TextFile.prepareToVisit: skipLines:"+skipCnt+" haveReadHeader:" + haveReadHeader +" headerDelimiter:" + headerDelimiter + " firstLineFields:" + firstLineFields+" lastHeaderPattern:" + lastHeaderPattern);
	}

        if (headerDelimiter != null) {
            if (debug) {
                System.err.println(
                    "TextFile.prepareToVisit: headerDelimiter");
            }
            boolean starts = headerDelimiter.startsWith("starts:");
            if (starts) {
                headerDelimiter =
                    headerDelimiter.substring("starts:".length());
            }
            while (true) {
                String line = visitInfo.getRecordIO().readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (starts) {
                    if (line.startsWith(headerDelimiter)) {
                        break;
                    }
                } else {
                    if (line.equals(headerDelimiter)) {
                        break;
                    }
                }
                if ( !haveReadHeader) {
                    headerLines.add(line);
                }
                //Don't go crazy if we miss the header delimiter
                if (headerLines.size() > 500) {
                    throw new IllegalStateException(
                        "Reading way too many header lines");
                }
            }
        } else if (isHeaderStandard()) {
            if (debug) {
                System.err.println("TextFile.prepareToVisit: isStandard");
            }
            while (true) {
                String line = visitInfo.getRecordIO().readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.length() == 0) {
                    break;
                }
                if ( !isHeaderLine(line)) {
                    visitInfo.getRecordIO().putBackLine(line);

                    break;
                }
                if ( !haveReadHeader) {
                    headerLines.add(line);
                    line = line.substring(1);
                    int idx = line.indexOf("=");
                    if (idx >= 0) {
                        List<String> toks = Utils.splitUpTo(line, "=", 2);
                        putProperty(toks.get(0), toks.get(1));
                    }
                }
            }
        } else if (lastHeaderPattern != null) {
            if (debug) {
                System.err.println(
                    "TextFile.prepareToVisit: lastHeaderPattern:"
                    + lastHeaderPattern);
            }
            boolean starts = lastHeaderPattern.startsWith("starts:");
            if (starts) {
                lastHeaderPattern =
                    lastHeaderPattern.substring("starts:".length());
            }
            while (true) {
                String line = visitInfo.getRecordIO().readLine();
                if (line == null) {
                    break;
                }
                if (starts) {
                    if (line.startsWith(lastHeaderPattern)) {
                        break;
                    }
                } else {
                    if (line.matches(lastHeaderPattern)) {
                        break;
                    }
                }
                headerLines.add(line);
                if (headerLines.size() > 500) {
                    throw new IllegalStateException(
                        "Reading way too many header lines");
                }

            }
        } else if (firstLineFields || (sfieldRow != null)) {
            int fieldRow = (sfieldRow != null)
                           ? Integer.parseInt(sfieldRow)
                           : 0;
            if (debug) {
                System.err.println(
                    "TextFile.prepareToVisit: firstLineFields=true skipLines="
                    + skipCnt);
            }
            String line       = null;
            String fieldsLine = null;
            while (true) {
                line = visitInfo.getRecordIO().readLine();
                if ( !haveReadHeader) {
                    headerLines.add(line);
                }
                skipCnt--;
		//Not sure if this should be here for all files but skip over any comment lines 
		if(line.startsWith("#")) continue;
                fieldRow--;
                if (fieldRow <= 0) {
                    fieldsLine = line;
                    break;
                }
            }
            //Read the rest of the header lines
            while (skipCnt > 0) {
                line = visitInfo.getRecordIO().readLine();
                if (line == null) {
                    break;
                }
                if ( !haveReadHeader) {
                    headerLines.add(line);
                }
                skipCnt--;
            }

	    if(debug) {
		System.err.println("headerLines:" + headerLines);
		System.err.println("fieldsLine:" + fieldsLine);		
	    }
            if (fieldsLine != null) {
                String defaultType      = getProperty("record.type.default", null);
                String delim      = getProperty(PROP_DELIMITER, ",");
                String sampleLine = visitInfo.getRecordIO().readLine();
                visitInfo.getRecordIO().putBackLine(sampleLine);
                List<String> toks = Utils.tokenizeColumns(fieldsLine, delim);
                List<String> sampleToks = Utils.tokenizeColumns(sampleLine,
								delim);
                List<String> cleaned = new ArrayList<String>();
                boolean      didDate = false;
                for (int tokIdx = 0; tokIdx < toks.size(); tokIdx++) {
                    String tok    = toks.get(tokIdx);
                    String sample = tokIdx<sampleToks.size()?sampleToks.get(tokIdx).toLowerCase():"";
                    tok = tok.replaceAll("\"", "");
                    String        name  = tok;
                    String        id    = Utils.makeID(tok);
                    StringBuilder attrs = new StringBuilder();
                    name = Utils.makeLabel(name.replaceAll(",", "&#44;"));
                    attrs.append(attrLabel(name));
                    boolean isDate =
                        id.matches(
                            "^(timestamp|week_ended|date|month|year|as_of|end_date|per_end_date|obs_date|quarter)$");
                    //                    System.err.println("id:" + id +" isDate:" + isDate);
                    if ( !isDate) {
                        isDate = Utils.isDate(sample);
                    }
		    if(!isDate) {
			String type = getProperty("record.type." + id,null);
			if(type==null) type=defaultType;
			if(type==null) {
			    if (Utils.isNumber(sample)) {
				type =RecordField.TYPE_DOUBLE;
			    } else {
				type  = RecordField.TYPE_STRING;
			    }
			}
			attrs.append(attrType(type));
		    }

                    if (isDate) {
                        if ( !didDate) {
                            String format = (String) getProperty(id
                                                + ".format");
                            if (format == null) {
                                if (id.equals("timestamp")) {
                                    format = "BAD";
                                    format = "yyyy-MM-dd HH:mm:ss";
                                } else if (id.equals("time")) {
                                    format = "yyyy-MM-dd'T'HH:mm:ss";
                                } else {
                                    format = "yyyy-MM-dd";
                                }
                            }
                            attrs.append(attrType(RecordField.TYPE_DATE));
                            attrs.append(attrFormat(format));
                            didDate = true;
                        } else {
                            attrs.append(attrType(RecordField.TYPE_STRING));
                        }
                    } else {
                        attrs.append(attrChartable());
                    }
                    String unit = (String) getProperty(id + ".unit");
                    if (unit != null) {
                        attrs.append(attrUnit(unit));
                    }
                    cleaned.add(id + "[" + attrs + "]");
                }
                String f = makeFields(cleaned);
                putProperty(PROP_FIELDS, f);
            }
        } else {
            commentLineStart = getProperty("commentLineStart", null);
            boolean seenLastHeaderPattern = false;
            for (int i = 0; i < skipCnt; ) {
                String line = visitInfo.getRecordIO().readLine();
                if ((commentLineStart != null)
                        && line.startsWith(commentLineStart)) {
                    continue;
                }
                if ( !haveReadHeader) {
                    headerLines.add(line);
                }
                i++;
            }
            if (headerLines.size() != skipCnt) {
		System.err.println(headerLines);
                throw new IllegalArgumentException(
                    "Bad number of header lines:" + headerLines.size() +" expected:" + skipCnt);

            }
            if (debug) {
                System.err.println(
                    "TextFile.prepareToVisit: default header lines="
                    + headerLines + " skipCnt=" + skipCnt);
            }
        }


        initProperties();

        return visitInfo;


    }


    /**
     * _more_
     *
     * @return _more_
     */
    @Override
    public String getTextHeader() {
        if (getHeaderLines().size() == 0) {
            doQuickVisit();
        }
        StringBuffer textHeader = new StringBuffer();
        for (String line : getHeaderLines()) {
            List<String> toks = Utils.splitUpTo(line, "=", 2);
            if (toks.size() == 2) {
                if (toks.get(0).trim().indexOf(" ") < 0) {
                    continue;
                }
            }
            textHeader.append(line);
            textHeader.append("\n");
        }

        return textHeader.toString();
    }



    /**
     * _more_
     *
     * @param fields _more_
     */
    public void putFields(String[] fields) {
        String f = makeFields(fields);
        putProperty(PROP_FIELDS, f);
    }

    /**
     *
     * @param fields _more_
     */
    public void putFields(List<String> fields) {
        String f = makeFields(fields);
        putProperty(PROP_FIELDS, f);
    }


    /**
     * _more_
     *
     * @param line _more_
     *
     * @return _more_
     */
    public boolean isLineValidData(String line) {
        if ((commentLineStart != null) && line.startsWith(commentLineStart)) {
            return false;
        }

        return true;
    }


    /**
     * _more_
     *
     * @param fields _more_
     *
     * @return _more_
     */
    public String makeFields(String[] fields) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i] == null) {
                continue;
            }
            if (i > 0) {
                sb.append(",");
            }
            sb.append(fields[i]);
        }

        return sb.toString();
    }

    /**
     * _more_
     *
     * @param fields _more_
     *
     * @return _more_
     */
    public String makeFields(List<String> fields) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i) == null) {
                continue;
            }
            if (i > 0) {
                sb.append(",");
            }
            sb.append(fields.get(i));
        }

        return sb.toString();
    }



    /**
     * _more_
     *
     * @param id _more_
     * @param attrs _more_
     *
     * @return _more_
     */
    public static String makeField(String id, String... attrs) {
        StringBuffer asb = new StringBuffer();
        for (String attr : attrs) {
            if (attr != null) {
                asb.append(attr);
                asb.append(" ");
            }
        }

        return id + "[" + asb + "]";
    }

    /**
     * _more_
     *
     * @param index _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    public PointRecord getRecord(int index) throws Exception {
        throw new IllegalArgumentException("Not implemented");
    }

    /**
     * _more_
     *
     * @param visitInfo _more_
     * @param record _more_
     * @param howMany _more_
     *
     * @return _more_
     *
     * @throws Exception _more_
     */
    @Override
    public boolean skip(VisitInfo visitInfo, BaseRecord record, int howMany)
            throws Exception {
        TextRecord textRecord = (TextRecord) record;
        for (int i = 0; i < howMany; i++) {
            String line = textRecord.readNextLine(visitInfo.getRecordIO());
            if (line == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * _more_
     *
     * @param d _more_
     *
     * @return _more_
     */
    public static String attrValue(double d) {
        return attrValue("" + d);
    }

    /**
     * _more_
     *
     * @param d _more_
     *
     * @return _more_
     */
    public static String attrSortOrder(int d) {
        return HtmlUtils.attr(ATTR_SORTORDER, "" + d);
    }


    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public static String attrValue(String v) {
        return HtmlUtils.attr(ATTR_VALUE, v);
    }


    /**
     * _more_
     *
     * @param pattern _more_
     *
     * @return _more_
     */
    public static String attrPattern(String pattern) {
        return HtmlUtils.attr(ATTR_PATTERN, pattern);
    }


    /**
     * _more_
     *
     * @param n _more_
     * @param v _more_
     *
     * @return _more_
     */
    public static String attr(String n, String v) {
        return HtmlUtils.attr(n, v);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public static String attrType(String v) {
        return HtmlUtils.attr(ATTR_TYPE, v);
    }


    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public static String attrWidth(int v) {
        return HtmlUtils.attr("width", "" + v);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public static String attrLabel(String v) {
        v = v.replaceAll(",", " ");

        return HtmlUtils.attr(ATTR_LABEL, v);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public static String attrMissing(double v) {
        return HtmlUtils.attr(ATTR_MISSING, "" + v);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public static String attrMissing(String v) {
        return HtmlUtils.attr(ATTR_MISSING, v);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public static String attrFormat(String v) {
        return HtmlUtils.attr(ATTR_FORMAT, v);
    }

    /**
     * _more_
     *
     * @param v _more_
     *
     * @return _more_
     */
    public static String attrUnit(String v) {
        return HtmlUtils.attr(ATTR_UNIT, v);
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public static String attrChartable() {
        return HtmlUtils.attr(ATTR_CHARTABLE, "true");
    }

    /**
     * _more_
     *
     * @return _more_
     */
    public static String attrSearchable() {
        return HtmlUtils.attr(ATTR_SEARCHABLE, "true");
    }
}
