/*
 *  Copyright (C) :	2002,2003,2004,2005,2006,2007,2008,2009
 *			European Synchrotron Radiation Facility
 *			BP 220, Grenoble 38043
 *			FRANCE
 * 
 *  This file is part of Tango.
 * 
 *  Tango is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Tango is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Tango.  If not, see <http://www.gnu.org/licenses/>.
 */
 
// $Id$
//
// Description:

package fr.esrf.tangoatk.core.attribute;

import fr.esrf.Tango.AttrWriteType;
import fr.esrf.tangoatk.core.*;

import java.util.*;

import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.AttrDataFormat;
import fr.esrf.TangoApi.DbAttribute;
import fr.esrf.TangoApi.AttributeInfoEx;

/**
 * <code>AttributeFactory</code> is an extension of {@link AEntityFactory}
 * which takes care of instantiating Attribute objects. It is a Singleton,
 * so please use getInstance to instantiate the Factory.
 * @version $Revision$
 */
public class AttributeFactory extends AEntityFactory {

  private static AttributeFactory instance;
  protected Vector<AAttribute>   attributes = new Vector<AAttribute> ();
  protected String[] attNames   = new String[0]; // For fast string search

  /**
   * Creates a new <code>AttributeFactory</code> instance. Do not use
   * this.
   * @see #getInstance
   */
  protected AttributeFactory() {}

  /**
   * <code>getInstance</code> returns an instance of the AttributeFactory.
   * There will be only one AttributeFactory per running instance of the JVM.
   * @return an <code>AttributeFactory</code> value
   */
  public static AttributeFactory getInstance() {
    if (instance == null) {
      instance = new AttributeFactory();
    }
    return instance;
  }

  /**
   * Returns all attributes belonging to this factory.
   * @return  Attributes belonging to this factory
   */
  public AAttribute[] getAttributes() {

    AAttribute[] ret = new AAttribute[attributes.size()];
    synchronized(this) {
      for(int i=0;i<attributes.size();i++)
        ret[i] = (AAttribute)attributes.get(i);
    }
    return ret;

  }

  public int getSize() {
    return attributes.size();
  }

  private int getAttributePos(String fqname) {
    return Arrays.binarySearch(attNames,fqname.toLowerCase());
  }

  /**
   * Get Entities using wildcard.
   * @param name a <code>String</code> value containing the name of the
   * entity
   * @param device a <code>Device</code> value containing the device from
   * which the entity is to be obtained.
   * @return a List containing the corresponding IAttributes
   * @throws DevFailed if an error occurs
   */
  protected synchronized List<IEntity> getWildCardEntities(String name, Device device)
          throws DevFailed {

    List<IEntity> list = new Vector<IEntity> ();

    /*
     * Get the array of configs from the device.
     * Pass null to get all attributes.
     */
    AttributeInfoEx[] config = device.getAttributeInfo((String[])null);

    /*
     * loop through all these configs and see if we havn't already
     * imported it.
     */
    for (int i = 0; i < config.length; i++) {

      String fqname = getFQName(device, config[i].name);
      AAttribute attribute;

      int pos = getAttributePos(fqname);

      if( pos>=0 ) {
        attribute = attributes.get(pos);
      } else {
        attribute = initAttribute(device, config[i],-(pos+1),fqname);
      }

      list.add(attribute);
    }

    return list;
  }

    /**
     * <code>getStatelessDevice</code>
     *
     * @param deviceName a <code>String</code> value containing the
     * name of the device to get.
     * @return a <code>Device</code> if this device is already known.
     * @see DeviceFactory#getDevice
     */
    Device getConnectionlessDevice(String deviceName) throws ConnectionException
    {
        return DeviceFactory.getInstance().getConnectionlessDevice(deviceName);
    }

