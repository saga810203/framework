package org.jfw.apt.model.web.handlers;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import org.jfw.apt.Utils;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.AbstractMethodGenerater;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestHandler;

public class ValidateParamHandler extends RequestHandler {

	@Override
	public void init() {

	}

	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		List<MethodParamEntry> mpes = this.rmcg.getParams();

		for (int i = 0; i < mpes.size(); ++i) {
			MethodParamEntry mpe = mpes.get(i);
			List<? extends AnnotationMirror> ans = mpe.getRef().getAnnotationMirrors();
			for (AnnotationMirror anm : ans) {
				Object obj = Utils.getReturnValueOnAnnotation("validParamClass", anm);
				Class<ValidParameter> bpcls = Utils.getClass(obj, ValidParameter.class);
				if (bpcls != null) {
					ValidParameter vp;
					try {
						vp = bpcls.newInstance();
					} catch (Exception e) {
						throw new AptException(mpe.getRef(),
								"create Object instance with " + bpcls.getName() + "error:" + e.getMessage());
					}
					vp.build(sb, mpe, rmcg);
				}
			}
		}
	}
	public static abstract class ValidParameter {
		
		public static final String RESULT_NAME_KEY = ValidParameter.class.getName()+".RESULT_NAME";
		protected String name;
		protected String methodName;
		protected String infoEl;
		protected StringBuilder sb;
		protected MethodParamEntry mpe;
		protected AbstractMethodGenerater amg;
		
		
		protected String resultName ;
		
		
		public abstract void doValid();
		public String[] eanbleClassnames(){
			return new String[]{};
		}
		public abstract String annotationClassName();
		
		public void handleInvalid(){
			this.sb.append("throw new IllegalArgumentException(\"invalid parameter:" + this.name + "\");");
		}
		
		public void build(StringBuilder sb, MethodParamEntry mpe, AbstractMethodGenerater amg)
				throws AptException {
			
		
			this.sb = sb;
			this.mpe = mpe;
			this.amg = amg;
			this.name = mpe.getName();
			
			
			String pcn = mpe.getTypeName();
			String[] ecns = this.eanbleClassnames();
			boolean enable = ecns.length==0;
			
			for(String cn :ecns){
				if(cn.equals(pcn)) {
					enable= true;
					break;
				}
			}
			if(!enable){
				String an = this.annotationClassName();
				if(an==null) an = "[TODO:]";
				throw new AptException(mpe.getRef(), an+" nosupported java type");
			}
		//	this.methodName = amg.get
			this.resultName =(String)amg.getAttribute(RESULT_NAME_KEY);
			if(this.resultName==null){
				this.resultName = amg.getTempalteVariableName();
				amg.setAttribute(RESULT_NAME_KEY,this.resultName);
				sb.append(" boolean ").append(this.resultName).append(" = true;");
			}else{
				sb.append(this.resultName).append(" = true;");
			}
			this.doValid();
			sb.append("if(!").append(this.resultName).append("){");
			this.handleInvalid();
			sb.append("}");			
		}
	}
}
