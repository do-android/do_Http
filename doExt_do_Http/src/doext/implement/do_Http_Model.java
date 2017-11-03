package doext.implement;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.CharArrayBuffer;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoJsonHelper;
import core.helper.DoTextHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoEventCenter;
import core.object.DoInvokeResult;
import deviceone.org.apache.http.entity.ContentType;
import deviceone.org.apache.http.entity.mime.HttpMultipartMode;
import deviceone.org.apache.http.entity.mime.MultipartEntityBuilder;
import deviceone.org.apache.http.entity.mime.content.StringBody;
import deviceone.org.apache.http.util.Args;
import doext.define.do_Http_IMethod;
import doext.define.do_Http_MAbstract;
import doext.http.afinal.net.tsz.afinal.FinalHttp;
import doext.http.afinal.net.tsz.afinal.http.AjaxCallBack;
import doext.http.afinal.net.tsz.afinal.http.HttpHandler;
import doext.utils.DoCustomFileBody;
import doext.utils.DoCustomFileBody.FileFormListener;
import doext.utils.FileUploadUtil;
import doext.utils.FileUploadUtil.FileUploadListener;

/**
 * 自定义扩展MM组件Model实现，继承Do_Http_MAbstract抽象类，并实现Do_Http_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.model.getUniqueKey());
 */
public class do_Http_Model extends do_Http_MAbstract implements do_Http_IMethod {

	private Map<String, Object> requestHeaders;
	private Map<String, Object> responseHeaders;
	private Map<String, HttpHandler<File>> downloadTasks;

	private boolean isUpdateHeader;
	private boolean isSetRedirect = true;

