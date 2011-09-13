package edu.columbia.cs.cg.relations;

import java.util.Arrays;

import edu.columbia.cs.cg.document.Document;

public class Entity implements Comparable<Entity> {
	public static final Entity NULL_ENTITY = new Entity("NULL","NULL",0,0,"NULL",null);
	
	private String id;
	private String entityType;
	private int offset;
	private int length;
	private String value;
	private Document document;
	
	public Entity(String id, String entityType, int offset, int length,
			String value, Document doc){
		setId(id);
		setEntityType(entityType);
		setOffset(offset);
		setLength(length);
		setValue(value);
		setDocument(doc);
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getEntityType() {
		return entityType;
	}
	
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}
	
	public int getLength() {
		return length;
	}
	
	public void setLength(int length) {
		this.length = length;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public Document getDocument() {
		return document;
	}
	
	public void setDocument(Document document) {
		this.document = document;
	}
	
	@Override
	public String toString(){
		return entityType + "(" + value + ")";
	}

	@Override
	public int compareTo(Entity o) {
		int startIndex=offset;
		int endIndex=offset+length;
		int otherStartIndex=o.offset;
		int otherEndIndex=o.offset+o.length;
		if(startIndex<otherStartIndex){
			return -1;
		}else if(startIndex>otherStartIndex){
			return 1;
		}

		if(endIndex<otherEndIndex){
			return -1;
		}else if(endIndex>otherEndIndex){
			return 1;
		}

		return 0;
	}
	
	@Override
	public int hashCode(){
		return id.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof Entity){
			return id.equals(((Entity) o).id);
		}
		return false;
	}
	
}
