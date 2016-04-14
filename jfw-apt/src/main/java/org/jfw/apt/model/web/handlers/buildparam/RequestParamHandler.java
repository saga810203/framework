package org.jfw.apt.model.web.handlers.buildparam;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.web.RequestParam;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.core.ClassName;
import org.jfw.apt.model.core.TypeName;
import org.jfw.apt.model.web.Field;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.RequestParamModel;
import org.jfw.apt.model.web.handlers.BuildParamHandler;

public class RequestParamHandler extends BuildParamHandler.BuildParameter {

	@Override
	public void build(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg) throws AptException {
		RequestParam rp = mpe.getRef().getAnnotation(RequestParam.class);
		RequestParamModel rpm;
		if (rp == null) {
			rpm = new RequestParamModel();
		} else {
			rpm = RequestParamModel.build(rp,mpe.getName().trim());
		}

		RequestParamTransfer rpt = null;
		Class<RequestParamTransfer> cls = null;
		try {
			cls = TransferFactory.getRequestParamTransfer(mpe.getTypeName());
			if (cls != null) {
				rpt = cls.newInstance();
			}
		} catch (Exception e) {
			throw new AptException(mpe.getRef(), "create object instance error [" + cls.toString() + "]");
		}
		if (null == rpt) {
			rpt = new DefaultTransfer();
		}
		rpt.transfer(sb, mpe, rmcg, rpm);
	}

	private static class DefaultTransfer implements RequestParamTransfer {
		private RequestParamTransfer.FieldRequestParam frp[];
		private RequestParamModel annotation;
		private MethodParamEntry mpe;
		private ClassName res;
		private Map<String, List<TypeName>> properties;

		private void initTargetType(MethodParamEntry mpe) throws AptException {
			this.mpe = mpe;
			TypeName tn = annotation.getRealClass();
			if(TypeName.OBJECT.equals(tn))tn = TypeName.get(this.mpe.getRef().asType());
			
			if (!tn.getClass().equals(ClassName.class)) {
				throw new AptException(this.mpe.getRef(), "can't create instance or invalid Class:" + tn.toString());
			}
			res = (ClassName) tn;
			if (!res.canInstance())
				throw new AptException(this.mpe.getRef(), "can't create instance or invalid Class:" + tn.toString());
			this.properties = res.getAllSetter();
		}

		private List<RequestParamTransfer.FieldRequestParam> getFieldRequstParamWithDeclared() throws AptException {
			List<RequestParamTransfer.FieldRequestParam> list = new LinkedList<RequestParamTransfer.FieldRequestParam>();
			for (Field field : this.annotation.getFields()) {
				RequestParamTransfer.FieldRequestParam rfp = new RequestParamTransfer.FieldRequestParam();
				rfp.setDefaultValue(Utils.emptyToNull(field.getDefaultValue()));
				rfp.setValue(Utils.emptyToNull(field.getName()));
				if (rfp.getValue() == null)
					throw new AptException(this.mpe.getRef(), "@FieldParam'value not null or emtry string");

				rfp.setParamName(Utils.emptyToNull(field.getParamName()));
				if (rfp.getParamName() == null)
					rfp.setParamName(rfp.getValue());

				rfp.setRequired(field.isRequired());
				rfp.setValueClassName(field.getClassName());
				List<TypeName> plist = this.properties.get(rfp.getValue());
				if (plist == null)
					throw new AptException(this.mpe.getRef(), "@FieldParam'value not in Bean property");
				if (plist.size() > 1) {
					rfp.setTransferClass(TransferFactory.getRequestParamTransfer(rfp.getValueClassName()));
					if (rfp.getTransferClass() == null)
						throw new AptException(mpe.getRef(),
								"@FieldParam'valueClassName not found RequestParamTransfer");
					boolean found = false;
					for (TypeName tn : plist) {
						if (tn.toString().equals(rfp.getValueClassName())) {
							found = true;
							break;
						}
					}
					if (!found) {
						throw new AptException(mpe.getRef(), "@FieldParam'valueClassName not found in Bean property");
					}
				} else {
					rfp.setTransferClass(TransferFactory.getRequestParamTransfer(list.get(0).toString()));
					if (rfp.getTransferClass() == null)
						throw new AptException(mpe.getRef(), "@FieldParam'value not found RequestParamTransfer");
				}
				if (this.annotation.getParamName().length()>0) {
					rfp.setParamName(this.annotation.getParamName() + "_" + rfp.getParamName());
				}
				list.add(rfp);
			}
			return list;
		}

