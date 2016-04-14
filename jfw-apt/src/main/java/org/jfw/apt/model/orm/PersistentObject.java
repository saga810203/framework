package org.jfw.apt.model.orm;

import static javax.lang.model.element.NestingKind.MEMBER;
import static javax.lang.model.element.NestingKind.TOP_LEVEL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.Table;
import org.jfw.apt.annotation.orm.Unique;
import org.jfw.apt.exception.AptException;

public class PersistentObject {

	protected TypeElement ref;
	protected String javaName;
	protected PersistentObjectKind kind;
	protected String fromSentence;


	protected String comment;
	protected PersistentObject parent;

	protected String parentTableAlias;
	
	protected List<UniqueConstraint> uniques= new ArrayList<UniqueConstraint>();
	protected UniqueConstraint primaryKey;



	protected List<Column> columns = new ArrayList<Column>();
	
	
	
	
	//only in kind = TABLE
	protected Table table;

	protected PersistentObject() {
	}

	public static PersistentObject build(TypeElement ref, org.jfw.apt.annotation.orm.VirtualTable an)
			throws AptException {
		PersistentObject po = new PersistentObject();
		po.ref = ref;
		po.kind = PersistentObjectKind.VIRTUAL_TABLE;
		List<? extends TypeParameterElement> list = ref.getTypeParameters();
		if (list != null && list.size() > 0)
			throw new AptException(ref, "invalid PersistentObject");
		if (ref.getEnclosingElement().getKind() != ElementKind.PACKAGE)
			throw new AptException(ref, "invalid PersistentObject");
		po.javaName = ref.getQualifiedName().toString();
		po.fromSentence = null;
		po.comment = an.value();
		po.fillColumns();
		return po;
	}

	public static PersistentObject build(TypeElement ref, org.jfw.apt.annotation.orm.Table an) throws AptException {
		PersistentObject po = new PersistentObject();
		po.ref = ref;
		po.kind = PersistentObjectKind.TABLE;
		List<? extends TypeParameterElement> list = ref.getTypeParameters();
		if (list != null && list.size() > 0)
			throw new AptException(ref, "invalid PersistentObject");
		if (ref.getEnclosingElement().getKind() != ElementKind.PACKAGE)
			throw new AptException(ref, "invalid PersistentObject");
		po.javaName = ref.getQualifiedName().toString();

		
		String tableName =null;
		int index  = po.javaName.lastIndexOf(".");
		if(index >= 0){
			tableName = po.javaName.substring(index+1);
		}else{
			tableName = po.javaName;
		}
		Utils.checkPersistentObjectName(tableName, ref);
		po.fromSentence = Utils.javaNameConverToDbName(tableName);
		po.comment = an.value();
		po.fillColumns();
		po.table = an;
		return po;
	}

	public static PersistentObject build(TypeElement ref, org.jfw.apt.annotation.orm.ExtendTable an)
			throws AptException {
		PersistentObject po = new PersistentObject();
		po.ref = ref;
		po.kind = PersistentObjectKind.EXTEND_TABLE;
		List<? extends TypeParameterElement> list = ref.getTypeParameters();
		if (list != null && list.size() > 0)
			throw new AptException(ref, "invalid PersistentObject");
		if (ref.getEnclosingElement().getKind() != ElementKind.PACKAGE)
			throw new AptException(ref, "invalid PersistentObject");
		po.javaName = ref.getQualifiedName().toString();

		po.comment = an.value();
		po.fillCalcColumns();
		return po;
	}

	public static PersistentObject build(TypeElement ref, org.jfw.apt.annotation.orm.View an) throws AptException {
		PersistentObject po = new PersistentObject();
		po.ref = ref;
		po.kind = PersistentObjectKind.VIEW;
		List<? extends TypeParameterElement> list = ref.getTypeParameters();
		if (list != null && list.size() > 0)
			throw new AptException(ref, "invalid PersistentObject");
		if (ref.getEnclosingElement().getKind() != ElementKind.PACKAGE)
			throw new AptException(ref, "invalid PersistentObject");
		po.javaName = ref.getQualifiedName().toString();
		po.fromSentence = an.fromSentence();
		po.comment = an.value();
		po.fillCalcColumns();
		return po;
	}

