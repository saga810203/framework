package org.jfw.apt.model.web.handlers.viewHandler;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.web.handlers.ViewHandler;

public class JsonHandler  extends ViewHandler.ViewHandlerImpl {
	@Override
	public void init(StringBuilder sb) throws AptException {
		//default json view
//		JSON json = this.viewHandler.getRmcg().getRef().getAnnotation(JSON.class);
//		if(null==json) throw new AptException(this.viewHandler.getRmcg().getRef(),"invalid @JSON:nofunod @JSON");
		this.viewHandler.getRmcg().readOut(sb);
	}
	@Override
	public void handlerFail(StringBuilder sb) throws AptException {
		ViewUtils.printJSONException(this.viewHandler,sb);		
	}

	@Override
	public void handlerSuccess(StringBuilder sb) throws AptException {
		boolean hasResult = !"void".equals(this.getViewHandler().getRmcg().getReturnType());
		if(hasResult){
			ViewUtils.printJSONWithValue(this.viewHandler,sb);
		}else{
			ViewUtils.printJSONSuccess(this.viewHandler,sb);
		}	
	}

}
