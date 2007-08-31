/*
 * JSON-RPC-Java - a JSON-RPC to Java Bridge with dynamic invocation
 *
 * $Id: DictionarySerializer.java,v 1.1 2004/04/01 06:51:29 mclark Exp $
 *
 * Copyright Metaparadigm Pte. Ltd. 2004.
 * Michael Clark <michael@metaparadigm.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public (LGPL)
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details: http://www.gnu.org/
 *
 */

package com.metaparadigm.jsonrpc;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Iterator;
import org.json.JSONObject;

class DictionarySerializer extends Serializer
{
    private static Class[] _serializableClasses = new Class[]
	{ Dictionary.class, Hashtable.class };

    private static Class[] _JSONClasses = new Class[]
	{ JSONObject.class };

    public Class[] getSerializableClasses() { return _serializableClasses; }
    public Class[] getJSONClasses() { return _JSONClasses; }


    public ObjectMatch doTryToUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("java_class");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	if(!(java_class.equals("java.util.Dictionary") ||
	     java_class.equals("java.util.Hashtable")))
	    throw new UnmarshallException("not a Dictionary");
	JSONObject jsonmap = jso.getJSONObject("map");
	if(jsonmap == null)
	    throw new UnmarshallException("map missing");
	ObjectMatch m = new ObjectMatch(-1);
	Iterator i = jsonmap.keys();
	String key = null;
	try {
	    while(i.hasNext()) {
		key = (String)i.next();
		m = tryToUnmarshall(null, jsonmap.get(key)).max(m);
	    }
	} catch (UnmarshallException e) {
	    throw new UnmarshallException
		("key " + key + " " + e.getMessage());
	}
	return m;
    }

    public Object doUnmarshall(Class clazz, Object o)
	throws UnmarshallException
    {
	JSONObject jso = (JSONObject)o;
	String java_class = jso.getString("java_class");
	if(java_class == null)
	    throw new UnmarshallException("no type hint");	
	Hashtable ht = null;
	if(java_class.equals("java.util.Dictionary") ||
	   java_class.equals("java.util.Hashtable")) {
	    ht = new Hashtable();
	} else {
	    throw new UnmarshallException("not a Dictionary");
	}
	JSONObject jsonmap = jso.getJSONObject("map");
	if(jsonmap == null)
	    throw new UnmarshallException("map missing");
	Iterator i = jsonmap.keys();
	String key = null;
	try {
	    while(i.hasNext()) {
		key = (String)i.next();
		ht.put(key, unmarshall(null, jsonmap.get(key)));
	    }
	} catch (UnmarshallException e) {
	    throw new UnmarshallException
		("key " + key + " " + e.getMessage());
	}
	return ht;
    }

    public Object doMarshall(Object o)
	throws MarshallException
    {
	Dictionary ht = (Dictionary)o;
	JSONObject obj = new JSONObject();
	JSONObject map = new JSONObject();
	obj.put("java_class", o.getClass().getName());
	obj.put("map", map);
	Object key = null;
	Object val = null;
	try {
	    Enumeration en = ht.keys();
	    while(en.hasMoreElements()) {
		key = en.nextElement();
		val = ht.get(key);
		// only support String keys
		map.put(key.toString(), marshall(val));
	    }
	} catch (MarshallException e) {
	    throw new MarshallException
		("map key " + key + " " + e.getMessage());
	}
	return obj;
    }

}
