package org.jfw.util.init;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class InitEntrySortUtil {
	public static List<InitEntry> sort(Collection<InitEntry> ies) {
		List<InitEntry> result = new LinkedList<InitEntry>();
		Map<InitEntry, Collection<InitEntry>> eles = new HashMap<InitEntry, Collection<InitEntry>>();
		for (InitEntry ie : ies) {
			eles.put(ie, ie.getDependBeanIds());
		}
		while (!eles.isEmpty()) {
			int oldsize = eles.size();
			for (Map.Entry<InitEntry, Collection<InitEntry>> entry : eles.entrySet()) {
				if (entry.getValue().size() == 0)
					result.add(entry.getKey());
			}
			for (InitEntry t : result) {
				eles.remove(t);
			}
			if (eles.size() == oldsize)
				throw new RuntimeException("the data is error:reason exists loop depends");
			for (Map.Entry<InitEntry, Collection<InitEntry>> entry : eles.entrySet()) {
				entry.getValue().removeAll(result);
			}
		}
		return result;
	}

}
