package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.handlers.BuildParamHandler;

public class UploadHandler extends BuildParamHandler.BuildParameter{

	@Override
	public void build(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg) throws AptException {
		sb.append("if(!org.jfw.util.web.fileupload.impl.UploadItemIteratorImpl.isMultipartContent(req)){")
		  .append("throw new RuntimeException(\"invalid request with Multipart\")")
		  .append("}");		
		sb.append("org.jfw.util.web.fileupload.UploadItemIterator ").append(mpe.getName())
		.append(" = org.jfw.util.web.fileupload.impl.UploadItemIteratorImpl.build(req);\r\n");
	}
}
