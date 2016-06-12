package org.jfw.apt.model.web.handlers.validparam;

import org.jfw.apt.annotation.web.validparam.MinValue;
import org.jfw.apt.model.web.handlers.ValidateParamHandler;

public class MinValueHandler extends ValidateParamHandler.ValidParameter {


	@Override
	public void doValid() {
		String val = this.mpe.getRef().getAnnotation(MinValue.class).value();
		if(val!=null && val.trim().length() == 0){			
			sb.append(this.resultName).append(" = ").append(this.mpe.getName()).append(" >=").append(val).append(";"); 
		}
	}
	


	@Override
	public String annotationClassName() {
		return "@MinValue";
	}

}