  /**
   * Get attribute without contacting the device
   * @param name <code>String</code> value containing the name of the
   * state attribute to be instantiated.
   * @return a <code>List</code> value containing the corresponding IAttributes
   * @throws ConnectionException In case of failure
   * @see IAttribute
   */
    @Override
    protected synchronized List<IEntity> getConnectionlessSingleAttribute(String name) throws ConnectionException
    {
        List<IEntity> list = new Vector<IEntity>();
        Device d = getConnectionlessDevice(extractDeviceName(name));
        list.add(getConnectionlessStateAttribute(name, d));
        return list;
    }

  /**
   * <code>getConnectionlessStateAttribute</code> returns an attribute corresponding
   * to the name given in the first parameter. If such an attribute already
   * exists, the existing attribute is returned. Otherwise it is created.
   * @param fqname a <code>String</code> value containing the state attribute name
   * fully qualified with device name.
   * @param device a <code>Device</code> value the device
   * @return an <code>IEntity</code> value
   * @throws ConnectionException In case of failure
   */
    protected synchronized IEntity getConnectionlessStateAttribute(String fqname, Device device)  throws ConnectionException
    {
        /*
         * Check if the attribute has already been imported
         */
        int pos = getAttributePos(fqname);

        /*
         * if so we return the old attribute and exit.
         */
        if (pos >= 0)
        {
            return attributes.get(pos);
        }

        /*
         * To obtain a new attribute we must find its name.
         * The name is passed fully-qualified, that is with the
         * device-name prefixed, like eas/test-api/1/Attr_name.
         */
        String name = extractEntityName(fqname);

        return initConnectionlessStateAttribute(device, -(pos + 1), fqname);
    }

  /**
   * <code>getSingleEntity</code> returns an attribute corresponding
   * to the name given in the first parameter. If such an attribute already
   * exists, the existing attribute is returned. Otherwise it is created.
   * @param fqname a <code>String</code> value containing the EntityName
   * fully qualified with device name.
   * @param device a <code>Device</code> value the device
   * @return an <code>IEntity</code> value
   * @exception DevFailed if an error occurs
   */
  protected synchronized IEntity getSingleEntity(String fqname, Device device)
          throws DevFailed {

    /*
     * Check if the attribute has already been imported
     */
    int pos = getAttributePos(fqname);

    /*
     * if so we return the old attribute and exit.
     */
    if (pos>=0) return attributes.get(pos);

    /*
     * To obtain a new attribute we must find its name.
     * The name is passed fully-qualified, that is with the
     * device-name prefixed, like eas/test-api/1/Attr_name.
     */
    String name = extractEntityName(fqname);

    /*
     * We obtain the config for our attribute from the device and
     * return an initialized attribute.
     */
    AttributeInfoEx config = device.getAttributeInfo(name);
    return initAttribute(device, config,-(pos+1),fqname);
  }


  /**
   * Returns an Attribute corresponding to the given name. If such an attribute
   * already exists, the existing attribute is returned. Otherwise it is created.
   * @param fqname a <code>String</code> value containing the EntityName
   * fully qualified with device name.
   * @return null if the type of the IEntity is not AAttribute, a valid entity otherwise.
   * @throws ConnectionException In case of failure
   * @throws DevFailed In case of failure
   */
  public IAttribute getAttribute(String fqname)
          throws ConnectionException, DevFailed {

    Device d = null;
    IEntity ie = null;
    AAttribute att = null;

    // Check wether the attribute has alrerady been imported.
    synchronized(this) {
      int pos = getAttributePos(fqname);
      if (pos>=0) ie = attributes.get(pos);
    }

    if (ie == null) {
      // Create the entity
      d  = getDevice(extractDeviceName(fqname));
      ie = getSingleEntity(fqname, d);
    }

    // Does not exists.
    if (ie == null)
      return null;

    // Check entity type
    if (ie instanceof AAttribute) {
      att = (AAttribute) ie;
      return att;
    } else
      return null;

  }

