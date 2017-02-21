package com.wenba.comm.eventlog.bi;

import com.wenba.comm.eventlog.bi.http.BasicHeader;
import com.wenba.comm.eventlog.bi.http.ByteArrayBody;
import com.wenba.comm.eventlog.bi.http.MultipartEntity;
import com.wenba.comm.eventlog.bi.http.StringBody;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;
public class BiEventHandler {
	private static String TAG = "BiEventHandler";

	private static final String SEQUENCE_NUMBER_FILE = "sequence_number";
	private static final boolean LOG_ON = true;

	private final static Pattern EVENT_LOG_PATTERN = Pattern.compile("event_\\w+\\.log");
	private final static Pattern EVENT_LOG_ARGS_PATTERN = Pattern.compile("event_\\w+\\.args");
	private static AtomicLong sequenceNumber = new AtomicLong(-1L);

	private final int  K = 1024;
	private int  BLOCK_SIZE = 16 * K;
	private int  MAX_FILE_SIZE = 512 * K;
	private long SLEEP_NANO_TIME = TimeUnit.MINUTES.toNanos(5);
	private long SCAN_INTERVAL =   TimeUnit.MINUTES.toMillis(30);
	private long DISCARD_INTERVAL = TimeUnit.HOURS.toMillis(24);
	private String[] mobileNetworkAllowed = new String[]{"3G", "4G"};
	private String clientConfUrl = null;
	private String[] serverArray = null;
	private int serverIndex = 0;
	private int curServerFail = 0;

	private ConcurrentLinkedQueue<String> logQueue = new ConcurrentLinkedQueue<String>();

	private AtomicInteger logBufferSize = new AtomicInteger();

	private Thread workerThread;
	private AtomicBoolean isAlive = new AtomicBoolean(true);
	private boolean serverParamRequested = false;
	private int serverParamRequestCount = 0;

	private List<LogFilePair> uploadFilePairs = new LinkedList<LogFilePair>();

	private Snapshot pendingWorkBeforeExit;
	private UserEventContext hostEnv;

	private static class Snapshot {
		private byte[] gzipArgs;
		private byte[] gzipBody;
	}

	public BiEventHandler(UserEventContext context, boolean testEnv) {
		this.hostEnv = context;

		if (testEnv) {
			clientConfUrl = "http://wb-conf.ufile.ucloud.com.cn/client_qa.conf";
			serverArray = new String[]{""};
		}else {
			clientConfUrl = "http://wb-conf.ufile.ucloud.com.cn/client.conf";
			serverArray = new String[] {"http://flume.xueba100.com:80"};
		}

		prepare();
	}

	private static class LogFilePair {
		File argsFile;
		File logFile;

		@Override
		public String toString() {
			String msg = "argsFile: " + argsFile.getName() + ", len = " + argsFile.length()
					+ ", logFile: " + logFile.getName() + ", len = " + logFile.length();
			return msg;
		}

		public LogFilePair(File argsFile, File logFile) {
			this.argsFile = argsFile;
			this.logFile = logFile;
		}

		public void deleteFiles() {
			argsFile.delete();
			logFile.delete();
		}

		public static File argsFileForLog(File logFile) {
			String logFileName = logFile.getName();
			Matcher matcher = EVENT_LOG_PATTERN.matcher(logFileName);
			if (!matcher.matches()) {
				return null;
			}

			String baseName = logFileName.substring(0, logFileName.indexOf('.'));

			File file = new File(logFile.getParent(), String.format("%s.args", baseName));
			if (!file.exists()) {
				return null;
			}

			return file;
		}
	}

	private void parseNetworkAllowed(Properties properties) {
		if (properties == null) {
			return;
		}

		String mobileNetwork = properties.getProperty("mobileNetworkAllowed");
		if (mobileNetwork == null) {
			return;
		}

		String[] nets = mobileNetwork.split(",");

		int count = 0;

		for (int i = 0; i < nets.length; i++) {
			nets[i] = nets[i].trim();
			String net = nets[i];
			if (net.equalsIgnoreCase("2G")
					|| net.equalsIgnoreCase("3G")
					|| net.equalsIgnoreCase("4G")) {
				count++;
			}else {
				nets[i] = null;
			}
		}

		mobileNetworkAllowed = new String[count];

		if (count > 0) {
			int parsed = 0;
			for (int i = 0; i < nets.length; i++) {
				if (nets[i] != null) {
					mobileNetworkAllowed[parsed++] = nets[i];
					if (parsed == mobileNetworkAllowed.length) {
						break;
					}
				}
			}
		}
	}

