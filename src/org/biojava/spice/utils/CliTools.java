/*
 *                  BioJava development code
 *
 * Copyright for this code is held jointly by the individual
 * authors.  These should be listed in @author doc comments.
 *
 * For more information on the BioJava project and its aims,
 * or to join the biojava-l mailing list, visit the home page
 * at:
 *
 *      http://www.biojava.org/
 *
 */

package org.biojava.spice.utils;

import java.io.*;
import java.util.*;
import java.beans.*;
import java.lang.reflect.*;
import org.biojava.spice.Config.ConfigurationException;

/**
 * Utilities for autoconfiguring javabeans based on command line arguments.
 *
 * @author Thomas Down
 */

public class CliTools {
    private CliTools() {
    }
    
    /**
     * Configure a JavaBean based on a set of command line arguments.  
     * For a command line construct such as "-foo 42", this method will use
     * available <code>BeanInfo</code> (usually obtained by introspection)
     * to find a property named "foo".  The argument will be interpretted
     * according to the type of the "foo" property, then the appropriate
     * mutator method (generally named setFoo) will be called to configure
     * the property on the bean.
     *
     * <p>
     * Currently supported property types are <code>int, double,
     * boolean, String, File, Reader, Writer, InputStream, OutputStream</code>,
     * plus arrays of all the above types.  In the case of arrays, the option
     * may appear multiple times on the command line, otherwise recurrance of
     * the same option is an error.
     * </p>
     *
     * <p>
     * For stream types, the parameter is intepretted as a filename unless it
     * is equal to "-" in which case standard input or standard output are
     * used as appropriate.  Each of the standard streams may only be used
     * one.
     * </p>
     *
     * <p>
     * In the future, this method will probably be extended to handle multiple
     * parameter occurances, and use Annotations to generate more useful help
     * messages when something goes wrong.
     * </p>
     *
     * @return A string array which contains any 'anonymous' arguments (may be empty)
     */
    