	public do_Http_Model() throws Exception {
		super();
		requestHeaders = new HashMap<String, Object>();
		responseHeaders = new HashMap<String, Object>();
		downloadTasks = new HashMap<String, HttpHandler<File>>();
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("request".equals(_methodName)) {
			request(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("upload".equals(_methodName)) {
			upload(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("download".equals(_methodName)) {
			download(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("download1".equals(_methodName)) {
			download1(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("stopDownload".equals(_methodName)) {
			stopDownload(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("setRequestHeader".equals(_methodName)) {
			setRequestHeader(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("getResponseHeader".equals(_methodName)) {
			getResponseHeader(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("form".equals(_methodName)) {
			form(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("setRedirect".equals(_methodName)) {
			setRedirect(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.model.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {

		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 请求；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void request(JSONObject _dictParas, DoIScriptEngine _scriptEngine, final DoInvokeResult _invokeResult) throws Exception {
		if (!isNetworkAvailable()) {
			fireFail(408, "网络离线，没有可用网络！");
			fireResult(408, "网络离线，没有可用网络！");
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String content = doRequest();
					if (content != null) {
						_invokeResult.setResultText(content);
						fireEvent("success", _invokeResult);
					}
				} catch (SocketTimeoutException e) {
					fireFail(408, "请求超时，" + e.getMessage());
					fireResult(408, "请求超时，" + e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
					fireFail(400, "请求失败，" + e.getMessage());
					fireResult(400, "请求失败，" + e.getMessage());
					DoServiceContainer.getLogEngine().writeError("Http Error!" + e.getMessage(), e);
				}
			}
		}).start();
	}

	private boolean isNetworkAvailable() {
		try {
			Context ctx = DoServiceContainer.getPageViewFactory().getAppContext();
			ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			return (info != null && info.isConnected());
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private String getMethodValue(String method) throws Exception {
		if ("".equals(method)) {
			method = getProperty("method").getDefaultValue();
		}
		return method;
	}

	private String doRequest() throws Exception {
		String method = getMethodValue(getPropertyValue("method"));
		if (null == method || "".equals(method)) {
			throw new RuntimeException("请求类型方式失败，method：" + method);
		}
		String url = getPropertyValue("url");
		if (null == url || "".equals(url)) {
			throw new RuntimeException("请求地址错误，url：" + url);
		}
		int timeout = DoTextHelper.strToInt(getPropertyValue("timeout"), 5000);
		if ("post".equalsIgnoreCase(method)) {
			return doPost(url, timeout, "post");
		} else if ("put".equalsIgnoreCase(method)) {
			return doPost(url, timeout, "put");
		} else if ("patch".equalsIgnoreCase(method)) {
			return doPost(url, timeout, "patch");
		} else if ("get".equalsIgnoreCase(method)) {
			return doGet(url, timeout);
		} else if ("delete".equalsIgnoreCase(method)) {
			return doDelete(url, timeout);
		}

		throw new RuntimeException("请求类型方式失败，method：" + method);
	}

	public HttpClient getHttpClient(int timeOut) throws Exception {
		HttpClient httpClient = null;
		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		trustStore.load(null, null);
		SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

		HttpParams params = new BasicHttpParams();
		params.setParameter("http.protocol.handle-redirects", isSetRedirect); // 默认不让重定向
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);

		ConnManagerParams.setTimeout(params, timeOut);
		HttpConnectionParams.setConnectionTimeout(params, timeOut);
		HttpConnectionParams.setSoTimeout(params, timeOut);
		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schReg.register(new Scheme("https", sf, 443));
		httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, schReg), params);
		return httpClient;
	}

	private String getResponseContent(HttpResponse response) throws ParseException, IOException {
		HttpEntity entity = response.getEntity();
		if (null == entity) {
			return "";
		}
		String responseEncoding = null;
		try {
			responseEncoding = this.getPropertyValue("responseEncoding");
			if (TextUtils.isEmpty(responseEncoding)) {
				responseEncoding = HTTP.UTF_8;
			}
		} catch (Exception e) {

		}
		return toString(entity, !TextUtils.isEmpty(responseEncoding) ? Charset.forName(responseEncoding) : null);
	}

	private String toString(final HttpEntity entity, Charset charset) throws IOException, ParseException {
		Args.notNull(entity, "Entity");
		final InputStream instream = entity.getContent();
		if (instream == null) {
			return null;
		}
		try {
			Args.check(entity.getContentLength() <= Integer.MAX_VALUE, "HTTP entity too large to be buffered in memory");
			int i = (int) entity.getContentLength();
			if (i < 0) {
				i = 4096;
			}
			if (charset == null) {
				try {
					final ContentType contentType = ContentType.get(entity);
					if (contentType != null) {
						charset = contentType.getCharset();
					}
				} catch (final UnsupportedCharsetException ex) {
					throw new UnsupportedEncodingException(ex.getMessage());
				}
			}
			final Reader reader = new InputStreamReader(instream, charset);
			final CharArrayBuffer buffer = new CharArrayBuffer(i);
			final char[] tmp = new char[1024];
			int l;
			while ((l = reader.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
			return buffer.toString();
		} finally {
			instream.close();
		}
	}

	private String doGet(String url, int timeout) throws Exception {
		String content = null;
		HttpClient httpClient = null;
		try {
			httpClient = getHttpClient(timeout);
			HttpGet get = new HttpGet(url);
			get.setHeader("Content-Type", setHeader(getPropertyValue("contentType")));
			if (isUpdateHeader) {
				addRequestHeader(get);
				isUpdateHeader = false;
			}

			HttpResponse response = httpClient.execute(get);
			saveResponseHeader(response);
			int statusCode = response.getStatusLine().getStatusCode();
			String responseContent = getResponseContent(response);
			if (statusCode == 200) {
				content = responseContent;
			} else {
				fireFail(statusCode, "GET请求失败，状态码描述：" + response.getStatusLine().getReasonPhrase());
			}
			// 保留原有逻辑，触发result事件，由前端判断请求状态码执行逻辑处理
			fireResult(statusCode, responseContent);
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return content;
	}

	private String doPost(String url, int timeout, String method) throws Exception {
		String body = getPropertyValue("body");
		String content = null;
		HttpClient httpClient = null;
		HttpEntityEnclosingRequestBase _request = null;
		try {
			httpClient = getHttpClient(timeout);
			if ("post".equals(method)) {
				_request = new HttpPost(url);
			} else if ("put".equals(method)) {
				_request = new HttpPut(url);
			} else if ("patch".equals(method)) {
				_request = new HttpPatch(url);
			}
			if (isUpdateHeader) {
				addRequestHeader(_request);
				isUpdateHeader = false;
			}
			StringEntity se = new StringEntity(body, HTTP.UTF_8);
			se.setContentType(setHeader(getPropertyValue("contentType")));
			_request.setEntity(se);
			BasicHttpResponse response = (BasicHttpResponse) httpClient.execute(_request);
			saveResponseHeader(response);
			int statusCode = response.getStatusLine().getStatusCode();
			String responseContent = getResponseContent(response);
			if (statusCode == 200) {
				content = responseContent;
			} else {
				fireFail(statusCode, method.toUpperCase(Locale.getDefault()) + "请求失败，状态码描述：" + response.getStatusLine().getReasonPhrase());
			}
			fireResult(statusCode, responseContent);
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return content;
	}

	private String doDelete(String url, int timeout) throws Exception {
		String content = null;
		HttpClient httpClient = null;
		try {
			httpClient = getHttpClient(timeout);
			HttpDelete delete = new HttpDelete(url);
			delete.setHeader("Content-Type", setHeader(getPropertyValue("contentType")));
			if (isUpdateHeader) {
				addRequestHeader(delete);
				isUpdateHeader = false;
			}
			HttpResponse response = httpClient.execute(delete);
			saveResponseHeader(response);
			int statusCode = response.getStatusLine().getStatusCode();
			String responseContent = getResponseContent(response);
			if (statusCode == 200) {
				content = responseContent;
			} else {
				fireFail(statusCode, "DELETE请求失败，状态码描述：" + response.getStatusLine().getReasonPhrase());
			}
			fireResult(statusCode, responseContent);
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
		return content;
	}

	private String setHeader(String contentType) {
		if (null == contentType || "".equals(contentType)) {
			contentType = "application/x-www-form-urlencoded";
		}
		return contentType;
	}

	class SSLSocketFactoryEx extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);
			sslContext.init(null, new TrustManager[] { new MyTrustManager() }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	class MyTrustManager implements X509TrustManager {
		@Override
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		@Override
		public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
		}

		@Override
		public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
			try {
				chain[0].checkValidity();
			} catch (Exception e) {
				throw new java.security.cert.CertificateException("Certificate not valid or trusted.");
			}
		}
	}

	@Override
	public void upload(JSONObject _dictParas, DoIScriptEngine _scriptEngine, final DoInvokeResult _invokeResult) throws Exception {
		if (!isNetworkAvailable()) {
			fireFail(408, "网络离线，没有可用网络！");
			fireResult(408, "网络离线，没有可用网络！");
			return;
		}
		String path = DoJsonHelper.getString(_dictParas, "path", "");
		final String name = DoJsonHelper.getString(_dictParas, "name", "file");
		final String fileName = DoJsonHelper.getString(_dictParas, "filename", "");

		String fileFullPath = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentApp(), path);
		final File file = new File(fileFullPath);
		if (file.exists()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						int timeout = DoTextHelper.strToInt(getPropertyValue("timeout"), 500000);
						String url = getPropertyValue("url");
						FileUploadUtil uploadUtil = new FileUploadUtil(timeout, url);
						final HttpURLConnection _conn = uploadUtil.getHttpURLConnection();
						if (isUpdateHeader) {
							addRequestHeader(_conn);
							isUpdateHeader = false;
						}
						uploadUtil.setListener(new FileUploadListener() {
							@Override
							public void transferred(long count, long current) {
								fireProgress(count, current);
							}

							@Override
							public void onSuccess(String result) {
								_invokeResult.setResultText(result);
								fireEvent("success", _invokeResult);
								saveResponseHeader(_conn);
							}

							@Override
							public void onFailure(int statusCode, String msg) {
								fireFail(statusCode, msg);
								fireResult(statusCode, msg);
							}

							@Override
							public void onResult(int statusCode, String content) {
								fireResult(statusCode, content);
								saveResponseHeader(_conn);
							}
						});

						String method = getMethodValue(getPropertyValue("method"));
						if (!TextUtils.isEmpty(method) && "PUT".equalsIgnoreCase(method)) {
							method = "PUT";
						} else {
							method = "POST";
						}

						uploadUtil.uploadFile(file, name, method,fileName);
					} catch (Exception e) {
						DoServiceContainer.getLogEngine().writeError("Http upload \n", e);
					}
				}
			}).start();
		} else {
			fireResult(400, "本地文件不存在");
			DoServiceContainer.getLogEngine().writeInfo("Http upload \n", path + " 文件不存在");
		}
	}

	@Override
	public void download(JSONObject _dictParas, DoIScriptEngine _scriptEngine, final DoInvokeResult _invokeResult) throws Exception {
		if (!isNetworkAvailable()) {
			fireFail(408, "网络离线，没有可用网络！");
			fireResult(408, "网络离线，没有可用网络！");
			return;
		}
		String path = DoJsonHelper.getString(_dictParas, "path", "");
		String fileFullPath = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentApp(), path);
		int beginIndex = fileFullPath.lastIndexOf(File.separator) + 1;
		String filePath = fileFullPath.substring(0, beginIndex);
		String fileName = fileFullPath.substring(beginIndex);
		if (!DoIOHelper.existDirectory(fileName)) {
			DoIOHelper.createDirectory(filePath);
		}

		FinalHttp fh = new FinalHttp();
		if (isUpdateHeader) {
			addRequestHeader(fh);
			isUpdateHeader = false;
		}
		String url = getPropertyValue("url");
		fh.download(url, filePath + fileName, false, new AjaxCallBack<File>() {
			@Override
			public void onSuccess(File t, String taskId) {
				super.onSuccess(t, taskId);
				fireEvent("success", _invokeResult);
				fireResult(200, "OK");
			}

			@Override
			public void onLoading(long count, long current) {
				super.onLoading(count, current);
				fireProgress(count, current);
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg, String taskId) {
				super.onFailure(t, errorNo, strMsg, taskId);
				fireFail(400, "Http Download, " + strMsg);
				fireResult(400, strMsg);
			}
		}, null);
	}

	@Override
	public void download1(JSONObject _dictParas, DoIScriptEngine _scriptEngine, final DoInvokeResult _invokeResult) throws Exception {
		if (!isNetworkAvailable()) {
			fireFail(408, "网络离线，没有可用网络！");
			fireResult(408, "网络离线，没有可用网络！");
			return;
		}
		String _taskId = DoJsonHelper.getString(_dictParas, "taskId", "");
		if (TextUtils.isEmpty(_taskId)) {
			throw new Exception("taskId不能为空！");
		}

		HttpHandler<File> hh = downloadTasks.get(_taskId);
		if (hh != null) { //表示已经存在
			throw new Exception("taskId不能重复！");
		}

		String _path = DoJsonHelper.getString(_dictParas, "path", "");
		if (TextUtils.isEmpty(_path)) {
			throw new Exception("path不能为空！");
		}

		String _fileFullPath = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentApp(), _path);
		int _beginIndex = _fileFullPath.lastIndexOf(File.separator) + 1;
		String _filePath = _fileFullPath.substring(0, _beginIndex);
		String _fileName = _fileFullPath.substring(_beginIndex);
		if (!DoIOHelper.existDirectory(_fileName)) {
			DoIOHelper.createDirectory(_filePath);
		}

		boolean _isBreakpoint = DoJsonHelper.getBoolean(_dictParas, "isBreakpoint", false);

		FinalHttp _fh = new FinalHttp();
		if (isUpdateHeader) {
			addRequestHeader(_fh);
			isUpdateHeader = false;
		}
		String _url = getPropertyValue("url");
		HttpHandler<File> _handler = _fh.download(_url, _filePath + _fileName, _isBreakpoint, new AjaxCallBack<File>() {
			@Override
			public void onSuccess(File t, String taskId) {
				super.onSuccess(t, taskId);
				removeDownloadTask(taskId);
				fireEvent("success", _invokeResult);
				fireResult(200, "OK");
			}

			@Override
			public void onLoading(long count, long current) {
				super.onLoading(count, current);
				fireProgress(count, current);
			}

			@Override
			public void onFailure(Throwable t, int errorNo, String strMsg, String taskId) {
				super.onFailure(t, errorNo, strMsg, taskId);
				removeDownloadTask(taskId);
				fireFail(400, "Http Download, " + strMsg);
				fireResult(400, strMsg);
			}
		}, _taskId);

		downloadTasks.put(_taskId, _handler);
	}

	@Override
	public void stopDownload(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _taskId = DoJsonHelper.getString(_dictParas, "taskId", "");
		if (TextUtils.isEmpty(_taskId)) {
			throw new Exception("taskId不能为空！");
		}
		removeDownloadTask(_taskId);
	}

	private void removeDownloadTask(String _taskId) {
		HttpHandler<File> hh = downloadTasks.get(_taskId);
		if (hh != null) {
			hh.stop();
			hh = null;
			downloadTasks.remove(_taskId);
		}
	}

	@Override
	public void form(final JSONObject _dictParas, final DoIScriptEngine _scriptEngine, final DoInvokeResult _invokeResult) throws Exception {
		if (!isNetworkAvailable()) {
			fireFail(408, "网络离线，没有可用网络！");
			fireResult(408, "网络离线，没有可用网络！");
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					HttpClient httpClient = null;
					try {
						List<File> mFiles = null;
						List<String> mFieldNames = null;
						List<Integer> mFieldIndexs = null;
						long totalSize = 0;
						JSONObject data = DoJsonHelper.getJSONObject(_dictParas, "data");
						JSONArray files = DoJsonHelper.getJSONArray(data, "files");
						JSONArray texts = DoJsonHelper.getJSONArray(data, "texts");
						int timeout = DoTextHelper.strToInt(getPropertyValue("timeout"), 500000);
						String url = getPropertyValue("url");
						httpClient = getHttpClient(timeout);
						String method = getMethodValue(getPropertyValue("method"));
						HttpEntityEnclosingRequestBase requestBase = null;
						if (!TextUtils.isEmpty(method) && "PUT".equalsIgnoreCase(method)) {
							requestBase = new HttpPut(url);
						} else {
							requestBase = new HttpPost(url);
						}
						if (isUpdateHeader) {
							addRequestHeader(requestBase);
							isUpdateHeader = false;
						}
						MultipartEntityBuilder builder = MultipartEntityBuilder.create();
						builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
						if (files != null) {
							mFiles = new ArrayList<File>();
							mFieldNames = new ArrayList<String>();
							mFieldIndexs = new ArrayList<Integer>();
							for (int i = 0; i < files.length(); i++) {
								JSONObject childObj = files.getJSONObject(i);
								String value = DoJsonHelper.getString(childObj, "value", "");
								String fileFullPath = DoIOHelper.getLocalFileFullPath(_scriptEngine.getCurrentApp(), value);
								if (fileFullPath == null || !new File(fileFullPath).exists()) {
									DoServiceContainer.getLogEngine().writeInfo(value + " 文件不存在", "Http form \n");
									continue;
								}
								File mFile = new File(fileFullPath);
								mFiles.add(mFile);
								mFieldNames.add(DoJsonHelper.getString(childObj, "key", ""));
								totalSize += mFile.length();
								mFieldIndexs.add(i);
							}
						}

						final long mToTalSize = totalSize;
						if (mFiles != null && mFiles.size() > 0) {
							for (int i = 0; i < mFiles.size(); i++) {
								File mFile = mFiles.get(i);
								final int index = mFieldIndexs.get(i);
								DoCustomFileBody fileBody = new DoCustomFileBody(mFile);
								fileBody.setListener(new FileFormListener() {
									@Override
									public void transferred(long count, long current, String filename) {
										fireProgress(mToTalSize, current, count, index);
									}
								});
								builder.addPart(mFieldNames.get(i), fileBody);
							}
						}

						if (texts != null) {
							for (int i = 0; i < texts.length(); i++) {
								JSONObject childObj = texts.getJSONObject(i);
								String key = DoJsonHelper.getString(childObj, "key", "");
								String value = DoJsonHelper.getString(childObj, "value", "");
								builder.addPart(key, new StringBody(value, ContentType.create(HTTP.PLAIN_TEXT_TYPE, HTTP.UTF_8)));
							}
						}
						HttpEntity entity = builder.build();
						requestBase.setEntity(entity);
						HttpResponse response = httpClient.execute(requestBase);
						int statusCode = response.getStatusLine().getStatusCode();
						String responseContent = getResponseContent(response);
						if (statusCode == HttpStatus.SC_OK) {
							_invokeResult.setResultText(responseContent);
							fireEvent("success", _invokeResult);
						} else {
							fireFail(statusCode, "FORM请求失败，状态码描述：" + response.getStatusLine().getReasonPhrase());
						}
						fireResult(statusCode, responseContent);
					} catch (Exception e) {
						throw e;
					} finally {
						if (httpClient != null) {
							httpClient.getConnectionManager().shutdown();
						}
					}
				} catch (SocketTimeoutException e) {
					fireFail(408, "请求超时，" + e.getMessage());
					fireResult(408, "请求超时，" + e.getMessage());
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
					fireFail(400, "请求失败，" + e.getMessage());
					fireResult(400, "请求失败，" + e.getMessage());
					DoServiceContainer.getLogEngine().writeError("Http Error!" + e.getMessage(), e);
				}
			}
		}).start();
	}

	private void fireFail(int statusCode, String msg) {
		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		JSONObject jsonNode = new JSONObject();
		try {
			jsonNode.put("status", statusCode);
			jsonNode.put("message", msg);
			_invokeResult.setResultNode(jsonNode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireEvent("fail", _invokeResult);
		DoServiceContainer.getLogEngine().writeInfo("statusCode:" + statusCode + " Msg:" + msg, "Http失败");
	}

	private void fireResult(int statusCode, String content) {
		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		JSONObject jsonNode = new JSONObject();
		try {
			jsonNode.put("status", statusCode);
			jsonNode.put("data", content);
			_invokeResult.setResultNode(jsonNode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireEvent("result", _invokeResult);
	}

	private void fireProgress(long total, long curr) {
		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		JSONObject jsonNode = new JSONObject();
		try {
			jsonNode.put("currentSize", curr / 1024f);
			jsonNode.put("totalSize", total / 1024f);
			_invokeResult.setResultNode(jsonNode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireEvent("progress", _invokeResult);
	}

	private void fireProgress(long total, long curr, long fileSzie, int index) {
		DoInvokeResult _invokeResult = new DoInvokeResult(getUniqueKey());
		JSONObject jsonNode = new JSONObject();
		try {
			jsonNode.put("index", index);
			jsonNode.put("currentSize", curr / 1024f);
			jsonNode.put("totalSize", total / 1024f);
			jsonNode.put("currentFileSize", fileSzie / 1024f);
			_invokeResult.setResultNode(jsonNode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		fireEvent("progress", _invokeResult);
	}

	private void fireEvent(String eventName, DoInvokeResult _invokeResult) {
		DoEventCenter eventCenter = getEventCenter();
		if (eventCenter != null) {
			eventCenter.fireEvent(eventName, _invokeResult);
		}
	}

	private void saveResponseHeader(HttpResponse _response) {
		responseHeaders.clear();
		Header[] arr = _response.getHeaders("Set-Cookie");
		JSONArray _array = new JSONArray();
		if (arr != null && arr.length > 0) {
			for (int i = 0; i < arr.length; i++) {
				_array.put(arr[i].getValue());
			}
			responseHeaders.put("Set-Cookie", _array);
		}
		Header[] _arrHeader = _response.getAllHeaders();

		for (Header header : _arrHeader) {
			if (!header.getName().equals("Set-Cookie")) {
				if (!TextUtils.isEmpty(header.getName()) && !TextUtils.isEmpty(header.getValue())) {
					responseHeaders.put(header.getName(), header.getValue());
				}
			}
		}
	}

	private void saveResponseHeader(HttpURLConnection _conn) {
		responseHeaders.clear();
		Map<String, List<String>> _map = _conn.getHeaderFields();
		Set<String> keys = _map.keySet();
		StringBuffer sb = new StringBuffer();
		for (String _key : keys) {
			String _value = _conn.getHeaderField(_key);
			if ("Set-Cookie".equalsIgnoreCase(_key)) {
				sb.append(_value);
			} else {
				responseHeaders.put(_key, _value);
			}
		}
		if (sb.length() > 0) {
			responseHeaders.put("Set-Cookie", sb.toString());
		}
	}

	private void addRequestHeader(FinalHttp _fh) {
		// 遍历map集合
		for (String key : requestHeaders.keySet()) {
			if (key.equalsIgnoreCase("cookie")) {
				_fh.addHeader("cookie", requestHeaders.get(key).toString());
			} else {
				_fh.addHeader(key, requestHeaders.get(key).toString());
			}
		}
		isUpdateHeader = false;

	}

	private void addRequestHeader(HttpURLConnection _conn) {
		// 遍历map集合
		for (String key : requestHeaders.keySet()) {
			if (key.equalsIgnoreCase("cookie")) {
				_conn.addRequestProperty("cookie", requestHeaders.get(key).toString());
			} else {
				_conn.addRequestProperty(key, requestHeaders.get(key).toString());
			}
		}
		isUpdateHeader = false;

	}

	private void addRequestHeader(HttpRequestBase _request) {
		// 遍历map集合
		for (String key : requestHeaders.keySet()) {
			if (key.equalsIgnoreCase("cookie")) {
				_request.addHeader("cookie", requestHeaders.get(key).toString());
			} else {
				_request.addHeader(key, requestHeaders.get(key).toString());
			}
		}
		isUpdateHeader = false;
	}

	@Override
	public void setRequestHeader(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if (!isUpdateHeader) {
			requestHeaders.clear();
			responseHeaders.clear();
		}

		String _key = (String) _dictParas.get("key");
		String _value = (String) _dictParas.get("value");
		if (_key != null && !TextUtils.isEmpty(_key) && _value != null) {
			isUpdateHeader = true;
			if (_key.equalsIgnoreCase("cookie")) {
				requestHeaders.put("Cookie", _value);
			} else {
				requestHeaders.put(_key, _value);
			}
		}
	}

	@Override
	public void getResponseHeader(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _key = DoJsonHelper.getString(_dictParas, "key", "");
		//如果为空获取所有
		if (TextUtils.isEmpty(_key)) {
			Set<String> _keys = responseHeaders.keySet();
			JSONObject _obj = new JSONObject();
			for (String _tempKey : _keys) {
				if (_tempKey == null) {
					continue;
				}
				_obj.put(_tempKey, responseHeaders.get(_tempKey));
			}
			_invokeResult.setResultNode(_obj);
		} else {
			if (_key.equalsIgnoreCase("Set-Cookie")) {
				Object _obj = responseHeaders.get("Set-Cookie");
				if (_obj != null) {
					_invokeResult.setResultText(_obj.toString());
				}
			} else {
				if (responseHeaders.containsKey(_key)) {
					_invokeResult.setResultText(responseHeaders.get(_key).toString());
				}
			}
		}
	}

	public class HttpPatch extends HttpPut {

		public HttpPatch(String url) {
			super(url);
		}

		@Override
		public String getMethod() {
			return "PATCH";
		}
	}

	@Override
	public void setRedirect(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		isSetRedirect = DoJsonHelper.getBoolean(_dictParas, "isSetRedirect", true);
	}

}