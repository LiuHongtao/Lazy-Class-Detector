package visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import util.AstUtil;
import util.FileUtil;

public class LazyClassDetector extends ASTVisitor {
	
	private Set<String> projectClassSet;
	private HashMap<String, String> projectClassMap;
	private Set<String> relevantClassSetofProject;
	
	public Set<String> getLazyClassSet() {
		Set<String> lazyClassSet = new HashSet<String>(projectClassSet);
		
		for (String className: relevantClassSetofProject) {
			lazyClassSet.remove(projectClassMap.get(className));
		}
		
		System.out.println(lazyClassSet.size() == projectClassSet.size() - relevantClassSetofProject.size());
		
		return lazyClassSet;
	}

	public LazyClassDetector(String projectPath) {
		projectClassSet = new HashSet<String>();
		projectClassMap = new HashMap<>();
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
		String pkgClassName = getPkgClassName(node);
		projectClassSet.add(pkgClassName);
		projectClassMap.put(node.getName().getIdentifier(), pkgClassName);
		
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
			pkgClassName = ((CompilationUnit) parent).getPackage().getName().getFullyQualifiedName() +
					'.' + node.getName().getIdentifier();
		}
		else if (parent instanceof TypeDeclaration) {
			pkgClassName = getPkgClassName((TypeDeclaration) parent) + 
					'.' + node.getName().getIdentifier();
		}
		
		return pkgClassName;
	}
}
