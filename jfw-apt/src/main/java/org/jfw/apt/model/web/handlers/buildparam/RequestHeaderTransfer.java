package org.jfw.apt.model.web.handlers.buildparam;



import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestHeaderModel;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;

public interface RequestHeaderTransfer {
void transfer(StringBuilder sb,MethodParamEntry mpe,RequestMappingCodeGenerator rmcg,RequestHeaderModel annotation) throws AptException;
    void transferBeanProperty(StringBuilder sb,MethodParamEntry mpe,RequestMappingCodeGenerator rmcg,RequestHeaderTransfer.FieldRequestParam frp);
    public static class FieldRequestParam{
        private String value;
        private String paramName;
        private String valueClassName;
        private String defaultValue;
        private boolean required;
        private Class<RequestHeaderTransfer> transferClass;
		public Class<RequestHeaderTransfer> getTransferClass() {
			return transferClass;
		}
		public void setTransferClass(Class<RequestHeaderTransfer> transferClass) {
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
