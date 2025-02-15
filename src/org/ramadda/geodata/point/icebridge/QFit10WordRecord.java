
/*
 * Copyright 2013 ramadda.org
 * http://ramadda.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or (at
 * your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */



package org.ramadda.geodata.point.icebridge;

import org.ramadda.data.record.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;





/** This is generated code from generate.tcl. Do not edit it! */
public class QFit10WordRecord extends org.ramadda.geodata.point.icebridge.QfitRecord {
    public static final int ATTR_FIRST = org.ramadda.geodata.point.icebridge.QfitRecord.ATTR_LAST;
    public static final List<RecordField> FIELDS = new ArrayList<RecordField>();
    public static final int ATTR_RELATIVETIME =  ATTR_FIRST + 1;
    public static final RecordField RECORDATTR_RELATIVETIME;
    public static final int ATTR_LASERLATITUDE =  ATTR_FIRST + 2;
    public static final RecordField RECORDATTR_LASERLATITUDE;
    public static final int ATTR_LASERLONGITUDE =  ATTR_FIRST + 3;
    public static final RecordField RECORDATTR_LASERLONGITUDE;
    public static final int ATTR_ELEVATION =  ATTR_FIRST + 4;
    public static final RecordField RECORDATTR_ELEVATION;
    public static final int ATTR_STARTSIGNALSTRENGTH =  ATTR_FIRST + 5;
    public static final RecordField RECORDATTR_STARTSIGNALSTRENGTH;
    public static final int ATTR_REFLECTEDSIGNALSTRENGTH =  ATTR_FIRST + 6;
    public static final RecordField RECORDATTR_REFLECTEDSIGNALSTRENGTH;
    public static final int ATTR_AZIMUTH =  ATTR_FIRST + 7;
    public static final RecordField RECORDATTR_AZIMUTH;
    public static final int ATTR_PITCH =  ATTR_FIRST + 8;
    public static final RecordField RECORDATTR_PITCH;
    public static final int ATTR_ROLL =  ATTR_FIRST + 9;
    public static final RecordField RECORDATTR_ROLL;
    public static final int ATTR_GPSTIME =  ATTR_FIRST + 10;
    public static final RecordField RECORDATTR_GPSTIME;
    public static final int ATTR_LAST = ATTR_FIRST + 11;
    