  /**
   * Check wether the given name correspond to an existing attribute.
   * @param fqname Full entity name
   * @return True if the attribute exists.
   */
  public boolean isAttribute(String fqname) {

    try {
      return (getAttribute(fqname)!=null);
    } catch (ConnectionException de) {
//      System.out.println("AttributeFactory.isAttribute(" + fqname + ") : " + de.getErrors()[0].desc);
      return false;
    } catch (DevFailed dfe) {
//      System.out.println("AttributeFactory.isAttribute(" + fqname + ") : " + dfe.errors[0].desc);
      return false;
    } catch (Exception e) {
      // Unexpected exception
      System.out.println("AttributeFactory.isAttribute(" + fqname + ") : Unexpected exception caught...");
      e.printStackTrace();
      return false;
    }

  }

  /**
   * Check whether the given name corresponds to a state attribute whose device is not reachable.
   * @param fqname Full entity name
   * @return True if the state attribute exists.
   */
    public boolean isConnectionLessAttribute(String fqname)
    {
        try
        {
            return (getConnectionlessSingleAttribute(fqname) != null);
        }
        catch (ConnectionException de)
        {
            System.out.println("AttributeFactory.isConnectionLessAttribute(" + fqname + ") : " + de.getErrors()[0].desc);
            return false;
        }
        catch (Exception e)
        {
            // Unexpected exception
            System.out.println("AttributeFactory.isConnectionLessAttribute(" + fqname + ") : Unexpected exception caught...");
            e.printStackTrace();
            return false;
        }
    }

  /**
   * <code>initAttribute</code> ask getAttributeOfType for an
   * AAttribute of the type given in the AttributeInfo passed as
   * parameter, and then calls <code>init()</code> on the attribute
   *
   * @param device a <code>Device</code> value
   * @param config an <code>AttributeInfo</code> value
   * @param insertionPos Insertion postion of this new attribute in the global list
   * @param fqname Full entity name (can include host and port)
   * @return an <code>AAttribute</code> value
   */
  protected AAttribute initAttribute(Device device,
                                     AttributeInfoEx config,
                                     int insertionPos,
                                     String fqname) {

    AAttribute attribute = getAttributeOfType(device,config);
    long t0 = System.currentTimeMillis();
    attribute.init(device, config.name, config, true);
    DeviceFactory.getInstance().trace(DeviceFactory.TRACE_SUCCESS,"AttributeFactory.init(" + fqname + ")",t0);

    // Build the new attNames array
    buildNames(fqname,insertionPos);

    attributes.add(insertionPos, attribute);

    dumpFactory("Adding " + fqname);

    return attribute;
  }

  /**
   * <code>initConnectionlessStateAttribute</code> ask getConnectionlessDevStateScalar for an
   * AAttribute of the type given in the AttributeInfo passed as
   * parameter, and then calls <code>init()</code> on the attribute
   *
   * @param device a <code>Device</code> value
   * @param insertionPos Insertion postion of this new attribute in the global list
   * @param fqname Full entity name (can include host and port)
   * @return an <code>AAttribute</code> value
   * @throws ConnectionException In case of failure
   */
    protected AAttribute initConnectionlessStateAttribute(Device device,
            int insertionPos,
            String fqname)  throws ConnectionException
    {

        AAttribute attribute = getConnectionlessDevStateScalar(device, fqname);
        long t0 = System.currentTimeMillis();
        attribute.connectionlessInit(device, extractEntityName(fqname), true);
        DeviceFactory.getInstance().trace(DeviceFactory.TRACE_SUCCESS, "AttributeFactory.initConnectionlessStateAttribute(" + fqname + ")", t0);

        // Build the new attNames array
        buildNames(fqname, insertionPos);

        attributes.add(insertionPos, attribute);

        dumpFactory("Adding " + fqname);

        return attribute;
    }

  protected void buildNames(String fqname,int insertionPos) {

    int lgth = attNames.length;
    String[] newAttNames=new String[lgth+1];
    System.arraycopy(attNames,0,newAttNames,0,insertionPos);
    System.arraycopy(attNames,insertionPos,newAttNames,insertionPos+1,lgth-insertionPos);
    newAttNames[insertionPos]=fqname.toLowerCase();
    attNames=newAttNames;

  }

