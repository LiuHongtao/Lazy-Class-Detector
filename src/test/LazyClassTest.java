package test;

import java.util.Iterator;
import java.util.Map;

import visitor.LazyClassDetector;

public class LazyClassTest {
	
	public LazyClassTest() {
		String projectPath = "D:\\Qualitas Corpus\\101-struts-2.3.24.1-src";
		LazyClassDetector detector = new LazyClassDetector(projectPath);
		
//		for (String name: detector.getLazyClassSet()) {
//			System.out.println(name);
//		}
		
		Iterator iter = detector.getIsLazyClassMap().entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String)entry.getKey();
			Boolean val = (Boolean)entry.getValue();
			
			System.out.println(key + "\t" + val);
		}
	}
}
