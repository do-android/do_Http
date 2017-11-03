package doext.define;

import org.apache.http.protocol.HTTP;

import core.object.DoMultitonModule;
import core.object.DoProperty;
import core.object.DoProperty.PropertyDataType;

public abstract class do_Http_MAbstract extends DoMultitonModule {

	protected do_Http_MAbstract() throws Exception {
		super();
	}

	/**
	 * 初始化
	 */
	@Override
	public void onInit() throws Exception {
		super.onInit();
		//注册属性
		this.registProperty(new DoProperty("body", PropertyDataType.String, "", false));
		this.registProperty(new DoProperty("contentType", PropertyDataType.String, "text/html", false));
		this.registProperty(new DoProperty("method", PropertyDataType.String, "get", false));
		this.registProperty(new DoProperty("timeout", PropertyDataType.Number, "5000", false));
		this.registProperty(new DoProperty("url", PropertyDataType.String, "", false));
		this.registProperty(new DoProperty("responseEncoding", PropertyDataType.String, HTTP.UTF_8, false));
	}
}