    static {
    FIELDS.add(RECORDATTR_RELATIVETIME = new RecordField("relativeTime", "relativeTime", "", ATTR_RELATIVETIME, "", "int", "int", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_RELATIVETIME.setValueGetter(new ValueGetter() {
    public double getValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return (double) ((QFit10WordRecord)record).relativeTime;
    }
    public String getStringValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((QFit10WordRecord)record).relativeTime;
    }
    });
    FIELDS.add(RECORDATTR_LASERLATITUDE = new RecordField("laserLatitude", "laserLatitude", "", ATTR_LASERLATITUDE, "", "int", "int", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_LASERLATITUDE.setValueGetter(new ValueGetter() {
    public double getValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return (double) ((QFit10WordRecord)record).laserLatitude;
    }
    public String getStringValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((QFit10WordRecord)record).laserLatitude;
    }
    });
    FIELDS.add(RECORDATTR_LASERLONGITUDE = new RecordField("laserLongitude", "laserLongitude", "", ATTR_LASERLONGITUDE, "", "int", "int", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_LASERLONGITUDE.setValueGetter(new ValueGetter() {
    public double getValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return (double) ((QFit10WordRecord)record).laserLongitude;
    }
    public String getStringValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((QFit10WordRecord)record).laserLongitude;
    }
    });
    FIELDS.add(RECORDATTR_ELEVATION = new RecordField("elevation", "elevation", "", ATTR_ELEVATION, "mm", "int", "int", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_ELEVATION.setValueGetter(new ValueGetter() {
    public double getValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return (double) ((QFit10WordRecord)record).elevation;
    }
    public String getStringValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((QFit10WordRecord)record).elevation;
    }
    });
    FIELDS.add(RECORDATTR_STARTSIGNALSTRENGTH = new RecordField("startSignalStrength", "startSignalStrength", "", ATTR_STARTSIGNALSTRENGTH, "", "int", "int", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_STARTSIGNALSTRENGTH.setValueGetter(new ValueGetter() {
    public double getValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return (double) ((QFit10WordRecord)record).startSignalStrength;
    }
    public String getStringValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((QFit10WordRecord)record).startSignalStrength;
    }
    });
    FIELDS.add(RECORDATTR_REFLECTEDSIGNALSTRENGTH = new RecordField("reflectedSignalStrength", "reflectedSignalStrength", "", ATTR_REFLECTEDSIGNALSTRENGTH, "", "int", "int", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_REFLECTEDSIGNALSTRENGTH.setValueGetter(new ValueGetter() {
    public double getValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return (double) ((QFit10WordRecord)record).reflectedSignalStrength;
    }
    public String getStringValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((QFit10WordRecord)record).reflectedSignalStrength;
    }
    });
    FIELDS.add(RECORDATTR_AZIMUTH = new RecordField("azimuth", "azimuth", "", ATTR_AZIMUTH, "millidegree", "int", "int", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_AZIMUTH.setValueGetter(new ValueGetter() {
    public double getValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return (double) ((QFit10WordRecord)record).azimuth;
    }
    public String getStringValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((QFit10WordRecord)record).azimuth;
    }
    });
    FIELDS.add(RECORDATTR_PITCH = new RecordField("pitch", "pitch", "", ATTR_PITCH, "millidegree", "int", "int", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_PITCH.setValueGetter(new ValueGetter() {
    public double getValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return (double) ((QFit10WordRecord)record).pitch;
    }
    public String getStringValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((QFit10WordRecord)record).pitch;
    }
    });
    FIELDS.add(RECORDATTR_ROLL = new RecordField("roll", "roll", "", ATTR_ROLL, "millidegree", "int", "int", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_ROLL.setValueGetter(new ValueGetter() {
    public double getValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return (double) ((QFit10WordRecord)record).roll;
    }
    public String getStringValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((QFit10WordRecord)record).roll;
    }
    });
    FIELDS.add(RECORDATTR_GPSTIME = new RecordField("gpsTime", "gpsTime", "", ATTR_GPSTIME, "", "int", "int", 0, SEARCHABLE_NO,CHARTABLE_NO));
    RECORDATTR_GPSTIME.setValueGetter(new ValueGetter() {
    public double getValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return (double) ((QFit10WordRecord)record).gpsTime;
    }
    public String getStringValue(BaseRecord record, RecordField field, VisitInfo visitInfo) {
    return ""+ ((QFit10WordRecord)record).gpsTime;
    }
    });
    
    }
    

    int startSignalStrength;
    int reflectedSignalStrength;
    int azimuth;
    int pitch;
    int roll;
    int gpsTime;
    

    public  QFit10WordRecord(QFit10WordRecord that)  {
        super(that);
        this.relativeTime = that.relativeTime;
        this.laserLatitude = that.laserLatitude;
        this.laserLongitude = that.laserLongitude;
        this.elevation = that.elevation;
        this.startSignalStrength = that.startSignalStrength;
        this.reflectedSignalStrength = that.reflectedSignalStrength;
        this.azimuth = that.azimuth;
        this.pitch = that.pitch;
        this.roll = that.roll;
        this.gpsTime = that.gpsTime;
        
        
    }



    public  QFit10WordRecord(RecordFile file)  {
        super(file);
    }



    public  QFit10WordRecord(RecordFile file, boolean bigEndian)  {
        super(file, bigEndian);
    }



    public int getLastAttribute()  {
        return ATTR_LAST;
    }



    public  boolean equals(Object object)  {
        if(!super.equals(object)) {System.err.println("bad super"); return false;} if(!(object instanceof QFit10WordRecord)) return false;
        QFit10WordRecord that = (QFit10WordRecord ) object;
        if(this.relativeTime!= that.relativeTime) {System.err.println("bad relativeTime");  return false;}
        if(this.laserLatitude!= that.laserLatitude) {System.err.println("bad laserLatitude");  return false;}
        if(this.laserLongitude!= that.laserLongitude) {System.err.println("bad laserLongitude");  return false;}
        if(this.elevation!= that.elevation) {System.err.println("bad elevation");  return false;}
        if(this.startSignalStrength!= that.startSignalStrength) {System.err.println("bad startSignalStrength");  return false;}
        if(this.reflectedSignalStrength!= that.reflectedSignalStrength) {System.err.println("bad reflectedSignalStrength");  return false;}
        if(this.azimuth!= that.azimuth) {System.err.println("bad azimuth");  return false;}
        if(this.pitch!= that.pitch) {System.err.println("bad pitch");  return false;}
        if(this.roll!= that.roll) {System.err.println("bad roll");  return false;}
        if(this.gpsTime!= that.gpsTime) {System.err.println("bad gpsTime");  return false;}
        return true;
    }




    protected void addFields(List<RecordField> fields)  {
        super.addFields(fields);
        fields.addAll(FIELDS);
    }



    public double getValue(int attrId)  {
        if(attrId == ATTR_RELATIVETIME) return relativeTime;
        if(attrId == ATTR_LASERLATITUDE) return laserLatitude;
        if(attrId == ATTR_LASERLONGITUDE) return laserLongitude;
        if(attrId == ATTR_ELEVATION) return elevation;
        if(attrId == ATTR_STARTSIGNALSTRENGTH) return startSignalStrength;
        if(attrId == ATTR_REFLECTEDSIGNALSTRENGTH) return reflectedSignalStrength;
        if(attrId == ATTR_AZIMUTH) return azimuth;
        if(attrId == ATTR_PITCH) return pitch;
        if(attrId == ATTR_ROLL) return roll;
        if(attrId == ATTR_GPSTIME) return gpsTime;
        return super.getValue(attrId);
        
    }



    public int getRecordSize()  {
        return super.getRecordSize() + 40;
    }



    public BaseRecord.ReadStatus read(RecordIO recordIO) throws Exception  {
        DataInputStream dis = recordIO.getDataInputStream();
        BaseRecord.ReadStatus status= super.read(recordIO);
        if(status!=BaseRecord.ReadStatus.OK)  return status;
        relativeTime =  readInt(dis);
        laserLatitude =  readInt(dis);
        laserLongitude =  readInt(dis);
        elevation =  readInt(dis);
        startSignalStrength =  readInt(dis);
        reflectedSignalStrength =  readInt(dis);
        azimuth =  readInt(dis);
        pitch =  readInt(dis);
        roll =  readInt(dis);
        gpsTime =  readInt(dis);
        
        
        return BaseRecord.ReadStatus.OK;
    }



    public void write(RecordIO recordIO) throws IOException  {
        DataOutputStream dos = recordIO.getDataOutputStream();
        super.write(recordIO);
        writeInt(dos, relativeTime);
        writeInt(dos, laserLatitude);
        writeInt(dos, laserLongitude);
        writeInt(dos, elevation);
        writeInt(dos, startSignalStrength);
        writeInt(dos, reflectedSignalStrength);
        writeInt(dos, azimuth);
        writeInt(dos, pitch);
        writeInt(dos, roll);
        writeInt(dos, gpsTime);
        
    }



    public int doPrintCsv(VisitInfo visitInfo,PrintWriter pw)  {
        boolean includeVector = visitInfo.getProperty(PROP_INCLUDEVECTOR, false);
        int superCnt = super.doPrintCsv(visitInfo,pw);
        int myCnt = 0;
        if(superCnt>0) pw.print(',');
        pw.print(getStringValue(RECORDATTR_RELATIVETIME, relativeTime));
        myCnt++;
        pw.print(',');
        pw.print(getStringValue(RECORDATTR_LASERLATITUDE, laserLatitude));
        myCnt++;
        pw.print(',');
        pw.print(getStringValue(RECORDATTR_LASERLONGITUDE, laserLongitude));
        myCnt++;
        pw.print(',');
        pw.print(getStringValue(RECORDATTR_ELEVATION, elevation));
        myCnt++;
        pw.print(',');
        pw.print(getStringValue(RECORDATTR_STARTSIGNALSTRENGTH, startSignalStrength));
        myCnt++;
        pw.print(',');
        pw.print(getStringValue(RECORDATTR_REFLECTEDSIGNALSTRENGTH, reflectedSignalStrength));
        myCnt++;
        pw.print(',');
        pw.print(getStringValue(RECORDATTR_AZIMUTH, azimuth));
        myCnt++;
        pw.print(',');
        pw.print(getStringValue(RECORDATTR_PITCH, pitch));
        myCnt++;
        pw.print(',');
        pw.print(getStringValue(RECORDATTR_ROLL, roll));
        myCnt++;
        pw.print(',');
        pw.print(getStringValue(RECORDATTR_GPSTIME, gpsTime));
        myCnt++;
        return myCnt+superCnt;
        
    }



    public int doPrintCsvHeader(VisitInfo visitInfo,PrintWriter pw)  {
        int superCnt = super.doPrintCsvHeader(visitInfo,pw);
        int myCnt = 0;
        boolean includeVector = visitInfo.getProperty(PROP_INCLUDEVECTOR, false);
        if(superCnt>0) pw.print(',');
        RECORDATTR_RELATIVETIME.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_LASERLATITUDE.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_LASERLONGITUDE.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_ELEVATION.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_STARTSIGNALSTRENGTH.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_REFLECTEDSIGNALSTRENGTH.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_AZIMUTH.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_PITCH.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_ROLL.printCsvHeader(visitInfo,pw);
        myCnt++;
        pw.print(',');
        RECORDATTR_GPSTIME.printCsvHeader(visitInfo,pw);
        myCnt++;
        return myCnt+superCnt;
        
    }



    public void print(Appendable buff)  throws Exception  {
        super.print(buff);
        buff.append(" relativeTime: " + relativeTime+" \n");
        buff.append(" laserLatitude: " + laserLatitude+" \n");
        buff.append(" laserLongitude: " + laserLongitude+" \n");
        buff.append(" elevation: " + elevation+" \n");
        buff.append(" startSignalStrength: " + startSignalStrength+" \n");
        buff.append(" reflectedSignalStrength: " + reflectedSignalStrength+" \n");
        buff.append(" azimuth: " + azimuth+" \n");
        buff.append(" pitch: " + pitch+" \n");
        buff.append(" roll: " + roll+" \n");
        buff.append(" gpsTime: " + gpsTime+" \n");
        
    }



    public int getStartSignalStrength()  {
        return startSignalStrength;
    }


    public void setStartSignalStrength(int newValue)  {
        startSignalStrength = newValue;
    }


    public int getReflectedSignalStrength()  {
        return reflectedSignalStrength;
    }


    public void setReflectedSignalStrength(int newValue)  {
        reflectedSignalStrength = newValue;
    }


    public int getAzimuth()  {
        return azimuth;
    }


    public void setAzimuth(int newValue)  {
        azimuth = newValue;
    }


    public int getPitch()  {
        return pitch;
    }


    public void setPitch(int newValue)  {
        pitch = newValue;
    }


    public int getRoll()  {
        return roll;
    }


    public void setRoll(int newValue)  {
        roll = newValue;
    }


    public int getGpsTime()  {
        return gpsTime;
    }


    public void setGpsTime(int newValue)  {
        gpsTime = newValue;
    }



}



