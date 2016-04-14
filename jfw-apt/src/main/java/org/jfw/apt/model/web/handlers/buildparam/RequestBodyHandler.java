package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.web.RequestBody;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.handlers.BuildParamHandler;

public class RequestBodyHandler extends BuildParamHandler.BuildParameter {
	@Override
	public void build(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg) throws AptException {
		RequestBody rb = mpe.getRef().getAnnotation(RequestBody.class);
		if (Utils.isPrimitive(mpe.getTypeName()))
			throw new AptException(mpe.getRef(), "@RequestBody not with on primitive");
		if (rb == null)
			return;
		String tn = Utils.emptyToNull(rb.targetTypeName());
		if (tn == null) {
			tn = mpe.getTypeName();
		}
		boolean parameterized = tn.indexOf("<") >= 0;
		sb.append(mpe.getTypeName()).append(" ").append(mpe.getName()).append(" =  null;\r\n");

		String localName = rmcg.getTempalteVariableName();
		sb.append("java.io.InputStream ").append(localName).append(" = req.getInputStream();\r\n").append("try{\r\n").append(mpe.getName())
				.append(" = ");
		if (!parameterized) {
			sb.append("org.jfw.util.json.JsonService.fromJson(new java.io.InputStreamReader(").append(localName)
					.append(", org.jfw.util.ConstData.UTF8),").append(tn).append(".class);");
		} else {
			sb.append("org.jfw.util.json.JsonService.<").append(tn).append(">fromJson(new java.io.InputStreamReader(")
					.append(localName)
					.append(", org.jfw.util.ConstData.UTF8),new new org.jfw.util.reflect.TypeReference<").append(tn)
					.append(">(){}.getType() );");
		}
		sb.append("}finally{\r\n").append(localName).append(".close();}");

	}

	// @Override
	// public void build(StringBuilder sb, int index, Type type,
	// ControllerMethodCodeGenerator cmcg, Object annotation) {
	// Utils.writeNameOfType(type, sb);
	// sb.append(" param").append(index).append(";");
	// String localName = Utils.getLocalVarName();
	// sb.append(localName).append(" = req.getInputStream();\r\n")
	// .append("try{\r\n").append("param").append(index).append(" = ");
	// if(type instanceof Class){
	// sb.append("org.jfw.util.json.JsonService.fromJson(new
	// java.io.InputStreamReader(").append(localName)
	// .append(", org.jfw.util.ConstData.UTF8),");
	// Utils.writeNameOfType(type, sb);
	// sb.append(".class);");
	// }else{
	// sb.append("org.jfw.util.json.JsonService.<");
	// Utils.writeNameOfType(type, sb);
	// sb.append(">fromJson(new java.io.InputStreamReader(").append(localName)
	// .append(", org.jfw.util.ConstData.UTF8),new new
	// org.jfw.util.reflect.TypeReference<");
	// Utils.writeNameOfType(type, sb);
	// sb.append(">(){}.getType() );");
	// }
	// sb.append("}finally{\r\n").append(localName).append(".close();}");
	// }

}
