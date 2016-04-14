package org.jfw.util.sort;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DependSortService<T> {
    private final Map<T, LinkedList<T>> eles = new HashMap<T, LinkedList<T>>();
    
    private void add(T ele,Map<T,List<T>> depends){
        List<T> list = depends.get(ele);
        depends.remove(ele);
        this.add(ele, list);
        if(list!=null&&(list.size()>0)){
            for(T t:list)
            this.add(t, depends);
        }
    }
    
    public void add(List<T> eles,Map<T,List<T>> depends){
        Map<T,List<T>> copyDepends = new HashMap<T,List<T>>();
        copyDepends.putAll(depends);
        for(T t:eles){
            this.add(t, copyDepends);
        }
    }

    public void add(T ele, List<T> depends) {
        LinkedList<T> dpds = eles.get(ele);
        if (dpds == null) {
            dpds = new LinkedList<T>();
        }
        if (null != depends && depends.size() > 0) {
            dpds.addAll(depends);
        }
        eles.put(ele, dpds);
        if (null != depends && depends.size() > 0) {
            for (T t : depends)
                this.add(t, (List<T>)null);
        }
    }

    public List<T> sort() {
        LinkedList<T> result = new LinkedList<T>();
        while (!eles.isEmpty()) {
            int oldsize = eles.size();
            for (Map.Entry<T, LinkedList<T>> entry : eles.entrySet()) {
                if(entry.getValue().size()==0) result.add(entry.getKey());
            }
            for (T t : result) {
                eles.remove(t);
            }
            if(eles.size()==oldsize) throw new RuntimeException("the data is error:reason exists loop depends");
            for (Map.Entry<T, LinkedList<T>> entry : eles.entrySet()) {
               entry.getValue().removeAll(result);
            }
        }
        return result;
    }
}
