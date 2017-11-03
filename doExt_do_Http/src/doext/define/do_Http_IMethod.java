package doext.define;

import org.json.JSONObject;

import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;

/**
 * 声明自定义扩展组件方法
 */
public interface do_Http_IMethod {
	
	void request(JSONObject _dictParas,DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception ;
	
	void upload(JSONObject _dictParas,DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception ;
	
	void download(JSONObject _dictParas,DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception ;
	
	void setRequestHeader(JSONObject _dictParas,DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception ;
	
	void getResponseHeader(JSONObject _dictParas,DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception ;
	
	void form(JSONObject _dictParas,DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception ;
	
	void setRedirect(JSONObject _dictParas,DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception ;

	void stopDownload(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception;

	void download1(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception;
	
}