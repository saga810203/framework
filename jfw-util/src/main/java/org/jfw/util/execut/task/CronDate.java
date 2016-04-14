package org.jfw.util.execut.task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;

import org.jfw.util.DateUtil;

public class CronDate {
	private static final int[] DEFAULT_MI = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
			21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
			48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59 };
	private static final int[] DEFAULT_HH24 = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
			20, 21, 22, 23 };
	private static final int[] DEFAULT_DDM = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
			21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31 };
	private static final int[] DEFAULT_MM = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 };
	private static final int[] DEFAULT_DDW = { 0, 1, 2, 3, 4, 5, 6 };

	private final int[] mi;
	private final int[] hh24;
	private final int[] ddm;
	private final int[] mm;
	private final int[] ddw;
	private final String cronString;

	private int indexInMI = 0;
	private int indexInHH24 = 0;
	private int indexInDDM = 0;
	private int indexInMM = 0;
	private int currYear = 0;

	private CronDate(String pCronStr, int[] pMI, int[] pHH24, int[] pDDM, int[] pMM, int[] pDDW) {
		this.cronString = pCronStr;
		this.mi = pMI;
		this.hh24 = pHH24;
		this.ddm = pDDM;
		this.mm = pMM;
		this.ddw = pDDW;
	}

	public String getCronString() {
		return cronString;
	}

	static public CronDate buildCronDate(String cronStr) {
		try {
			String[] ss = cronStr.split(" ");
			ArrayList<String> list = new ArrayList<String>();
			for (String s : ss) {
				s = s.trim();
				if (!s.equals(""))
					list.add(s);
			}
			int[] pmi = parserValue(list.get(0), DEFAULT_MI);
			if (0 > pmi[0])
				throw new IllegalArgumentException();

			int[] phh24 = parserValue(list.get(1), DEFAULT_HH24);
			if (0 > phh24[0])
				throw new IllegalArgumentException();

			int[] pddm = parserValue(list.get(2), DEFAULT_DDM);
			if (1 > pddm[0])
				throw new IllegalArgumentException();

			int[] pmm = parserValue(list.get(3), DEFAULT_MM);
			if (1 > pmm[0])
				throw new IllegalArgumentException();

			int[] pddw = parserValue(list.get(4), DEFAULT_DDW);
			if (0 > pddw[0] || pddw[pddw.length - 1] > 6)
				throw new IllegalArgumentException();

			return new CronDate(cronStr, pmi, phh24, pddm, pmm, pddw);
		} catch (Exception e) {
			throw new IllegalArgumentException("错误的定时任务表达式：" + cronStr);
		}

	}

	static private int[] parserValue(String val, int[] defaultValue) {
		TreeSet<Integer> result = new TreeSet<Integer>();

		String[] vals = val.split(",");
		for (String s : vals) {
			parserOneValue(s.trim(), defaultValue, result);
		}

		if (result.size() == 0)
			throw new IllegalArgumentException();

		int[] r2 = new int[result.size()];

		Iterator<Integer> it = result.iterator();
		for (int j = 0; j < r2.length; j++) {
			it.hasNext();
			r2[j] = it.next().intValue();
		}
		return r2;
	}

	static private void parserOneValue(String token, int[] defaultValue, TreeSet<Integer> result) {
		int firstValue = defaultValue[0];
		int lastValue = defaultValue[defaultValue.length - 1];

		if ("".equals(token))
			return;
		int each = 1;
		int index = token.indexOf("/");
		if (index > 0) {
			each = Integer.parseInt(token.substring(index + 1));
			if (each <= 0)
				throw new IllegalArgumentException();
			token = token.substring(0, index).trim();
		}

		if (token.equals("*")) {
			for (int i = firstValue; i <= lastValue; i += each)
				result.add(i);

			// for (int i: defaultValue) {
			// if(i % each == 0) result.add(i);
			// }
			return;
		}
		index = token.indexOf("-");
		if (index > 0) {
			int start = Integer.parseInt(token.substring(0, index));

			int end = Integer.parseInt(token.substring(index + 1));
			end = Math.min(end, lastValue);

			for (int j = start; j <= end; j += each)
				result.add(j);

			// for (int j = start; j <= end; ++j)
			// {
			// if (j % each == 0)
			// result.add(j);
			//
			// }
			return;
		}
		int iValue = Integer.parseInt(token);
		if ((iValue >= firstValue) && (iValue <= lastValue))
			result.add(iValue);
	}

	private Calendar generetorValidDate() {
		int dayOfMonth = this.ddm[this.indexInDDM];
		int month = this.mm[this.indexInMM] - 1;
		if (dayOfMonth > DateUtil.getLastDayOfMonth(month, this.currYear))
			return null;

		Calendar cal = DateUtil.getDateByYYYYMMDD(this.currYear, month, dayOfMonth);
		int dw = cal.get(Calendar.DAY_OF_WEEK) - 1;

		for (int i : this.ddw) {
			if (i == dw)
				return cal;
		}
		return null;
	}

	private Calendar generatorNextDay() {
		Calendar result = null;
		while (null == result) {
			if (this.indexInDDM == (this.ddm.length - 1)) {
				this.indexInDDM = 0;
				if (this.indexInMM == (this.mm.length - 1)) {
					this.indexInMM = 0;
					this.currYear++;
				} else {
					this.indexInMM++;
				}
			} else {
				this.indexInDDM++;
			}
			result = generetorValidDate();
		}
		return result;
	}

	synchronized public long getNextTime() {
		Calendar cal = null;
		if (this.indexInMI == (this.mi.length - 1)) {
			this.indexInMI = 0;
			if (this.indexInHH24 == (this.hh24.length - 1)) {
				this.indexInHH24 = 0;
				cal = this.generatorNextDay();
			} else {
				this.indexInHH24++;
			}
		} else {
			this.indexInMI++;
		}
		if (null == cal) {
			cal = Calendar.getInstance();
			cal.set(this.currYear, this.mm[this.indexInMM] - 1, this.ddm[this.indexInDDM]);
		}
		cal.set(Calendar.HOUR_OF_DAY, this.hh24[this.indexInHH24]);
		cal.set(Calendar.MINUTE, this.mi[this.indexInMI]);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTimeInMillis();
	}

	private static int match(int src, int[] dest) {
		for (int i = 0; i < dest.length; ++i) {
			if (src == dest[i])
				return i;
		}
		return -1;
	}

	private boolean isMatch(Calendar cal) {
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int week = cal.get(Calendar.DAY_OF_WEEK) - 1;
		return (match(day, this.ddm) >= 0) && (match(month, this.mm) >= 0) && (match(week, this.ddw) >= 0);
	}

	private void generatorMatchDate(Calendar cal) {
		boolean match = isMatch(cal);
		while (!match) {
			cal.add(Calendar.DAY_OF_MONTH, 1);
			match = isMatch(cal);
		}
		this.currYear = cal.get(Calendar.YEAR);
		this.indexInDDM = match(cal.get(Calendar.DAY_OF_MONTH), this.ddm);
		this.indexInMM = match(cal.get(Calendar.MONTH) + 1, this.mm);
	}

	synchronized long getNextTimeByTime(long time) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(time);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		generatorMatchDate(cal);

		if (cal.getTimeInMillis() > time) {
			this.indexInHH24 = 0;
			this.indexInMI = 0;
			cal.set(Calendar.HOUR_OF_DAY, this.hh24[0]);
			cal.set(Calendar.MINUTE, this.mi[0]);
			cal.set(Calendar.SECOND, 0);
			return cal.getTimeInMillis();
		}

		if (hour > this.hh24[this.hh24.length - 1]) {
			this.indexInMI = this.mi.length - 1;
			this.indexInHH24 = this.hh24.length - 1;
			return this.getNextTime();
		} else if (hour < this.hh24[0]) {
			this.indexInHH24 = 0;
			this.indexInMI = 0;
			cal.set(Calendar.HOUR_OF_DAY, this.hh24[0]);
			cal.set(Calendar.MINUTE, this.mi[0]);
			cal.set(Calendar.SECOND, 0);
			return cal.getTimeInMillis();
		} else {
			for (int i = this.hh24.length; i > 0; --i) {
				if (this.hh24[i - 1] == hour) {
					this.indexInHH24 = i - 1;
					break;
				}
				if (this.hh24[i - 1] < hour) {
					this.indexInHH24 = i;
				}
			}

			if (this.hh24[this.indexInHH24] == hour) {
				if (minute > this.mi[this.mi.length - 1]) {
					this.indexInMI = this.mi.length - 1;
					return getNextTime();
				}

				if (minute < this.mi[0]) {
					this.indexInMI = 0;
					cal.set(Calendar.HOUR_OF_DAY, this.hh24[this.indexInHH24]);
					cal.set(Calendar.MINUTE, this.mi[0]);
					cal.set(Calendar.SECOND, 0);
					return cal.getTimeInMillis();

				}

				for (int i = this.mi.length; i > 0; --i) {
					if (this.mi[i - 1] == minute) {
						this.indexInMI = i - 1;
					}
					if (this.hh24[i - 1] < hour) {
						this.indexInMI = i;
					}
				}
				return getNextTime();

			} else {
				this.indexInMI = this.mi.length - 1;
				return getNextTime();
			}

		}

	}

	// public static void out(int[] ii)
	// {
	// System.out.print("[");
	// for(int i:ii)
	// {
	// System.out.print(i);
	// System.out.print(",");
	// }
	// System.out.println("]");
	// }
	public static void main(String[] args) {
		String cronStr = "2,5-9 13-18 6,9,22,30 4-10/2 *";
		String ss = "2014-07-31 10:08:00";

		CronDate c = CronDate.buildCronDate(cronStr);

		java.text.SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.YEAR, Integer.parseInt(ss.substring(0, 4)));
		cal.set(Calendar.MONTH, Integer.parseInt(ss.substring(5, 7)) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(ss.substring(8, 10)));
		cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(ss.substring(11, 13)));
		cal.set(Calendar.MINUTE, Integer.parseInt(ss.substring(14, 16)));
		cal.set(Calendar.SECOND, Integer.parseInt(ss.substring(17, 19)));

		// System.out.println(sdf.format(cal.getTime()));

		long l = c.getNextTimeByTime(cal.getTimeInMillis());

		cal.setTimeInMillis(l);

		System.out.println(sdf.format(cal.getTime()));
		//
		cal.setTimeInMillis(c.getNextTime());
		System.out.println(sdf.format(cal.getTime()));
		cal.setTimeInMillis(c.getNextTime());
		System.out.println(sdf.format(cal.getTime()));
		cal.setTimeInMillis(c.getNextTime());
		System.out.println(sdf.format(cal.getTime()));
		cal.setTimeInMillis(c.getNextTime());
		System.out.println(sdf.format(cal.getTime()));
		cal.setTimeInMillis(c.getNextTime());
		System.out.println(sdf.format(cal.getTime()));
		cal.setTimeInMillis(c.getNextTime());
		System.out.println(sdf.format(cal.getTime()));

	}

}