  /**
   * <code>getAttributeOfType</code> figures out what type of
   * attribute is demanded, and deletates the work of instantiating the
   * attribute to getScalar, getSpectrum, or getImage. The typeinference
   * is based on the value of AttributeConfig.data_format.
   * @param device a <code>Device</code> value
   * @param config an <code>AttributeInfo</code> value
   * @return an <code>AAttribute</code> value
   * @throws IllegalArgumentException if the format is unknown.
   */
  protected AAttribute getAttributeOfType(Device device,AttributeInfoEx config) {

    if (config == null) {
      System.out.println("Warning, AttributeFactory.getAttributeOfType(): Warning, null AttributeInfo pointer got from " + device.getName());
      return new InvalidAttribute();
    }

    AttrDataFormat format = config.data_format;
    String name = config.name;

    switch (format.value()) {
      case AttrDataFormat._SCALAR:
        return getScalar(device,config);
      case AttrDataFormat._SPECTRUM:
        return getSpectrum(device,config);
      case AttrDataFormat._IMAGE:
        return getImage(device,config);
      default:
        System.out.println("Warning, AttributeFactory.getAttributeOfType(" + device.getName() + "/" + name +
                           ") : Unsupported attribute format [" + format.value() +"]");
        return new InvalidAttribute();
    }

  }

  /**
   * <code>getConnectionlessDevStateScalar</code> does the work of instantiating the
   * DevState scalar attribute even if the device is not responding.
   * @param device a <code>Device</code> value
   * @param fqname Full entity name (can include host and port)
   * @return an <code>AAttribute</code> value
   * @throws ConnectionException if the attribute name is not state.
   */
    protected AAttribute getConnectionlessDevStateScalar(Device device, String fqname) throws ConnectionException
    {
        String attName = extractEntityName(fqname);
        if (!attName.equalsIgnoreCase("state"))
            throw new ConnectionException("Only state attribute can be created without connection to the device.");
        
        DevStateScalar dss = new DevStateScalar();
        return dss;
    }


  private double getMinimumIncrement(Device device, AttributeInfoEx config) {

    String name = config.name;
    DbAttribute dbAtt;
    String[] propVal;
    double inc = Double.NaN;

    if (config.writable == AttrWriteType.READ_WRITE) {

      try {
        dbAtt = device.get_attribute_property(name);
        if (dbAtt != null) {
          if (!dbAtt.is_empty("minimumIncrement")) {
            propVal = dbAtt.get_value("minimumIncrement");
            if (propVal != null) {
              try {
                inc = Double.parseDouble(propVal[0]);
              } catch (NumberFormatException e1) {
                System.out.println("Warning, " + device.getName() + "/" + name + "/minimumIncrement, invalid number");
              }
            }
          }
        }
      } catch (Exception ex) {
        System.out.println("get_attribute_property(" + name + ") thrown exception");
        ex.printStackTrace();
      }

    }

    return inc;

  }

  private EnumScalar getEnumScalarFromScalar(Device device,AttributeInfoEx config,DbAttribute dbAtt)
  {
     String         name = config.name;
     String[]       propVal=null;

     String[]       enumLabs =null;
     String[]       enumSetExcludeLabs = null;
     EnumScalar     ens = null;

     try
     {
         if (dbAtt != null)
         {
             if (!dbAtt.is_empty("EnumLabels"))
             {
                 //System.out.println("Found EnumLabels property for "+name);
                 propVal = dbAtt.get_value("EnumLabels");
                 //System.out.println("EnumLabels = "+propVal);
                 if (propVal != null)
                    enumLabs = EnumScalar.getNonEmptyLabels(propVal);
             }
         }
     }
     catch (Exception ex)
     {
         System.out.println("get_attribute_property("+name+") thrown exception");
         ex.printStackTrace();
     }

     if (enumLabs == null)
         return null;

     if (enumLabs.length <= 1)
         return null;

     try
     {
         dbAtt = device.get_attribute_property(name);
         if (dbAtt != null)
         {
             if (!dbAtt.is_empty("EnumSetExclusion"))
             {
                 //System.out.println("Found EnumSetExclusion property for "+name);
                 propVal = dbAtt.get_value("EnumSetExclusion");
                 //System.out.println("EnumSetExclusion = "+propVal);
                 if (propVal != null)
                 {
                     enumSetExcludeLabs = EnumScalar.getNonEmptyLabels(propVal);
                 }
             }
         }
     }
     catch (Exception ex)
     {
         System.out.println("get_attribute_property("+name+") thrown exception");
         ex.printStackTrace();
     }

     if (enumSetExcludeLabs != null)
        if (enumSetExcludeLabs.length < 1)
           enumSetExcludeLabs=null;

     ens = new EnumScalar(enumLabs, enumSetExcludeLabs);
     return ens;
  }