		private List<RequestParamTransfer.FieldRequestParam> getFieldRequstParamWithDefault() throws AptException {
			List<RequestParamTransfer.FieldRequestParam> list = new LinkedList<RequestParamTransfer.FieldRequestParam>();
			String[] exincludes = annotation.getExcludeFields();
			if (exincludes != null && exincludes.length > 0) {
				for (String s : exincludes) {
					this.properties.remove(s);
				}
			}
			for (Map.Entry<String, List<TypeName>> en : this.properties.entrySet()) {
				List<TypeName> li = en.getValue();
				Class<RequestParamTransfer> clRpt = null;
				String valueClassName = null;
				if (li.size() == 1) {
					clRpt = TransferFactory.getRequestParamTransfer(li.get(0).toString());
					valueClassName = li.get(0).toString();
				} else {
					for (TypeName ttn : li) {
						if (clRpt == null) {
							clRpt = TransferFactory.getRequestParamTransfer(ttn.toString());
							valueClassName = ttn.toString();
							// if(clRpt !=null) validType = ttn;
						} else {
							if (null != TransferFactory.getRequestParamTransfer(ttn.toString())) {
								throw new AptException(mpe.getRef(), "parameter Bean property can set mulit Value");
							}
						}
					}
				}
				if (clRpt == null)
					continue;
				RequestParamTransfer.FieldRequestParam rfp = new RequestParamTransfer.FieldRequestParam();
				rfp.setValue(en.getKey());
				if (this.annotation.getParamName().length()>0) {
					rfp.setParamName(annotation.getParamName()+ "_" + en.getKey());
				} else {
					rfp.setParamName(en.getKey());
				}
				rfp.setDefaultValue("");
				rfp.setRequired(false);
				rfp.setTransferClass(clRpt);
				rfp.setValueClassName(valueClassName);
				list.add(rfp);
			}
			return list;
		}

		private void buildField() throws AptException {
			List<RequestParamTransfer.FieldRequestParam> list ;
			Field[] fields = this.annotation.getFields();
			if (fields.length > 0) {
				list = this.getFieldRequstParamWithDeclared();
			} else {
				
				list = this.getFieldRequstParamWithDefault();
			}
			this.frp = list.toArray(new RequestParamTransfer.FieldRequestParam[list.size()]);
		}

		@Override
		public void transfer(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg,
				RequestParamModel annotation) throws AptException {
			this.annotation = annotation;
			this.initTargetType(mpe);
			this.buildField();

			sb.append(res.toString()).append(" ").append(mpe.getName()).append(" = new ").append(res.toString())
					.append("();\r\n");
			for (RequestParamTransfer.FieldRequestParam rptFrp : this.frp) {
				RequestParamTransfer rptf;
				try {
					rptf = rptFrp.getTransferClass().newInstance();
				} catch (Exception e) {
					throw new AptException(mpe.getRef(),
							"create TransferClass instance error:" + (null == e.getMessage() ? "" : e.getMessage()));
				}
				rptf.transferBeanProperty(sb, mpe, rmcg, rptFrp);
			}
		}

		@Override
		public void transferBeanProperty(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg,
				FieldRequestParam frp) {
			throw new UnsupportedOperationException();
		}

	}
}