	public static PersistentObject build(TypeElement ref, org.jfw.apt.annotation.orm.ExtendView an)
			throws AptException {
		PersistentObject po = new PersistentObject();
		po.ref = ref;
		po.kind = PersistentObjectKind.EXTEND_VIEW;
		List<? extends TypeParameterElement> list = ref.getTypeParameters();
		if (list != null && list.size() > 0)
			throw new AptException(ref, "invalid PersistentObject");
		if (ref.getEnclosingElement().getKind() != ElementKind.PACKAGE)
			throw new AptException(ref, "invalid PersistentObject");
		po.javaName = ref.getQualifiedName().toString();
		po.fromSentence = an.fromSentence();
		po.parentTableAlias = an.tableAlias();
		po.comment = an.value();
		po.fillCalcColumns();
		return po;
	}
	
	private void initUniques() throws AptException{
		List<String> list = new ArrayList<String>();
		List<UniqueConstraint> uns = new ArrayList<UniqueConstraint>();
		
		Unique un = this.table.primaryKey();
		if(Utils.emptyToNull(un.name())!= null) throw new AptException(ref,"@Table'primayKey'name must be empty string");
		if(un.value().length!=0){
			this.primaryKey = UniqueConstraint.build(this, un,true);
		} 
		for(Unique unique:table.uniques()){
			UniqueConstraint uc = UniqueConstraint.build(this,unique,false);
					
			if(list.contains(uc.getName())){
				throw new AptException(this.ref,"@Table exists many same name("+uc.getName()+") @Unique");
			}
			list.add(uc.getName());
			uns.add(uc);			
		}
		this.uniques.clear();
		this.uniques.addAll(uns);		
	}

	

	public List<UniqueConstraint> getUniques() {
		return uniques;
	}

	private void fillColumns() throws AptException {
		List<? extends Element> eles = ref.getEnclosedElements();
		for (Element el : eles) {

			if (el.getKind() != ElementKind.FIELD)
				continue;
			org.jfw.apt.annotation.orm.Column col = el.getAnnotation(org.jfw.apt.annotation.orm.Column.class);
			if (null != col){
				Column mCol = Column.build(col, el);
				this.columns.add(mCol);	
			}
			org.jfw.apt.annotation.orm.CalcColumn calcCol = el.getAnnotation(org.jfw.apt.annotation.orm.CalcColumn.class);
			if (null != calcCol){
				throw new AptException(el,"this class can't include field with @CalcColumn");
			}
		}
	}

	private void fillCalcColumns() throws AptException {
		List<? extends Element> eles = ref.getEnclosedElements();
		for (Element el : eles) {

			if (el.getKind() != ElementKind.FIELD)
				continue;
			org.jfw.apt.annotation.orm.CalcColumn col = el.getAnnotation(org.jfw.apt.annotation.orm.CalcColumn.class);
			if (null != col){
				Column mCol = CalcColumn.build(col, el);
				this.columns.add(mCol);	
			}
			org.jfw.apt.annotation.orm.Column calcCol = el.getAnnotation(org.jfw.apt.annotation.orm.Column.class);
			if (null != calcCol){
				throw new AptException(el,"this class can't include field with @Column");
			}
		}
	}
	

	public void init(OrmDefine root) throws AptException {
		if (kind == PersistentObjectKind.VIRTUAL_TABLE) {
			this.parent = root.getSupperPersistentObject(ref, PersistentObjectKind.VIRTUAL_TABLE);
		} else if (kind == PersistentObjectKind.TABLE) {
			this.parent = root.getSupperPersistentObject(ref, PersistentObjectKind.VIRTUAL_TABLE,
					PersistentObjectKind.TABLE);
			this.initUniques();
		} else if (kind == PersistentObjectKind.EXTEND_TABLE) {
			this.parent = root.getSupperPersistentObject(ref,PersistentObjectKind.TABLE);
			if(this.parent==null) throw new AptException(this.ref,"Class with @ExtendTable must be extend Class with @Table");
			this.fromSentence = this.parent.fromSentence;
		} else if (kind == PersistentObjectKind.VIEW) {
			this.parent = root.getSupperPersistentObject(ref, PersistentObjectKind.VIEW);
		} else {
			this.parent = root.getSupperPersistentObject(ref, PersistentObjectKind.TABLE);
			if(this.parent==null) throw new AptException(this.ref,"Class with @ExtendView must be extend Class with @Table");
		}
	}