  private EnumScalar getTangoEnumScalar(AttributeInfoEx config)
  {
     String[]       enumLabs =null;
     EnumScalar     ens = null;
     
     enumLabs = config.enum_label;

     if (enumLabs == null) return null;

     if (enumLabs.length <= 0) return null;

     ens = new EnumScalar(enumLabs);
     return ens;
  }

  private EnumSpectrum getTangoEnumSpectrum(AttributeInfoEx config)
  {
     String[]       enumLabs =null;
     EnumSpectrum     ens = null;
     
     enumLabs = config.enum_label;

     if (enumLabs == null) return null;

     if (enumLabs.length <= 1) return null;

     ens = new EnumSpectrum(enumLabs);
     return ens;
  }


  /**
   * <code>getScalar</code> figures out what kind of scalar is to be
   * created. This is done by looking at AttributeInfo.data_type
   *
   * @param device a <code>Device</code> value
   * @param config an <code>AttributeInfo</code> value
   * @return an <code>AAttribute</code> value being instanceof either
   * ShortScalar, DoubleScalar, LongScalar, StringScalar, BooleanScalar or DevStateScalar.
   * @throws IllegalArgumentException if the type is unknown.
   */
  private AAttribute getScalar(Device device, AttributeInfoEx config) {

    String name = config.name;
    int dataType = config.data_type;
    BooleanScalar bs = null;
    DevStateScalar dss = null;
    EnumScalar ens = null;
    RawImage ri = null;

    if (dataType == Tango_DEV_STRING) {
      return new StringScalar();
    }

    NumberScalar ns = new NumberScalar();

    // Handle confirmation window
    DbAttribute dbAtt = null;
    try {
      dbAtt = device.get_attribute_property(name);
      if(!dbAtt.is_empty("ConfirmationMessage")) {
        ns.confirmationMessage = dbAtt.get_value("ConfirmationMessage")[0];
      }
    } catch (DevFailed e) {}

    switch (dataType) {

      case Tango_DEV_UCHAR:
        ns.setNumberHelper(new UCharScalarHelper(ns));
        return ns;

      case Tango_DEV_SHORT:
        ens = getEnumScalarFromScalar(device, config, dbAtt);
        if (ens != null) {
          ens.setEnumHelper(new EnumScalarHelper(ens));
          return ens;
        } else
          ns.setNumberHelper(new ShortScalarHelper(ns));
        break;

      case Tango_DEV_USHORT:
        ens = getEnumScalarFromScalar(device, config, dbAtt);
        if (ens != null) {
          ens.setEnumHelper(new EnumScalarHelper(ens));
          return ens;
        } else
          ns.setNumberHelper(new UShortScalarHelper(ns));
        break;

      case Tango_DEV_DOUBLE:
        ns.setNumberHelper(new DoubleScalarHelper(ns));
        ns.setMinimumIncrement(getMinimumIncrement(device, config));
        break;

      case Tango_DEV_FLOAT:
        ns.setNumberHelper(new FloatScalarHelper(ns));
        ns.setMinimumIncrement(getMinimumIncrement(device, config));
        break;

      case Tango_DEV_LONG:
        ens = getEnumScalarFromScalar(device, config, dbAtt);
        if (ens != null) {
          ens.setEnumHelper(new EnumScalarHelper(ens));
          return ens;
        } else
          ns.setNumberHelper(new LongScalarHelper(ns));
        break;

      case Tango_DEV_ULONG:
        ens = getEnumScalarFromScalar(device, config, dbAtt);
        if (ens != null) {
          ens.setEnumHelper(new EnumScalarHelper(ens));
          return ens;
        } else
          ns.setNumberHelper(new ULongScalarHelper(ns));
        break;

      case Tango_DEV_LONG64:
        ens = getEnumScalarFromScalar(device, config, dbAtt);
        if (ens != null) {
          ens.setEnumHelper(new EnumScalarHelper(ens));
          return ens;
        } else
          ns.setNumberHelper(new Long64ScalarHelper(ns));
        break;

      case Tango_DEV_ULONG64:
        ens = getEnumScalarFromScalar(device, config, dbAtt);
        if (ens != null) {
          ens.setEnumHelper(new EnumScalarHelper(ens));
          return ens;
        } else
          ns.setNumberHelper(new ULong64ScalarHelper(ns));
        break;

      case Tango_DEV_BOOLEAN:
        bs = new BooleanScalar();
        return bs;

      case Tango_DEV_STATE:
        dss = new DevStateScalar();
        return dss;

      case Tango_DEV_ENCODED:
        if (config.format.equalsIgnoreCase(AAttribute.RAW_IMAGE_FORMAT)) {
          ri = new RawImage();
          return ri;
        } else {
          System.out.println("Warning, AttributeFactory.getScalar(" + device.getName() + "/" + name +
              ") : Unsupported DevEncoded attribute. Try to set the format attribute property to 'RawImage'.");
          return new InvalidAttribute();
        }

      case Tango_DEV_ENUM:
        ens = getTangoEnumScalar(config);
        if (ens == null) // failed to instantiate correctly Enum
        {
          System.out.println("Warning, Tango_DEV_ENUM, AttributeFactory.getTangoEnumScalar(" + device.getName() + "/" + name + ") : Failed ");
          return new InvalidAttribute();
        }
        ens.setEnumHelper(new EnumScalarHelper(ens));
        return ens;

      default:
        System.out.println("Warning, AttributeFactory.getScalar(" + device.getName() + "/" + name +
            ") : Unsupported data type [" + dataType + "]");
        return new InvalidAttribute();
    }

    return ns;
  }

