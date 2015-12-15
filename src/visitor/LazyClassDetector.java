package visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import util.AstUtil;
import util.FileUtil;

public class LazyClassDetector extends ASTVisitor {

	private HashMap<String, Boolean> isLazyClassMap;
	
	private HashMap<String, String> projectClassMap;
	private Set<String> projectClassSet;
	private Set<String> relevantClassSetofProject;
	
	public HashMap<String, Boolean> getIsLazyClassMap() {		
		Iterator iter = projectClassMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			String key = (String)entry.getKey();
			String val = (String)entry.getValue();
			
			if (!relevantClassSetofProject.contains(val)) {
				isLazyClassMap.put(key, true);
			}
		}
		
		System.out.println(isLazyClassMap.size());
		
		return isLazyClassMap;
	}
	
	public Set<String> getLazyClassSet() {	
		Set<String> lazyClassSet = new HashSet<String>(projectClassSet);
		lazyClassSet.removeAll(relevantClassSetofProject);

		System.out.println(lazyClassSet.size());
		
		return lazyClassSet;
	}

	public LazyClassDetector(String projectPath) {
		isLazyClassMap = new HashMap<>();
		
		projectClassMap = new HashMap<>();
		projectClassSet = new HashSet<>();
		relevantClassSetofProject = new HashSet<String>();
		
		dataCollection(projectPath);
	}
	
	//traverse all types to collect information
	private void dataCollection(String projectPath) {
		FileUtil fileTool = new FileUtil();
		ArrayList<String> filePath = fileTool.getAllJavaFilePath(projectPath);

		AstUtil astUtil = new AstUtil(); 
		for (String path: filePath) {
			try {
				CompilationUnit compUnit = astUtil.getCompUnit(path);				
				compUnit.accept(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		String className = node.getName().toString();
		String pkgClassName = getPkgClassName(node);
		projectClassSet.add(className);
		projectClassMap.put(pkgClassName, className);
		
		Set<String> relevantClassSetofClass = new HashSet<String>();
		
		InheritanceSetDetector inheritanceDetector = new InheritanceSetDetector(node);
		relevantClassSetofClass.addAll(
				inheritanceDetector.getSuperClassSet());
		
		AssociateSetDetector associateDetector = new AssociateSetDetector(node.getFields());
		relevantClassSetofClass.addAll(
				associateDetector.getAssociateClassSet());

		relevantClassSetofProject.addAll(relevantClassSetofClass);
		
		if (node instanceof TypeDeclaration &&
				((TypeDeclaration)node).isInterface()) {
			return true;
		}
		
		DependentSetDetector dependentDetector = new DependentSetDetector(node.getMethods(), node.getFields());
		relevantClassSetofClass.addAll(
				dependentDetector.getDependentClassSet());
		
		relevantClassSetofProject.addAll(relevantClassSetofClass);
		
		return true;
	}
	
	private String getPkgClassName(TypeDeclaration node) {
		ASTNode parent = node.getParent();
		String pkgClassName = "";
		if (parent instanceof CompilationUnit) {
			PackageDeclaration pkg = ((CompilationUnit) parent).getPackage();
			if (pkg == null) {
				pkgClassName = node.getName().getIdentifier();
			}
			else {
				pkgClassName = ((CompilationUnit) parent).getPackage().getName().getFullyQualifiedName() +
					'.' + node.getName().getIdentifier();
			}
		}
		else if (parent instanceof TypeDeclaration) {
			pkgClassName = getPkgClassName((TypeDeclaration) parent) + 
					'.' + node.getName().getIdentifier();
		}
		
		return pkgClassName;
	}
}
