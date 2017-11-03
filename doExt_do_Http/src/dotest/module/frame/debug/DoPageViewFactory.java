package dotest.module.frame.debug;

import android.app.Activity;
import android.app.Application;
import core.interfaces.DoIPageViewFactory;

public class DoPageViewFactory implements DoIPageViewFactory {

	private Activity currentActivity;
	private Application application;

	@Override
	public void closePage(String _animationType, String _data, int _layer) {

	}

	@Override
	public void closePage(String _animationType, String _data, String _id) {

	}

	public void setCurrentActivity(Activity currentActivity) {
		this.currentActivity = currentActivity;
	}

	@Override
	public Activity getAppContext() {
		return currentActivity;
	}

	@Override
	public Application getApplicationContext() {
		return this.application;
	}

	@Override
	public void setApplicationContext(Application _application) {
		this.application = _application;
	}

	@Override
	public void exitApp() {

	}

	@Override
	public void openPage(String arg0, String arg1, String arg2, String arg3, String arg4, String arg5, String arg6, String arg7, String arg8, String arg9) {
		
	}

}
