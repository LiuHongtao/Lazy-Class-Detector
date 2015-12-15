package test;

import visitor.LazyClassDetector;

public class LazyClassTest {
	
	public LazyClassTest() {
		String projectPath = "D:\\Qualitas Corpus\\001-apache-ant-1.9.6-src";
		LazyClassDetector detector = new LazyClassDetector(projectPath);
		
		System.out.println(detector.getLazyClassSet().size());
		for (String name: detector.getLazyClassSet()) {
			System.out.println(name);
		}
	}
}
