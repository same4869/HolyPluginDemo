package com.wenba.comm;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@SuppressLint("SimpleDateFormat")
public class DateUtil {
	public static final String tag = DateUtil.class.getSimpleName();

	private static long wenbaTime = System.currentTimeMillis();
	private static long localTime = System.currentTimeMillis();

	public static void setWenbaTime(long time) {
		wenbaTime = time;
		localTime = System.currentTimeMillis();
	}

	public static long getCurWenbaTime() {
		return System.currentTimeMillis() + (wenbaTime - localTime);
	}

	public static int getTimeHour(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(time));
		cal.get(Calendar.HOUR_OF_DAY);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public static String getDateString4(long time) {
		Calendar curDate = Calendar.getInstance(Locale.CHINA);
		curDate.setTimeInMillis(DateUtil.getCurWenbaTime());
		Calendar date = Calendar.getInstance(Locale.CHINA);
		date.setTimeInMillis(time);



		long day = date.get(Calendar.DAY_OF_YEAR);
		long today = curDate.get(Calendar.DAY_OF_YEAR);
		if (day == today) {
			return WenbaApplication.getInstance().getApplicationContext().getString(R.string.today);
		} else {
			long year = date.get(Calendar.YEAR);
			long toYear = curDate.get(Calendar.YEAR);
			SimpleDateFormat sdf2 = null;
			if (year == toYear) {
				sdf2 = new SimpleDateFormat("MM月dd日", Locale.getDefault());
			} else {
				sdf2 = new SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault());
			}

			return sdf2.format(new Date(time));
		}
	}

	public static String getDateString5(long time) {
		Calendar curDate = Calendar.getInstance(Locale.CHINA);
		curDate.setTimeInMillis(DateUtil.getCurWenbaTime());
		Calendar date = Calendar.getInstance(Locale.CHINA);
		date.setTimeInMillis(time);

		long year = date.get(Calendar.YEAR);
		long toYear = curDate.get(Calendar.YEAR);
		SimpleDateFormat sdf2 = null;
		if (year == toYear) {
			sdf2 = new SimpleDateFormat("MM月", Locale.getDefault());
		} else {
			sdf2 = new SimpleDateFormat("yyyy年MM月", Locale.getDefault());
		}

		return sdf2.format(new Date(time));
	}
	
	public static String getDateString6(long time) {
		Calendar curDate = Calendar.getInstance(Locale.CHINA);
		curDate.setTimeInMillis(DateUtil.getCurWenbaTime());
		Calendar date = Calendar.getInstance(Locale.CHINA);
		date.setTimeInMillis(time);

		long day = date.get(Calendar.DAY_OF_YEAR);
		long today = curDate.get(Calendar.DAY_OF_YEAR);

		if (day == today) {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
			return WenbaApplication.getInstance().getApplicationContext().getString(R.string.today) + " " + sdf.format(new Date(time));
		} else {
			long year = date.get(Calendar.YEAR);
			long toYear = curDate.get(Calendar.YEAR);
			SimpleDateFormat sdf2 = null;
			if (year == toYear) {
				sdf2 = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
			} else {
				sdf2 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.getDefault());
			}

			return sdf2.format(new Date(time));
		}
	}

	/**
	 *
	 * @param timestampString
	 * @param formats
     * @return
     */
	public static String timeStamp2Date(String timestampString, String formats) {
		Long timestamp = Long.parseLong(timestampString) * 1000;
		String date = new java.text.SimpleDateFormat(formats).format(new java.util.Date(timestamp));
		return date;
	}

}
