
package org.jfw.apt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.core.TypeName;

public class AbstractMethodGenerater {
	protected Map<String,Object> attributes = new HashMap<String,Object>();
	protected String returnType;
	protected String name;
	protected List<MethodParamEntry> params = new ArrayList<MethodParamEntry>();


	protected List<String> throwables =  new ArrayList<String>() ;


	protected ExecutableElement ref;
	protected TypeElement enclosing;
	protected List<String> modifiers = new ArrayList<String>() ;
	
	
	
	
	public void fillMeta(ExecutableElement ref) throws AptException{
		this.ref = ref;
		this.modifiers.clear();
		for(Modifier m:ref.getModifiers()){
			this.modifiers.add(m.toString());
		}				
		this.enclosing = (TypeElement)ref.getEnclosingElement();
		try{
		this.returnType =TypeName.get(ref.getReturnType()).toString();
		}catch(Exception e){
			throw new AptException(ref,"unSupported Method returnType");
		}
		this.name = ref.getSimpleName().toString();

		List<? extends VariableElement> eles = ref.getParameters();
		this.params.clear();
		for(int i = 0 ; i < eles.size() ; ++i){
			try{
			this.params.add(MethodParamEntry.build(eles.get(i)));
			}catch(Exception e){
				throw new AptException(eles.get(i),"unSupported Method parameterType");
			}
		}
		
	    List<? extends TypeMirror> ths =	ref.getThrownTypes();
	    this.throwables.clear();
	    for(int i = 0 ; i < ths.size() ; ++i){
	    	this.throwables.add(TypeName.get(ths.get(i)).toString());
	    }
	}
	
	public void setAttribute(String key,Object obj){
		this.attributes.put(key,obj);
	}
	public Object getAttribute(String key){
		return attributes.get(key);
	}
	public List<MethodParamEntry> getParams() {
		return params;
	}
	
	public ExecutableElement getRef() {
		return ref;
	}

	private static final String TVN = AbstractMethodGenerater.class.getName()+"_TVN";
	public String getTempalteVariableName(){
		Object obj = this.attributes.get(TVN);
		int i = obj==null?0:((Integer)obj).intValue();		
		++i;
		this.attributes.put(TVN,i);
		return "tmp"+i;		
	}
	
	public String getReturnType() {
		return returnType;
	}

	public String getName() {
		return name;
	}
}