  /**
   * <code>getSpectrum</code> figures out what kind of spectrum is to be
   * created. This is done by looking at AttributeInfo.data_type
   *
   * @param config an <code>AttributeInfo</code> value
   * @return an <code>AAttribute</code> value being instanceof either
   * ShortSpectrum, DoubleSpectrum, LongSpectrum or BooleanSpectrum
   * @throws IllegalArgumentException if the type is unknown.
   */
  private AAttribute getSpectrum(Device device,AttributeInfoEx config) {
    String name = config.name;
    int dataType = config.data_type;
    NumberSpectrum ns = new NumberSpectrum();
    StringSpectrum ss = null;
    BooleanSpectrum bs = null;
    DevStateSpectrum dss = null;
    EnumSpectrum     ens = null;

    switch (dataType) {
      case Tango_DEV_UCHAR:
        ns.setNumberHelper(new UCharSpectrumHelper(ns));
        return ns;
      case Tango_DEV_SHORT:
        ns.setNumberHelper(new ShortSpectrumHelper(ns));
        return ns;
      case Tango_DEV_USHORT:
        ns.setNumberHelper(new UShortSpectrumHelper(ns));
        return ns;
      case Tango_DEV_DOUBLE:
        ns.setNumberHelper(new DoubleSpectrumHelper(ns));
        return ns;
      case Tango_DEV_FLOAT:
        ns.setNumberHelper(new FloatSpectrumHelper(ns));
        return ns;
      case Tango_DEV_LONG:
        ns.setNumberHelper(new LongSpectrumHelper(ns));
        return ns;
      case Tango_DEV_ULONG:
        ns.setNumberHelper(new ULongSpectrumHelper(ns));
        return ns;
      case Tango_DEV_LONG64:
        ns.setNumberHelper(new Long64SpectrumHelper(ns));
        return ns;
      case Tango_DEV_ULONG64:
        ns.setNumberHelper(new ULong64SpectrumHelper(ns));
        return ns;
      case Tango_DEV_STRING:
        ss = new StringSpectrum();
        return ss;
      case Tango_DEV_BOOLEAN:
        bs = new BooleanSpectrum();
        return bs;
      case Tango_DEV_STATE:
        dss = new DevStateSpectrum();
        return dss;
      case Tango_DEV_ENUM:
            ens = getTangoEnumSpectrum(config);
            if (ens == null) // failed to instantiate correctly Enum Spectrum
            {
                System.out.println("Warning, Tango_DEV_ENUM, AttributeFactory.getTangoEnumSpectrum(" + device.getName() + "/" + name + ") : Failed ");
                return new InvalidAttribute();
            }
            ens.setEnumSpectrumHelper(new EnumSpectrumHelper(ens));
            return ens;
      default:
        System.out.println("Warning, AttributeFactory.getSpectrum(" + device.getName() + "/" + name +
                           ") : Unsupported data type [" + dataType +"]");
        return new InvalidAttribute();

    }
  }