    public static String[] configureBean(Object bean, String[] args) 
    		throws ConfigurationException
    {
        BeanInfo bi;
        try {
            bi = Introspector.getBeanInfo(bean.getClass());
        } catch (Exception ex) {
            throw new ConfigurationException(ex,"Couldn't get information for target bean");
        }
        
        Map propertiesByName = new HashMap();
        for (Iterator pi = Arrays.asList(bi.getPropertyDescriptors()).iterator(); pi.hasNext(); ) {
            PropertyDescriptor pd = (PropertyDescriptor) pi.next();
            propertiesByName.put(pd.getName(), pd);
        }
        
        List anonArgs = new ArrayList();
        Map arrayProps = new HashMap();
        Set usedProps = new HashSet();
        
        boolean stdInUsed = false;
        boolean stdOutUsed = false;
        
        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            if (arg.charAt(0) == '-') {
                PropertyDescriptor pd = (PropertyDescriptor) propertiesByName.get(arg.substring(1));
                
                boolean arrayMode = false;
                Object propVal = null;
                Class propType = null;
                
                if (pd == null) {
                    if (arg.startsWith("-no")) {
                        String altPropName = Introspector.decapitalize(arg.substring(3));
                        pd = (PropertyDescriptor) propertiesByName.get(altPropName);
                        if (pd == null) {
                            throw new ConfigurationException("No property named " + arg.substring(1) + " or " + altPropName);
                        }
                        propType = pd.getPropertyType();
                        if (propType == Boolean.TYPE) {
                            propVal = Boolean.FALSE;
                        } else {
                            throw new ConfigurationException("Negatory option " + arg + " does not refer to a boolean property");
                        }
                    } else {
                        throw new ConfigurationException("No property named " + arg.substring(1));
                    }
                } else {
                    propType = pd.getPropertyType();
                    
                    if (propType.isArray()) {
                        arrayMode = true;
                        propType = propType.getComponentType();
                    }
                    
                    if (propType == Integer.TYPE) {
                        try {
                            propVal = new Integer(args[++i]);
                        } catch (Exception ex) {
                            throw new ConfigurationException("Option " + arg + " requires an integer parameter");
                        }
                    } else if (propType == Double.TYPE) {
                        try {
                            propVal = new Double(args[++i]);
                        } catch (Exception ex) {
                            throw new ConfigurationException("Option " + arg + " requires a numerical parameter");
                        }
                    } else if (propType == String.class) {
                        propVal = args[++i];
                    } else if (propType == Boolean.TYPE) {
                        propVal = Boolean.TRUE;
                    } else if (File.class.isAssignableFrom(propType)) {
                        // can't distinguish if the file is for reading or writing
                        // at the moment, so accept it without validation.
                        propVal = new File(args[++i]);
                    } else if (Reader.class.isAssignableFrom(propType)) {
                        String name = args[++i];
                        if ("-".equals(name)) {
                            if (stdInUsed) {
                                throw new ConfigurationException("Can't use standard input more than once");
                            }
                            propVal = new InputStreamReader(System.in);
                            stdInUsed = true;
                        } else {
                            try {
                                propVal = new FileReader(new File(name));
                            } catch (Exception ex) {
                                throw new ConfigurationException("Can't open " + name + " for input");
                            }
                        }
                    } else if (InputStream.class.isAssignableFrom(propType)) {
                        String name = args[++i];
                        if ("-".equals(name)) {
                            if (stdInUsed) {
                                throw new ConfigurationException("Can't use standard input more than once");
                            }
                            propVal = System.in;
                            stdInUsed = true;
                        } else {
                            try {
                                propVal = new FileInputStream(new File(name));
                            } catch (Exception ex) {
                                throw new ConfigurationException("Can't open " + name + " for input");
                            }
                        }
                    } else if (Writer.class.isAssignableFrom(propType)) {
                        String name = args[++i];
                        if ("-".equals(name)) {
                            if (stdOutUsed) {
                                throw new ConfigurationException("Can't use standard output more than once");
                            }
                            propVal = new OutputStreamWriter(System.out);
                            stdOutUsed = true;
                        } else {
                            try {
                                propVal = new FileWriter(new File(name));
                            } catch (Exception ex) {
                                throw new ConfigurationException("Can't open " + name + " for output");
                            }
                        }
                    } else if (OutputStream.class.isAssignableFrom(propType)) {
                        String name = args[++i];
                        if ("-".equals(name)) {
                            if (stdOutUsed) {
                                throw new ConfigurationException("Can't use standard output more than once");
                            }
                            propVal = System.out;
                            stdOutUsed = true;
                        } else {
                            try {
                                propVal = new FileOutputStream(new File(name));
                            } catch (Exception ex) {
                                throw new ConfigurationException("Can't open " + name + " for output");
                            }
                        }
                    } else {
                        System.err.println("Unsupported optionType for " + arg);
                        System.exit(1);
                    }
                }
                    
                if (arrayMode) {
                    List valList = (List) arrayProps.get(pd);
                    if (valList == null) {
                        valList = new ArrayList();
                        arrayProps.put(pd, valList);
                    }
                    valList.add(propVal);
                } else {
                    if (usedProps.contains(pd)) {
                        throw new ConfigurationException("Multiple values supplied for " + pd.getName());
                    }
                    try {
                        pd.getWriteMethod().invoke(bean, new Object[] {propVal});
                    } catch (InvocationTargetException ex) {
                        throw new ConfigurationException(ex.getTargetException(),"Error configuring '" + pd.getName() + "'");
                    } catch (Exception ex) {
                        throw new ConfigurationException(ex,"Error configuring '" + pd.getName() + "'");
                    }
                    usedProps.add(pd);
                }
            } else {
                anonArgs.add(arg);
            }
        }
        
        for (Iterator api = arrayProps.entrySet().iterator(); api.hasNext(); ) {
            Map.Entry me = (Map.Entry) api.next();
            PropertyDescriptor pd = (PropertyDescriptor) me.getKey();
            List vals = (List) me.getValue();
            
            Class compType = pd.getPropertyType().getComponentType();
            Object valArray;
            if (compType.isPrimitive()) {
                if (compType == Integer.TYPE) {
                    valArray = CollectTools.toIntArray(vals);
                } else if (compType == Double.TYPE) {
                    valArray = CollectTools.toDoubleArray(vals);
                } else {
                    throw new ConfigurationException("Arrays of type " + compType.getName() + " are currently unsupported");
                }
            } else {
                valArray = vals.toArray((Object[]) Array.newInstance(compType, vals.size()));
            }
            try {
                pd.getWriteMethod().invoke(bean, new Object[] {valArray});
            } catch (InvocationTargetException ex) {
                throw new ConfigurationException( ex.getTargetException(),"Error configuring '" + pd.getName() + "'");
            } catch (Exception ex) {
                throw new ConfigurationException(ex,"Error configuring '" + pd.getName() + "'");
            }
        }
        
        return (String[]) anonArgs.toArray(new String[anonArgs.size()]);
    }
}