	public List<Column> getAllColumn() {
		if (this.parent == null)
			return Collections.unmodifiableList(this.columns);

		List<Column> result = new ArrayList<Column>();
		result.addAll(parent.getAllColumn());
		result.addAll(this.columns);
		return result;
	}
	
	public String getQueryFields()
	{
		StringBuilder sb = new StringBuilder();
		boolean hascomma = false;
		if(this.parent !=null){
			List<Column> list = this.parent.getAllColumn();
			for(int i = 0 ; i < list.size() ; ++i){
				Column col = list.get(i);
				if(!col.isInQuery()) continue;
				if(!hascomma){
					hascomma = true;
				}else{
					sb.append(",");
				}
				if(this.kind == PersistentObjectKind.EXTEND_VIEW){
					sb.append(this.parentTableAlias.trim()).append(".").append(col.getDbName());
				}else{
					sb.append(col.getDbName());
				}
			}
		}
		for(int i = 0 ; i < this.columns.size() ; ++i){
			Column col = this.columns.get(i);
			if(!col.isInQuery()) continue;
			if(!hascomma){
				hascomma = true;
			}else{
				sb.append(",");
			}
			sb.append(col.getDbName());
		}
		return sb.toString();
	}

	public UniqueConstraint getUniqueConstraint(String name){
		for(UniqueConstraint uc : this.uniques){
			if(uc.getName().equals(name)) return uc;
		}
		return null;
	}
	

	public TypeElement getRef() {
		return ref;
	}

	public PersistentObjectKind getKind() {
		return kind;
	}

	public String getJavaName() {
		return javaName;
	}
	public void warnMessage(Messager messager){
		StringBuilder sb = new StringBuilder();
		sb.append("fromSentence:").append(null==this.fromSentence?"":this.fromSentence).append("\r\n");
		sb.append("JAVANAME:").append(this.getJavaName()).append("\r\n");	
		if(null!=this.parent){
			sb.append("PARENT:").append(this.parent.getJavaName()).append("\r\n");
		}
		sb.append("Fields:").append(this.getQueryFields()).append("\r\n");
		if(this.fromSentence!=null && fromSentence.trim().length()>0)sb.append("FromSentence:").append(this.fromSentence).append("\r\n");
		messager.printMessage(Kind.WARNING,sb.toString(), this.ref);
		for(Column col:this.columns){
			col.warnMessage(messager);
		}
	}
	public String getFromSentence() {
		return fromSentence;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (!(obj instanceof PersistentObject))
			return false;
		return this.javaName.equals(((PersistentObject) obj).javaName);
	}
	public UniqueConstraint getPrimaryKey() {
		return this.primaryKey;
	}

	public static String getClassNameWithField(Element ele, TypeMirror tm) throws AptException {
		if (tm == null)
			tm = ele.asType();
		switch (tm.getKind()) {
		case BOOLEAN:
			return "boolean";
		case BYTE:
			return "byte";
		case SHORT:
			return "short";
		case INT:
			return "int";
		case LONG:
			return "long";
		case CHAR:
			return "char";
		case FLOAT:
			return "float";
		case DOUBLE:
			return "double";
		case DECLARED:
			DeclaredType dt = (DeclaredType) tm;
			List<? extends TypeMirror> list = dt.getTypeArguments();
			if (list != null && list.size() > 0)
				throw new AptException(ele, "invalid field type");
			TypeElement te = (TypeElement) dt.asElement();

			List<String> names = new ArrayList<>();
			for (Element e = te; e.getKind().isClass() || e.getKind().isInterface(); e = e.getEnclosingElement()) {
				if (!(te.getNestingKind() == TOP_LEVEL || te.getNestingKind() == MEMBER))
					throw new AptException(ele, "invalid field type");
				names.add(e.getSimpleName().toString());
			}
			Element type = te;
			while (type.getKind() != ElementKind.PACKAGE) {
				type = type.getEnclosingElement();
			}
			names.add(((PackageElement) type).getQualifiedName().toString());
			Collections.reverse(names);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < names.size(); ++i) {
				String name = names.get(i);
				if (null != name && name.trim().length() > 0) {
					if (sb.length() > 0) {
						sb.append(".");
					}
					sb.append(name.trim());
				}

			}
			return sb.toString();
		case ARRAY:
			return getClassNameWithField(ele, ((ArrayType) tm).getComponentType()) + "[]";
		default:
			throw new AptException(ele, "invalid field type");
		}
	}
	public PersistentObject getParent() {
		return parent;
	}

}