  /**
   * <code>getSpectrum</code> figures out what kind of spectrum is to be
   * created. This is done by looking at AttributeInfo.data_type
   *
   * @param config an <code>AttributeInfo</code> value
   * @return an <code>AAttribute</code> value being instanceof either
   * ShortSpectrum, DoubleSpectrum, or LongSpectrum
   * @throws IllegalArgumentException if the type is unknown.
   */
  private AAttribute getImage(Device device,AttributeInfoEx config) {
    String name = config.name;
    int dataType = config.data_type;
    NumberImage nimg = new NumberImage();

    BooleanImage bi = null;
    StringImage si = null;

    switch (dataType) {
      case Tango_DEV_UCHAR:
        nimg.setNumberHelper(new UCharImageHelper(nimg));
        return nimg;
      case Tango_DEV_SHORT:
        nimg.setNumberHelper(new ShortImageHelper(nimg));
        return nimg;
      case Tango_DEV_USHORT:
        nimg.setNumberHelper(new UShortImageHelper(nimg));
        return nimg;
      case Tango_DEV_DOUBLE:
        nimg.setNumberHelper(new DoubleImageHelper(nimg));
        return nimg;
      case Tango_DEV_FLOAT:
        nimg.setNumberHelper(new FloatImageHelper(nimg));
        return nimg;
      case Tango_DEV_LONG:
        nimg.setNumberHelper(new LongImageHelper(nimg));
        return nimg;
      case Tango_DEV_ULONG:
        nimg.setNumberHelper(new ULongImageHelper(nimg));
        return nimg;
      case Tango_DEV_LONG64:
        nimg.setNumberHelper(new Long64ImageHelper(nimg));
        return nimg;
      case Tango_DEV_ULONG64:
        nimg.setNumberHelper(new ULong64ImageHelper(nimg));
        return nimg;
      case Tango_DEV_BOOLEAN:
        bi = new BooleanImage();
        return bi;
      case Tango_DEV_STRING:
        si = new StringImage();
        return si;
      default:
        System.out.println("Warning, AttributeFactory.getSpectrum(" + device.getName() + "/" + name +
                           ") : Unsupported data type [" + dataType +"]");
        return new InvalidAttribute();
    }
  }

  private void dumpFactory(String msg) {
    if((DeviceFactory.getInstance().getTraceMode() & DeviceFactory.TRACE_ATTFACTORY)!=0) {
      System.out.println("-- AttributeFactory : " + msg + " --");
      for(int i=0;i<attNames.length;i++) {
        System.out.println("  " + i + ":" + attNames[i]);
      }
      System.out.println("-- AttributeFactory --------------------------------------");
    }
  }

  public String getVersion() {
    return "$Id$";
  }

}
