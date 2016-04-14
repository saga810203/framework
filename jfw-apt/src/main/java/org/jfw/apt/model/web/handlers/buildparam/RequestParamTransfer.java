package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.RequestParamModel;

public interface RequestParamTransfer {
    void transfer(StringBuilder sb,MethodParamEntry mpe,RequestMappingCodeGenerator rmcg,RequestParamModel annotation) throws AptException;
    
    
    void transferBeanProperty(StringBuilder sb,MethodParamEntry mpe,RequestMappingCodeGenerator rmcg,RequestParamTransfer.FieldRequestParam frp);
    
    public static class FieldRequestParam{
        private String value;
        private String paramName;
        private String valueClassName;
        private String defaultValue;
        private boolean required;
        private Class<RequestParamTransfer> transferClass;
		public Class<RequestParamTransfer> getTransferClass() {
			return transferClass;
		}
		public void setTransferClass(Class<RequestParamTransfer> transferClass) {
			this.transferClass = transferClass;
		}
		public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        public String getParamName() {
            return paramName;
        }
        public void setParamName(String paramName) {
            this.paramName = paramName;
        }
        public String getValueClassName() {
            return this.valueClassName;
        }
        public void setValueClassName(String valueClassName) {
            this.valueClassName = valueClassName;
        }
        public String getDefaultValue() {
            return defaultValue;
        }
        public void setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
        }
        public boolean isRequired() {
            return required;
        }
        public void setRequired(boolean required) {
            this.required = required;
        }
    }
}
