package edu.columbia.cs.cg.document.loaders.impl.ace2005;
import java.util.*;

public class ACEEMention implements Comparable {

	ACEEntity m_entity;

	String m_strID;
	String m_strType;

	// full span
	int m_nBeg;
	int m_nLen;

	// head span
	int m_nHBeg;
	int m_nHLen;

	// mention attributes
	HashMap m_mapAtt;

	public ACEEMention()
	{
		m_mapAtt = new HashMap();
	}

	public String getAttribute(String strKey)
	{
		return (String) m_mapAtt.get(strKey);
	}

	public void setAttribute(String strKey, String strVal)
	{
		m_mapAtt.put(strKey, strVal);
	}

	public boolean includes(ACEEMention anno)
	{
		return m_nBeg <= anno.m_nBeg && 
		m_nBeg + m_nLen >= anno.m_nBeg + anno.m_nLen;
	}

	public String getBegin()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<");
		sb.append(m_strID);

		Set entries = m_mapAtt.entrySet();
		Iterator eit = entries.iterator();
		while (eit.hasNext()) {
			Map.Entry entry = (Map.Entry) eit.next();
			String strKey = (String) entry.getKey();
			String strValue = (String) entry.getValue();
			sb.append(" " + strKey + "=" + strValue);
		}
		sb.append(">");

		return sb.toString();
	}

	public String getEnd()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("</");
		sb.append(m_strID);
		sb.append(">");

		return sb.toString();
	}

	public int compareTo(Object o)
	{
		ACEEMention em = (ACEEMention) o;

		if (m_nHBeg < em.m_nHBeg)
			return -1;

		if (m_nHBeg == em.m_nHBeg)
			if (m_nHLen < em.m_nHLen)
				return -1;
			else if (m_nHLen == em.m_nHLen)
				return 0;

		return 1;
	}

	public String getId(){
		return m_strID;
	}
	
	public String getType(){
		return m_strType;
	}
	
	public int getExtOffset(){
		return m_nBeg;
	}
	
	public int getExtLength(){
		return m_nLen;
	}
	
	public int getHeadOffset(){
		return m_nHBeg;
	}
	
	public int getHeadLength(){
		return m_nHLen;
	}
}
