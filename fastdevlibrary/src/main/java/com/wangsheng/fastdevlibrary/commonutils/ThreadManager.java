package com.wangsheng.fastdevlibrary.commonutils;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**线程管理类
 * @use ThreadManager.getInstance().runThread(...);
 * 		在使用ThreadManager的Context被销毁前ThreadManager.getInstance().destroyThread(...);
 *      在应用退出前ThreadManager.getInstance().finish();
 */
public class ThreadManager {
	private static final String TAG = "ThreadManager";


	private Map<String, ThreadBean> threadMap;
	private ThreadManager() {
		threadMap = new HashMap<String, ThreadBean>();
	}

	private static ThreadManager threadManager;
	public static ThreadManager getInstance() {
		if (threadManager == null) {
			threadManager = new ThreadManager();
		}

		return threadManager;
	}


	/**运行线程
	 * @param name
	 * @param runnable
	 * @return
	 */
	public Handler runThread(String name, Runnable runnable) {
		if (StringUtil.isNotEmpty(name, true) == false || runnable == null) {
//			LogUtils.loge(TAG, "runThread  StringUtil.isNotEmpty(name, true) == false || runnable == null >> return");
			return null;
		}
		name = StringUtil.getTrimedString(name);
//		LogUtils.logd(TAG, "\n runThread  name = " + name);

		Handler handler = getHandler(name);
		if (handler != null) {
//			LogUtils.logw(TAG, "handler != null >>  destroyThread(name);");
			destroyThread(name);
		}

		HandlerThread thread = new HandlerThread(name);
		thread.start();//创建一个HandlerThread并启动它
		handler = new Handler(thread.getLooper());//使用HandlerThread的looper对象创建Handler
		handler.post(runnable);//将线程post到Handler中

		threadMap.put(name, new ThreadBean(name, thread, runnable, handler));

//		LogUtils.logd(TAG, "runThread  added name = " + name + "; threadMap.size() = " + threadMap.size() + "\n");
		return handler;
	}

	/**获取线程Handler
	 * @param name
	 * @return
	 */
	private Handler getHandler(String name) {
		ThreadBean tb = getThread(name);
		return tb == null ? null : tb.getHandler();
	}

	/**获取线程
	 * @param name
	 * @return
	 */
	public ThreadBean getThread(String name) {
		return name == null ? null : threadMap.get(name);
	}


	/**销毁线程
	 * @param nameList
	 * @return
	 */
	public void destroyThread(List<String> nameList) {
		if (nameList != null) {
			for (String name : nameList) {
				destroyThread(name);
			}
		}
	}
	/**销毁线程
	 * @param name
	 * @return
	 */
	public void destroyThread(String name) {
		destroyThread(getThread(name));
	}
	/**销毁线程
	 * @param tb
	 * @return
	 */
	private void destroyThread(ThreadBean tb) {
		if (tb == null) {
//			LogUtils.loge(TAG, "destroyThread  tb == null >> return;");
			return;
		}

		destroyThread(tb.getHandler(), tb.getRunnable());
		if (tb.getName() != null) { // StringUtil.isNotEmpty(tb.getName(), true)) {
			threadMap.remove(tb.getName());
		}
	}
	/**销毁线程
	 * @param handler
	 * @param runnable
	 * @return
	 */
	public void destroyThread(Handler handler, Runnable runnable) {
		if (handler == null || runnable == null) {
//			LogUtils.loge(TAG, "destroyThread  handler == null || runnable == null >> return;");
			return;
		}

		try {
			handler.removeCallbacks(runnable);
		} catch (Exception e) {
			Log.e(TAG, "onDestroy try { handler.removeCallbacks(runnable);...  >> catch  : " + e.getMessage());
		}
	}


	/**结束ThreadManager所有进程
	 */
	public void finish() {
		threadManager = null;
		if (threadMap == null || threadMap.keySet() == null) {
//			LogUtils.logd(TAG, "finish  threadMap == null || threadMap.keySet() == null >> threadMap = null; >> return;");
			threadMap = null;
			return;
		}
		List<String> nameList = new ArrayList<String>(threadMap.keySet());//直接用Set在系统杀掉应用时崩溃
		if (nameList != null) {
			for (String name : nameList) {
				destroyThread(name);
			}
		}
		threadMap = null;
		Log.d(TAG, "\n finish  finished \n");
	}

	/**线程类
	 * @author Lemon
	 */
	public class ThreadBean {

		private String name;
		private HandlerThread thread;
		private Runnable runnable;
		private Handler handler;

		public ThreadBean() {
		}

		public ThreadBean(String name, HandlerThread thread, Runnable runnable, Handler handler) {
			this.name = name;
			this.thread = thread;
			this.runnable = runnable;
			this.handler = handler;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public HandlerThread getThread() {
			return thread;
		}

		public void setThread(HandlerThread thread) {
			this.thread = thread;
		}

		public Runnable getRunnable() {
			return runnable;
		}

		public void setRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		public Handler getHandler() {
			return handler;
		}

		public void setHandler(Handler handler) {
			this.handler = handler;
		}




	}

}