	private void parseSleepTime(Properties properties) {
		if (properties == null) {
			return;
		}

		String sleepTime = properties.getProperty("sleepTime");
		try {
			int val = Integer.valueOf(sleepTime);
			//[1 - 60]
			if (val >= 1 && val <= 60) {
				SLEEP_NANO_TIME = TimeUnit.MINUTES.toNanos(val);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void parseScanInterval(Properties properties) {
		if (properties == null) {
			return;
		}

		String scanInterval = properties.getProperty("scanInterval");
		try {
			int val = Integer.valueOf(scanInterval);
			//10 - 3600
			if (val >= 10 && val <= 3600) {
				SCAN_INTERVAL = TimeUnit.MINUTES.toMillis(val);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void configConnection(URLConnection conn) {
		conn.setDoInput(true);
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(10000);
	}

	private void parseDiscardInterval(Properties properties) {
		if (properties == null) {
			return;
		}

		String discardInterval = properties.getProperty("discardInterval");
		try {
			int val = Integer.valueOf(discardInterval);
			//[1 - 240]
			if (val >= 1 && val <= 240) {
				DISCARD_INTERVAL = TimeUnit.HOURS.toMillis(val);
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void parseMaxFileSize(Properties properties) {
		if (properties == null) {
			return;
		}

		String maxFileSize = properties.getProperty("maxFileSize");
		try {

			int val = Integer.valueOf(maxFileSize);
			//[512 - 2048]
			if (val >= 512 && val <= 2048) {
				MAX_FILE_SIZE = val * K;
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void parseBlockSize(Properties properties) {
		if (properties == null) {
			return;
		}

		String blockSize = properties.getProperty("blockSize");
		try {
			int val = Integer.valueOf(blockSize);
			//[1 - 512]
			if (val >= 1 && val <= 512) {
				BLOCK_SIZE = val * K;
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void parseServers(Properties properties) {
		if (properties == null) {
			return;
		}

		String servers = properties.getProperty("servers");
		if (servers == null || servers.length() == 0) {
			return;
		}

		String[] srvs = servers.split(",");

		int count = 0;
		for (int i = 0; i < srvs.length; i++) {
			srvs[i] = srvs[i].trim();

			String srv = srvs[i];
			if (srv.startsWith("http://") || srv.startsWith("https://")) {
				count++;
			}else {
				srvs[i] = null;
			}
		}

		if (count > 0) {
			serverIndex = 0;
			curServerFail = 0;

			serverArray = new String[count];

			int parsed = 0;
			for (int i = 0; i < srvs.length; i++) {
				if (srvs[i] != null) {
					serverArray[parsed++] = srvs[i];
					if (parsed == serverArray.length) {
						break;
					}
				}
			}
		}
	}

	private void requestServerParam() {
		if (serverParamRequested) {
			return;
		}

		if (!isNetworkOk()) {
			return;
		}

		if (serverParamRequestCount > 10) {
			return;
		}

		serverParamRequestCount++;

		hostEnv.logInfo(TAG, "requestServerParam start");

		Properties properties = null;
		try {

			URL url = new URL(clientConfUrl);

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			configConnection(conn);
			conn.setRequestMethod("GET");

			int code = conn.getResponseCode();
			if (code == 200) {
				properties = new Properties();
				properties.load(conn.getInputStream());
			}else {
				hostEnv.logInfo(TAG, "requestServerParam props failed: response code = " + code + ", msg = " + conn.getResponseMessage());
			}

		} catch (Exception e) {
			hostEnv.logInfo(TAG, "requestServerParam props failed: " + e.getMessage());
		}

		if (properties == null) {
			return;
		}

		hostEnv.logInfo(TAG, "requestServerParam props: " + properties);
		//#所有参数都是可选的, 如果没有设置,则为程序默认值
		//#mobileNetworkAllowed=3G,4G  [2G, 3G, 4G]
		//#sleepTime=5     -- min, 周期启动检查时间 [1 - 60]
		//#scanInterval=30 -- min, 周期扫描磁盘,删除过期文件时间 [10 - 3600]
		//#discardInterval=24 -- hour, 距离当前时间超过该值的文件将被删除 [1 - 240]
		//#maxFileSize = 512  -- KB, 超过该大小的文件将被丢弃,不上传 [512 - 2048]
		//#blockSize = 16     -- KB, 触发上传和存盘的大小 [1 - 512]
		//#servers=http://192.168.90.21:8100, http://192.168.90.23:8100

		serverParamRequested = true;

		parseNetworkAllowed(properties);
		parseSleepTime(properties);
		parseScanInterval(properties);
		parseDiscardInterval(properties);
		parseMaxFileSize(properties);
		parseBlockSize(properties);
		parseServers(properties);
	}

	private void onServerFail() {
		hostEnv.logInfo(TAG, "onServerFail, curServerFail: " + curServerFail);

		//update server
		curServerFail++;
		if (curServerFail >= 5) {
			serverIndex = (serverIndex + 1) % serverArray.length;
			curServerFail = 0;

			hostEnv.logInfo(TAG, "onServerFail, switch to: " + serverArray[serverIndex] + ", serverIndex: " + serverIndex);
		}
	}

	private Runnable workRunner = new Runnable() {
		long prevCheckTime = 0;

		@Override
		public void run() {
			while(isAlive.get()) {
				hostEnv.logInfo(TAG, "ready to doWork");
				try {
					requestServerParam();
				} catch (Exception e) {
					hostEnv.logInfo(TAG, "requestServerParam Exception: " + e);
				}

				try {
					doWork();
				} catch (Throwable e) {
					hostEnv.logInfo(TAG, "doWork Exception: " + e);
				}

				//have a rest
				LockSupport.parkNanos(SLEEP_NANO_TIME);
			}

			if (pendingWorkBeforeExit != null) {
				hostEnv.logInfo(TAG, "pending work not null, write to disk");
				writeToDisk(pendingWorkBeforeExit);
				pendingWorkBeforeExit = null;
			}

			hostEnv.logInfo(TAG, "User event workder has terminated: id = " + Thread.currentThread().hashCode());
		}

		private void scanLogFilePairs() {
			hostEnv.logInfo(TAG, "scan for obsolete files");
			uploadFilePairs.clear();

			File dirs = hostEnv.getEventDataDir();
			File[] files = dirs.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					boolean match = EVENT_LOG_PATTERN.matcher(pathname.getName()).matches();
					return match;
				}
			});

			for (File file : files) {
				File argsFile = LogFilePair.argsFileForLog(file);
				if (argsFile == null) {
					//argsFile not exist, delete
					file.delete();
					continue;
				}

				boolean good = true;

				long diff = System.currentTimeMillis() - file.lastModified() ;
				if (diff > DISCARD_INTERVAL) {
					good = false;
				}else if (argsFile.length() > MAX_FILE_SIZE || file.length() > MAX_FILE_SIZE) {
					good = false;
				}else if (argsFile.length() == 0 || file.length() == 0) {
					good = false;
				}

				if (!good) {
					//file outdated, delete 
					file.delete();
					argsFile.delete();
					hostEnv.logInfo(TAG, "delete file pair: " + argsFile.getName() + ", " + file.getName());
				} else {
					uploadFilePairs.add(new LogFilePair(argsFile, file));
				}
			}

			hostEnv.logInfo(TAG, "collected files: " + uploadFilePairs);
			prevCheckTime = System.currentTimeMillis();
		}

		private void doUpload() {
			uploadLogFilePairs();

			Snapshot snapshot = makeSnapshot();
			if (snapshot != null) {
				uploadSnapshot(snapshot);
			}

			if (pendingWorkBeforeExit != null) {
				uploadSnapshot(pendingWorkBeforeExit);
				pendingWorkBeforeExit = null;
			}
		}

		private void uploadLogFilePairs() {
			//upload previous file first
			Iterator<LogFilePair> iterator = uploadFilePairs.iterator();

			while (iterator.hasNext()) {
				LogFilePair fileData = iterator.next();

				try {
					byte[] args = readFile(fileData.argsFile);
					byte[] body = readFile(fileData.logFile);

					int code = uploadPayload(args, body);

					if (code == 0 || code == -2) {
						if (code == 0) {
							hostEnv.logInfo(TAG, "upload success: " + fileData.argsFile + ", length = " + args.length
									+ ", " + fileData.logFile + ", length = " + body.length);
						}else {
							hostEnv.logInfo(TAG, "upload fail because server return error, drop it: " + fileData.argsFile + ", length = " + args.length
									+ ", " + fileData.logFile + ", length = " + body.length);
						}

						fileData.deleteFiles();
						iterator.remove();

					}else if (code == -1) {
						onServerFail();
					}

				} catch (Throwable e) {
					iterator.remove();
					hostEnv.logError(TAG, "error in upload file log: " + e.getMessage());
				}
			}
		}

		private void uploadSnapshot(Snapshot snapshot) {
			if (snapshot == null) {
				return;
			}

			try {
				byte[] args = snapshot.gzipArgs;
				byte[] body = snapshot.gzipBody;

				if (body.length <= MAX_FILE_SIZE) {
					int code = uploadPayload(Arrays.copyOf(args, args.length), Arrays.copyOf(body, body.length));
					if (code == 0) {
						//do nothing, memory will reclaimed auto
						hostEnv.logInfo(TAG, "upload content success: args len = " + args.length
								+ ", body len = " + body.length);
					}else if (code == -1) {
						LogFilePair filePair = writeSnapshotFileForUpload(snapshot);
						if (filePair != null) {
							hostEnv.logInfo(TAG, "upload fail: write to disk: " + filePair.toString());
						}

						onServerFail();
					}else if (code == -2) {
						hostEnv.logInfo(TAG, "upload content fail because server return error, drop it");
					}
				}else {
					hostEnv.logInfo(TAG, "discard content: args length = " + args.length
							+ ", body length = " + body.length);
				}

			} catch (Throwable e) {
				hostEnv.logError(TAG, "error in upload mem log: " + e.getMessage());
			}
		}

		private void doWork() {
			if (System.currentTimeMillis() - prevCheckTime > SCAN_INTERVAL) {
				scanLogFilePairs();
			}

			if (isNetworkOk()) {
				doUpload();
			}else {
				if (logBufferSize.get() > BLOCK_SIZE) {
					hostEnv.logInfo(TAG, "log buffer overflow, write to disk");
					writeSnapshotFileForUpload(makeSnapshot());
				}
			}

			hostEnv.logInfo(TAG, "doWork finished");
		}
	};

	private int getMaskChar() {
		String val = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		return val.charAt((int)(System.currentTimeMillis() % val.length()));
	}

	public byte[] gzip(byte[] body) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		GZIPOutputStream gzipStream = new GZIPOutputStream(baos);
		gzipStream.write(body);
		gzipStream.close();

		return baos.toByteArray();
	}

	/**
	 *
	 * @param args
	 * @param body
	 * @return 0 - success, -1 network error, -2 server returned error
	 */
	private int uploadPayload(byte[] args, byte[] body) {
		int maskA = getMaskChar();
		int maskB = maskA | 0x80;

		//HttpPost post = new HttpPost("http://192.168.90.82:2768");

		hostEnv.logInfo(TAG, "server count: " + serverArray.length + ", curIndex: " + serverIndex
				+ ", failCount = " + curServerFail);

		hostEnv.logInfo(TAG, "this server: " + serverArray[serverIndex]);
		try {
			MultipartEntity entity = new MultipartEntity();

			{
				for(int i = 0; i < args.length; i ++) {
					args[i] ^= maskB;
				}

				entity.addPart("data1", new ByteArrayBody(args, null));
			}

			entity.addPart("data2", new StringBody(String.valueOf((char)maskA)));

			{
				for (int i = 0; i < body.length; i++) {
					body[i] ^= maskB;
				}

				entity.addPart("data3", new ByteArrayBody(body, null));
			}

			URL postURL = new URL(serverArray[serverIndex]);

			HttpURLConnection conn = (HttpURLConnection)postURL.openConnection();
			configConnection(conn);
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");

			//header
			BasicHeader header = entity.getContentEncoding();
			if (header != null) {
				conn.setRequestProperty(header.getName(), header.getValue());
			}

			header = entity.getContentType();
			if (header != null) {
				conn.setRequestProperty(header.getName(), header.getValue());
			}

			String length = String.valueOf(entity.getContentLength());
			conn.setRequestProperty("Content-Length", length);
			entity.writeTo(conn.getOutputStream());

			if (conn.getResponseCode() == 200) {
				String resp = inputStreamToString(conn.getInputStream());
				JSONObject object = new JSONObject(resp);

				int code = object.optInt("statusCode", -1);
				if (code == 0) {
					return 0;
				}else {
					return -2;
				}
			}

		}catch(Throwable e) {
			hostEnv.logError(TAG, "upload event body fail: " + e);
		}

		return -1;
	}

	private String inputStreamToString(InputStream stream) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream(4096);

		byte[] chunk = new byte[1024];

		int count = 0;

		while((count = stream.read(chunk)) > 0) {
			out.write(chunk, 0, count);
		}

		byte[] body = out.toByteArray();
		out.close();

		return new String(body);
	}

	private void prepare() {
		if (sequenceNumber.get() == -1) {
			sequenceNumber.set(getSequenceNumber());
		}

		workerThread = new Thread(workRunner);
		workerThread.start();

		//删除不符合pattern的文件
		purgeLogFiles();
	}

	public long takeSequenceNumber() {
		return sequenceNumber.getAndIncrement();
	}

	private void purgeLogFiles() {
		File logDir = hostEnv.getEventDataDir();
		if(logDir == null){
			return;
		}

		final ArrayList<File> victims = new ArrayList<File>();
		logDir.list(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				boolean good = false;

				if (EVENT_LOG_PATTERN.matcher(filename).matches()) {
					good = true;
				}else if (EVENT_LOG_ARGS_PATTERN.matcher(filename).matches()) {
					good = true;
				}else if (filename.equals(SEQUENCE_NUMBER_FILE)) {
					good = true;
				}

				if (!good) {
					victims.add(new File(dir, filename));
				}

				hostEnv.logError(TAG, "purgeLogFiles: test = " + filename + ", good = " + good);

				return false;
			}
		});

		for(File file : victims) {
			delete(file);
		}
	}

	private void delete(File file) {
		if (file == null) {
			return;
		}

		if (file.isFile() && file.exists()) {
			file.delete();
		}

		if (file.isDirectory()) {
			File[] children = file.listFiles();

			for (File child : children) {
				delete(child);
			}

			file.delete();
		}
	}

	/**
	 * 读取sequenceNumber
	 *
	 * @return
	 */
	private long getSequenceNumber() {
		File file = new File(hostEnv.getEventDataDir(), SEQUENCE_NUMBER_FILE);
		if (file == null || !file.exists() || file.length() == 0) {
			return 0;
		}
		long sequenceNumber = 0;
		FileInputStream inputStream = null;

		try {
			byte[] buffer = new byte[256];
			inputStream = new FileInputStream(file);

			int count = inputStream.read(buffer);

			String content = new String(buffer, 0, count);

			sequenceNumber = Long.parseLong(content);
		} catch (Exception e) {
			hostEnv.logInfo("wenba", e.getMessage());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					hostEnv.logInfo("wenba", e.getMessage());
				}
			}
		}

		hostEnv.logError(TAG, "read sequence: " + sequenceNumber);
		return sequenceNumber;
	}

	/**
	 * 存储sequenceNumber
	 */
	@SuppressWarnings("unused")
	private void saveSequenceNumber() {
		hostEnv.logInfo(TAG, "saveSequenceNumber: " + sequenceNumber);
		File file = new File(hostEnv.getEventDataDir(), SEQUENCE_NUMBER_FILE);
		if (file == null) {
			return;
		}

		OutputStream outputStream = null;
		try {
			if (!file.exists()) {
				file.getParentFile().mkdirs();
				file.createNewFile();
			}

			outputStream = new FileOutputStream(file);
			outputStream.write(String.valueOf(sequenceNumber).getBytes());
		} catch (Exception e) {
			hostEnv.logInfo("wenba", e.getMessage());
		} finally {
			closeObject(outputStream);
		}
	}

	private boolean isNetworkOk() {
		if (hostEnv.isWiFiAvaliable()) {
			return true;
		}

		String mobile = hostEnv.getMobileNetwork();

		for (String allowed : mobileNetworkAllowed) {
			if (allowed.equalsIgnoreCase(mobile)) {
				return true;
			}
		}

		return false;
	}

	public void wakeupWorker() {
		LockSupport.unpark(workerThread);
	}

	private String getCommArgs() {
		return hostEnv.getCommonArguments();
	}

	public static byte[] readFile(File file) throws Exception {
		InputStream stream = null;;

		try {
			byte[] body = new byte[(int)file.length()];

			stream = new FileInputStream(file);
			stream.read(body);
			return body;
		} finally {
			closeObject(stream);
		}
	}

	private LogFilePair newLogFileData() {
		File dirs = hostEnv.getEventDataDir();
		if (dirs == null) {
			return null;
		}
		if (!dirs.exists()) {
			dirs.mkdirs();
		}
		if (!dirs.isDirectory()) {
			dirs.delete();
			dirs.mkdirs();
		}

		String prefix = "event_" + UUID.randomUUID().toString().replace("-", "");

		File argsFile = new File(dirs, prefix + ".args");
		File logFile = new File(dirs, prefix + ".log");

		return new LogFilePair(argsFile, logFile);
	}

	private static String getJsonPair(String key, String value) {
		StringBuffer jsonPair = new StringBuffer();
		jsonPair.append("\"");
		jsonPair.append(key);
		jsonPair.append("\":\"");
		jsonPair.append(value);
		jsonPair.append("\",");
		return jsonPair.toString();
	}

	public void addEvent(String log) {
		if (log == null) {
			return;
		}

		if (isAlive.get() == false) {
			throw new RuntimeException("add event to a dead BiEventHandler instance");
		}


		if (LOG_ON) {
			hostEnv.logInfo(TAG, "add log：" + log.toString() + ", curSize: " + logBufferSize.get()
					+ ", logRecordSize: " + log.getBytes().length);
		}

		int bufferSize = logBufferSize.addAndGet(log.getBytes().length);
		logQueue.offer(log.toString());


		if (bufferSize >= BLOCK_SIZE) {
			wakeupWorker();
		}
	}

	private synchronized LogFilePair writeSnapshotFileForUpload(Snapshot snapshot) {
		if (snapshot == null) {
			return null;
		}

		LogFilePair pair = writeToDisk(snapshot);
		if (pair != null) {
			uploadFilePairs.add(pair);
		}

		return pair;
	}

	public void snapshotToDisk(boolean saveBeforeExit) {
		final Snapshot snapshot = makeSnapshot();
		if (snapshot == null) {
			return;
		}

		if (saveBeforeExit) {
			writeSnapshotFileForUpload(snapshot);
		} else {
			hostEnv.postTask(new Runnable() {

				@Override
				public void run() {
					writeSnapshotFileForUpload(snapshot);
				}
			});
		}
	}

	private Snapshot makeSnapshot() {
		String body = drainQueueContent();
		if (body == null || body.length() == 0) {
			return null;
		}

		byte[] gzipArgs = null;
		byte[] gzipBody = null;

		try {
			gzipArgs = gzip(getCommArgs().getBytes());
			gzipBody = gzip(body.getBytes());
		} catch (Exception e) {
			return null;
		}

		Snapshot snapshot = new Snapshot();
		snapshot.gzipArgs = gzipArgs;
		snapshot.gzipBody = gzipBody;
		return snapshot;
	}

	public synchronized void onUserLogout() {
		//make snapshot
		pendingWorkBeforeExit = makeSnapshot();

		isAlive.set(false);
		wakeupWorker();
	}

	private synchronized String drainQueueContent() {
		StringBuffer logsBuffer = new StringBuffer();

		while (!logQueue.isEmpty()) {
			String body = logQueue.poll();
			if (body == null) {
				break;
			}

			logBufferSize.getAndAdd(-body.getBytes().length);

			logsBuffer.append(body);
			logsBuffer.append("\n");
		}

		if (logsBuffer.length() > 0) {
			logsBuffer.deleteCharAt(logsBuffer.length() - 1);
		}

		return logsBuffer.toString();
	}

	private synchronized LogFilePair  writeToDisk(Snapshot snapShot) {
		saveSequenceNumber();

		if (snapShot == null) {
			return null;
		}

		LogFilePair fileData = newLogFileData();

		//gzip it
		OutputStream argsStream = null;
		OutputStream bodyStream = null;

		try {
			argsStream = new FileOutputStream(fileData.argsFile);
			bodyStream = new FileOutputStream(fileData.logFile);

			argsStream.write(snapShot.gzipArgs);
			bodyStream.write(snapShot.gzipBody);

		} catch (Exception e) {
			hostEnv.logInfo(TAG, e.getMessage());

			fileData.deleteFiles();
			return null;
		}finally {
			closeObject(argsStream);
			closeObject(bodyStream);
		}

		return fileData;
	}

	public static void closeObject(Closeable closeable) {
		if (closeable == null) {
			return;
		}

		try {
			closeable.close();
		} catch (Exception e) {

		}
	}
}
